package de.gmcs.circuitbreaker;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class CircuitBreakerTest {

    @Test
    public void testCall() throws Throwable {
        ProceedingJoinPoint point = mockProceedingJoinPoint();
        when(point.proceed()).thenReturn(1);

        CircuitBreaker circuitBreaker = new CircuitBreaker();

        assertThat(circuitBreaker.call(point)).isEqualTo(1);
    }

    @Test
    public void testCall_error() throws Throwable {
        ProceedingJoinPoint point = mockProceedingJoinPoint();
        when(point.proceed()).thenThrow(new RuntimeException());

        CircuitBreaker circuitBreaker = new CircuitBreaker();

        assertThat(circuitBreaker.call(point)).isNull();
    }

    @Test
    public void testCall_timeout() throws Throwable {
        ProceedingJoinPoint point = mockProceedingJoinPoint();
        when(point.proceed()).thenAnswer(new Answer<Integer>() {

            @Override
            public Integer answer(InvocationOnMock invocation) throws Throwable {
                sleep(500);
                return Integer.valueOf(1);
            }
        });

        CircuitBreaker circuitBreaker = new CircuitBreaker();

        assertThat(circuitBreaker.call(point)).isNull();
    }

    @Test
    public void testCall_circuitBreakerOpensUp() throws Throwable {
        ProceedingJoinPoint point = mockProceedingJoinPoint();
        when(point.proceed())
                .thenReturn(1)
                .thenReturn(2)
                .thenThrow(new RuntimeException())
                .thenThrow(new RuntimeException())
                .thenReturn(3)
                .thenReturn(4)
                .thenReturn(5);

        CircuitBreaker circuitBreaker = new CircuitBreaker();

        assertThat(circuitBreaker.call(point)).isEqualTo(1);
        assertThat(circuitBreaker.call(point)).isEqualTo(2);
        assertThat(circuitBreaker.call(point)).isEqualTo(null);
        assertThatExceptionOfType(CircuitBreakerOpenException.class).isThrownBy(() -> {
            circuitBreaker.call(point);
        }).withStackTraceContaining("circuitbreaker is currently open and cannot handle operations");
        sleep(1000);
        assertThat(circuitBreaker.call(point)).isEqualTo(null);
        assertThatExceptionOfType(CircuitBreakerOpenException.class).isThrownBy(() -> {
            circuitBreaker.call(point);
        }).withStackTraceContaining("circuitbreaker is currently open and cannot handle operations");
        sleep(1000);
        assertThat(circuitBreaker.call(point)).isEqualTo(3);
        assertThat(circuitBreaker.call(point)).isEqualTo(4);
        assertThat(circuitBreaker.call(point)).isEqualTo(5);
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private ProceedingJoinPoint mockProceedingJoinPoint() throws Exception {
        MethodSignature signature = mock(MethodSignature.class);
        when(signature.getMethod()).thenReturn(TestClient.class.getMethod("callService", Object.class));

        ProceedingJoinPoint point = mock(ProceedingJoinPoint.class);
        when(point.getSignature()).thenReturn(signature);

        return point;
    }

    private static class TestClient {

        @IntegrationPoint(maxErrorRatio = 0.4, errorTimeout = 250, openTimePeriod = 1000)
        public Object callService(Object o) {
            return null;
        }
    }
}
