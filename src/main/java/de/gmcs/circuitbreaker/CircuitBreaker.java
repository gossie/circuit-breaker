package de.gmcs.circuitbreaker;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.Optional;

import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.ProceedingJoinPoint;

@Aspect
public class CircuitBreaker {

    private enum Status {
        OPEN,
        HALF_OPEN,
        CLOSED
    }

    private Status status = Status.OPEN;
    private long successfulCalls;
    private long unsuccessfulCalls;
    private double maxErrorRatio;
    private long timeout;

    public CircuitBreaker(double maxErrorRatio, long timeout) {
        this.maxErrorRatio = maxErrorRatio;
        this.timeout = timeout;
    }

    /**
     *
     * @param argument The argument is passed to the encapsulated function.
     * @return Returns an optional of the return value of the encapsulated function.
     */
    @Around("execution(* *(..)) && @annotation(IntegrationPoint)")
    public Object call(ProceedingJoinPoint point) {
        Object result;

        ExecutorService threadpool = Executors.newFixedThreadPool(1);
        Future<Object> future = threadpool.submit(new ServiceCall(point));
        try {
            result = future.get(timeout, TimeUnit.MILLISECONDS);
            ++successfulCalls;
        } catch(Exception e) {
            result = null;
            ++unsuccessfulCalls;
        } finally {
            threadpool.shutdown();
        }

        return result;
    }


    private class ServiceCall implements Callable<Object> {
        private ProceedingJoinPoint point;

        ServiceCall(ProceedingJoinPoint point) {
            this.point = point;
        }

        public Object call() {
            try {
                return point.proceed();
            } catch(Throwable e) {
                throw new RuntimeException();
            }
        }
    }
}
