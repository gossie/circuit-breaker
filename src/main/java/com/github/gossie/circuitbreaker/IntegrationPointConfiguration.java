package com.github.gossie.circuitbreaker;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Use this annotation to annotate classes that contain {@link IntegrationPoint}s.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface IntegrationPointConfiguration {

    public static final double DEFAULT_MAX_ERROR_RATIO = 0.05;
    public static final long DEFAULT_OPEN_TIME_PERIOD = 5000L;
    public static final int DEFAULT_MAX_NUMBER_OF_SAMPLES = 1000;

    /**
     * If, within the last n calls (where n is {@link IntegrationPointConfiguration#maxNumberOfSamples}), the
     * result of unsuccessfulCalls / n is greater that maxErrorRatio, the {@link CircuitBreaker} opens up.
     */
    double maxErrorRatio() default DEFAULT_MAX_ERROR_RATIO;

    /**
     * The amount of time the {@link CircuitBreaker} stays open.
     */
    long openTimePeriod() default DEFAULT_OPEN_TIME_PERIOD;

    /**
     * The number of samples the {@link CircuitBreaker} collects.
     */
    int maxNumberOfSamples() default DEFAULT_MAX_NUMBER_OF_SAMPLES;
}
