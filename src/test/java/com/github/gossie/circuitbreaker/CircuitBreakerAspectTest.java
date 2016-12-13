package com.github.gossie.circuitbreaker;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.function.Consumer;
import java.util.Optional;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class CircuitBreakerAspectTest {

    @Test
    public void testCall() throws Throwable {
        ProceedingJoinPoint point = mockProceedingJoinPoint("callService");
        when(point.proceed()).thenReturn(1);

        CircuitBreakerAspect circuitBreakerAspect = new CircuitBreakerAspect();

        assertThat(circuitBreakerAspect.call(point)).isEqualTo(1);
    }

    @Test
    public void testCall_error() throws Throwable {
        ProceedingJoinPoint point = mockProceedingJoinPoint("callService");
        when(point.proceed()).thenThrow(new RuntimeException());

        CircuitBreakerAspect circuitBreakerAspect = new CircuitBreakerAspect();

        assertThat(circuitBreakerAspect.call(point)).isNull();
    }

    @Test
    public void testCall_timeout() throws Throwable {
        ProceedingJoinPoint point = mockProceedingJoinPoint("callService");
        when(point.proceed()).thenAnswer(new Answer<Integer>() {

            @Override
            public Integer answer(InvocationOnMock invocation) throws Throwable {
                sleep(500);
                return Integer.valueOf(1);
            }
        });

        CircuitBreakerAspect circuitBreakerAspect = new CircuitBreakerAspect();

        assertThat(circuitBreakerAspect.call(point)).isNull();
    }

    @Test
    public void testCall_circuitBreakerAspectOpensUp() throws Throwable {
        ProceedingJoinPoint point = mockProceedingJoinPoint("callService");
        when(point.proceed())
                .thenReturn(1)
                .thenReturn(2)
                .thenThrow(new RuntimeException())
                .thenThrow(new RuntimeException())
                .thenReturn(3)
                .thenReturn(4)
                .thenReturn(5);

        CircuitBreakerAspect circuitBreakerAspect = new CircuitBreakerAspect();

        assertThat(circuitBreakerAspect.call(point)).isEqualTo(1);
        assertThat(circuitBreakerAspect.call(point)).isEqualTo(2);
        assertThat(circuitBreakerAspect.call(point)).isEqualTo(null);
        assertThat(circuitBreakerAspect.call(point)).isEqualTo(null);
        sleep(1000);
        assertThat(circuitBreakerAspect.call(point)).isEqualTo(null);
        assertThat(circuitBreakerAspect.call(point)).isEqualTo(null);
        sleep(1000);
        assertThat(circuitBreakerAspect.call(point)).isEqualTo(3);
        assertThat(circuitBreakerAspect.call(point)).isEqualTo(4);
        assertThat(circuitBreakerAspect.call(point)).isEqualTo(5);
    }

    @Test
    public void testCallOptional() throws Throwable {
        ProceedingJoinPoint point = mockProceedingJoinPoint("callOptionalService");
        when(point.proceed()).thenReturn(Optional.of(1));

        CircuitBreakerAspect circuitBreakerAspect = new CircuitBreakerAspect();

        assertThat(circuitBreakerAspect.call(point)).isInstanceOfSatisfying(Optional.class, o -> assertThat(o).contains(1));
    }

    @Test
    public void testCallOptional_timeout() throws Throwable {
        ProceedingJoinPoint point = mockProceedingJoinPoint("callOptionalService");
        when(point.proceed()).thenAnswer(new Answer<Optional<Integer>>() {

            @Override
            public Optional<Integer> answer(InvocationOnMock invocation) throws Throwable {
                sleep(500);
                return Optional.of(1);
            }
        });

        CircuitBreakerAspect circuitBreakerAspect = new CircuitBreakerAspect();

        assertThat(circuitBreakerAspect.call(point)).isInstanceOfSatisfying(Optional.class, o -> assertThat(o).isEmpty());
    }

    @Test
    public void testCallOptional_circuitBreakerAspectOpensUp() throws Throwable {
        ProceedingJoinPoint point = mockProceedingJoinPoint("callOptionalService");
        when(point.proceed())
                .thenReturn(Optional.of(1))
                .thenReturn(Optional.of(2))
                .thenThrow(new RuntimeException())
                .thenThrow(new RuntimeException())
                .thenReturn(Optional.of(3))
                .thenReturn(Optional.of(4))
                .thenReturn(Optional.of(5));

        CircuitBreakerAspect circuitBreakerAspect = new CircuitBreakerAspect();

        assertThat(circuitBreakerAspect.call(point)).isInstanceOfSatisfying(Optional.class, o -> assertThat(o).contains(1));
        assertThat(circuitBreakerAspect.call(point)).isInstanceOfSatisfying(Optional.class, o -> assertThat(o).contains(2));
        assertThat(circuitBreakerAspect.call(point)).isInstanceOfSatisfying(Optional.class, o -> assertThat(o).isEmpty());
        assertThat(circuitBreakerAspect.call(point)).isInstanceOfSatisfying(Optional.class, o -> assertThat(o).isEmpty());
        sleep(1000);
        assertThat(circuitBreakerAspect.call(point)).isInstanceOfSatisfying(Optional.class, o -> assertThat(o).isEmpty());
        assertThat(circuitBreakerAspect.call(point)).isInstanceOfSatisfying(Optional.class, o -> assertThat(o).isEmpty());
        sleep(1000);
        assertThat(circuitBreakerAspect.call(point)).isInstanceOfSatisfying(Optional.class, o -> assertThat(o).contains(3));
        assertThat(circuitBreakerAspect.call(point)).isInstanceOfSatisfying(Optional.class, o -> assertThat(o).contains(4));
        assertThat(circuitBreakerAspect.call(point)).isInstanceOfSatisfying(Optional.class, o -> assertThat(o).contains(5));
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private ProceedingJoinPoint mockProceedingJoinPoint(String methodName) throws Exception {
        MethodSignature signature = mock(MethodSignature.class);
        when(signature.getMethod()).thenReturn(TestClient.class.getMethod(methodName, Object.class));

        ProceedingJoinPoint point = mock(ProceedingJoinPoint.class);
        when(point.getSignature()).thenReturn(signature);

        return point;
    }

    @IntegrationPointConfiguration(maxErrorRatio = 0.4, openTimePeriod = 900, maxNumberOfSamples = 2)
    private static class TestClient {

        @IntegrationPoint(errorTimeout = 250)
        public Object callService(Object o) {
            return null;
        }

        @IntegrationPoint(errorTimeout = 250)
        public Optional<Object> callOptionalService(Object o) {
            return null;
        }
    }
}
