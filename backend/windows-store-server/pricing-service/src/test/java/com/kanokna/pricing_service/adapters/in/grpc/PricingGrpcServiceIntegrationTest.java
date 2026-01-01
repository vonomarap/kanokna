package com.kanokna.pricing_service.adapters.in.grpc;

import com.kanokna.catalog.v1.BillOfMaterials;
import com.kanokna.catalog.v1.BomLine;
import com.kanokna.common.v1.Currency;
import com.kanokna.common.v1.Dimensions;
import com.kanokna.pricing.v1.CalculateQuoteRequest;
import com.kanokna.pricing.v1.CalculateQuoteResponse;
import com.kanokna.pricing.v1.ValidatePromoCodeRequest;
import com.kanokna.pricing.v1.ValidatePromoCodeResponse;
import com.kanokna.pricing_service.application.dto.CalculateQuoteCommand;
import com.kanokna.pricing_service.application.dto.PromoCodeValidationResponse;
import com.kanokna.pricing_service.application.dto.QuoteResponse;
import com.kanokna.pricing_service.application.dto.ValidatePromoCodeCommand;
import com.kanokna.pricing_service.application.port.in.CalculateQuoteUseCase;
import com.kanokna.pricing_service.application.port.in.ValidatePromoCodeUseCase;
import com.kanokna.pricing_service.domain.model.Money;
import com.kanokna.pricing_service.domain.model.PremiumLine;
import com.kanokna.pricing_service.domain.model.PricingDecision;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PricingGrpcServiceIntegrationTest {

    @Test
    @DisplayName("CalculateQuote maps request and response")
    void calculateQuoteMapsRequestAndResponse() {
        CalculateQuoteUseCase calculateQuoteUseCase = mock(CalculateQuoteUseCase.class);
        ValidatePromoCodeUseCase validatePromoCodeUseCase = mock(ValidatePromoCodeUseCase.class);
        PricingGrpcMapper mapper = new PricingGrpcMapper();
        PricingGrpcService service = new PricingGrpcService(
            calculateQuoteUseCase,
            validatePromoCodeUseCase,
            mapper
        );

        QuoteResponse quoteResponse = new QuoteResponse();
        quoteResponse.setQuoteId("QUOTE-1");
        quoteResponse.setBasePrice("1000 RUB");
        quoteResponse.setDiscount("100 RUB");
        quoteResponse.setSubtotal("900 RUB");
        quoteResponse.setTax("180 RUB");
        quoteResponse.setTotal("1080 RUB");
        quoteResponse.setCurrency("RUB");
        quoteResponse.setValidUntil(Instant.parse("2026-01-01T10:00:00Z"));
        quoteResponse.setOptionPremiums(List.of(
            QuoteResponse.PremiumLineDto.from(
                PremiumLine.of("OPT-A", "Handle", Money.of(new BigDecimal("50"), "RUB"))
            )
        ));
        quoteResponse.setDecisionTrace(List.of(
            QuoteResponse.PricingDecisionDto.from(
                PricingDecision.of("BA-PRC-CALC-02", "BASE_PRICE", "eventType=PRICING_STEP decision=CALCULATED keyValues=area_m2=1")
            )
        ));
        when(calculateQuoteUseCase.calculateQuote(any())).thenReturn(quoteResponse);

        CalculateQuoteRequest request = CalculateQuoteRequest.newBuilder()
            .setProductTemplateId("WINDOW-STD")
            .setDimensions(Dimensions.newBuilder().setWidthCm(120).setHeightCm(130).build())
            .setResolvedBom(BillOfMaterials.newBuilder()
                .addLines(BomLine.newBuilder().setSku("OPT-A").setDescription("Handle").setQuantity(1).build())
                .build())
            .setCurrency(Currency.CURRENCY_RUB)
            .setPromoCode("PROMO10")
            .setRegion("RU")
            .build();

        TestObserver<CalculateQuoteResponse> observer = new TestObserver<>();
        service.calculateQuote(request, observer);

        assertNull(observer.error);
        assertTrue(observer.completed);
        assertNotNull(observer.value);
        assertEquals("QUOTE-1", observer.value.getQuoteId());
        assertEquals(108000, observer.value.getTotal().getAmountMinor());
        assertEquals(Currency.CURRENCY_RUB, observer.value.getTotal().getCurrency());
        assertEquals(1, observer.value.getOptionPremiumsCount());
        assertEquals(1, observer.value.getDecisionTraceCount());

        ArgumentCaptor<CalculateQuoteCommand> captor = ArgumentCaptor.forClass(CalculateQuoteCommand.class);
        verify(calculateQuoteUseCase).calculateQuote(captor.capture());
        CalculateQuoteCommand command = captor.getValue();
        assertEquals("WINDOW-STD", command.getProductTemplateId());
        assertEquals(0, command.getWidthCm().compareTo(new BigDecimal("120")));
        assertEquals(0, command.getHeightCm().compareTo(new BigDecimal("130")));
        assertEquals(List.of("OPT-A"), command.getResolvedBom());
        assertEquals("RUB", command.getCurrency());
        assertEquals("PROMO10", command.getPromoCode());
        assertEquals("RU", command.getRegion());
    }

    @Test
    @DisplayName("ValidatePromoCode maps request and response")
    void validatePromoCodeMapsRequestAndResponse() {
        CalculateQuoteUseCase calculateQuoteUseCase = mock(CalculateQuoteUseCase.class);
        ValidatePromoCodeUseCase validatePromoCodeUseCase = mock(ValidatePromoCodeUseCase.class);
        PricingGrpcMapper mapper = new PricingGrpcMapper();
        PricingGrpcService service = new PricingGrpcService(
            calculateQuoteUseCase,
            validatePromoCodeUseCase,
            mapper
        );

        when(validatePromoCodeUseCase.validatePromoCode(any()))
            .thenReturn(PromoCodeValidationResponse.valid("50 RUB"));

        ValidatePromoCodeRequest request = ValidatePromoCodeRequest.newBuilder()
            .setPromoCode("PROMO10")
            .setSubtotal(com.kanokna.common.v1.Money.newBuilder()
                .setAmountMinor(10000)
                .setCurrency(Currency.CURRENCY_RUB)
                .build())
            .build();

        TestObserver<ValidatePromoCodeResponse> observer = new TestObserver<>();
        service.validatePromoCode(request, observer);

        assertNull(observer.error);
        assertTrue(observer.completed);
        assertNotNull(observer.value);
        assertTrue(observer.value.getValid());
        assertEquals(5000, observer.value.getDiscountAmount().getAmountMinor());
        assertEquals(Currency.CURRENCY_RUB, observer.value.getDiscountAmount().getCurrency());

        ArgumentCaptor<ValidatePromoCodeCommand> captor = ArgumentCaptor.forClass(ValidatePromoCodeCommand.class);
        verify(validatePromoCodeUseCase).validatePromoCode(captor.capture());
        ValidatePromoCodeCommand command = captor.getValue();
        assertEquals("PROMO10", command.getPromoCode());
        assertEquals(0, command.getSubtotal().compareTo(new BigDecimal("100.00")));
        assertEquals("RUB", command.getCurrency());
    }

    private static class TestObserver<T> implements StreamObserver<T> {
        private T value;
        private Throwable error;
        private boolean completed;

        @Override
        public void onNext(T value) {
            this.value = value;
        }

        @Override
        public void onError(Throwable t) {
            this.error = t;
        }

        @Override
        public void onCompleted() {
            this.completed = true;
        }
    }
}
