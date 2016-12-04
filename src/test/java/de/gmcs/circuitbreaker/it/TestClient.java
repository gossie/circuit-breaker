package de.gmcs.circuitbreaker.it;

import de.gmcs.circuitbreaker.IntegrationPoint;

import org.junit.Test;

public class TestClient {

    private TestService service = new TestService();

    @IntegrationPoint
    public Integer serviceCall() {
        return service.service();
    }
}
