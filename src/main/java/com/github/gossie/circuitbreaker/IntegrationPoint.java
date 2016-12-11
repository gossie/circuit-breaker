package com.github.gossie.circuitbreaker;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Use this annotation to annotate methods that contain invocations of the service you want to encapsulate.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface IntegrationPoint {

    public static final long DEFAULT_ERROR_TIMEOUT = 250L;

    /**
     * The time (in milliseconds) a {@link CircuitBreaker} waits until the execuion of the integration point
     * is marked as unsuccessful.
     */
    long errorTimeout() default DEFAULT_ERROR_TIMEOUT;
}
