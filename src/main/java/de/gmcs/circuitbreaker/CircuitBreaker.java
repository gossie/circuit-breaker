package de.gmcs.circuitbreaker;

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

@Aspect("perthis(@annotation(IntegrationPoint))")
public class CircuitBreaker {

    private State state;

    @Around("execution(* *(..)) && @annotation(IntegrationPoint)")
    public Object call(ProceedingJoinPoint point) throws InterruptedException {
        IntegrationPoint annotation = retrieveAnntotation(point);

        initializeState(annotation);

        if (state.isOpen()) {
            return determineEmptyResult(point);
        }

        long timeout = annotation.errorTimeout();

        Object result;

        ExecutorService threadpool = Executors.newFixedThreadPool(1);
        Future<Object> future = threadpool.submit(new ServiceCall(point));

        try {
            result = future.get(timeout, TimeUnit.MILLISECONDS);
            state.incrementSuccessfulCalls();
        } catch (ExecutionException | TimeoutException e) {
            result = determineEmptyResult(point);
            state.incrementUnsuccessfulCalls();
        } finally {
            threadpool.shutdown();
        }

        return result;
    }

    private Object determineEmptyResult(ProceedingJoinPoint point) {
        Class<?> returnType = ((MethodSignature) point.getSignature()).getMethod().getReturnType();
        if(returnType.equals(Optional.class)) {
            return Optional.empty();
        }
        return null;
    }

    private void initializeState(IntegrationPoint annotation) {
        if (state == null) {
            state = new State(annotation.maxErrorRatio(), annotation.openTimePeriod());
        }
    }

    private IntegrationPoint retrieveAnntotation(ProceedingJoinPoint point) {
        return ((MethodSignature) point.getSignature()).getMethod().getAnnotation(IntegrationPoint.class);
    }
}
