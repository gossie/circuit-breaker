package de.gmcs.circuitbreaker;

public class IntegrationPointExecutionException extends RuntimeException {

    private static final long serialVersionUID = -3032993760324114414L;

    public IntegrationPointExecutionException(Throwable t) {
        super(t);
    }

}
