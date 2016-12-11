package com.github.gossie.circuitbreaker;

/**
 * The exception is thrown by the {@link ServiceCall} if an execution error occurs.
 */
class IntegrationPointExecutionException extends RuntimeException {

    private static final long serialVersionUID = -3032993760324114414L;

    IntegrationPointExecutionException(Throwable t) {
        super(t);
    }

}
