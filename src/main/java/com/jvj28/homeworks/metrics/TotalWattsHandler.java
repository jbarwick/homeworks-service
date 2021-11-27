package com.jvj28.homeworks.metrics;

import com.jvj28.homeworks.data.Model;

import java.util.Set;

public class TotalWattsHandler implements MetricHandler {

    @Override
    public Set<Metric> getMetricValues(Model model) {
        return Set.of(new Metric(Metrics.total_watts, model.getCurrentUsage()));
    }
}
