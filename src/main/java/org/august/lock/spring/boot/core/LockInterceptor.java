package org.august.lock.spring.boot.core;

import java.lang.reflect.Method;

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

/**
 * 锁拦截器
 * @author TanRq
 *
 */
@Aspect
@Component
public class LockInterceptor {
	
	Logger logger=LoggerFactory.getLogger(LockInterceptor.class);
	
	@Autowired
	private ServiceBeanFactory serviceBeanFactory;
	
	private ThreadLocal<LockService> localLockService = new ThreadLocal<>();
	
	@Around(value="@annotation(org.august.lock.spring.boot.annotation.Lock)")
	public Object lockHandle(ProceedingJoinPoint joinPoint) throws Throwable {
		
		MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature(); 
	    Method targetMethod = methodSignature.getMethod();
	    Method realMethod = joinPoint.getTarget().getClass().getDeclaredMethod(methodSignature.getName(), targetMethod.getParameterTypes());
	    
	    Lock lock= realMethod.getAnnotation(Lock.class);
	    
	    Builder keyBuilder = LockKey.newBuilder()
	    		                    .leaseTime(lock.leaseTime())
	    		                    .waitTime(lock.waitTime())
	    		                    .timeUnit(lock.timeUnit());
	    
	    Object[] args= joinPoint.getArgs();
	    
	    for(int i=0;i<realMethod.getParameters().length;i++) {
	    	if(realMethod.getParameters()[i].isAnnotationPresent(Key.class)) {
	    		keyBuilder.appendKey(args[i].toString());
	    	}
	    }
	    
	    if(keyBuilder.isEmptyKeys()) {
	    	String className = joinPoint.getTarget().getClass().getName();
	    	String methodName = methodSignature.getName();
	    	keyBuilder.appendKey(new StringBuilder(className).append(".").append(methodName).toString());
	    }
	    LockKey lockKey=keyBuilder.build();
	    
	    LockService lockService = serviceBeanFactory.getService(lock.lockType());
	    lockService.setLockKey(lockKey);
	    
	    localLockService.set(lockService);
	    
	    lockService.lock();
	    
	    return joinPoint.proceed();
	}
	
	@AfterReturning(value="@annotation(org.august.lock.spring.boot.annotation.Lock)")
	public void afterReturning(JoinPoint joinPoint) {
		localLockService.get().release();
	}
	
	@AfterThrowing(value="@annotation(org.august.lock.spring.boot.annotation.Lock)")
	public void afterThrowing(JoinPoint joinPoint) {
		localLockService.get().release();
	}
	
}
