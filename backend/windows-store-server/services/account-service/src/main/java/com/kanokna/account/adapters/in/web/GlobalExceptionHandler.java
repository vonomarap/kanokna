package com.kanokna.account.adapters.in.web;

import com.kanokna.shared.core.DomainException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global exception handler mapping domain errors to HTTP responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DomainException.class)
    public ProblemDetail handleDomainException(DomainException ex) {
        HttpStatus status = mapStatus(ex.getCode());
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(status, ex.getMessage());
        detail.setTitle(ex.getCode());
        return detail;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        detail.setTitle("Bad Request");
        return detail;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Account service error");
        detail.setTitle("Internal Error");
        return detail;
    }

    private HttpStatus mapStatus(String code) {
        return switch (code) {
            case "ERR-ACCT-UNAUTHORIZED" -> HttpStatus.FORBIDDEN;
            case "ERR-ACCT-PROFILE-NOT-FOUND",
                 "ERR-ACCT-ADDRESS-NOT-FOUND" -> HttpStatus.NOT_FOUND;
            case "ERR-ACCT-ADDRESS-DUPLICATE" -> HttpStatus.CONFLICT;
            case "ERR-ACCT-CONCURRENT-MODIFICATION" -> HttpStatus.CONFLICT;
            case "ERR-ACCT-INVALID-PHONE",
                 "ERR-ACCT-INVALID-LANGUAGE",
                 "ERR-ACCT-INVALID-CURRENCY",
                 "ERR-ACCT-INVALID-CONFIG-NAME",
                 "ERR-ACCT-INVALID-CONFIG-SNAPSHOT",
                 "ERR-ACCT-INVALID-ADDRESS",
                 "ERR-ACCT-LABEL-TOO-LONG" -> HttpStatus.BAD_REQUEST;
            case "ERR-ACCT-KEYCLOAK-UNAVAILABLE" -> HttpStatus.SERVICE_UNAVAILABLE;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}
