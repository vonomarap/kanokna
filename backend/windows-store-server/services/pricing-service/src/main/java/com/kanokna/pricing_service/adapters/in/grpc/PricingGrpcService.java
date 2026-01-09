package com.kanokna.pricing_service.adapters.in.grpc;

import com.kanokna.pricing.v1.CalculateQuoteRequest;
import com.kanokna.pricing.v1.CalculateQuoteResponse;
import com.kanokna.pricing.v1.PricingServiceGrpc;
import com.kanokna.pricing.v1.ValidatePromoCodeRequest;
import com.kanokna.pricing.v1.ValidatePromoCodeResponse;
import com.kanokna.pricing_service.application.dto.CalculateQuoteCommand;
import com.kanokna.pricing_service.application.dto.PromoCodeValidationResponse;
import com.kanokna.pricing_service.application.dto.QuoteResponse;
import com.kanokna.pricing_service.application.dto.ValidatePromoCodeCommand;
import com.kanokna.pricing_service.application.port.in.CalculateQuoteUseCase;
import com.kanokna.pricing_service.application.port.in.ValidatePromoCodeUseCase;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

/**
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
