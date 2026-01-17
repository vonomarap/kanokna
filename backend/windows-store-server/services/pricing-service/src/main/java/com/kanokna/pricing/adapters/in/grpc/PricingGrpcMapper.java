package com.kanokna.pricing.adapters.in.grpc;

import com.google.protobuf.Timestamp;
import com.kanokna.catalog.v1.BillOfMaterials;
import com.kanokna.catalog.v1.BomLine;
import com.kanokna.common.v1.Currency;
import com.kanokna.common.v1.Money;
import com.kanokna.pricing.v1.CalculateQuoteRequest;
import com.kanokna.pricing.v1.CalculateQuoteResponse;
import com.kanokna.pricing.v1.PremiumLine;
import com.kanokna.pricing.v1.PricingDecision;
import com.kanokna.pricing.v1.ValidatePromoCodeRequest;
import com.kanokna.pricing.v1.ValidatePromoCodeResponse;
import com.kanokna.pricing.application.dto.CalculateQuoteCommand;
import com.kanokna.pricing.application.dto.PromoCodeValidationResponse;
import com.kanokna.pricing.application.dto.QuoteResponse;
import com.kanokna.pricing.application.dto.ValidatePromoCodeCommand;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper between pricing domain/application DTOs and protobuf messages.
 */
@Component
public class PricingGrpcMapper {

    public CalculateQuoteCommand toCommand(CalculateQuoteRequest request) {
        CalculateQuoteCommand command = new CalculateQuoteCommand();
        command.setProductTemplateId(request.getProductTemplateId());
        command.setWidthCm(BigDecimal.valueOf(request.getDimensions().getWidthCm()));
        command.setHeightCm(BigDecimal.valueOf(request.getDimensions().getHeightCm()));
        command.setResolvedBom(extractOptionIds(request.getResolvedBom()));
        command.setCurrency(mapCurrency(request.getCurrency()));
        command.setPromoCode(request.getPromoCode());
        command.setRegion(request.getRegion());
        return command;
    }

    public CalculateQuoteResponse toResponse(QuoteResponse response) {
        CalculateQuoteResponse.Builder builder = CalculateQuoteResponse.newBuilder()
            .setQuoteId(response.getQuoteId())
            .setBasePrice(toMoney(response.getBasePrice()))
            .setDiscount(toMoney(response.getDiscount()))
            .setSubtotal(toMoney(response.getSubtotal()))
            .setTax(toMoney(response.getTax()))
            .setTotal(toMoney(response.getTotal()))
            .setValidUntil(toTimestamp(response.getValidUntil()));

        if (response.getOptionPremiums() != null) {
            for (QuoteResponse.PremiumLineDto line : response.getOptionPremiums()) {
                builder.addOptionPremiums(PremiumLine.newBuilder()
                    .setOptionId(line.getOptionId())
                    .setOptionName(line.getOptionName())
                    .setAmount(toMoney(line.getAmount()))
                    .build());
            }
        }

        if (response.getDecisionTrace() != null) {
            for (QuoteResponse.PricingDecisionDto decision : response.getDecisionTrace()) {
                builder.addDecisionTrace(PricingDecision.newBuilder()
                    .setStep(decision.getStep())
                    .setRuleApplied(decision.getRuleApplied())
                    .setResult(decision.getResult())
                    .build());
            }
        }

        return builder.build();
    }

    public ValidatePromoCodeCommand toCommand(ValidatePromoCodeRequest request) {
        ValidatePromoCodeCommand command = new ValidatePromoCodeCommand();
        command.setPromoCode(request.getPromoCode());
        command.setSubtotal(fromMinor(request.getSubtotal().getAmountMinor()));
        command.setCurrency(mapCurrency(request.getSubtotal().getCurrency()));
        return command;
    }

    public ValidatePromoCodeResponse toResponse(PromoCodeValidationResponse response) {
        ValidatePromoCodeResponse.Builder builder = ValidatePromoCodeResponse.newBuilder()
            .setValid(response.isValid())
            .setErrorMessage(response.getErrorMessage() == null ? "" : response.getErrorMessage());

        if (response.getDiscountAmount() != null) {
            builder.setDiscountAmount(toMoney(response.getDiscountAmount()));
        }

        return builder.build();
    }

    private List<String> extractOptionIds(BillOfMaterials bom) {
        if (bom == null || bom.getLinesCount() == 0) {
            return List.of();
        }
        return bom.getLinesList().stream()
            .map(BomLine::getSku)
            .filter(sku -> sku != null && !sku.isBlank())
            .collect(Collectors.toList());
    }

    private Money toMoney(String amountWithCurrency) {
        ParsedMoney parsed = parseMoney(amountWithCurrency);
        if (parsed == null) {
            return Money.newBuilder()
                .setAmountMinor(0)
                .setCurrency(Currency.CURRENCY_UNSPECIFIED)
                .build();
        }
        long minor = parsed.amount().movePointRight(2).setScale(0, RoundingMode.HALF_UP).longValue();
        return Money.newBuilder()
            .setAmountMinor(minor)
            .setCurrency(mapCurrency(parsed.currency()))
            .build();
    }

    private ParsedMoney parseMoney(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String[] parts = value.trim().split(" +");
        if (parts.length < 2) {
            return null;
        }
        return new ParsedMoney(new BigDecimal(parts[0]), parts[1]);
    }

    private BigDecimal fromMinor(long amountMinor) {
        return BigDecimal.valueOf(amountMinor).movePointLeft(2);
    }

    private Currency mapCurrency(String currency) {
        if (currency == null) {
            return Currency.CURRENCY_UNSPECIFIED;
        }
        if ("RUB".equalsIgnoreCase(currency)) {
            return Currency.CURRENCY_RUB;
        }
        if ("EUR".equalsIgnoreCase(currency)) {
            return Currency.CURRENCY_EUR;
        }
        if ("USD".equalsIgnoreCase(currency)) {
            return Currency.CURRENCY_USD;
        }
        return Currency.CURRENCY_UNSPECIFIED;
    }

    private String mapCurrency(Currency currency) {
        if (currency == Currency.CURRENCY_RUB) {
            return "RUB";
        }
        if (currency == Currency.CURRENCY_EUR) {
            return "EUR";
        }
        if (currency == Currency.CURRENCY_USD) {
            return "USD";
        }
        return "RUB";
    }

    private Timestamp toTimestamp(Instant instant) {
        return Timestamp.newBuilder()
            .setSeconds(instant.getEpochSecond())
            .setNanos(instant.getNano())
            .build();
    }

    private record ParsedMoney(BigDecimal amount, String currency) {
    }
}
