package org.august.lock.spring.boot.core.strategy;

import org.august.lock.spring.boot.core.LockKey;

import java.lang.reflect.Method;

/**
 * @BelongsPackage: org.august.lock.spring.boot.core.strategy
 * @Author: zsx
 * @CreateTime: 2019-04-17 10:01
 * @Description:
 */
public interface KeyStrategy {

    /**
     * 添加锁
     * @param keyBuilder
     * @param realMethod
     * @param args
     */
    public void addKey(LockKey.Builder keyBuilder, Method realMethod, Object[] args) throws IllegalAccessException;

}
