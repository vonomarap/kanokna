package com.kanokna.account.adapters.in.grpc;

import com.kanokna.shared.core.DomainException;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import net.devh.boot.grpc.server.advice.GrpcAdvice;
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler;

/**
 * gRPC exception handler for account service.
 */
@GrpcAdvice
public class GrpcExceptionAdvice {

    @GrpcExceptionHandler(DomainException.class)
    public StatusRuntimeException handleDomainException(DomainException ex) {
        return mapDomainException(ex).asRuntimeException();
    }

    @GrpcExceptionHandler(IllegalArgumentException.class)
    public StatusRuntimeException handleIllegalArgument(IllegalArgumentException ex) {
        return Status.INVALID_ARGUMENT.withDescription(ex.getMessage()).asRuntimeException();
    }

    @GrpcExceptionHandler(Exception.class)
    public StatusRuntimeException handleUnexpected(Exception ex) {
        return Status.INTERNAL.withDescription("Account service error").withCause(ex).asRuntimeException();
    }

    private Status mapDomainException(DomainException ex) {
        return switch (ex.getCode()) {
            case "ERR-ACCT-UNAUTHORIZED" -> Status.PERMISSION_DENIED.withDescription(ex.getMessage());
            case "ERR-ACCT-PROFILE-NOT-FOUND",
                 "ERR-ACCT-ADDRESS-NOT-FOUND" -> Status.NOT_FOUND.withDescription(ex.getMessage());
            case "ERR-ACCT-ADDRESS-DUPLICATE" -> Status.ALREADY_EXISTS.withDescription(ex.getMessage());
            case "ERR-ACCT-INVALID-PHONE",
                 "ERR-ACCT-INVALID-LANGUAGE",
                 "ERR-ACCT-INVALID-CURRENCY",
                 "ERR-ACCT-INVALID-CONFIG-NAME",
                 "ERR-ACCT-INVALID-CONFIG-SNAPSHOT",
                 "ERR-ACCT-INVALID-ADDRESS",
                 "ERR-ACCT-LABEL-TOO-LONG" -> Status.INVALID_ARGUMENT.withDescription(ex.getMessage());
            case "ERR-ACCT-CONCURRENT-MODIFICATION" -> Status.ABORTED.withDescription(ex.getMessage());
            case "ERR-ACCT-KEYCLOAK-UNAVAILABLE" -> Status.UNAVAILABLE.withDescription(ex.getMessage());
            default -> Status.INTERNAL.withDescription(ex.getMessage());
        };
    }
}
