package com.jvj28.homeworks.api;

public class RequestTimeoutException extends RuntimeException {

    public RequestTimeoutException(String message) {
        super(message);
    }
}
