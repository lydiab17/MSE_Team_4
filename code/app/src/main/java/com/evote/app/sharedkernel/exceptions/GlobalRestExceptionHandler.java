package com.evote.app.sharedkernel.exceptions;

import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Globaler Exception-Handler für REST-Controller.
 */
@RestControllerAdvice
public class GlobalRestExceptionHandler {

  /**
   * Behandelt Rate-Limit-Überschreitungen (Resilience4j).
   *
   * @param ex Exception von Resilience4j
   * @return HTTP 429 (Too Many Requests) mit kurzer Fehlermeldung
   */
  @ExceptionHandler(RequestNotPermitted.class)
  public ResponseEntity<String> handleRateLimit(RequestNotPermitted ex) {
    return ResponseEntity
            .status(HttpStatus.TOO_MANY_REQUESTS)
            .body("Rate limit exceeded. Please try again later.");
  }
}
