package com.jvj28.homeworks.api;

public class NotFoundException extends RuntimeException {

    private final Object obj;

    public NotFoundException(Object obj, String message) {
        super(message);
        this.obj = obj;
    }

    public NotFoundException(Object obj) {
        this.obj = obj;
    }

    public Object getObject() {
        return obj;
    }
}
