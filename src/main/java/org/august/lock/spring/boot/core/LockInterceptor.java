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
import org.august.lock.spring.boot.core.strategy.ClassKeyStrategy;
import org.august.lock.spring.boot.core.strategy.KeyStrategy;
import org.august.lock.spring.boot.core.strategy.MethodKeyStrategy;
import org.august.lock.spring.boot.core.strategy.ParameterKeyStrategy;
import org.august.lock.spring.boot.core.strategy.PropertiesKeyStrategy;
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

//        Builder keyBuilder = LockKey.newBuilder()
//                .leaseTime(lock.leaseTime())
//                .waitTime(lock.waitTime())
//                .timeUnit(lock.timeUnit());

        Object[] args = joinPoint.getArgs();
        
        String className = joinPoint.getTarget().getClass().getName();
        String methodName = methodSignature.getName();
        
        KeyStrategy keyStrategy = getKeyStrategy(className, methodName, realMethod,args);

        Builder keyBuilder = new KeyStrategyContext(keyStrategy).generateBuilder();
        
        LockKey lockKey = keyBuilder
        		.leaseTime(lock.leaseTime())
        		.waitTime(lock.waitTime())
        		.timeUnit(lock.timeUnit())
        		.build();
        
        LockService lockService = serviceBeanFactory.getService(lock.lockType());
        lockService.setLockKey(lockKey);

        localLockService.set(lockService);

        lockService.lock();

        return joinPoint.proceed();
    }
    
    private KeyStrategy getKeyStrategy(String className,String methodName,Method realMethod, Object[] args) {
    	//参数锁
    	for (int i = 0; i < realMethod.getParameters().length; i++) {
            if (realMethod.getParameters()[i].isAnnotationPresent(Key.class)) {
                return new ParameterKeyStrategy(className, methodName, realMethod, args);
            }
        }
    	//方法锁
    	if(realMethod.getAnnotation(Key.class).value().length>0) {
    		return new MethodKeyStrategy(className, methodName, realMethod, args);
    	}
    	//属性锁
    	for (int i = 0; i < args.length; i++) {
            Object obj = args[i];
            @SuppressWarnings("rawtypes")
			Class objectClass = obj.getClass();
            Field[] fields = objectClass.getDeclaredFields();
            for (Field field : fields) {
                if (null != field.getAnnotation(Key.class)) {
                    return new PropertiesKeyStrategy(className, methodName, realMethod, args);
                }
            }
        }
    	//类名和方法名作为key的锁
    	return new ClassKeyStrategy(className, methodName, realMethod, args);
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
