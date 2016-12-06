package de.gmcs.circuitbreaker;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

@Aspect
public class CircuitBreaker {

    private State state;

    @Around("execution(* *(..)) && @annotation(IntegrationPoint)")
    public Object call(ProceedingJoinPoint point) throws CircuitBreakerOpenException, InterruptedException {
        initializeState(point);

        if (state.isOpen()) {
            throw new CircuitBreakerOpenException("circuitbreaker is currently open and cannot handle operations");
        }

        long timeout = determineValue(point, (a) -> a.errorTimeout());

        Object result;

        ExecutorService threadpool = Executors.newFixedThreadPool(1);
        Future<Object> future = threadpool.submit(new ServiceCall(point));

        try {
            result = future.get(timeout, TimeUnit.MILLISECONDS);
            state.incrementSuccessfulCalls();
        } catch (ExecutionException | TimeoutException e) {
            result = null;
            state.incrementUnsuccessfulCalls();
        } catch (InterruptedException e) {
            throw e;
        } finally {
            threadpool.shutdown();
        }

        return result;
    }

    private void initializeState(ProceedingJoinPoint point) {
        if(state == null) {
            double maxErrorRatio = determineValue(point, (a) -> a.maxErrorRatio());
            long openTimePeriod = determineValue(point, (a) -> a.openTimePeriod());
            state = new State(maxErrorRatio, openTimePeriod);
        }
    }

	private <T> T determineValue(ProceedingJoinPoint point, Function<IntegrationPoint, T> function) {
        IntegrationPoint annotation = retrieveAnntotation(point);
        return function.apply(annotation);
    }

    private IntegrationPoint retrieveAnntotation(ProceedingJoinPoint point) {
        return ((MethodSignature) point.getSignature()).getMethod().getAnnotation(IntegrationPoint.class);
    }

    private static class ServiceCall implements Callable<Object> {

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
}
