package voting_management.infrastructure.aop;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.evote.app.votingmanagement.infrastructure.aop.LoggingAspect;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LoggingAspectTest {

  @Test
  @DisplayName("logAround: ruft proceed() auf und gibt Ergebnis unverändert zurück")
  void logAround_success_returnsResult() throws Throwable {
    // Arrange
    LoggingAspect aspect = new LoggingAspect();

    ProceedingJoinPoint pjp = mock(ProceedingJoinPoint.class);
    Signature sig = mock(Signature.class);

    when(pjp.getSignature()).thenReturn(sig);
    when(sig.toShortString()).thenReturn("VotingService.someMethod(..)");
    when(pjp.getArgs()).thenReturn(new Object[] {"a", 123});
    when(pjp.proceed()).thenReturn("OK");

    // Act
    Object result = aspect.logAround(pjp);

    // Assert
    assertEquals("OK", result);
    verify(pjp, times(1)).proceed();
    verify(pjp, times(1)).getSignature();
    verify(pjp, times(1)).getArgs();
    verify(sig, times(1)).toShortString();
  }

  @Test
  @DisplayName(
      "logAround: wenn proceed() Exception wirft, wird IllegalStateException geworfen und Ursache gesetzt")
  void logAround_exception_wrapsIntoIllegalStateException() throws Throwable {
    // Arrange
    LoggingAspect aspect = new LoggingAspect();

    ProceedingJoinPoint pjp = mock(ProceedingJoinPoint.class);
    Signature sig = mock(Signature.class);

    when(pjp.getSignature()).thenReturn(sig);
    when(sig.toShortString()).thenReturn("VotingService.fail(..)");
    when(pjp.getArgs()).thenReturn(new Object[] {});
    RuntimeException original = new RuntimeException("boom");
    when(pjp.proceed()).thenThrow(original);

    // Act
    IllegalStateException ex =
        assertThrows(IllegalStateException.class, () -> aspect.logAround(pjp));

    // Assert
    assertNotNull(ex.getMessage());
    assertTrue(
        ex.getMessage().contains("VotingService.fail(..) failed after"),
        "Message sollte Methodennamen + 'failed after' enthalten");
    assertSame(original, ex.getCause());
    verify(pjp, times(1)).proceed();
  }

  @Test
  @DisplayName(
      "logAround: wenn proceed() Error wirft, wird nicht gefangen (weil catch(Exception) )")
  void logAround_error_isNotCaught() throws Throwable {
    // Arrange
    LoggingAspect aspect = new LoggingAspect();

    ProceedingJoinPoint pjp = mock(ProceedingJoinPoint.class);
    Signature sig = mock(Signature.class);

    when(pjp.getSignature()).thenReturn(sig);
    when(sig.toShortString()).thenReturn("VotingService.error(..)");
    when(pjp.getArgs()).thenReturn(new Object[] {});
    AssertionError err = new AssertionError("serious");
    when(pjp.proceed()).thenThrow(err);

    // Act + Assert
    AssertionError thrown = assertThrows(AssertionError.class, () -> aspect.logAround(pjp));
    assertSame(err, thrown);
  }
}
