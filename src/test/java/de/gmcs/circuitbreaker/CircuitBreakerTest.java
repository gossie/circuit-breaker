package de.gmcs.circuitbreaker;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;

public class CircuitBreakerTest {

    @Test
    public void testCall() throws Throwable {
        Client client = new Client();
        Method method = Client.class.getMethod("call", Integer.class);

        CircuitBreaker circuitBreaker = new CircuitBreaker();
        Object result = circuitBreaker.invoke(client, method, new Object[] { Integer.valueOf(0) });

        assertThat(result)
            .isEqualTo(1);
    }

    @Test
    public void testCall_error() throws Throwable {
        Client client = new Client() {
            @IntegrationPoint
            public Integer call(Integer i) {
                throw new RuntimeException();
            }
        };
        Method method = Client.class.getMethod("call", Integer.class);

        CircuitBreaker circuitBreaker = new CircuitBreaker();
        Object result = circuitBreaker.invoke(client, method, new Object[] { Integer.valueOf(0) });

        assertThat(result).isNull();
    }

    @Test
    public void testCall_timeout() throws Throwable {
      Client client = new Client() {
          @IntegrationPoint
          public Integer call(Integer i) {
              sleep(500);
              return super.call(i);
          }
      };
      Method method = Client.class.getMethod("call", Integer.class);

      CircuitBreaker circuitBreaker = new CircuitBreaker();
      Object result = circuitBreaker.invoke(client, method, new Object[] { Integer.valueOf(0) });

      assertThat(result).isNull();
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch(InterruptedException e) {}
    }


    private static class Client {
        @IntegrationPoint
        public Integer call(Integer i) {
            return i.intValue() + 1;
        }
    }
}
