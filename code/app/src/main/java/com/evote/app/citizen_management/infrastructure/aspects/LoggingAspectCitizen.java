package com.evote.app.citizen_management.infrastructure.aspects;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Aspect für Login-Logging.
 *
 * @author Lydia Boes
 * @version 1.0
 */
@Aspect // Markiert diese Klasse als Aspect (AOP)
@Component // Spring-Bean: wird automatisch von Spring gefunden und verwaltet.
public class LoggingAspectCitizen {

    // SLF4J-Logger für diese Klasse
    private static final Logger log = LoggerFactory.getLogger(LoggingAspectCitizen.class);

    // Merkt sich Login-Versuche pro E-Mail (Key = email, Value = Anzahl Versuche).
    private final Map<String, Integer> attempts = new HashMap<>();

    /**
     * Diese Methode wird AUTOMATISCH aufgerufen,
     * nachdem CitizenService.loginCitizen(..) ausgeführt wurde.
     *
     * @param joinPoint enthält Infos über den Methodenaufruf (Parameter, Methode, Klasse)
     * @param success   Rückgabewert der Methode loginCitizen(..)
     */
    @AfterReturning(
            // Pointcut: Triggert genau dann, wenn die Methode loginCitizen(..) im CitizenService ausgeführt wurde
            // und einen boolean zurückliefert.
            pointcut = "execution(boolean com.evote.app.citizen_management.application.services.CitizenService.loginCitizen(..))",
            // "returning" bindet den Rückgabewert der Zielmethode an den Parameter "success".
            returning = "success"
    )
    public void logLoginAttempt(JoinPoint joinPoint, boolean success) {
        // holt alle Argumente (Parameter), mit denen die Methode loginCitizen aufgerufen wurde
        // und speichert sie in einem generischen Objekt-Array.
        Object[] args = joinPoint.getArgs();

        // E-Mail, die geloggt werden soll
        String email;

        // Name der aufgerufenen Methode
        String methodName = joinPoint.getSignature().getName();

        // Name der Zielklasse, auf der die Methode ausgeführt wurde
        String className = joinPoint.getTarget().getClass().getSimpleName();

        // prüft, ob es das Array überhaupt gibt und ob mindestens ein Element im Array ist
        if (args != null && args.length > 0) {
            email = String.valueOf(args[0]);
            // email wird aus args[0] extrahiert
            // Umwandlung von Object zu String
        } else {
            email = "unknown";
        }

        // Holt den bisherigen Zähler oder 0, falls noch kein Eintrag existiert
        // Erhöht den Zähler für diese E-Mail um 1
        int count = attempts.getOrDefault(email, 0) + 1;

        // Speichert den neuen Zählerstand zurück in die Map.
        attempts.put(email, count);

        if (success) {
            // Bei Erfolg: Einträge für diese E-Mail entfernen, damit der Zähler beim nächsten Login neu startet.
            attempts.remove(email);

            // Info-Log: Erfolgsmeldung mit Klasse, Methode, E-Mail und Anzahl Versuche bis zum Erfolg.
            log.info(
                    "Klasse: {} | Methode: {} | Login erfolgreich | email={} | Versuche={}",
                    className, methodName, email, count
            );
        } else {
            // Warn-Log: Fehlgeschlagenen Versuch loggen (Zähler bleibt bestehen und steigt bei erneutem Versuch).
            log.warn(
                    "Klasse: {} | Methode: {} | Login fehlgeschlagen | email={} | Versuche={}",
                    className, methodName, email, count
            );
        }
    }
}
