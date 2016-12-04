package de.gmcs.circuitbreaker;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.function.Function;

import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class CircuitBreakerTest {

    @Test
    public void testCall() throws Throwable {
        ProceedingJoinPoint point = mock(ProceedingJoinPoint.class);
        when(point.proceed()).thenReturn(1);

        CircuitBreaker circuitBreaker = new CircuitBreaker(0.1, 250);

        assertThat(circuitBreaker.call(point)).isEqualTo(1);
    }

    @Test
    public void testCall_error() throws Throwable {
        ProceedingJoinPoint point = mock(ProceedingJoinPoint.class);
        when(point.proceed()).thenThrow(new RuntimeException());

        CircuitBreaker circuitBreaker = new CircuitBreaker(0.1, 250);

        assertThat(circuitBreaker.call(point)).isNull();
    }

    @Test
    public void testCall_timeout() throws Throwable {
        ProceedingJoinPoint point = mock(ProceedingJoinPoint.class);
        when(point.proceed()).thenAnswer(new Answer<Integer>() {
            public Integer answer(InvocationOnMock invocation) throws Throwable {
                sleep(500);
                return Integer.valueOf(1);
            }
        });

        CircuitBreaker circuitBreaker = new CircuitBreaker(0.1, 250);

        assertThat(circuitBreaker.call(point)).isNull();
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch(InterruptedException e) {}
    }
}
