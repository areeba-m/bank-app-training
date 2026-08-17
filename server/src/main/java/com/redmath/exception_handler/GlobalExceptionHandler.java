package com.redmath.exception_handler;

import com.redmath.authentication.exception.*;
import com.redmath.exception_handler.dto.ErrorResponse;
import com.redmath.exception_handler.exception.ResourceNotFoundException;
import com.redmath.transactions.exception.InsufficientBalanceException;
import com.redmath.transfer.exception.SelfTransferException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({ResourceNotFoundException.class, UsernameNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleResourceNotFound(RuntimeException ex, HttpServletRequest request) {
        ErrorResponse error = generateError(ex.getMessage(), HttpStatus.NOT_FOUND, request);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleEmailAlreadyExists(EmailAlreadyExistsException ex,
                                                                  HttpServletRequest request) {
        ErrorResponse error = generateError(ex.getMessage(), HttpStatus.CONFLICT, request);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler({InvalidRefreshTokenException.class,
            SelfTransferException.class,
            MissingRequestHeaderException.class,
            InvalidPasswordException.class})
    public ResponseEntity<ErrorResponse> handleBadRequests(Exception ex, HttpServletRequest request) {
        log.warn("Bad request at {}: {}", request.getRequestURI(), ex.getMessage());
        ErrorResponse error = generateError(ex.getMessage(), HttpStatus.BAD_REQUEST, request);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(BadCredentialsException ex,
                                                                  HttpServletRequest request) {
        log.warn("Authentication failed for path: {}. Reason: {}", request.getRequestURI(), ex.getMessage());
        ErrorResponse error = generateError("Invalid email or password", HttpStatus.UNAUTHORIZED, request);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientBalance(InsufficientBalanceException ex,
                                                                   HttpServletRequest request) {
        log.error("Transaction rejected: {} at path: {}", ex.getMessage(), request.getRequestURI());
        ErrorResponse error = generateError(ex.getMessage(), HttpStatus.FORBIDDEN, request);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex,
                                                                   HttpServletRequest request) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining(", "));

        ErrorResponse error = generateError(message, HttpStatus.BAD_REQUEST, request);
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(EmailSendException.class)
    public ResponseEntity<ErrorResponse> handleEmailSendException(EmailSendException ex, HttpServletRequest request) {
        log.error("Unable to send email {} {}", request.getMethod(), request.getRequestURI(), ex);
        ErrorResponse error = generateError(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, request);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(OtpCooldownException.class)
    public ResponseEntity<ErrorResponse> handleOtpCooldownException(OtpCooldownException ex,
                                                                  HttpServletRequest request) {
        ErrorResponse error = generateError(ex.getMessage(), HttpStatus.TOO_MANY_REQUESTS, request);
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(error);
    }

    @ExceptionHandler(InvalidTokenScopeException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTokenScopeException(InvalidTokenScopeException ex,
                                                                  HttpServletRequest request) {
        ErrorResponse error = generateError(ex.getMessage(), HttpStatus.UNAUTHORIZED, request);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error processing request {} {}", request.getMethod(), request.getRequestURI(), ex);
        ErrorResponse error =
                generateError("An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR, request);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    private ErrorResponse generateError(String message, HttpStatus status, HttpServletRequest request){
        return new ErrorResponse(
                Instant.now(),
                message,
                status.value(),
                request.getRequestURI()
        );
    }
}
