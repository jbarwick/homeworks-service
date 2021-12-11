package com.jvj28.homeworks.metrics;

import com.jvj28.homeworks.metrics.handler.MetricHandler;
import com.jvj28.homeworks.model.Model;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;

@Service
public class MetricsService {

    private final ArrayList<Metric> metrics = new ArrayList<>();
    private final Model model;

    public MetricsService(Model model) {
        this.model = model;
    }

    /**
     * Add the {@link Metrics} item to the collection.  The {@link MetricHandler} is
     * used to retrieve the values from the database or redis cache.
     *
     * Do Note that the handler can return MULTIPLE Metric objects.  So, this function
     * always executes the handler to gather all the individual metrics to collect.
     *
     * If an individual {@link Metric} is already in the list, only the value is updated.
     *
     * @param metrics the Metrics ENUM that you wish to add
     */
    public void add(Metrics metrics) {
        // Instantiate the handler class
        MetricHandler handler = metrics.handler();
        // A null is possible if for some weird reason the class cannot be instantiated
        if (handler != null)
            add(handler.getMetricValues(model));
    }

    /**
     * Add a Metric to the list of metrics to collect.  If
     * the metric is already in this list, its value is updated
     *
     * @param metrics The Collection of metrics to add
     */
    public void add(Collection<Metric> metrics) {
        metrics.forEach(this::add);
    }

    /**
     * Add a metrics to the list of collected metrics.  If the metric is already
     * in the list, its value is updated.
     *
     * @param metric The metric to add to the collection
     */
    public void add(Metric metric) {
        Metric existing = getMetric(metric);
        if (existing == null)
            this.metrics.add(metric);
        else
            existing.setValue(metric.getValue());
    }

    /**
     * Return the {@link Metric} that is in the list
     * @param metric The Metric to find int he list
     * @return The Metric in the list or null
     */
    public Metric getMetric(Metric metric) {
        return this.metrics.stream().filter(m -> m.equals(metric)).findFirst().orElse(null);
    }

    /**
     * Iterate through ALL the metrics defined in the ENUM Metrics, lookup all the values
     * from the Model database registry and put in an array of the Metric object.
     * <p>
     * Running toString on the returned MetricsCollector will output a Prometheus compatible list
     * of metrics
     *
     * @return a MetricsCollector object.
     */
    public MetricsService collect() {
        this.metrics.clear();
        Metrics.stream().forEach(this::add);
        return this;
    }

    /**
     * Clear the collect list of metrics from list.
     */
    public void clear() {
        this.metrics.clear();
    }

    /**
     * Return the list of metrics each on a newline.
     * Example:
     * metric_name[label_key="label_name"] metric_value\n
     *
     * @return The list of metric and values
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        this.metrics.forEach(m -> sb.append(m.toString()).append("\n"));
        return sb.toString();
    }
}
