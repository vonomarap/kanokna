package com.kanokna.cart.adapters.out.grpc;

import com.kanokna.cart.application.dto.BomLineDto;
import com.kanokna.cart.application.dto.DimensionsDto;
import com.kanokna.cart.application.port.out.PricingClient;
import com.kanokna.cart.application.port.out.PricingPort;
import com.kanokna.cart.domain.model.ConfigurationSnapshot;
import com.kanokna.catalog.v1.BillOfMaterials;
import com.kanokna.catalog.v1.BomLine;
import com.kanokna.common.v1.Currency;
import com.kanokna.common.v1.Dimensions;
import com.kanokna.common.v1.Money;
import com.kanokna.pricing.v1.CalculateQuoteRequest;
import com.kanokna.pricing.v1.CalculateQuoteResponse;
import com.kanokna.pricing.v1.PricingServiceGrpc;
import com.kanokna.pricing.v1.ValidatePromoCodeRequest;
import com.kanokna.pricing.v1.ValidatePromoCodeResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class PricingGrpcClient implements PricingPort, PricingClient {
    private final PricingServiceGrpc.PricingServiceBlockingStub stub;

    public PricingGrpcClient(
        @GrpcClient("pricing-service")
        PricingServiceGrpc.PricingServiceBlockingStub stub
    ) {
        this.stub = stub;
    }

    @Override
    @CircuitBreaker(name = "pricingService", fallbackMethod = "calculateQuoteFallback")
    public PriceQuote calculateQuote(ConfigurationSnapshot snapshot, com.kanokna.shared.money.Currency currency) {
        CalculateQuoteRequest request = CalculateQuoteRequest.newBuilder()
            .setProductTemplateId(snapshot.productTemplateId())
            .setDimensions(Dimensions.newBuilder()
                .setWidthCm(snapshot.widthCm())
                .setHeightCm(snapshot.heightCm())
                .build())
            .setResolvedBom(toBillOfMaterials(snapshot.resolvedBom()))
            .setCurrency(mapCurrency(currency))
            .setPromoCode("")
            .setRegion("")
            .build();

        CalculateQuoteResponse response = stub
            .withDeadlineAfter(2, TimeUnit.SECONDS)
            .calculateQuote(request);
        return toPriceQuote(response);
    }

    @Override
    @CircuitBreaker(name = "pricingService", fallbackMethod = "validatePromoFallback")
    public PromoValidationResult validatePromoCode(String promoCode, com.kanokna.shared.money.Money subtotal) {
        ValidatePromoCodeRequest request = ValidatePromoCodeRequest.newBuilder()
            .setPromoCode(promoCode)
            .setSubtotal(toMoney(subtotal))
            .build();

        ValidatePromoCodeResponse response = stub
            .withDeadlineAfter(2, TimeUnit.SECONDS)
            .validatePromoCode(request);

        if (response == null) {
            return PromoValidationResult.unavailable();
        }
        com.kanokna.shared.money.Money discount = response.hasDiscountAmount()
            ? toDomainMoney(response.getDiscountAmount())
            : com.kanokna.shared.money.Money.zero(subtotal.getCurrency());
        return new PromoValidationResult(
            true,
            response.getValid(),
            discount,
            null,
            blankToNull(response.getErrorMessage())
        );
    }

    @Override
    @CircuitBreaker(name = "pricingService", fallbackMethod = "calculateQuoteClientFallback")
    public PricingClient.PriceQuote calculateQuote(PricingClient.PriceQuoteRequest request) {
        CalculateQuoteRequest protoRequest = CalculateQuoteRequest.newBuilder()
            .setProductTemplateId(request.productTemplateId())
            .setDimensions(toDimensions(request.dimensions()))
            .setResolvedBom(toBillOfMaterials(request.resolvedBom()))
            .setCurrency(mapCurrency(request.currency()))
            .setPromoCode(request.promoCode() == null ? "" : request.promoCode())
            .setRegion(request.region() == null ? "" : request.region())
            .build();

        CalculateQuoteResponse response = stub
            .withDeadlineAfter(2, TimeUnit.SECONDS)
            .calculateQuote(protoRequest);

        PricingPort.PriceQuote quote = toPriceQuote(response);
        return new PricingClient.PriceQuote(
            quote.available(),
            quote.quoteId(),
            quote.unitPrice(),
            quote.validUntil()
        );
    }

    @Override
    @CircuitBreaker(name = "pricingService", fallbackMethod = "validatePromoClientFallback")
    public PricingClient.PromoValidationResult validatePromoCode(PricingClient.PromoValidationRequest request) {
        ValidatePromoCodeRequest protoRequest = ValidatePromoCodeRequest.newBuilder()
            .setPromoCode(request.promoCode())
            .setSubtotal(toMoney(request.subtotal()))
            .build();

        ValidatePromoCodeResponse response = stub
            .withDeadlineAfter(2, TimeUnit.SECONDS)
            .validatePromoCode(protoRequest);

        if (response == null) {
            return new PricingClient.PromoValidationResult(false, false, null, null, null);
        }
        com.kanokna.shared.money.Money discount = response.hasDiscountAmount()
            ? toDomainMoney(response.getDiscountAmount())
            : com.kanokna.shared.money.Money.zero(request.subtotal().getCurrency());
        return new PricingClient.PromoValidationResult(
            true,
            response.getValid(),
            discount,
            blankToNull(response.getErrorMessage()),
            null
        );
    }

    private PriceQuote calculateQuoteFallback(
        ConfigurationSnapshot snapshot,
        com.kanokna.shared.money.Currency currency,
        Throwable ex
    ) {
        return PriceQuote.unavailable();
    }

    private PromoValidationResult validatePromoFallback(
        String promoCode,
        com.kanokna.shared.money.Money subtotal,
        Throwable ex
    ) {
        return PromoValidationResult.unavailable();
    }

    private PricingClient.PriceQuote calculateQuoteClientFallback(
        PricingClient.PriceQuoteRequest request,
        Throwable ex
    ) {
        return new PricingClient.PriceQuote(false, null, null, null);
    }

    private PricingClient.PromoValidationResult validatePromoClientFallback(
        PricingClient.PromoValidationRequest request,
        Throwable ex
    ) {
        return new PricingClient.PromoValidationResult(false, false, null, null, null);
    }

    private PricingPort.PriceQuote toPriceQuote(CalculateQuoteResponse response) {
        if (response == null || response.getQuoteId().isBlank()) {
            return PricingPort.PriceQuote.unavailable();
        }
        com.kanokna.shared.money.Money unitPrice = toDomainMoney(response.getTotal());
        Instant validUntil = response.hasValidUntil()
            ? Instant.ofEpochSecond(response.getValidUntil().getSeconds(), response.getValidUntil().getNanos())
            : null;
        return new PricingPort.PriceQuote(true, response.getQuoteId(), unitPrice, validUntil);
    }

    private BillOfMaterials toBillOfMaterials(List<ConfigurationSnapshot.BomLineSnapshot> lines) {
        if (lines == null || lines.isEmpty()) {
            return BillOfMaterials.newBuilder().build();
        }
        BillOfMaterials.Builder builder = BillOfMaterials.newBuilder();
        for (ConfigurationSnapshot.BomLineSnapshot line : lines) {
            builder.addLines(BomLine.newBuilder()
                .setSku(line.sku())
                .setDescription(line.description())
                .setQuantity(line.quantity())
                .build());
        }
        return builder.build();
    }

    private BillOfMaterials toBillOfMaterials(List<BomLineDto> lines) {
        if (lines == null || lines.isEmpty()) {
            return BillOfMaterials.newBuilder().build();
        }
        BillOfMaterials.Builder builder = BillOfMaterials.newBuilder();
        for (BomLineDto line : lines) {
            builder.addLines(BomLine.newBuilder()
                .setSku(line.sku())
                .setDescription(line.description())
                .setQuantity(line.quantity())
                .build());
        }
        return builder.build();
    }

    private Dimensions toDimensions(DimensionsDto dto) {
        if (dto == null) {
            return Dimensions.newBuilder().build();
        }
        return Dimensions.newBuilder()
            .setWidthCm(dto.widthCm())
            .setHeightCm(dto.heightCm())
            .build();
    }

    private Money toMoney(com.kanokna.shared.money.Money money) {
        if (money == null) {
            return Money.newBuilder()
                .setAmountMinor(0)
                .setCurrency(Currency.CURRENCY_UNSPECIFIED)
                .build();
        }
        int scale = money.getCurrency().getDefaultScale();
        long minor = money.getAmount()
            .movePointRight(scale)
            .setScale(0, RoundingMode.HALF_UP)
            .longValue();
        return Money.newBuilder()
            .setAmountMinor(minor)
            .setCurrency(mapCurrency(money.getCurrency()))
            .build();
    }

    private com.kanokna.shared.money.Money toDomainMoney(Money money) {
        if (money == null) {
            return null;
        }
        com.kanokna.shared.money.Currency currency = mapCurrency(money.getCurrency());
        BigDecimal amount = BigDecimal.valueOf(money.getAmountMinor())
            .movePointLeft(currency.getDefaultScale());
        return com.kanokna.shared.money.Money.of(amount, currency);
    }

    private Currency mapCurrency(com.kanokna.shared.money.Currency currency) {
        if (currency == null) {
            return Currency.CURRENCY_UNSPECIFIED;
        }
        return switch (currency) {
            case RUB -> Currency.CURRENCY_RUB;
            case EUR -> Currency.CURRENCY_EUR;
            case USD -> Currency.CURRENCY_USD;
        };
    }

    private Currency mapCurrency(String currency) {
        if (currency == null) {
            return Currency.CURRENCY_UNSPECIFIED;
        }
        return switch (currency.toUpperCase()) {
            case "RUB" -> Currency.CURRENCY_RUB;
            case "EUR" -> Currency.CURRENCY_EUR;
            case "USD" -> Currency.CURRENCY_USD;
            default -> Currency.CURRENCY_UNSPECIFIED;
        };
    }

    private com.kanokna.shared.money.Currency mapCurrency(Currency currency) {
        return switch (currency) {
            case CURRENCY_EUR -> com.kanokna.shared.money.Currency.EUR;
            case CURRENCY_USD -> com.kanokna.shared.money.Currency.USD;
            case CURRENCY_RUB -> com.kanokna.shared.money.Currency.RUB;
            default -> com.kanokna.shared.money.Currency.RUB;
        };
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value;
    }
}
