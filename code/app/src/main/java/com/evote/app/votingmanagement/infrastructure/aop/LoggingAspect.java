package com.evote.app.votingmanagement.infrastructure.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Einfacher AOP-Logging-Aspect.
 *
 * <p>Loggt alle Methodenaufrufe im VotingApplicationService:
 * - Methodenname + Parameter
 * - Rückgabewert
 * - Dauer in ms
 * - Exceptions
 */
@Aspect
@Component
public class LoggingAspect {

  private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

  /**
   * Around-Advice für alle öffentlichen Methoden im VotingApplicationService.
   *
   * <p>Pointcut:
   * execution(public * com.evote.app.votingmanagement.application.services.VotingApplicationService.*(..))
   */
  @Around(
          "execution(public * "
                  + "com.evote.app.votingmanagement.application.services."
                  + "VotingApplicationService.*(..))"
  )
  public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
    String methodName = joinPoint.getSignature().toShortString();
    Object[] args = joinPoint.getArgs();

    long start = System.currentTimeMillis();
    log.info(">>> {} called with args={}", methodName, args);

    try {
      Object result = joinPoint.proceed();  // eigentlicher Methodenaufruf

      long duration = System.currentTimeMillis() - start;
      log.info("<<< {} returned={} ({} ms)", methodName, result, duration);

      return result;
    } catch (Throwable ex) {
      long duration = System.currentTimeMillis() - start;
      log.error(
              "xxx {} threw {} after {} ms",
              methodName,
              ex.toString(),
              duration
      );
      throw ex;  // wichtig: Exception weiterwerfen
    }
  }
}
