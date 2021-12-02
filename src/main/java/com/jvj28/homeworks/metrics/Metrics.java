package com.jvj28.homeworks.metrics;

import java.lang.reflect.InvocationTargetException;
import java.util.stream.Stream;

@SuppressWarnings("java:S115")
public enum Metrics {

    circuit_level(CircuitLevelHandler.class),
    circuit_watts(CircuitWattsHandler.class),
    total_watts(TotalWattsHandler.class),
    network_status(NetstatHandler.class),
    processor_date(SystemDateHandler.class);

    private final Class<? extends MetricHandler> handler;

    Metrics(Class<? extends MetricHandler> handler) {
        this.handler = handler;
    }

    public MetricHandler handler() {
        try {
            return this.handler.getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            return null;
        }
    }

    public static Stream<Metrics> stream() {
        return Stream.of(Metrics.values());
    }
}
