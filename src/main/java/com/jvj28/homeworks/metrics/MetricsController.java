package com.jvj28.homeworks.metrics;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class MetricsController {

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
        return service.collect().toString();
    }
}
