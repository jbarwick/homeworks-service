package com.jvj28.homeworks.metrics.handler;

import com.jvj28.homeworks.metrics.Metric;
import com.jvj28.homeworks.model.Model;

import java.util.Collection;

public interface MetricHandler {

    Collection<Metric> getMetricValues(Model model);

}
