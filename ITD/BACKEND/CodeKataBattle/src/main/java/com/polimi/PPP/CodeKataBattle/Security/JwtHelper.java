package com.polimi.PPP.CodeKataBattle.Security;


import com.polimi.PPP.CodeKataBattle.Model.JWTTokenUseCase;
import com.polimi.PPP.CodeKataBattle.Model.RoleEnum;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.asm.Advice;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.access.AccessDeniedException;

import java.io.InputStream;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalUnit;
import java.util.Base64;
import java.util.Date;

import static java.time.temporal.ChronoUnit.DAYS;


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

    private final int MINUTES = 3600;

    public String generateToken(Long userId, RoleEnum role) {
        var now = Instant.now();

        try{
            return Jwts.builder()
                    .subject(String.valueOf(userId))
                    .claim("useCase", JWTTokenUseCase.USER.name())
                    .claim("role", role.name())
                    .issuedAt(Date.from(now))
                    .expiration(Date.from(now.plus(MINUTES, ChronoUnit.MINUTES)))
                    .signWith(getPrivateKey(), Jwts.SIG.RS256)
                    .compact();
        }catch (Exception e){
            throw new RuntimeException("Error creating token");
        }

    }

    public String generateSubmissionToken(Long battleId, Long userId, LocalDateTime submissionDeadline) {
        var now = Instant.now();

        try{
            return Jwts.builder()
                    .claim("battleId", battleId)
                    .subject(String.valueOf(userId))
                    .claim("useCase", JWTTokenUseCase.SUBMISSION.name())
                    .issuedAt(Date.from(now))
                    .expiration(Date.from(submissionDeadline.atZone(java.time.ZoneId.systemDefault()).toInstant()))
                    .signWith(getPrivateKey(), Jwts.SIG.RS256)
                    .compact();
        }catch (Exception e){
            throw new RuntimeException("Error creating submissions token");
        }

    }

    public String generateInviteToken(Long battleInviteId, LocalDateTime battleDeadline){
        var now = Instant.now();

        var timestampDeadline = battleDeadline.toInstant(ZoneOffset.UTC);

        try{
            return Jwts.builder()
                    .subject(String.valueOf(battleInviteId))
                    .claim("useCase", JWTTokenUseCase.BATTLE_INVITE.name())
                    .issuedAt(Date.from(now))
                    .expiration(Date.from(timestampDeadline)) // Till the start of the battle
                    .signWith(getPrivateKey(), Jwts.SIG.RS256)
                    .compact();
        }catch (Exception e){
            throw new RuntimeException("Error creating invite token");
        }
    }

    public Long extractBattleInviteId(String token) {

        Claims tokenBody = getTokenBody(token);

        if(tokenBody.get("useCase", String.class).equals(JWTTokenUseCase.BATTLE_INVITE.name()))
            return Long.parseLong(tokenBody.getSubject());
        else
            throw new AccessDeniedException("Access denied: Invalid token use case");
    }

    public String extractUseCase(String token) {
        return getTokenBody(token).get("useCase", String.class);
    }

    public Long extractBattleId(String token) {
        Claims tokenBody = getTokenBody(token);

        if(tokenBody.get("useCase", String.class).equals(JWTTokenUseCase.SUBMISSION.name()))
            return Long.parseLong(tokenBody.get("battleId", String.class));

        else
            throw new AccessDeniedException("Access denied: Invalid token use case");
    }

    public Long extractUserId(String token) {
        return Long.parseLong(getTokenBody(token).getSubject());
    }

    public RoleEnum extractRole(String token) {
        return RoleEnum.valueOf(getTokenBody(token).get("role", String.class));
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
        //check expiration of jwt using UTC timestamps
        return claims.getExpiration().before(new Date());
    }
}