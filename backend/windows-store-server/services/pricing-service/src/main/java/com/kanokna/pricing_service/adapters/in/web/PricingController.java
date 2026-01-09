package com.kanokna.pricing_service.adapters.in.web;

import com.kanokna.pricing_service.application.dto.*;
import com.kanokna.pricing_service.application.port.in.CalculateQuoteUseCase;
import com.kanokna.pricing_service.application.port.in.ValidatePromoCodeUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * REST controller for public pricing operations.
 * Provides endpoints for quote calculation and promo code validation.
 *
 * Per DevelopmentPlan.xml#DP-SVC-pricing-service
 */
@RestController
@RequestMapping("/api/v1/pricing")
public class PricingController {

    private final CalculateQuoteUseCase calculateQuoteUseCase;
    private final ValidatePromoCodeUseCase validatePromoCodeUseCase;

    public PricingController(
            CalculateQuoteUseCase calculateQuoteUseCase,
            ValidatePromoCodeUseCase validatePromoCodeUseCase) {
        this.calculateQuoteUseCase = calculateQuoteUseCase;
        this.validatePromoCodeUseCase = validatePromoCodeUseCase;
    }

    /**
     * Calculate a price quote for a product configuration.
     *
     * POST /api/v1/pricing/quotes
     *
     * Request body:
     * {
     *   "productTemplateId": "WINDOW-PVC-STANDARD",
     *   "widthCm": 120.0,
     *   "heightCm": 150.0,
     *   "selectedOptionIds": ["OPT-LAMINATION-GOLD-OAK", "OPT-HANDLE-ROTO"],
     *   "currency": "RUB",
     *   "promoCode": "WELCOME10",
     *   "region": "RU"
     * }
     *
     * Response 200:
     * {
     *   "quoteId": "uuid",
     *   "productTemplateId": "WINDOW-PVC-STANDARD",
     *   "basePrice": "RUB 18000.00",
     *   "optionPremiums": [...],
     *   "discount": "RUB 2000.00",
     *   "subtotal": "RUB 20000.00",
     *   "tax": "RUB 4000.00",
     *   "total": "RUB 24000.00",
     *   "currency": "RUB",
     *   "validUntil": "2025-12-31T12:05:00Z",
     *   "decisionTrace": [...]
     * }
     *
     * Error responses:
     * - 404: Price book not found for product
     * - 400: Invalid dimensions or configuration
     */
    @PostMapping("/quotes")
    public ResponseEntity<QuoteResponse> calculateQuote(@Valid @RequestBody CalculateQuoteCommand command) {
        QuoteResponse response = calculateQuoteUseCase.calculateQuote(command);
        return ResponseEntity.ok(response);
    }

    /**
     * Validate a promotional code and calculate potential discount.
     *
     * POST /api/v1/pricing/promo-codes/validate
     *
     * Request body:
     * {
     *   "promoCode": "WELCOME10",
     *   "subtotal": "20000.00",
     *   "currency": "RUB"
     * }
     *
     * Response 200:
     * {
     *   "valid": true,
     *   "discountAmount": "RUB 2000.00",
     *   "errorMessage": null
     * }
     *
     * Or if invalid:
     * {
     *   "valid": false,
     *   "discountAmount": null,
     *   "errorMessage": "ERR-PROMO-EXPIRED"
     * }
     */
    @PostMapping("/promo-codes/validate")
    public ResponseEntity<PromoCodeValidationResponse> validatePromoCode(
            @Valid @RequestBody ValidatePromoCodeCommand command) {
        PromoCodeValidationResponse response = validatePromoCodeUseCase.validatePromoCode(command);
        return ResponseEntity.ok(response);
    }
}
