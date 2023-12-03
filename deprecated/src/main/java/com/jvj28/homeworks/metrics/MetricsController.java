package com.jvj28.homeworks.metrics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class MetricsController {

    private final Logger log = LoggerFactory.getLogger(MetricsController.class);

    private final MetricsService service;

    public MetricsController(MetricsService service) {
        this.service = service;
    }

    /**
     * Return metrics that can be consumed by Prometheus
     * @return ResponseEntity with the body a string of a metrics list.
     */
    @ResponseBody
    @GetMapping(value = "/metrics", produces = {MediaType.TEXT_PLAIN_VALUE} )
    public String getMetrics() {
        Thread.currentThread().setName("/metrics");
        log.debug("/metrics: Collecting metrics and sending");
        try {
            return service.collect().toString();
        } catch (Exception e) {
            throw new MetricsException(e.getMessage());
        }
    }
}
