package com.github.gossie.circuitbreaker;

import java.util.concurrent.Callable;
import java.util.function.Function;

import org.aspectj.lang.ProceedingJoinPoint;

/**
 * The class represents a call of an {@link IntegrationPoint}. It is started in a new {@link Thread}.
 */
class ServiceCall<A, R> implements Callable<R> {

    private Function<A, R> function;
    private A argument;

    ServiceCall(Function<A, R> function, A argument) {
        this.function = function;
        this.argument = argument;
    }

    @Override
    public R call() {
        try {
            return function.apply(argument);
        } catch (Throwable e) {
            throw new IntegrationPointExecutionException(e);
        }
    }
}
