package com.jvj28.homeworks.metrics.handler;

import com.jvj28.homeworks.metrics.Metric;
import com.jvj28.homeworks.model.Model;

import java.util.Set;

import static com.jvj28.homeworks.metrics.Metrics.processor_date;

public class SystemDateHandler implements MetricHandler {
    @Override
    public Set<Metric> getMetricValues(Model model) {
        return Set.of(new Metric(processor_date, Long.toString(model.getProcessorDate().getTime())));
    }
}
