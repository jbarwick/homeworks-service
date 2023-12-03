package com.jvj28.homeworks.auth;

import com.jvj28.homeworks.auth.contract.ApiAuthErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.*;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;

@RestControllerAdvice
public class ApiAuthControllerAdvice extends ResponseEntityExceptionHandler {

    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiAuthErrorResponse handleBadCredentialsException(BadCredentialsException ex, WebRequest request) {
        return new ApiAuthErrorResponse(LocalDateTime.now(), ex.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(DisabledException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiAuthErrorResponse handleDisabledException(DisabledException ex, WebRequest request) {
        return new ApiAuthErrorResponse(LocalDateTime.now(), ex.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(LockedException.class)
    @ResponseStatus(HttpStatus.LOCKED)
    public ApiAuthErrorResponse handleLockedException(LockedException ex, WebRequest request) {
        return new ApiAuthErrorResponse(LocalDateTime.now(), ex.getMessage(), HttpStatus.LOCKED);
    }

    @ExceptionHandler(AccountExpiredException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiAuthErrorResponse handleAccountExpiredException(AccountExpiredException ex, WebRequest request) {
        return new ApiAuthErrorResponse(LocalDateTime.now(), ex.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AuthenticationCredentialsNotFoundException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiAuthErrorResponse handleCredentialsNotFoundException(AuthenticationCredentialsNotFoundException ex, WebRequest request) {
        return new ApiAuthErrorResponse(LocalDateTime.now(), ex.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AuthenticationServiceException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiAuthErrorResponse handleServiceException(AuthenticationServiceException ex, WebRequest request) {
        return new ApiAuthErrorResponse(LocalDateTime.now(), ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(CredentialsExpiredException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiAuthErrorResponse handleCredentialsExpiredException(CredentialsExpiredException ex, WebRequest request) {
        return new ApiAuthErrorResponse(LocalDateTime.now(), ex.getMessage(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(InsufficientAuthenticationException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiAuthErrorResponse handleInsufficientAuthenticationException(InsufficientAuthenticationException ex, WebRequest request) {
        return new ApiAuthErrorResponse(LocalDateTime.now(), ex.getMessage(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(InternalAuthenticationServiceException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiAuthErrorResponse handleInternalAuthenticationServiceException(InternalAuthenticationServiceException ex, WebRequest request) {
        return new ApiAuthErrorResponse(LocalDateTime.now(), ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ProviderNotFoundException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiAuthErrorResponse handleProviderNotFoundException(ProviderNotFoundException ex, WebRequest request) {
        return new ApiAuthErrorResponse(LocalDateTime.now(), ex.getMessage(), HttpStatus.UNAUTHORIZED);
    }

}
