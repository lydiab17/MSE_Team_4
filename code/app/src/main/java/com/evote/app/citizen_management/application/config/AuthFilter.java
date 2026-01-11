package com.evote.app.citizen_management.application.config;

import com.evote.app.citizen_management.application.services.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Authentifizierungsfilter zur Überprüfung von JWTs in eingehenden HTTP-Anfragen.
 *
 * <p>AuthFilter prüft jede eingehende HTTP-Anfrage, ob der Benutzer eingeloggt ist.
 *
 * <p>Der Filter wird einmal pro Request ausgeführt und prüft, ob ein {@code Authorization}-Header
 * mit einem gültigen Bearer-Token vorhanden ist. Bei erfolgreicher Validierung wird der Benutzer im
 * {@link SecurityContextHolder} gesetzt.
 *
 * <p>Für nicht geschützte Endpunkte wie Login und Registrierung wird keine Authentifizierung
 * erzwungen. Für alle anderen Endpunkte wird ohne gültigen Token eine {@code 401
 * Unauthorized}-Antwort zurückgegeben.
 */
@Component
public class AuthFilter extends OncePerRequestFilter {

  /**
   * Führt die Filterlogik für jede eingehende HTTP-Anfrage aus.
   *
   * <p>Extrahiert das JWT aus dem {@code Authorization}-Header, validiert es mithilfe des {@link
   * TokenService} und setzt bei Erfolg die Authentifizierung im Security-Kontext.
   *
   * @param request das aktuelle {@link HttpServletRequest}-Objekt
   * @param response das aktuelle {@link HttpServletResponse}-Objekt
   * @param filterChain die Filterkette zur Weiterleitung der Anfrage
   * @throws ServletException bei Servlet-spezifischen Fehlern
   * @throws IOException bei Ein-/Ausgabe-Fehlern
   */
  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String authHeader = request.getHeader("Authorization");

    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      String token = authHeader.substring(7);
      String user = TokenService.validateToken(token);

      if (user != null) {
        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(user, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(authentication);
      }
    } else if (!(request.getRequestURI().contains("/login")
        || request.getRequestURI().contains("/register"))) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }

    filterChain.doFilter(request, response);
  }
}
