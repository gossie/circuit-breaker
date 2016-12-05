package de.gmcs.circuitbreaker;

public class CircuitBreakerOpenException extends Exception {

    public CircuitBreakerOpenException(String message) {
        super(message);
    }
}
