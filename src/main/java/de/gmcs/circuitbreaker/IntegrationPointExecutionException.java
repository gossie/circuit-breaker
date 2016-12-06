package de.gmcs.circuitbreaker;

class IntegrationPointExecutionException extends RuntimeException {

    private static final long serialVersionUID = -3032993760324114414L;

    IntegrationPointExecutionException(Throwable t) {
        super(t);
    }

}
