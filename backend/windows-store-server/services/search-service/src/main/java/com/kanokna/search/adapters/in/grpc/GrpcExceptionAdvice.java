package com.kanokna.search.adapters.in.grpc;

import org.springframework.core.annotation.Order;
import org.springframework.grpc.server.GlobalServerInterceptor;
import org.springframework.stereotype.Component;

import com.kanokna.shared.core.DomainException;

import io.grpc.ForwardingServerCallListener;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;

/**
 * gRPC exception interceptor for search service.
 * Converts domain and application exceptions to appropriate gRPC Status codes.
 * 
 * Spring gRPC uses ServerInterceptor for exception handling instead of @GrpcAdvice.
 */
@Component
@GlobalServerInterceptor
@Order(1)
public class GrpcExceptionAdvice implements ServerInterceptor {

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {
        
        ServerCall.Listener<ReqT> listener = next.startCall(call, headers);
        
        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<>(listener) {
            @Override
            public void onHalfClose() {
                try {
                    super.onHalfClose();
                } catch (DomainException ex) {
                    Status status = mapDomainException(ex);
                    call.close(status, new Metadata());
                } catch (IllegalArgumentException ex) {
                    call.close(Status.INVALID_ARGUMENT.withDescription(ex.getMessage()), new Metadata());
                } catch (Exception ex) {
                    call.close(Status.INTERNAL.withDescription("Search service error").withCause(ex), new Metadata());
                }
            }
        };
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

