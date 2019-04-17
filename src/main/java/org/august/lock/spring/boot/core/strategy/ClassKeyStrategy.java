package org.august.lock.spring.boot.core.strategy;

import java.lang.reflect.Method;

import org.august.lock.spring.boot.core.LockKey;
import org.august.lock.spring.boot.core.LockKey.Builder;
import org.august.lock.spring.boot.exception.KeyBuilderException;

public class ClassKeyStrategy extends KeyStrategy {

	public ClassKeyStrategy(String className, String methodName, Method realMethod, Object[] args) {
		super(className, methodName, realMethod, args);
	}

	@Override
	public Builder generateBuilder() throws KeyBuilderException {
		Builder keyBuilder = LockKey.newBuilder();
        keyBuilder.appendKey(new StringBuilder(className).append(".").append(methodName).toString());
        return keyBuilder;
	}

}
