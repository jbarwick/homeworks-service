package com.jvj28.homeworks.metrics;

import com.jvj28.homeworks.data.Model;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Set;

@Service
public class MetricsService {

    private final ArrayList<Metric> metrics = new ArrayList<>();
    private final Model model;

    public MetricsService(Model model) {
        this.model = model;
    }

    private MetricsService add(Metrics metric) {
        MetricHandler handler = metric.handler();
        add(handler.getMetricValues(model));
        return this;
    }

    public void add(Set<Metric> metrics) {
        if (metrics != null)
            addIf(metrics);
    }

    private void addIf(Set<Metric> metrics) {
        for (Metric metric: metrics) {
            for (Metric m : this.metrics) {
                if (m.equals(metric)) {
                    m.setValue(metric.getValue());
                    return;
                }
            }
            this.metrics.add(metric);
        }
    }

    public MetricsService collect(Metrics m) {
        return add(m);
    }

    /**
     * Iterate through ALL the metrics defined in the ENUM Metrics, lookup all the values
     * from the Model database registry and put in an array of the Metric object.
     *
     * Running toString on the returned MetricsCollector will output a Prometheus compatible list
     * of metrics
     *
     * @return a MetricsCollector object.
     */
    public MetricsService collect() {
        this.metrics.clear();
        Metrics.stream().forEach(this::collect);
        return this;
    }

    public void clear() {
        this.metrics.clear();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        this.metrics.forEach(m -> sb.append("hw_").append(m.toString()).append("\n"));
        return sb.toString();
    }
}
