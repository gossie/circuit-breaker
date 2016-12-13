package com.github.gossie.circuitbreaker;

import java.util.function.Function;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.lang.reflect.Method;
import java.util.Optional;

/**
 * Document me!
 */
public class CircuitBreaker {

    private final ExecutorService threadpool;
    private State state;

    public CircuitBreaker(double maxErrorRatio, long openTimePeriod, int maxNumberOfSamples) {
        state = new State(maxErrorRatio, openTimePeriod, maxNumberOfSamples);
        threadpool = Executors.newFixedThreadPool(1);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                threadpool.shutdown();
            }
        });
    }

    /**
     * Document me!
     * @param function The {@link Function} wrapped by this CircuitBreaker.
     * @param argument The argument that should be passed to the {@link Function}.
     * @param errorTimeout The maximum number of milliseconds that the execution of the given {@link Function}
     *         may take.
     * @return Returns the return value of the wrapped method. If the operation times out, throws an
     *         exception or the CircuitBreaker is open, the method returns null or an empty Optional (if
     *         the return value of the wrapped method is Optional).
     * @throws InterruptedException
     */
    public <A, R> R call(Function<A, R> function, A argument, long errorTimeout) throws InterruptedException {

        if (state.isOpen()) {
            return (R) determineEmptyResult(function.getClass().getMethods()[0]);
        }

        R result;

        Future<R> future = threadpool.submit(new ServiceCall<A, R>(function, argument));

        try {
            result = future.get(errorTimeout, TimeUnit.MILLISECONDS);
            state.incrementSuccessfulCalls();
        } catch (ExecutionException | TimeoutException e) {
            result = (R) determineEmptyResult(function.getClass().getMethods()[0]);
            state.incrementUnsuccessfulCalls();
        }

        return result;
    }

    private Object determineEmptyResult(Method method) {

         if(method.getReturnType().equals(Optional.class)) {
             return Optional.empty();
         }
        return null;
    }
}
