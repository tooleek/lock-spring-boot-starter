package org.august.lock.spring.boot.core.strategy;

import org.august.lock.spring.boot.core.LockKey.Builder;
import org.august.lock.spring.boot.exception.KeyBuilderException;

import java.lang.reflect.Method;

/**
 * @BelongsPackage: org.august.lock.spring.boot.core.strategy
 * @Author: zsx
 * @CreateTime: 2019-04-17 10:01
 * @Description:
 */
public abstract class KeyStrategy {
	
	protected String className;
	protected String methodName;
	protected Method realMethod;
	protected Object[] args;
	
	public KeyStrategy(String className,String methodName,Method realMethod, Object[] args) {
		this.className=className;
		this.methodName=methodName;
		this.realMethod=realMethod;
		this.args=args;
	}

    /**
     * 添加锁
     * @param keyBuilder
     * @param realMethod
     * @param args
     */
	public abstract Builder generateBuilder() throws KeyBuilderException;

}
