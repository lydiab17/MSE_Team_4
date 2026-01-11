package com.evote.app.citizen_management.application.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Sicherheitskonfiguration der Anwendung.
 *
 * <p>SecurityConfig definiert die Sicherheitsregeln der Anwendung.
 *
 * <p>Konfiguriert Spring Security für eine zustandslose JWT-basierte Authentifizierung. Definiert
 * erlaubte und geschützte Endpunkte sowie die Einbindung des {@link AuthFilter} in die Filterkette.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

  private final AuthFilter authFilter;

  /**
   * Erstellt eine neue Sicherheitskonfiguration mit dem angegebenen AuthFilter.
   *
   * @param authFilter der Filter zur JWT-Authentifizierung
   */
  public SecurityConfig(AuthFilter authFilter) {
    this.authFilter = authFilter;
  }

  /**
   * Konfiguriert die {@link SecurityFilterChain} der Anwendung.
   *
   * <p>Deaktiviert CSRF, erzwingt eine zustandslose Session-Verwaltung und definiert Zugriffsregeln
   * für HTTP-Endpunkte.
   *
   * @param http das {@link HttpSecurity}-Konfigurationsobjekt
   * @return die konfigurierte {@link SecurityFilterChain}
   * @throws Exception bei Konfigurationsfehlern
   */
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

    http.csrf(csrf -> csrf.disable()) // Cross-Site Request Forgery
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers("/api/citizens/register", "/api/citizens/login")
                    .permitAll()
                    .requestMatchers("/api/votings/**")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .addFilterBefore(authFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }
}
