package com.redmath.exception_handler;

import com.redmath.authentication.exception.EmailAlreadyExistsException;
import com.redmath.authentication.exception.InvalidRefreshTokenException;
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
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({ResourceNotFoundException.class, UsernameNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleResourceNotFound(RuntimeException ex,
                                                                HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse(
                Instant.now(),
                ex.getMessage(),
                HttpStatus.NOT_FOUND.value(),
                request.getRequestURI());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleEmailAlreadyExists(EmailAlreadyExistsException ex,
                                                                  HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse(
                Instant.now(),
                ex.getMessage(),
                HttpStatus.CONFLICT.value(),
                request.getRequestURI());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler({InvalidRefreshTokenException.class, SelfTransferException.class})
    public ResponseEntity<ErrorResponse> handleBadRequests(RuntimeException ex, HttpServletRequest request) {
        log.warn("Bad request at {}: {}", request.getRequestURI(), ex.getMessage());
        ErrorResponse error = new ErrorResponse(
                Instant.now(),
                ex.getMessage(),
                HttpStatus.BAD_REQUEST.value(),
                request.getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(BadCredentialsException ex, HttpServletRequest request) {
        log.warn("Authentication failed for path: {}. Reason: {}", request.getRequestURI(), ex.getMessage());
        ErrorResponse error = new ErrorResponse(
                Instant.now(),
                "Invalid email or password",
                HttpStatus.UNAUTHORIZED.value(),
                request.getRequestURI());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientBalance(InsufficientBalanceException ex, HttpServletRequest request) {
        log.error("Transaction rejected: {} at path: {}", ex.getMessage(), request.getRequestURI());
        ErrorResponse error = new ErrorResponse(
                Instant.now(),
                ex.getMessage(),
                HttpStatus.FORBIDDEN.value(),
                request.getRequestURI());

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

        ErrorResponse error = new ErrorResponse(
                Instant.now(),
                message,
                HttpStatus.BAD_REQUEST.value(),
                request.getRequestURI()
        );
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error processing request {} {}",
                request.getMethod(),
                request.getRequestURI(),
                ex
        );

        ErrorResponse error = new ErrorResponse(
                Instant.now(),
                "An unexpected error occurred",
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
