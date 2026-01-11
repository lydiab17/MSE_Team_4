package com.evote.app.citizen_management.application.services;

import com.evote.app.sharedkernel.security.PseudonymToken;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Service;

/** Service zur Erzeugung, Validierung und Pseudonymisierung von Tokens. */
@Service
public class TokenService {

  /** Geheimschlüssel zur Signierung und Verifikation von Tokens. */
  private static final String SECRET = "COOLES_MODUL!";

  /**
   * Erzeugt ein signiertes JWT für den angegebenen Benutzernamen.
   *
   * <p>Das Token ist ab Erstellungszeitpunkt eine Stunde gültig.
   *
   * @param username der Benutzername, der als Subject im Token gesetzt wird
   * @return das erzeugte JWT als String
   */
  public static String generateToken(String username) {
    return Jwts.builder()
        .setSubject(username)
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
        .signWith(SignatureAlgorithm.HS256, SECRET)
        .compact();
  }

  /**
   * Validiert ein JWT und extrahiert den enthaltenen Benutzernamen.
   *
   * @param token das zu validierende JWT
   * @return der im Token enthaltene Benutzername oder {@code null}, falls das Token ungültig oder
   *     abgelaufen ist
   */
  public static String validateToken(String token) {
    try {
      return Jwts.parser().setSigningKey(SECRET).parseClaimsJws(token).getBody().getSubject();
    } catch (Exception e) {
      return null;
    }
  }

  /**
   * Erzeugt einen pseudonymisierten Token aus einer Citizen-ID.
   *
   * <p>Die Pseudonymisierung erfolgt mittels HMAC-SHA256 unter Verwendung des geheimen Schlüssels.
   * Das Ergebnis ist ein URL-sicherer, Base64-kodierter String.
   *
   * @param citizenId die zu pseudonymisierende Citizen-ID
   * @return ein {@link PseudonymToken}, das den pseudonymisierten Wert enthält
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
