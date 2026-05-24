package com.dbp.democarpultec.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ValidationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(EntityNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("message", exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException exception) {
        return ResponseEntity.badRequest().body(mapFieldErrors(exception.getBindingResult()));
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<Map<String, String>> handleBindException(BindException exception) {
        return ResponseEntity.badRequest().body(mapFieldErrors(exception.getBindingResult()));
    }

    @ExceptionHandler(InsufficientSeatsException.class)
    public ResponseEntity<Map<String, String>> handleInsufficientSeats(InsufficientSeatsException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", exception.getMessage()));
    }

    @ExceptionHandler(ReviewValidationException.class)
    public ResponseEntity<Map<String, String>> handleReviewValidation(ReviewValidationException exception) {
        return ResponseEntity.badRequest().body(Map.of("message", exception.getMessage()));
    }

    @ExceptionHandler(ReviewForbiddenException.class)
    public ResponseEntity<Map<String, String>> handleReviewForbidden(ReviewForbiddenException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", exception.getMessage()));
    }

    @ExceptionHandler(ReviewConflictException.class)
    public ResponseEntity<Map<String, String>> handleReviewConflict(ReviewConflictException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", exception.getMessage()));
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(ValidationException exception) {
        return ResponseEntity.badRequest().body(Map.of("message", exception.getMessage()));
    }

    @ExceptionHandler(ImageStorageException.class)
    public ResponseEntity<Map<String, String>> handleImageStorage(ImageStorageException exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", exception.getMessage()));
    }

    @ExceptionHandler({InvalidRideStateException.class, InvalidPickupStateException.class})
    public ResponseEntity<Map<String, String>> handleInvalidState(RuntimeException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", exception.getMessage()));
    }

    @ExceptionHandler(UserVerificationRequiredException.class)
    public ResponseEntity<Map<String, String>> handleVerificationRequired(UserVerificationRequiredException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", exception.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleAccessDenied(AccessDeniedException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", exception.getMessage()));
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<Map<String, String>> handleOptimisticLock(OptimisticLockingFailureException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("message", "Ride capacity changed by another request, please retry"));
    }

    private Map<String, String> mapFieldErrors(BindingResult bindingResult) {
        Map<String, String> errors = new HashMap<>();
        bindingResult.getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        return errors;
    }
}
