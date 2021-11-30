package com.jvj28.homeworks.metrics;

import com.jvj28.homeworks.data.Model;
import com.jvj28.homeworks.data.db.entity.CircuitEntity;

import java.util.Set;

import static com.jvj28.homeworks.metrics.Metrics.circuit_level;

/**
 * Maps the CIRCUIT in Homeworks server to a Set of Metric objects.
 * Uses the model to retrieve the circuits.  You will need to make sure
 * you have enabled "listening" to the processor so circuits are updated as they change.
 */
public class CircuitLevelHandler implements MetricHandler {

    @Override
    public Set<Metric> getMetricValues(Model model) {

        Set<Metric> metric_set = new java.util.HashSet<>();

        // The Circuit level handler gets the level for ALL circuits and creates
        // the Metric with label.  This will ONLY retrieve from the model. So,
        // update of the model must be done by external service (such as processor listening)
        model.getCircuits().forEach(c -> metric_set.add(
                new Metric(circuit_level, getAttributes(c), c.getLevel())));
        return metric_set;
    }

    private LabelList getAttributes(CircuitEntity circuit) {
        LabelList map = new LabelList();
        map.put("name", circuit.getName());
        map.put("address", circuit.getAddress());
        map.put("watts", Integer.toString(circuit.getWatts()));
        return map;
    }
}
