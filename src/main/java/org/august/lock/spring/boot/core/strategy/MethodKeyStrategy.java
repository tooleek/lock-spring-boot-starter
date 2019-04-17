package org.august.lock.spring.boot.core.strategy;

import org.august.lock.spring.boot.annotation.Key;
import org.august.lock.spring.boot.core.LockKey;
import org.august.lock.spring.boot.core.LockKey.Builder;
import org.august.lock.spring.boot.exception.KeyBuilderException;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @BelongsPackage: org.august.lock.spring.boot.core.strategy
 * @Author: zsx
 * @CreateTime: 2019-04-17 10:00
 * @Description: 方法锁处理
 */
public class MethodKeyStrategy extends KeyStrategy {

	public MethodKeyStrategy(String className, String methodName, Method realMethod, Object[] args) {
		super(className, methodName, realMethod, args);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Builder generateBuilder() throws KeyBuilderException {
		Builder keyBuilder = LockKey.newBuilder();
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
                        try {
							keyBuilder.appendKey(field.get(obj).toString());
						} catch (IllegalArgumentException e) {
							throw new KeyBuilderException("生成builder失败",e);
						} catch (IllegalAccessException e) {
							throw new KeyBuilderException("生成builder失败",e);
						}
                    }
                }
            }
        }
        return keyBuilder;
	}
}
