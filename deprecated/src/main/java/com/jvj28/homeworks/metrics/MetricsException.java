package com.jvj28.homeworks.metrics;

import java.io.Serializable;

public class MetricsException extends RuntimeException implements Serializable {

    private static final long serialVersionUID = 6773646076798213389L;

    public MetricsException(String message) {
        super(message);
    }
}
