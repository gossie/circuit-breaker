package de.gmcs.circuitbreaker;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface IntegrationPoint {

    public static final double DEFAULT_MAX_ERROR_RATIO = 0.05;
    public static final long DEFAULT_ERROR_TIMEOUT = 250L;
    public static final long DEFAULT_OPEN_TIMEOUT = 5000L;

    double maxErrorRatio() default DEFAULT_MAX_ERROR_RATIO;
    long errorTimeout() default DEFAULT_ERROR_TIMEOUT;
    long openTimeout() default DEFAULT_OPEN_TIMEOUT;
}
