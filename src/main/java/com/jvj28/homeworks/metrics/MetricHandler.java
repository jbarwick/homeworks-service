package com.jvj28.homeworks.metrics;

import com.jvj28.homeworks.model.Model;

import java.util.Set;

public interface MetricHandler {

    Set<Metric> getMetricValues(Model model);

}
