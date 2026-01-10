package com.kanokna.cart.adapters.in.grpc;

import com.kanokna.shared.core.DomainException;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import net.devh.boot.grpc.server.advice.GrpcAdvice;
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler;

/**
 * gRPC exception handler for cart-service.
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
        return Status.INTERNAL.withDescription("Cart service error").withCause(ex).asRuntimeException();
    }

    private Status mapDomainException(DomainException ex) {
        return switch (ex.getCode()) {
            case "ERR-CART-UNAUTHORIZED",
                 "ERR-CART-ANONYMOUS" -> Status.PERMISSION_DENIED.withDescription(ex.getMessage());
            case "ERR-CART-NOT-FOUND",
                 "ERR-CART-ANONYMOUS-NOT-FOUND",
                 "ERR-CART-ITEM-NOT-FOUND" -> Status.NOT_FOUND.withDescription(ex.getMessage());
            case "ERR-CART-INVALID-QUANTITY" -> Status.INVALID_ARGUMENT.withDescription(ex.getMessage());
            case "ERR-CART-INVALID-CONFIG",
                 "ERR-CART-QUOTE-EXPIRED",
                 "ERR-CART-EMPTY",
                 "ERR-CART-PROMO-INVALID",
                 "ERR-CART-PROMO-MIN-NOT-MET",
                 "ERR-CART-NO-PROMO",
                 "ERR-CART-INVALID-ITEMS" -> Status.FAILED_PRECONDITION.withDescription(ex.getMessage());
            case "ERR-CART-CATALOG-UNAVAILABLE",
                 "ERR-CART-PRICING-UNAVAILABLE",
                 "ERR-CART-PRICING-PARTIAL" -> Status.UNAVAILABLE.withDescription(ex.getMessage());
            default -> Status.INTERNAL.withDescription(ex.getMessage());
        };
    }
}
