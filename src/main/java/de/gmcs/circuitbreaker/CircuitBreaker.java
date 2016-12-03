package de.gmcs.circuitbreaker;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.Optional;

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
    private Function<Object, Object> function;

    public CircuitBreaker(double maxErrorRatio, long timeout, Function<Object, Object> function) {
        this.maxErrorRatio = maxErrorRatio;
        this.timeout = timeout;
        this.function = function;
    }

    /**
     *
     * @param argument The argument is passed to the encapsulated function.
     * @return Returns an optional of the return value of the encapsulated function.
     */
    public Optional<Object> call(Object argument) {
        Optional<Object> result;

        ExecutorService threadpool = Executors.newFixedThreadPool(1);
        Future<Object> future = threadpool.submit(new ServiceCall(argument));
        try {
            result = Optional.of(future.get(timeout, TimeUnit.MILLISECONDS));
            ++successfulCalls;
        } catch(Exception e) {
            result = Optional.empty();
            ++unsuccessfulCalls;
        } finally {
            threadpool.shutdown();
        }

        return result;
    }


    private class ServiceCall implements Callable<Object> {
        private Object argument;

        ServiceCall(Object argument) {
            this.argument = argument;
        }

        public Object call() {
            try {
                return function.apply(argument);
            } catch(Exception e) {
                throw new RuntimeException();
            }
        }
    }
}
