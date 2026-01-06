package com.evote.app.citizen_management.infrastructure.aspects;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspectCitizen {

    private static final Logger log = LoggerFactory.getLogger(LoggingAspectCitizen.class);

    /**
     * Diese Methode wird AUTOMATISCH aufgerufen,
     * nachdem CitizenService.loginCitizen(..) ausgeführt wurde.
     * @param joinPoint enthält Infos über den Methodenaufruf (Parameter, Methode, Klasse)
     * @param success Rückgabewert der Methode loginCitizen(..)
     */
    @AfterReturning(
            pointcut = "execution(boolean com.evote.app.citizen_management.application.services.CitizenService.loginCitizen(..))",
            returning = "success"
    )
    public void logLoginAttempt(JoinPoint joinPoint, boolean success) {

        // holt alle Argumente (Parameter), mit denen die Methode loginCitizen aufgerufen wurde
        // und speichert sie in einem generischen Objekt-Array.
        Object[] args = joinPoint.getArgs();
        String email;
        String methodName = joinPoint.getSignature().getName();

        if (args != null && args.length > 0) {
            email = String.valueOf(args[0]);
        } else {
            email = "unknown";
        }

        if (success) {
            // die geschweiften Klammern ersetzen Variablen im Log-Text
            log.info("Methode {}: Login erfolgreich (email={})", methodName, email);
        } else {
            log.warn("Methode {}: Login fehlgeschlagen (email={})", methodName, email);
        }
    }
}
