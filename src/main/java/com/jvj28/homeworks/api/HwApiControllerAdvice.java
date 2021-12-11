package com.jvj28.homeworks.api;

import com.jvj28.homeworks.api.contract.HwApiErrorResponse;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class HwApiControllerAdvice extends ResponseEntityExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public HwApiErrorResponse handleCityNotFoundException(
            NotFoundException ex, WebRequest request) {
        return new HwApiErrorResponse(LocalDateTime.now(), ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public HwApiErrorResponse handleCityNotFoundException(
            BadRequestException ex, WebRequest request) {
        return new HwApiErrorResponse(LocalDateTime.now(), ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RecordLockException.class)
    @ResponseStatus(HttpStatus.REQUEST_TIMEOUT)
    public HwApiErrorResponse handleCityNotFoundException(
            RecordLockException ex, WebRequest request) {
        return new HwApiErrorResponse(LocalDateTime.now(), ex.getMessage(), HttpStatus.REQUEST_TIMEOUT);
    }

    @ExceptionHandler(RequestTimeoutException.class)
    public HwApiErrorResponse handleCityNotFoundException(
            RequestTimeoutException ex, WebRequest request) {
        return new HwApiErrorResponse(LocalDateTime.now(), ex.getMessage(), HttpStatus.REQUEST_TIMEOUT);
    }

    @ExceptionHandler(GoneException.class)
    public HwApiErrorResponse handleCityNotFoundException(
            GoneException ex, WebRequest request) {
        return new HwApiErrorResponse(LocalDateTime.now(), ex.getMessage(), HttpStatus.GONE);
    }

    // I think this will cover ALL ServiceAdvice classes.  I don't think you can override anywhere else
    @Override
    protected @NonNull
    ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, @NonNull HttpHeaders headers,
            @NonNull HttpStatus status, @NonNull WebRequest request) {

        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage).collect(Collectors.toList());

        return new ResponseEntity<>(
                new HwApiErrorResponse(LocalDateTime.now(), String.join("\n", errors), status),
                HttpStatus.BAD_REQUEST);
    }
}
