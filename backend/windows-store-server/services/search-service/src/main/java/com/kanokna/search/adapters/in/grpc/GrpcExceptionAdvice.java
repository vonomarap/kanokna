package com.kanokna.search.adapters.in.grpc;

import com.kanokna.shared.core.DomainException;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import net.devh.boot.grpc.server.advice.GrpcAdvice;
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler;

/**
 * gRPC exception handler for search service.
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
        return Status.INTERNAL.withDescription("Search service error").withCause(ex).asRuntimeException();
    }

    private Status mapDomainException(DomainException ex) {
        return switch (ex.getCode()) {
            case "ERR-SEARCH-ES-UNAVAILABLE",
                 "ERR-AUTO-ES-UNAVAILABLE",
                 "ERR-INDEX-ES-UNAVAILABLE",
                 "ERR-DELETE-ES-UNAVAILABLE",
                 "ERR-FACET-ES-UNAVAILABLE",
                 "ERR-GET-ES-UNAVAILABLE",
                 "ERR-REINDEX-ES-UNAVAILABLE",
                 "ERR-REINDEX-CATALOG-UNAVAILABLE",
                 "ERR-REINDEX-LOCK-UNAVAILABLE" -> Status.UNAVAILABLE.withDescription(ex.getMessage());
            case "ERR-SEARCH-INDEX-NOT-FOUND",
                 "ERR-GET-PRODUCT-NOT-FOUND" -> Status.NOT_FOUND.withDescription(ex.getMessage());
            case "ERR-SEARCH-QUERY-TIMEOUT" -> Status.DEADLINE_EXCEEDED.withDescription(ex.getMessage());
            case "ERR-SEARCH-INVALID-FACET",
                 "ERR-AUTO-PREFIX-TOO-SHORT",
                 "ERR-INDEX-INVALID-EVENT",
                 "ERR-FACET-INVALID-FIELD" -> Status.INVALID_ARGUMENT.withDescription(ex.getMessage());
            case "ERR-REINDEX-IN-PROGRESS" -> Status.ABORTED.withDescription(ex.getMessage());
            case "ERR-INDEX-MAPPING-ERROR",
                 "ERR-REINDEX-ALIAS-SWAP-FAILED" -> Status.INTERNAL.withDescription(ex.getMessage());
            default -> Status.INTERNAL.withDescription(ex.getMessage());
        };
    }
}
