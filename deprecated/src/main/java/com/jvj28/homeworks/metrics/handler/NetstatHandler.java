package com.jvj28.homeworks.metrics.handler;

import com.jvj28.homeworks.metrics.Metric;
import com.jvj28.homeworks.model.Model;
import com.jvj28.homeworks.model.data.NetstatData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * Maps the CIRCUIT in Homeworks server to a Set of Metric objects.
 * Uses the model to retrieve the circuits.  You will need to make sure
 * you have enabled "listening" to the processor so circuits are updated as they change.
 */
public class NetstatHandler implements MetricHandler {

    private final Logger log = LoggerFactory.getLogger(NetstatHandler.class);

    @Override
    public Set<Metric> getMetricValues(Model model) {
        log.debug("Reading Netstat Metrics");
        NetstatData netstat = model.get(NetstatData.class);
        log.debug("Returning 4 metrics for Netstat: {}", netstat);
        return Set.of(
                new Metric("receive_error", netstat == null ? 0 : netstat.getErrorRx()),
                new Metric("receive_success", netstat == null ? 0 : netstat.getSuccessfulRx()),
                new Metric("transmit_error", netstat == null ? 0 : netstat.getErrorTx()),
                new Metric("transmit_success", netstat == null ? 0 : netstat.getSuccessfulTx()));
    }
}
