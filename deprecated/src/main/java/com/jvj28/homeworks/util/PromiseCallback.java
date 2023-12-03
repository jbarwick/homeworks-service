package com.jvj28.homeworks.util;

public interface PromiseCallback<T> {
    void onComplete(T command);
}
