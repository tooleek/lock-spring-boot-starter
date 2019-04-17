package org.august.lock.spring.boot.core.strategy;

import org.august.lock.spring.boot.annotation.Key;
import org.august.lock.spring.boot.core.LockKey;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @BelongsPackage: org.august.lock.spring.boot.core.strategy
 * @Author: zsx
 * @CreateTime: 2019-04-17 10:00
 * @Description: 属性锁处理
 */
public class PropertiesKeyStrategy implements KeyStrategy {


    @Override
    public void addKey(LockKey.Builder keyBuilder, Method realMethod, Object[] args) throws IllegalAccessException {
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
