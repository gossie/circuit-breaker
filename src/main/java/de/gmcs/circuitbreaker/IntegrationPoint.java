package de.gmcs.circuitbreaker;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface IntegrationPoint {

    public static final long DEFAULT_ERROR_TIMEOUT = 250L;

    long errorTimeout() default DEFAULT_ERROR_TIMEOUT;
}
