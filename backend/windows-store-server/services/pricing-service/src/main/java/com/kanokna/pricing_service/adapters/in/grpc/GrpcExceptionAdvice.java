package com.kanokna.pricing_service.adapters.in.grpc;

import com.kanokna.pricing_service.domain.exception.InvalidPromoCodeException;
import com.kanokna.pricing_service.domain.exception.PriceBookNotFoundException;
import com.kanokna.pricing_service.domain.exception.TaxRuleNotFoundException;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import net.devh.boot.grpc.server.advice.GrpcAdvice;
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler;

/**
 * gRPC exception handler for pricing service.
 */
@GrpcAdvice
public class GrpcExceptionAdvice {

    @GrpcExceptionHandler(PriceBookNotFoundException.class)
    public StatusRuntimeException handlePriceBookNotFound(PriceBookNotFoundException ex) {
        return Status.NOT_FOUND.withDescription(ex.getMessage()).asRuntimeException();
    }

    @GrpcExceptionHandler(InvalidPromoCodeException.class)
    public StatusRuntimeException handleInvalidPromo(InvalidPromoCodeException ex) {
        return Status.INVALID_ARGUMENT.withDescription(ex.getMessage()).asRuntimeException();
    }

    @GrpcExceptionHandler(TaxRuleNotFoundException.class)
    public StatusRuntimeException handleTaxRuleNotFound(TaxRuleNotFoundException ex) {
        return Status.NOT_FOUND.withDescription(ex.getMessage()).asRuntimeException();
    }

    @GrpcExceptionHandler(Exception.class)
    public StatusRuntimeException handleUnexpected(Exception ex) {
        return Status.INTERNAL.withDescription("Pricing service error").withCause(ex).asRuntimeException();
    }
}
