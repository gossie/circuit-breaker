package de.gmcs.circuitbreaker;

import static org.assertj.core.api.Assertions.assertThat;
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
            public Integer answer(InvocationOnMock invocation) throws Throwable {
                sleep(500);
                return Integer.valueOf(1);
            }
        });

        CircuitBreaker circuitBreaker = new CircuitBreaker();

        assertThat(circuitBreaker.call(point)).isNull();
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch(InterruptedException e) {}
    }
    
    private ProceedingJoinPoint mockProceedingJoinPoint()  throws Exception {
    	MethodSignature signature = mock(MethodSignature.class);
    	when(signature.getMethod()).thenReturn(TestClient.class.getMethod("callService", Object.class));
    	
    	ProceedingJoinPoint point = mock(ProceedingJoinPoint.class);
    	when(point.getSignature()).thenReturn(signature);
    	
    	return point;
    }
    
    
    private static class TestClient {
    	@IntegrationPoint(maxErrorRatio = 0.05, timeout = 250)
    	public Object callService(Object o) {
    		return null;
    	}
    }
}