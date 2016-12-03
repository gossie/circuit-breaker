package de.gmcs.circuitbreaker;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.function.Function;
import org.junit.Before;
import org.junit.Test;

public class CircuitBreakerTest {

    @Test
    public void testCall() throws Exception {
        Function<Object, Object> function = i -> ((Integer)i).intValue()+1;

        CircuitBreaker circuitBreaker = new CircuitBreaker(0.1, 250, function);

        assertThat(circuitBreaker.call(0)).contains(1);
    }

    @Test
    public void testCall_error() throws Exception {
        Function<Object, Object> function = i -> {throw new RuntimeException();};

        CircuitBreaker circuitBreaker = new CircuitBreaker(0.1, 250, function);

        assertThat(circuitBreaker.call(0)).isEmpty();
    }

    @Test
    public void testCall_timeout() throws Exception {
        Function<Object, Object> function = i -> {
            sleep(500);
            return -1;
        };

        CircuitBreaker circuitBreaker = new CircuitBreaker(0.1, 250, function);

        assertThat(circuitBreaker.call(0)).isEmpty();
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch(InterruptedException e) {}
    }
}
