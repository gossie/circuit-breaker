package de.gmcs.circuitbreaker.it;

import static org.assertj.core.api.Assertions.assertThat;

import de.gmcs.circuitbreaker.CircuitBreakerInitializer;

import org.junit.Test;

public class CircuitBreakerIT {

    @Test
    public void test() throws Exception {
        CircuitBreakerInitializer circuitBreakerInitializer = new CircuitBreakerInitializer();
        circuitBreakerInitializer.setRoot("de/gmcs/circuitbreaker");
        circuitBreakerInitializer.scan();
    }
}
