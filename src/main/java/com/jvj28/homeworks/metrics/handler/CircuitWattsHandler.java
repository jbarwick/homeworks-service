package com.jvj28.homeworks.metrics.handler;

import com.jvj28.homeworks.metrics.LabelList;
import com.jvj28.homeworks.metrics.Metric;
import com.jvj28.homeworks.model.Model;
import com.jvj28.homeworks.model.db.entity.CircuitEntity;

import java.util.Set;

import static com.jvj28.homeworks.metrics.Metrics.circuit_watts;

/**
 * Maps the CIRCUIT in Homeworks server to a Set of Metric objects.
 * Uses the model to retrieve the circuits.  You will need to make sure
 * you have enabled "listening" to the processor so circuits are updated as they change.
 */
public class CircuitWattsHandler implements MetricHandler {

    @Override
    public Set<Metric> getMetricValues(Model model) {

        Set<Metric> metricSet = new java.util.HashSet<>();

        // The Circuit level handler gets the level for ALL circuits and creates
        // the Metric with label.  This will ONLY retrieve from the model. So,
        // update of the model must be done by external service (such as processor listening)
        model.getCircuits().forEach(c -> metricSet.add(
                new Metric(circuit_watts, getAttributes(c), (int) (c.getLevel() * c.getWatts() / 100.0))));
        return metricSet;
    }

    private LabelList getAttributes(CircuitEntity circuit) {
        LabelList map = new LabelList();
        map.put("name", circuit.getName());
        map.put("address", circuit.getAddress());
        map.put("watts", Integer.toString(circuit.getWatts()));
        return map;
    }
}
