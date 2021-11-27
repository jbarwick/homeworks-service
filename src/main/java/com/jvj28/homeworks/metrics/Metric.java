package com.jvj28.homeworks.metrics;

import lombok.Data;
import lombok.NonNull;

import java.util.Objects;

/**
 * <p>Hold the value of a metric to display in the metrics list.</p>
 *
 * <p>WARNING:  value is NOT in the hash/equals.  DO NOT use a.equals(b) to compare
 * this object if you are trying to check if the values are equal</p>
 *
 * <p></p>if you want to compare this object including its value, you need to write your own
 * function.</p>
 * <pre>
 *     if (metric1.equals(metric2) {
 *         ... the metrics are the same Metric.  The value may be different.
 *     }
 *     if (metric1.equals(metric2) && metric1.getValue().equals(metric2.getValue) {
 *         ... the metrics and their values are the same.
 *     }
 * </pre>
 *
 */
@Data
public class Metric {

    @NonNull
    String name;

    LabelList label;

    @NonNull
    String value;

    public Metric(@NonNull String name, LabelList label, @NonNull String value) {
        this.name = name;
        this.label = label;
        this.value = value;
    }

    public Metric(@NonNull String name, @NonNull String value) {
        this.name = name;
        this.value =value;
    }

    public Metric(@NonNull Metrics metric, LabelList label, int value) {
        this(metric.toString(), label, Integer.toString(value));
    }

    public Metric(@NonNull Metrics metric, int value) {
        this(metric.toString(), Integer.toString(value));
    }

    public Metric(@NonNull Metrics metric, @NonNull String value) {
        this.name = metric.toString();
        this.value = value;
    }

    public Metric(@NonNull String name, long value) {
        this(name, Long.toString(value));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Metric metric = (Metric) o;
        return name.equals(metric.name) && Objects.equals(label, metric.label);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, label);
    }

    @Override
    public String toString() {
        if (label == null)
            return String.format("%s %s", name, value);
        else
            return String.format("%s{%s} %s", name, label, value);
    }
}
