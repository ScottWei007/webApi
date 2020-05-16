package com.scottwei.mt5webapi.mt5Api.exception;

/**
 * @author Scott Wei
 * @date 2019/7/30 18:54
 **/
public class RemoteException extends RuntimeException {

    public RemoteException() {
        super();
    }

    public RemoteException(String message) {
        super(message);
    }

    public RemoteException(String message, Throwable cause) {
        super(message, cause);
    }

    public RemoteException(Throwable cause) {
        super(cause);
    }

    protected RemoteException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    @Override
    public String toString() {
        return "RemoteException";
    }
}
