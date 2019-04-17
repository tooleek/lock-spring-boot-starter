package org.august.lock.spring.boot.core.strategy;

import org.august.lock.spring.boot.annotation.Key;
import org.august.lock.spring.boot.core.LockKey;

import java.lang.reflect.Method;

/**
 * @BelongsPackage: org.august.lock.spring.boot.core.strategy
 * @Author: zsx
 * @CreateTime: 2019-04-17 10:00
 * @Description: 参数锁处理
 */
public class ParameterKeyStrategy implements KeyStrategy {

    @Override
    public void addKey(LockKey.Builder keyBuilder, Method realMethod, Object[] args) {
        for (int i = 0; i < realMethod.getParameters().length; i++) {
            if (realMethod.getParameters()[i].isAnnotationPresent(Key.class)) {
                keyBuilder.appendKey(args[i].toString());
            }
        }
    }
}
