package org.august.lock.spring.boot.core.strategy;

import org.august.lock.spring.boot.core.LockKey;

import java.lang.reflect.Method;

/**
 * @BelongsPackage: org.august.lock.spring.boot.core.strategy
 * @Author: zsx
 * @CreateTime: 2019-04-17 10:14
 * @Description:
 */
public class SelectionStrategy {

    private KeyStrategy keyStrategy;

    public SelectionStrategy(KeyStrategy keyStrategy){
        this.keyStrategy = keyStrategy;
    }

    public void addKey(LockKey.Builder keyBuilder, Method realMethod, Object[] args) throws IllegalAccessException {
        keyStrategy.addKey(keyBuilder,realMethod,args);
    }
}
