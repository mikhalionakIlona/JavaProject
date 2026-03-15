package com.example.photostudio.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class LoggingAspect {
    @Pointcut("execution(* com.example.photostudio.service.*.*(..))")
    public void serviceMethods() {
    }

    @Around("serviceMethods()")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();

        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        log.debug("Вызов метода {}.{}", className, methodName);

        try {
            Object result = joinPoint.proceed();
            long elapsedTime = System.currentTimeMillis() - start;

            log.debug("Метод {}.{} выполнен за {} мс", className, methodName, elapsedTime);

            return result;
        } catch (Exception e) {
            long elapsedTime = System.currentTimeMillis() - start;
            log.error("Ошибка в методе {}.{}: {} (время выполнения: {} мс)",
                    className, methodName, e.getMessage(), elapsedTime);
            throw e;
        }
    }
}
