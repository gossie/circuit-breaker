package com.github.gossie.circuitbreaker;

import java.util.concurrent.Callable;

import org.aspectj.lang.ProceedingJoinPoint;

/**
 * The class represents a call of an {@link IntegrationPoint}. It is started in a new {@link Thread}.
 */
class ServiceCall implements Callable<Object> {

    private ProceedingJoinPoint point;

    ServiceCall(ProceedingJoinPoint point) {
        this.point = point;
    }

    @Override
    public Object call() {
        try {
            return point.proceed();
        } catch (Throwable e) {
            throw new IntegrationPointExecutionException(e);
        }
    }
}
