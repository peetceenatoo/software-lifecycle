package com.polimi.PPP.CodeKataBattle.Security;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.access.AccessDeniedException;

import java.io.InputStream;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;

@Slf4j
@NoArgsConstructor
public class JwtHelper {

    private final String PRIVATE_KEY_PATH = "RSAKeys/CKB_private.key";
    private final String PUBLIC_KEY_PATH = "RSAKeys/CKB_public.key";

    private PrivateKey getPrivateKey() throws Exception {
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

    private PublicKey getPublicKey() throws Exception {
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

    private final int MINUTES = 60;

    public String generateToken(Long userId) {
        var now = Instant.now();

        try{
            return Jwts.builder()
                    .subject(String.valueOf(userId))
                    .issuedAt(Date.from(now))
                    .expiration(Date.from(now.plus(MINUTES, ChronoUnit.MINUTES)))
                    .signWith(getPrivateKey(), Jwts.SIG.RS256)
                    .compact();
        }catch (Exception e){
            throw new RuntimeException("Error creating token");
        }

    }

    public Long extractUserId(String token) {
        return Long.parseLong(getTokenBody(token).getSubject());
    }

    public Boolean validateToken(String token) {
        return !isTokenExpired(token);
    }

    private Claims getTokenBody(String token) {

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

    private boolean isTokenExpired(String token) {
        Claims claims = getTokenBody(token);
        return claims.getExpiration().before(new Date());
    }
}