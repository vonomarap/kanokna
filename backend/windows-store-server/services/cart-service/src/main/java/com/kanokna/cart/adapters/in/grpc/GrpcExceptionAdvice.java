package com.kanokna.cart.adapters.in.grpc;

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
 * gRPC exception interceptor for cart-service.
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
                    call.close(Status.INTERNAL.withDescription("Cart service error").withCause(ex), new Metadata());
                }
            }
        };
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

