package de.gmcs.circuitbreaker.it;

import de.gmcs.circuitbreaker.IntegrationPoint;

import org.junit.Test;

public class TestClient {

    private TestService service = new TestService();

    @IntegrationPoint(timeout = 250)
    public Integer serviceCall() {
        return service.service();
    }
}
