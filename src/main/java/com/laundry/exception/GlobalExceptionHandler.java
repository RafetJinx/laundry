package com.laundry.exception;

import com.laundry.util.FormatInstant;
import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import jakarta.servlet.http.HttpServletRequest;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private ResponseEntity<ErrorResponse> buildErrorResponse(
            HttpStatus status,
            String message,
            List<ErrorDetail> errors,
            String traceId) {

        ErrorResponse errorResponse = new ErrorResponse(
                FormatInstant.format(Instant.now()),
                status.value(),
                status.getReasonPhrase(),
                message,
                errors,
                traceId
        );
        return new ResponseEntity<>(errorResponse, status);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        String traceId = MDC.get("traceId");
        List<ErrorDetail> errors = new ArrayList<>();
        String errorMessage = ex.getMessage();
        String mainMessage = "Data integrity violation occurred";

        if (errorMessage != null) {
            if (errorMessage.contains("Data truncation")) {
                errors.add(new ErrorDetail("DATA_TOO_LONG", "Data too long for column", ""));
            }
            if (errorMessage.contains("cannot be null")) {
                errors.add(new ErrorDetail("NULL_VALUE", "A required field is missing", ""));
            }
            if (errorMessage.contains("Duplicate entry")) {
                errors.add(new ErrorDetail("DUPLICATE_ENTRY", "Duplicate entry for a unique field", ""));
            }
        }

        return buildErrorResponse(HttpStatus.BAD_REQUEST, mainMessage, errors, traceId);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex) {
        String traceId = MDC.get("traceId");
        List<ErrorDetail> errors = List.of(
                new ErrorDetail("FILE_SIZE_EXCEEDED", "File exceeds the maximum limit", "file")
        );
        String message = "File size exceeds the maximum limit";
        return buildErrorResponse(HttpStatus.PAYLOAD_TOO_LARGE, message, errors, traceId);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {

        String traceId = MDC.get("traceId");
        List<ErrorDetail> errors = List.of(
                new ErrorDetail("TYPE_MISMATCH", ex.getMessage(), ex.getName())
        );
        return buildErrorResponse(HttpStatus.BAD_REQUEST,
                "Validation failed due to type mismatch", errors, traceId);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        String traceId = MDC.get("traceId");
        List<ErrorDetail> errors = new ArrayList<>();

        ex.getBindingResult().getFieldErrors().forEach(fieldError ->
                errors.add(new ErrorDetail("VALIDATION_ERROR", fieldError.getDefaultMessage(), fieldError.getField()))
        );

        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Validation failed", errors, traceId);
    }

    @ExceptionHandler(InvalidEmailException.class)
    public ResponseEntity<ErrorResponse> handleInvalidEmailException(InvalidEmailException ex) {
        String traceId = MDC.get("traceId");
        return buildErrorResponse(ex.getStatus(), ex.getMessage(), ex.getErrors(), traceId);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundException(NotFoundException ex) {
        String traceId = MDC.get("traceId");
        return buildErrorResponse(ex.getStatus(), ex.getMessage(), ex.getErrors(), traceId);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExistsException(UserAlreadyExistsException ex) {
        String traceId = MDC.get("traceId");
        return buildErrorResponse(ex.getStatus(), ex.getMessage(), ex.getErrors(), traceId);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentialsException(InvalidCredentialsException ex) {
        String traceId = MDC.get("traceId");
        return buildErrorResponse(ex.getStatus(), ex.getMessage(), ex.getErrors(), traceId);
    }

    @ExceptionHandler(InternalServerErrorException.class)
    public ResponseEntity<ErrorResponse> handleInternalServerErrorException(InternalServerErrorException ex) {
        String traceId = MDC.get("traceId");
        return buildErrorResponse(ex.getStatus(), ex.getMessage(), ex.getErrors(), traceId);
    }

    /**
     * CHANGED: Instead of ResponseStatusException logic,
     * now handle the new UsernameNotFoundException extends ApiBaseException
     */
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUsernameNotFoundException(UsernameNotFoundException ex) {
        String traceId = MDC.get("traceId");
        return buildErrorResponse(ex.getStatus(), ex.getMessage(), ex.getErrors(), traceId);
    }

    /**
     * CHANGED: Now that DeletionException extends ApiBaseException,
     * we treat it similarly (HTTP 500).
     */
    @ExceptionHandler(DeletionException.class)
    public ResponseEntity<ErrorResponse> handleDeletionException(DeletionException ex) {
        String traceId = MDC.get("traceId");
        return buildErrorResponse(ex.getStatus(), ex.getMessage(), ex.getErrors(), traceId);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex) {
        String traceId = MDC.get("traceId");
        List<ErrorDetail> errors = ex.getErrors();
        return buildErrorResponse(HttpStatus.FORBIDDEN, ex.getMessage(), errors, traceId);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleOtherExceptions(Exception ex) {
        // Fallback for unhandled exceptions
        String traceId = MDC.get("traceId");
        List<ErrorDetail> errors = List.of(new ErrorDetail("UNEXPECTED_ERROR", ex.getMessage(), ""));
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error occurred", errors, traceId);
    }
}
