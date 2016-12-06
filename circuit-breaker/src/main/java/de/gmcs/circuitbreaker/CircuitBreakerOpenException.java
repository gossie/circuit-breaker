package de.gmcs.circuitbreaker;

public class CircuitBreakerOpenException extends Exception {

    private static final long serialVersionUID = 4784817810920960954L;

    public CircuitBreakerOpenException(String message) {
        super(message);
    }
}
