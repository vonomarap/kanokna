package com.kanokna.pricing.adapters.in.grpc;

import org.springframework.core.annotation.Order;
import org.springframework.grpc.server.GlobalServerInterceptor;
import org.springframework.stereotype.Component;

import com.kanokna.pricing.domain.exception.InvalidPromoCodeException;
import com.kanokna.pricing.domain.exception.PriceBookNotFoundException;
import com.kanokna.pricing.domain.exception.TaxRuleNotFoundException;

import io.grpc.ForwardingServerCallListener;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;

/**
 * gRPC exception interceptor for pricing service.
 * Converts domain exceptions to appropriate gRPC Status codes.
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
                } catch (PriceBookNotFoundException ex) {
                    call.close(Status.NOT_FOUND.withDescription(ex.getMessage()), new Metadata());
                } catch (InvalidPromoCodeException ex) {
                    call.close(Status.INVALID_ARGUMENT.withDescription(ex.getMessage()), new Metadata());
                } catch (TaxRuleNotFoundException ex) {
                    call.close(Status.NOT_FOUND.withDescription(ex.getMessage()), new Metadata());
                } catch (Exception ex) {
                    call.close(Status.INTERNAL.withDescription("Pricing service error").withCause(ex), new Metadata());
                }
            }
        };
    }
}

