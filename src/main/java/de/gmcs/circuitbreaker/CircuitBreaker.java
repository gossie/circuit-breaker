package de.gmcs.circuitbreaker;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.Optional;

public class CircuitBreaker implements InvocationHandler {

    private enum Status {
        OPEN,
        HALF_OPEN,
        CLOSED
    }

    private Status status = Status.OPEN;
    private long successfulCalls;
    private long unsuccessfulCalls;

    @Override
    public Object invoke(Object instance, Method method, Object[] args) {
        double maxErrorRatio = determineMaxErrorRatio(method);
        long timeout = determineTimeout(method);

        Object result;

        ExecutorService threadpool = Executors.newFixedThreadPool(1);
        Future<Object> future = threadpool.submit(new ServiceCall(instance, method, args));

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

    private double determineMaxErrorRatio(Method method) {
        IntegrationPoint annotation = method.getAnnotation(IntegrationPoint.class);
        return annotation.maxErrorRatio();
    }

    private long determineTimeout(Method method) {
        IntegrationPoint annotation = method.getAnnotation(IntegrationPoint.class);
        return annotation.timeout();
    }


    private static class ServiceCall implements Callable<Object> {

        private Method method;
        private Object instance;
        private Object[] args;

        ServiceCall(Object instance, Method method, Object[] args) {
            this.method = method;
            this.instance = instance;
            this.args = args;
        }

        public Object call() {
            try {
                return method.invoke(instance, args);
            } catch(Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
