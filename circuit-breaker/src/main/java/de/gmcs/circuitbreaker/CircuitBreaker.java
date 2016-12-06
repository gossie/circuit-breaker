package de.gmcs.circuitbreaker;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import de.gmcs.circuitbreaker.status.Closed;
import de.gmcs.circuitbreaker.status.Open;
import de.gmcs.circuitbreaker.status.Status;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

@Aspect
public class CircuitBreaker {

    private Status status = new Closed();
    private long successfulCalls;
    private long unsuccessfulCalls;

    @Around("execution(* *(..)) && @annotation(IntegrationPoint)")
    public Object call(ProceedingJoinPoint point) throws CircuitBreakerOpenException, InterruptedException {
        if (status instanceof Open) {
            throw new CircuitBreakerOpenException("circuitbreaker is currently open and cannot handle operations");
        }

        double maxErrorRatio = determineMaxErrorRatio(point);
        long timeout = determineTimeout(point);

        Object result;

        ExecutorService threadpool = Executors.newFixedThreadPool(1);
        Future<Object> future = threadpool.submit(new ServiceCall(point));

        try {
            result = future.get(timeout, TimeUnit.MILLISECONDS);
            ++successfulCalls;
        } catch (ExecutionException | TimeoutException e) {
            result = null;
            ++unsuccessfulCalls;
        } catch (InterruptedException e) {
            throw e;
        } finally {
            calculateStatus(maxErrorRatio);
            threadpool.shutdown();
        }

        return result;
    }

    private void calculateStatus(double maxErrorRatio) {
        if (calculateCurrentRatio() > maxErrorRatio) {
            status = new Open();
        }
    }

    private double calculateCurrentRatio() {
        if (successfulCalls == 0L) {
            return 1.0;
        }
        return (double) unsuccessfulCalls / successfulCalls;
    }

    private double determineMaxErrorRatio(ProceedingJoinPoint point) {
        IntegrationPoint annotation = retrieveAnntotation(point);
        return annotation.maxErrorRatio();
    }

    private long determineTimeout(ProceedingJoinPoint point) {
        IntegrationPoint annotation = retrieveAnntotation(point);
        return annotation.errorTimeout();
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
