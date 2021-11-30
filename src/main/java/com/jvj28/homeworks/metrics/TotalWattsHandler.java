package com.jvj28.homeworks.metrics;

import com.jvj28.homeworks.data.Model;

import java.util.Set;

import static com.jvj28.homeworks.metrics.Metrics.total_watts;

public class TotalWattsHandler implements MetricHandler {

    @Override
    public Set<Metric> getMetricValues(Model model) {
        return Set.of(new Metric(total_watts, model.getCurrentUsage()));
    }
}
