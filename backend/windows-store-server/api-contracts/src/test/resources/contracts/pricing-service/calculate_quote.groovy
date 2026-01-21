import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Calculate pricing quote"
    request {
        method POST()
        url "/api/v1/pricing/quote"
        headers {
            contentType(applicationJson())
        }
        body(
            productTemplateId: "rehau-delight-2sash",
            dimensions: [
                widthCm: 120,
                heightCm: 150
            ],
            resolvedBom: [
                lines: [
                    [
                        sku: "SKU-1",
                        description: "Line 1",
                        quantity: 1
                    ]
                ]
            ],
            currency: "RUB",
            promoCode: "SUMMER20",
            region: "RU"
        )
    }
    response {
        status OK()
        headers {
            contentType(applicationJson())
        }
        body(
            quoteId: "00000000-0000-0000-0000-000000000001",
            basePrice: [
                amount: "12000.00",
                currency: "RUB"
            ],
            optionPremiums: [
                [
                    optionId: "triple-glazed",
                    optionName: "Triple Glazed Glass",
                    amount: [
                        amount: "2000.00",
                        currency: "RUB"
                    ]
                ]
            ],
            discount: [
                amount: "0.00",
                currency: "RUB"
            ],
            subtotal: [
                amount: "14000.00",
                currency: "RUB"
            ],
            tax: [
                amount: "2800.00",
                currency: "RUB"
            ],
            total: [
                amount: "16800.00",
                currency: "RUB"
            ],
            validUntil: "2026-01-20T00:00:00Z",
            decisionTrace: [
                [
                    step: "base_price",
                    ruleApplied: "DEFAULT",
                    result: "12000.00 RUB"
                ]
            ]
        )
    }
}
