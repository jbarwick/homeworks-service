package com.jvj28.homeworks.model;

public class RecordLockException extends RuntimeException {

    public RecordLockException() {
        super();
    }

    public RecordLockException(InterruptedException e) {
        super(e);
    }
}
