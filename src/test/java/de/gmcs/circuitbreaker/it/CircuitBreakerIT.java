package de.gmcs.circuitbreaker.it;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class CircuitBreakerIT {

    @Test
    public void test() {
        TestClient client = new TestClient();
        assertThat(client.serviceCall()).isEqualTo(1);
    }
}
