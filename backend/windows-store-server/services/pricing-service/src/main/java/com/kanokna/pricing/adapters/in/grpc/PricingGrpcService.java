package com.kanokna.pricing.adapters.in.grpc;

import org.springframework.grpc.server.service.GrpcService;

import com.kanokna.pricing.application.dto.CalculateQuoteCommand;
import com.kanokna.pricing.application.dto.PromoCodeValidationResponse;
import com.kanokna.pricing.application.dto.QuoteResponse;
import com.kanokna.pricing.application.dto.ValidatePromoCodeCommand;
import com.kanokna.pricing.application.port.in.CalculateQuoteUseCase;
import com.kanokna.pricing.application.port.in.ValidatePromoCodeUseCase;
import com.kanokna.pricing.v1.CalculateQuoteRequest;
import com.kanokna.pricing.v1.CalculateQuoteResponse;
import com.kanokna.pricing.v1.PricingServiceGrpc;
import com.kanokna.pricing.v1.ValidatePromoCodeRequest;
import com.kanokna.pricing.v1.ValidatePromoCodeResponse;

import io.grpc.stub.StreamObserver;

/**
 * MODULE_CONTRACT id="MC-pricing-grpc-adapter" LAYER="adapters.in.grpc"
 * INTENT="gRPC adapter for pricing quote calculation and promo code validation"
 * LINKS="Technology.xml#TECH-grpc;RequirementsAnalysis.xml#UC-PRICING-QUOTE"
 *
 * gRPC service for pricing operations.
 */
@GrpcService
public class PricingGrpcService extends PricingServiceGrpc.PricingServiceImplBase {

    private final CalculateQuoteUseCase calculateQuoteUseCase;
    private final ValidatePromoCodeUseCase validatePromoCodeUseCase;
    private final PricingGrpcMapper mapper;

    public PricingGrpcService(CalculateQuoteUseCase calculateQuoteUseCase,
            ValidatePromoCodeUseCase validatePromoCodeUseCase,
            PricingGrpcMapper mapper) {
        this.calculateQuoteUseCase = calculateQuoteUseCase;
        this.validatePromoCodeUseCase = validatePromoCodeUseCase;
        this.mapper = mapper;
    }

    @Override
    public void calculateQuote(CalculateQuoteRequest request,
            StreamObserver<CalculateQuoteResponse> responseObserver) {
        CalculateQuoteCommand command = mapper.toCommand(request);
        QuoteResponse response = calculateQuoteUseCase.calculateQuote(command);
        responseObserver.onNext(mapper.toResponse(response));
        responseObserver.onCompleted();
    }

    @Override
    public void validatePromoCode(ValidatePromoCodeRequest request,
            StreamObserver<ValidatePromoCodeResponse> responseObserver) {
        ValidatePromoCodeCommand command = mapper.toCommand(request);
        PromoCodeValidationResponse response = validatePromoCodeUseCase.validatePromoCode(command);
        responseObserver.onNext(mapper.toResponse(response));
        responseObserver.onCompleted();
    }
}
