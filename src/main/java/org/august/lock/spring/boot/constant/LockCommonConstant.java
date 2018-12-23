package org.august.lock.spring.boot.constant;

/**
 * 公共常量
 *
 * @author 54lxb
 * @version 1.1.0
 * @apiNote 知识改变命运，技术改变世界
 * @since 2018-12-23 15:35
 */
public interface LockCommonConstant {
    /**
     * 默认客户端名字
     */
    String LOCK = "Lock";
    /**
     * 默认SSL实现方式：JDK
     */
    String JDK = "JDK";
    /**
     * 逗号
     */
    String COMMA = ",";
    /**
     * 冒号
     */
    String COLON = ":";
    /**
     * 分号
     */
    String SEMICOLON = ";";
    /**
     * redis默认URL前缀
     */
    String REDIS_URL_PREFIX = "redis://";
}
