package com.evote.app.citizen_management.infrastructure.aspects;

import java.util.HashMap;
import java.util.Map;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Logging-Aspect für den Citizen-Login.
 *
 * <p>Dieses Aspect reagiert auf erfolgreiche und fehlgeschlagene Login-Versuche im {@code
 * CitizenService} und protokolliert relevante Informationen wie Klassenname, Methodenname,
 * E-Mail-Adresse und Anzahl der Login-Versuche.
 *
 * <p>Zusätzlich wird pro E-Mail-Adresse die Anzahl der aufeinanderfolgenden Login-Versuche gezählt.
 * Bei einem erfolgreichen Login wird der Zähler für die entsprechende E-Mail zurückgesetzt.
 */
@Aspect
@Component
public class LoggingAspectCitizen {

  /** Logger für die Ausgabe von Login-Informationen und Warnungen. */
  private static final Logger log = LoggerFactory.getLogger(LoggingAspectCitizen.class);

  /**
   * Map zur Speicherung der Anzahl fehlgeschlagener Login-Versuche pro E-Mail-Adresse.
   *
   * <p>Key: E-Mail-Adresse<br>
   * Value: Anzahl der Login-Versuche
   */
  private final Map<String, Integer> attempts = new HashMap<>();

  /**
   * Wird automatisch nach der Ausführung der Methode {@code CitizenService.loginCitizen(..)}
   * aufgerufen.
   *
   * <p>Die Methode ermittelt relevante Informationen aus dem {@link JoinPoint}, zählt die
   * Login-Versuche pro Benutzer und schreibt je nach Erfolg einen Info- oder Warn-Logeintrag.
   *
   * @param joinPoint enthält Informationen über den Methodenaufruf (z. B. Parameter, Klassen- und
   *     Methodenname)
   * @param success Rückgabewert der Methode {@code loginCitizen(..)}, {@code true} bei
   *     erfolgreichem Login, sonst {@code false}
   */
  @AfterReturning(
      pointcut =
          "execution(boolean com.evote.app.citizen_management.application.services.CitizenService.loginCitizen(..))",
      returning = "success")
  public void logLoginAttempt(JoinPoint joinPoint, boolean success) {

    Object[] args = joinPoint.getArgs();
    String email;
    String methodName = joinPoint.getSignature().getName();
    String className = joinPoint.getTarget().getClass().getSimpleName();

    if (args != null && args.length > 0) {
      email = String.valueOf(args[0]);
    } else {
      email = "unknown";
    }

    int count = attempts.getOrDefault(email, 0) + 1;
    attempts.put(email, count);

    if (success) {
      attempts.remove(email);
      log.info(
          "Klasse: {} | Methode: {} | Login erfolgreich | email={} | Versuche={}",
          className,
          methodName,
          email,
          count);
    } else {
      log.warn(
          "Klasse: {} | Methode: {} | Login fehlgeschlagen | email={} | Versuche={}",
          className,
          methodName,
          email,
          count);
    }
  }
}
