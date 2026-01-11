package com.evote.app.citizen_management.application.services;

import com.evote.app.sharedkernel.security.PseudonymToken;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
/**
 * Service zur Erstellung, Validierung und Pseudonymisierung von Tokens.
 * @author Lydia Boes, Fabian Schmitz
 * @version 2.0
 */
@Service
public class TokenService {

    /**
     * Geheimer Schlüssel zum Erstellen und Prüfen von Tokens.
     */
    private static final String SECRET = "COOLES_MODUL!";

    /**
     * Erzeugt ein signiertes JSON Web Token (JWT) für einen Benutzer.
     * Das Token enthält den Benutzernamen als Subject, den Zeitpunkt der
     * Erstellung sowie ein Ablaufdatum von einer Stunde.
     *
     * @param username der Benutzername, der im Token gespeichert wird
     * @return das erzeugte JWT als String
     */
    public static String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username) // wer ist der Benutzer
                .setIssuedAt(new Date()) // wann wurde das Token erstellt
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60)) // wie lange ist das Token gültig: 1 Stunde
                .signWith(SignatureAlgorithm.HS256, SECRET)
                // HS256 ist der kryptographische Algorithmus, der festlegt, wie unterschrieben wird
                // SECRET ist der geheime Schlüssel, mit dem unterschrieben wird
                .compact(); // macht aus allen Infos einen kompakten String
    }

    /**
     * Validiert ein JSON Web Token (JWT).
     * Ist das Token gültig und korrekt signiert, wird das im Token
     * gespeicherte Subject (z. B. Benutzername) zurückgegeben.
     * Bei einem ungültigen oder abgelaufenen Token wird null geliefert.
     *
     * @param token das zu prüfende JWT
     * @return das Subject des Tokens bei Erfolg, sonst null
     */
    public static String validateToken(String token) {
        try {
            return Jwts.parser() // Paser, um Token zu lesen und zu überprüfen
                    .setSigningKey(SECRET) // Übergibt dem Parser den geheimen Schlüssel
                    .parseClaimsJws(token) // zerlegt das JSON Web Token, prüft dessen Signatur und Gültigkeit und wirft bei einem ungültigen Token eine Exception
                    .getBody() // holt den Inhalt (Payload) des Tokens
                    .getSubject(); // holt den Subject-Wert (username) aus dem Payload
        } catch (Exception e) {
            return null; // ungültig
        }
    }

    /**
     * Erzeugt ein pseudonymisiertes Token aus einer Bürger-ID.
     * Die übergebene ID wird mithilfe eines HMAC-SHA256-Verfahrens
     * und eines geheimen Schlüssels in einen nicht rückrechenbaren
     * Wert umgewandelt. Das Ergebnis wird Base64-kodiert und als
     * PseudonymToken zurückgegeben.
     *
     * @param citizenId die zu pseudonymisierende Bürger-ID
     * @return ein PseudonymToken, das die pseudonymisierte ID enthält
     * @throws IllegalStateException wenn die Pseudonymisierung fehlschlägt
     */
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