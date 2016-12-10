package com.github.gossie.circuitbreaker;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface IntegrationPointConfiguration {

    public static final double DEFAULT_MAX_ERROR_RATIO = 0.05;
    public static final long DEFAULT_OPEN_TIME_PERIOD = 5000L;
    public static final int DEFAULT_MAX_NUMBER_OF_SAMPLES = 1000;

    double maxErrorRatio() default DEFAULT_MAX_ERROR_RATIO;
    long openTimePeriod() default DEFAULT_OPEN_TIME_PERIOD;
    int maxNumberOfSamples() default DEFAULT_MAX_NUMBER_OF_SAMPLES;
}
