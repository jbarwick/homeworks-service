package com.jvj28.homeworks.api;

import java.io.Serializable;

public class NotFoundException extends RuntimeException implements Serializable {

    private static final long serialVersionUID = -7914273648161723765L;

    private final Serializable obj;

    public NotFoundException(Serializable obj, String message) {
        super(message);
        this.obj = obj;
    }

    public NotFoundException(Serializable obj) {
        this.obj = obj;
    }

    public Serializable getObject() {
        return obj;
    }
}
