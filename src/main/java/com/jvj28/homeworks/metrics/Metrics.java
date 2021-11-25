package com.jvj28.homeworks.metrics;

import org.springframework.lang.NonNull;

import java.lang.reflect.InvocationTargetException;
import java.util.stream.Stream;

public enum Metrics {

    circuit_level(CircuitLevelHandler.class),
    circuit_watts(CircuitWattsHandler.class),
    total_watts(TotalWattsHandler.class);

    private final Class<? extends MetricHandler> _handler;

    Metrics(Class<? extends MetricHandler> handler) {
        this._handler = handler;
    }

    @NonNull
    public MetricHandler handler() {
        try {
            return this._handler.getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            return null;
        }
    }

    public static Stream<Metrics> stream() {
        return Stream.of(Metrics.values());
    }
}
