package com.jvj28.homeworks.model;

public class LockException extends RuntimeException {

    public LockException() {
        super();
    }

    public LockException(InterruptedException e) {
        super(e);
    }
}
