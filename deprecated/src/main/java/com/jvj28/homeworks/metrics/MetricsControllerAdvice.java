package com.jvj28.homeworks.metrics;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class MetricsControllerAdvice {

    @ExceptionHandler(MetricsException.class)
    @ResponseStatus(HttpStatus.OK)
    public String metricsException(MetricsException ex) {
        String msg = ex.getMessage();
        return new Metric("error", msg == null ? "no reason given" : msg).toString();
    }
}
