package org.august.lock.spring.boot.exception;

/**
 * 验证错误异常
 *
 * @author 刘强
 */
public class LockValidateException extends RuntimeException {

    private static final long serialVersionUID = -6925556550996662677L;

    public LockValidateException(String message, Throwable cause) {
        super(message, cause);
    }

    public LockValidateException(Throwable cause) {
        super(cause);
    }

    public LockValidateException(String message) {
        super(message);
    }

    public LockValidateException() {
        super();
    }
}
