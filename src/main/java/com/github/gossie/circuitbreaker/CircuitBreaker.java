package com.github.gossie.circuitbreaker;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import java.util.Optional;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

@Aspect("perthis(@within(IntegrationPointConfiguration))")
public class CircuitBreaker {

    private State state;

    @Around("execution(* *(..)) && @annotation(IntegrationPoint)")
    public Object call(ProceedingJoinPoint jointPoint) throws InterruptedException {
        IntegrationPoint integrationPoint = retrieveAnntotation(jointPoint);

        initializeState(jointPoint);

        if (state.isOpen()) {
            return determineEmptyResult(jointPoint);
        }

        long timeout = integrationPoint.errorTimeout();

        Object result;

        ExecutorService threadpool = Executors.newFixedThreadPool(1);
        Future<Object> future = threadpool.submit(new ServiceCall(jointPoint));

        try {
            result = future.get(timeout, TimeUnit.MILLISECONDS);
            state.incrementSuccessfulCalls();
        } catch (ExecutionException | TimeoutException e) {
            result = determineEmptyResult(jointPoint);
            state.incrementUnsuccessfulCalls();
        } finally {
            threadpool.shutdown();
        }

        return result;
    }

    private Object determineEmptyResult(ProceedingJoinPoint jointPoint) {
        Class<?> returnType = ((MethodSignature) jointPoint.getSignature()).getMethod().getReturnType();
        if(returnType.equals(Optional.class)) {
            return Optional.empty();
        }
        return null;
    }

    private void initializeState(ProceedingJoinPoint jointPoint) {
        if (state == null) {
            IntegrationPointConfiguration configuration = retrieveConfiguration(jointPoint);
            state = new State(configuration.maxErrorRatio(), configuration.openTimePeriod());
        }
    }

    private IntegrationPointConfiguration retrieveConfiguration(ProceedingJoinPoint jointPoint) {
        return ((MethodSignature) jointPoint.getSignature()).getMethod().getDeclaringClass().getAnnotation(IntegrationPointConfiguration.class);
    }

    private IntegrationPoint retrieveAnntotation(ProceedingJoinPoint jointPoint) {
        return ((MethodSignature) jointPoint.getSignature()).getMethod().getAnnotation(IntegrationPoint.class);
    }
}
