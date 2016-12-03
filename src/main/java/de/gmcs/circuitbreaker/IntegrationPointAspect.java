package de.gmcs.circuitbreaker;

import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.ProceedingJoinPoint;

@Aspect
public class IntegrationPointAspect {

    @Around("execution(* *(..)) && @annotation(IntegrationPoint)")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        Object result = point.proceed();
        return result;
    }
}
