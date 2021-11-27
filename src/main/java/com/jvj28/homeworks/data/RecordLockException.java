package com.jvj28.homeworks.data;

public class RecordLockException extends RuntimeException {

    public RecordLockException() {
        super();
    }

    public RecordLockException(InterruptedException e) {
        super(e);
    }
}
