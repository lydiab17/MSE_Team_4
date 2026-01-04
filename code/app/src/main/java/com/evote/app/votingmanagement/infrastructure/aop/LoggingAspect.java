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
   * execution(public * com.evote.app.votingmanagement.application.services.*.*(..))
   */
  @Around(
          "execution(public * com.evote.app.votingmanagement.application.services.*.*(..))\n"
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
    } catch (Exception ex) {
      long duration = System.currentTimeMillis() - start;
      String msg = String.format("%s failed after %d ms", methodName, duration);

      log.error("xxx {} failed after {} ms", methodName, duration, ex);

      throw new IllegalStateException(msg, ex);
    }
  }
}
