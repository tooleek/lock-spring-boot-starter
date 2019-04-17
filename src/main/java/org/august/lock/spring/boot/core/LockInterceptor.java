package org.august.lock.spring.boot.core;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.august.lock.spring.boot.annotation.Key;
import org.august.lock.spring.boot.annotation.Lock;
import org.august.lock.spring.boot.core.LockKey.Builder;
import org.august.lock.spring.boot.factory.ServiceBeanFactory;
import org.august.lock.spring.boot.service.LockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 锁拦截器
 *
 * @author TanRq
 */
@Aspect
@Component
public class LockInterceptor {

    Logger logger = LoggerFactory.getLogger(LockInterceptor.class);

    @Autowired
    private ServiceBeanFactory serviceBeanFactory;

    private ThreadLocal<LockService> localLockService = new ThreadLocal<>();

    @Around(value = "@annotation(org.august.lock.spring.boot.annotation.Lock)")
    public Object lockHandle(ProceedingJoinPoint joinPoint) throws Throwable {

        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method targetMethod = methodSignature.getMethod();
        Method realMethod = joinPoint.getTarget().getClass().getDeclaredMethod(methodSignature.getName(), targetMethod.getParameterTypes());

        Lock lock = realMethod.getAnnotation(Lock.class);

        Builder keyBuilder = LockKey.newBuilder()
                .leaseTime(lock.leaseTime())
                .waitTime(lock.waitTime())
                .timeUnit(lock.timeUnit());

        Object[] args = joinPoint.getArgs();

        //parameter lock
        boolean isParameterLock = false;
        for (int i = 0; i < realMethod.getParameters().length; i++) {
            if (realMethod.getParameters()[i].isAnnotationPresent(Key.class)) {
                keyBuilder.appendKey(args[i].toString());
                isParameterLock = true;
            }
        }
        if (!isParameterLock) {
            //methods lock
            if (null != realMethod.getAnnotation(Key.class)) {
                String[] values = realMethod.getAnnotation(Key.class).value();
                for (int i = 0; i < args.length; i++) {
                    Object obj = args[i];
                    Class objectClass = obj.getClass();
                    Field field = objectClass.getDeclaredFields()[0];
                    for (String value : values) {
                        String[] propertyName = value.split("\\.");
                        String[] className = objectClass.getName().split("\\.");
                        if (propertyName[0].equals(className[className.length - 1])) {
                            field.setAccessible(true);
                            if (field.getName().equals(propertyName[1])) {
                                keyBuilder.appendKey(field.get(obj).toString());
                            }
                        }
                    }
                }
            }
            //properties lock
            if (null == realMethod.getAnnotation(Key.class)) {
                for (int i = 0; i < args.length; i++) {
                    Object obj = args[i];
                    Class objectClass = obj.getClass();
                    Field[] fields = objectClass.getDeclaredFields();
                    for (Field field : fields) {
                        if (null != field.getAnnotation(Key.class)) {
                            field.setAccessible(true);
                            keyBuilder.appendKey(field.get(obj).toString());
                        }
                    }
                }
            }
        }

        if (keyBuilder.isEmptyKeys()) {
            String className = joinPoint.getTarget().getClass().getName();
            String methodName = methodSignature.getName();
            keyBuilder.appendKey(new StringBuilder(className).append(".").append(methodName).toString());
        }
        LockKey lockKey = keyBuilder.build();

        LockService lockService = serviceBeanFactory.getService(lock.lockType());
        lockService.setLockKey(lockKey);

        localLockService.set(lockService);

        lockService.lock();

        return joinPoint.proceed();
    }

    @AfterReturning(value = "@annotation(org.august.lock.spring.boot.annotation.Lock)")
    public void afterReturning(JoinPoint joinPoint) {
        localLockService.get().release();
    }

    @AfterThrowing(value = "@annotation(org.august.lock.spring.boot.annotation.Lock)")
    public void afterThrowing(JoinPoint joinPoint) {
        localLockService.get().release();
    }

}
