package com.evote.app.citizen_management.application.services;

import com.evote.app.sharedkernel.PseudonymToken;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;

@Service
public class TokenService {

    private static final String SECRET = "COOLES_MODUL!";

    public static String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60)) // 1 Stunde gültig
                .signWith(SignatureAlgorithm.HS256, SECRET)
                .compact();
    }

    public static String validateToken(String token) {
        try {
            System.out.println(token);
            return Jwts.parser()
                    .setSigningKey(SECRET)
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (Exception e) {
            return null; // ungültig
        }
    }

    public PseudonymToken pseudonymize(String citizenId) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] h = mac.doFinal(citizenId.getBytes(StandardCharsets.UTF_8));
            String token = Base64.getUrlEncoder().withoutPadding().encodeToString(h);
            return new PseudonymToken(token);
        } catch (Exception e) {
            throw new IllegalStateException("Pseudonymization failed", e);
        }
    }
}