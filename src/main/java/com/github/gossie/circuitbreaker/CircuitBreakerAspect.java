package com.github.gossie.circuitbreaker;

import java.util.function.Function;
import java.lang.reflect.Method;
import java.util.Optional;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

/**
 * The {@link CircuitBreakerAspect} is implemented as an AspectJ {@link Aspect}. For each class annotated with
 * the {@link IntegrationPoint} annotation, a {@link CircuitBreakerAspect} instance is created.
 */
@Aspect("perthis(@within(IntegrationPointConfiguration))")
public class CircuitBreakerAspect {

    private volatile CircuitBreaker circuitBreaker;

    /**
     * The method wraps around each method annotated with the {@link IntegrationPoint} annotation. It delegates
     * calls to the actual {@link CircuitBreaker}.
     *
     * @param jointPoint The {@link ProceedingJoinPoint} representing the original method invocation.
     * @return The method returns the return value of the {@link CircuitBreaker}.
     * @throws InterruptedException Is thrown if this {@link Thread} is interrupted while waiting.
     */
    @Around("execution(* *(..)) && @annotation(IntegrationPoint)")
    public Object call(ProceedingJoinPoint jointPoint) throws InterruptedException {
        IntegrationPoint integrationPoint = retrieveAnntotation(jointPoint);

        initializeState(jointPoint);

        if(methodReturnsOptional(jointPoint)) {
            return circuitBreaker.call(new Function<Object, Optional>() {

				@Override
				public Optional apply(Object t) {
					return proceedWithReturnTypeOptional(jointPoint);
				}
			}, null, integrationPoint.errorTimeout());
        } else {
        	return circuitBreaker.call(a -> proceedWithReturnTypeObject(jointPoint), null, integrationPoint.errorTimeout());
        }
    }

    private boolean methodReturnsOptional(ProceedingJoinPoint jointPoint) {
		Method method = ((MethodSignature) jointPoint.getSignature()).getMethod();
		return method.getReturnType().equals(Optional.class);
	}

	private Object proceedWithReturnTypeObject(ProceedingJoinPoint jointPoint) {
    	try {
			return jointPoint.proceed();
		} catch (Throwable e) {
			throw new RuntimeException("error in integration point", e);
		}
    }

	@SuppressWarnings("rawtypes")
	private Optional<?> proceedWithReturnTypeOptional(ProceedingJoinPoint jointPoint) {
    	try {
			return (Optional) jointPoint.proceed();
		} catch (Throwable e) {
			throw new RuntimeException("error in integration point", e);
		}
    }



    private void initializeState(ProceedingJoinPoint jointPoint) {
        if (circuitBreaker == null) {
            synchronized(this) {
                if(circuitBreaker == null) {
                    IntegrationPointConfiguration configuration = retrieveConfiguration(jointPoint);
                    circuitBreaker = new CircuitBreaker(configuration.maxErrorRatio(), configuration.openTimePeriod(), configuration.maxNumberOfSamples());
                }
            }
        }
    }

    private IntegrationPointConfiguration retrieveConfiguration(ProceedingJoinPoint jointPoint) {
        return ((MethodSignature) jointPoint.getSignature()).getMethod().getDeclaringClass().getAnnotation(IntegrationPointConfiguration.class);
    }

    private IntegrationPoint retrieveAnntotation(ProceedingJoinPoint jointPoint) {
        return ((MethodSignature) jointPoint.getSignature()).getMethod().getAnnotation(IntegrationPoint.class);
    }
}
