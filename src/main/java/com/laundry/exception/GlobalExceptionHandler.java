package com.laundry.exception;

import com.laundry.util.Format;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private ResponseEntity<ErrorResponse> buildErrorResponse(
            HttpStatus status,
            String message,
            List<ErrorDetail> errors,
            String traceId) {

        ErrorResponse errorResponse = new ErrorResponse(
                Format.format(Instant.now()),
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
        log.error("DataIntegrityViolationException caught. traceId={}, message={}", traceId, ex.getMessage(), ex);
        return buildErrorResponse(HttpStatus.BAD_REQUEST, mainMessage, errors, traceId);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex) {
        String traceId = MDC.get("traceId");
        List<ErrorDetail> errors = List.of(
                new ErrorDetail("FILE_SIZE_EXCEEDED", "File exceeds the maximum limit", "file")
        );
        String message = "File size exceeds the maximum limit";
        log.error("MaxUploadSizeExceededException caught. traceId={}, message={}", traceId, ex.getMessage(), ex);
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
        log.error("MethodArgumentTypeMismatchException caught. traceId={}, message={}", traceId, ex.getMessage(), ex);
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
        log.error("MethodArgumentNotValidException caught. traceId={}, message={}", traceId, ex.getMessage(), ex);
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Validation failed", errors, traceId);
    }

    @ExceptionHandler(InvalidEmailException.class)
    public ResponseEntity<ErrorResponse> handleInvalidEmailException(InvalidEmailException ex) {
        String traceId = MDC.get("traceId");
        log.error("InvalidEmailException caught. traceId={}, message={}", traceId, ex.getMessage(), ex);
        return buildErrorResponse(ex.getStatus(), ex.getMessage(), ex.getErrors(), traceId);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundException(NotFoundException ex) {
        String traceId = MDC.get("traceId");
        log.error("NotFoundException caught. traceId={}, message={}", traceId, ex.getMessage(), ex);
        return buildErrorResponse(ex.getStatus(), ex.getMessage(), ex.getErrors(), traceId);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExistsException(UserAlreadyExistsException ex) {
        String traceId = MDC.get("traceId");
        log.error("UserAlreadyExistsException caught. traceId={}, message={}", traceId, ex.getMessage(), ex);
        return buildErrorResponse(ex.getStatus(), ex.getMessage(), ex.getErrors(), traceId);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentialsException(InvalidCredentialsException ex) {
        String traceId = MDC.get("traceId");
        log.error("InvalidCredentialsException caught. traceId={}, message={}", traceId, ex.getMessage(), ex);
        return buildErrorResponse(ex.getStatus(), ex.getMessage(), ex.getErrors(), traceId);
    }

    @ExceptionHandler(InternalServerErrorException.class)
    public ResponseEntity<ErrorResponse> handleInternalServerErrorException(InternalServerErrorException ex) {
        String traceId = MDC.get("traceId");
        log.error("InternalServerErrorException caught. traceId={}, message={}", traceId, ex.getMessage(), ex);
        return buildErrorResponse(ex.getStatus(), ex.getMessage(), ex.getErrors(), traceId);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUsernameNotFoundException(UsernameNotFoundException ex) {
        String traceId = MDC.get("traceId");
        log.error("UsernameNotFoundException caught. traceId={}, message={}", traceId, ex.getMessage(), ex);
        return buildErrorResponse(ex.getStatus(), ex.getMessage(), ex.getErrors(), traceId);
    }

    @ExceptionHandler(DeletionException.class)
    public ResponseEntity<ErrorResponse> handleDeletionException(DeletionException ex) {
        String traceId = MDC.get("traceId");
        log.error("DeletionException caught. traceId={}, message={}", traceId, ex.getMessage(), ex);
        return buildErrorResponse(ex.getStatus(), ex.getMessage(), ex.getErrors(), traceId);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex) {
        String traceId = MDC.get("traceId");
        log.error("AccessDeniedException caught. traceId={}, message={}", traceId, ex.getMessage(), ex);
        List<ErrorDetail> errors = ex.getErrors();
        return buildErrorResponse(HttpStatus.FORBIDDEN, ex.getMessage(), errors, traceId);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(BadCredentialsException ex) {
        String traceId = MDC.get("traceId");
        log.error("BadCredentialsException caught. traceId={}, message={}", traceId, ex.getMessage(), ex);
        List<ErrorDetail> errors = List.of(
                new ErrorDetail("INVALID_CREDENTIALS", ex.getMessage(), "")
        );
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "Invalid credentials", errors, traceId);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequestException(BadRequestException ex) {
        String traceId = MDC.get("traceId");
        log.error("BadRequestException caught. traceId={}, message={}", traceId, ex.getMessage(), ex);
        return buildErrorResponse(ex.getStatus(), ex.getMessage(), ex.getErrors(), traceId);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedException(UnauthorizedException ex) {
        String traceId = MDC.get("traceId");
        log.error("UnauthorizedException caught. traceId={}, message={}", traceId, ex.getMessage(), ex);
        return buildErrorResponse(ex.getStatus(), ex.getMessage(), ex.getErrors(), traceId);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorResponse> handleNoSuchElementException(NoSuchElementException ex) {
        String traceId = MDC.get("traceId");
        log.error("NoSuchElementException caught. traceId={}, message={}", traceId, ex.getMessage(), ex);
        List<ErrorDetail> errors = List.of(new ErrorDetail("NO_SUCH_ELEMENT", ex.getMessage(), ""));
        return buildErrorResponse(HttpStatus.NOT_FOUND, "No such element found", errors, traceId);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        String traceId = MDC.get("traceId");
        log.error("IllegalArgumentException caught. traceId={}, message={}", traceId, ex.getMessage(), ex);
        List<ErrorDetail> errors = List.of(new ErrorDetail("ILLEGAL_ARGUMENT", ex.getMessage(), ""));
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Illegal argument provided", errors, traceId);
    }

    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<ErrorResponse> handleRestClientException(RestClientException ex) {
        String traceId = MDC.get("traceId");
        log.error("RestClientException caught. traceId={}, message={}", traceId, ex.getMessage(), ex);
        List<ErrorDetail> errors = List.of(new ErrorDetail("REST_CLIENT_ERROR", ex.getMessage(), ""));
        return buildErrorResponse(HttpStatus.BAD_GATEWAY, "Error during external service call", errors, traceId);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleOtherExceptions(Exception ex) {
        String traceId = MDC.get("traceId");
        log.error("Unexpected exception caught. traceId={}, message={}", traceId, ex.getMessage(), ex);
        List<ErrorDetail> errors = List.of(new ErrorDetail("UNEXPECTED_ERROR", ex.getMessage(), ""));
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error occurred", errors, traceId);
    }
}
