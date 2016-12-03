package de.gmcs.circuitbreaker;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.function.Function;
import org.junit.Before;
import org.junit.Test;

public class CircuitBreakerTest {

  @Test
  public void testCall() {
    Function<Object, Object> function = i -> ((Integer)i).intValue()+1;

    CircuitBreaker circuitBreaker = new CircuitBreaker(0.1, function);

    assertThat(circuitBreaker.call(0)).contains(1);
  }

  @Test
  public void testCall_error() {
    Function<Object, Object> function = i -> {throw new RuntimeException();};

    CircuitBreaker circuitBreaker = new CircuitBreaker(0.1, function);

    assertThat(circuitBreaker.call(0)).isEmpty();
  }
}
