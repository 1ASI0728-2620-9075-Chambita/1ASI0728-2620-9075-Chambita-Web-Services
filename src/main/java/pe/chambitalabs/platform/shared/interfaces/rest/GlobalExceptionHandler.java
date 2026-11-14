package pe.chambitalabs.platform.shared.interfaces.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import pe.chambitalabs.platform.shared.domain.exceptions.BadRequestException;
import pe.chambitalabs.platform.shared.domain.exceptions.BusinessRuleException;
import pe.chambitalabs.platform.shared.domain.exceptions.ConflictException;
import pe.chambitalabs.platform.shared.domain.exceptions.DomainException;
import pe.chambitalabs.platform.shared.domain.exceptions.ForbiddenException;
import pe.chambitalabs.platform.shared.domain.exceptions.ResourceNotFoundException;
import pe.chambitalabs.platform.shared.domain.exceptions.UnauthorizedException;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Turns exceptions into RFC 9457 problem details with messages in en_US or es_419 (TS15).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final MessageSource messageSource;

    public GlobalExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    ResponseEntity<ProblemDetail> handleNotFound(ResourceNotFoundException exception, Locale locale) {
        return problem(HttpStatus.NOT_FOUND, exception, locale);
    }

    @ExceptionHandler(ConflictException.class)
    ResponseEntity<ProblemDetail> handleConflict(ConflictException exception, Locale locale) {
        return problem(HttpStatus.CONFLICT, exception, locale);
    }

    @ExceptionHandler(ForbiddenException.class)
    ResponseEntity<ProblemDetail> handleForbidden(ForbiddenException exception, Locale locale) {
        return problem(HttpStatus.FORBIDDEN, exception, locale);
    }

    @ExceptionHandler(UnauthorizedException.class)
    ResponseEntity<ProblemDetail> handleUnauthorized(UnauthorizedException exception, Locale locale) {
        return problem(HttpStatus.UNAUTHORIZED, exception, locale);
    }

    @ExceptionHandler(BadRequestException.class)
    ResponseEntity<ProblemDetail> handleDomainBadRequest(BadRequestException exception, Locale locale) {
        return problem(HttpStatus.BAD_REQUEST, exception, locale);
    }

    @ExceptionHandler(BusinessRuleException.class)
    ResponseEntity<ProblemDetail> handleBusinessRule(BusinessRuleException exception, Locale locale) {
        return problem(HttpStatus.UNPROCESSABLE_ENTITY, exception, locale);
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    ResponseEntity<ProblemDetail> handleConcurrentUpdate(Locale locale) {
        var detail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, message("error.concurrent-update", locale));
        return ResponseEntity.status(HttpStatus.CONFLICT).body(detail);
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ProblemDetail> handleAccessDenied(Locale locale) {
        var detail = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, message("error.forbidden", locale));
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(detail);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ProblemDetail> handleValidation(MethodArgumentNotValidException exception, Locale locale) {
        Map<String, String> fields = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error ->
                fields.putIfAbsent(error.getField(), messageSource.getMessage(error, locale)));
        var detail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, message("error.validation", locale));
        detail.setProperty("fields", fields);
        return ResponseEntity.badRequest().body(detail);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            IllegalArgumentException.class})
    ResponseEntity<ProblemDetail> handleBadRequest(Exception exception, Locale locale) {
        LOGGER.debug("Bad request: {}", exception.getMessage());
        var detail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, message("error.bad-request", locale));
        return ResponseEntity.badRequest().body(detail);
    }

    private ResponseEntity<ProblemDetail> problem(HttpStatus status, DomainException exception, Locale locale) {
        var text = messageSource.getMessage(exception.getMessageKey(), exception.getArguments(),
                exception.getMessageKey(), locale);
        var detail = ProblemDetail.forStatusAndDetail(status, text);
        detail.setProperty("code", exception.getMessageKey());
        return ResponseEntity.status(status).body(detail);
    }

    private String message(String key, Locale locale) {
        return messageSource.getMessage(key, null, key, locale);
    }
}
