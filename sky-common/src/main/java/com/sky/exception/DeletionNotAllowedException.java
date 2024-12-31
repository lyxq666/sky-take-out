package com.sky.exception;

public class DeletionNotAllowedException extends BaseException {

    //删除操作不被允许
    public DeletionNotAllowedException(String msg) {
        super(msg);
    }

}
