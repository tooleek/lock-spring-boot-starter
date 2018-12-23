package org.august.lock.spring.boot.exception;

/**
 * 未知订阅操作的负载均衡模式类型
 *
 * @author 54lxb
 */
public class UnknownSubscriptionModeException extends RuntimeException {

    private static final long serialVersionUID = -2357364241653230393L;

    public UnknownSubscriptionModeException() {
        super("未知订阅操作的负载均衡模式类型");
    }

}
