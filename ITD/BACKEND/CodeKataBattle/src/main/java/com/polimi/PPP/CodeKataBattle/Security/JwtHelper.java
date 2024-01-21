package com.polimi.PPP.CodeKataBattle.Security;


import com.polimi.PPP.CodeKataBattle.DTOs.RoleDTO;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.persistence.Lob;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails;

import javax.crypto.SecretKey;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;

@Slf4j

public class JwtHelper {

    private static final String PRIVATE_KEY_PATH = "RSAKeys/CKB_private.key";
    private static final String PUBLIC_KEY_PATH = "RSAKeys/CKB_public.key";

    private static PrivateKey getPrivateKey() throws Exception {
        InputStream inputStream = new ClassPathResource(PRIVATE_KEY_PATH).getInputStream();
        byte[] keyBytes = inputStream.readAllBytes();
        String privateKeyPEM = new String(keyBytes)
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replaceAll(System.lineSeparator(), "")
                .replace("-----END PRIVATE KEY-----", "");

        byte[] decoded = Base64.getDecoder().decode(privateKeyPEM);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(spec);
    }

    private static PublicKey getPublicKey() throws Exception {
        InputStream inputStream = new ClassPathResource(PUBLIC_KEY_PATH).getInputStream();
        byte[] keyBytes = inputStream.readAllBytes();
        String publicKeyPEM = new String(keyBytes)
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replaceAll(System.lineSeparator(), "")
                .replace("-----END PUBLIC KEY-----", "");

        byte[] decoded = Base64.getDecoder().decode(publicKeyPEM);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(spec);
    }

    private static final int MINUTES = 60;

    public static String generateToken(String email, RoleDTO role) {
        var now = Instant.now();

        try{
            return Jwts.builder()
                    .subject(email)
                    .claim("role", role.getName())
                    .issuedAt(Date.from(now))
                    .expiration(Date.from(now.plus(MINUTES, ChronoUnit.MINUTES)))
                    .signWith(getPrivateKey(), Jwts.SIG.RS256)
                    .compact();
        }catch (Exception e){
            throw new RuntimeException("Error creating token");
        }

    }

    public static String extractUsername(String token) {
        return getTokenBody(token).getSubject();
    }

    public static RoleDTO extractRole(String token) {
        RoleDTO role = new RoleDTO();
        role.setName(getTokenBody(token).get("role", String.class));
        return role;
    }

    public static Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    private static Claims getTokenBody(String token) {

        try{
            return Jwts.parser()
                    .verifyWith(getPublicKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        }catch (Exception e){
            throw new AccessDeniedException("Access denied: " + e.getMessage());
        }

    }

    private static boolean isTokenExpired(String token) {
        Claims claims = getTokenBody(token);
        return claims.getExpiration().before(new Date());
    }
}