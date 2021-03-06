package com.jvj28.homeworks.metrics.handler;

import com.jvj28.homeworks.metrics.Metric;
import com.jvj28.homeworks.model.Model;

import java.util.Set;

import static com.jvj28.homeworks.metrics.Metrics.total_watts;

public class TotalWattsHandler implements MetricHandler {

    @Override
    public Set<Metric> getMetricValues(Model model) {
        return Set.of(new Metric(total_watts, model.getCurrentUsage()));
    }
}
