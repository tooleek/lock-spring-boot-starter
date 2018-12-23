package org.august.lock.spring.boot.exception;

/**
 * 未知读取操作的负载均衡模式类型
 *
 * @author 54lxb
 */
public class UnknownReadModeException extends RuntimeException {

    private static final long serialVersionUID = 809616566885491658L;

    public UnknownReadModeException() {
        super("未知读取操作的负载均衡模式类型");
    }

}
