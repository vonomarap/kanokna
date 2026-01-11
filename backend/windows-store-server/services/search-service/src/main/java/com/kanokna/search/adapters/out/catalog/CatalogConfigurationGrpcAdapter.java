package com.kanokna.search.adapters.out.catalog;

import com.kanokna.catalog.v1.CatalogConfigurationServiceGrpc.CatalogConfigurationServiceBlockingStub;
import com.kanokna.catalog.v1.ListProductTemplatesRequest;
import com.kanokna.catalog.v1.ProductTemplate;
import com.kanokna.catalog.v1.ProductTemplateStatus;
import com.kanokna.search.application.dto.CatalogProductEvent;
import com.kanokna.search.application.dto.CatalogProductPage;
import com.kanokna.search.application.port.out.CatalogConfigurationPort;
import com.kanokna.search.domain.model.ProductStatus;
import com.kanokna.shared.money.Currency;
import com.kanokna.shared.money.Money;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * gRPC adapter for catalog-configuration-service product template listing.
 * Uses the blocking stub bean from GrpcClientConfig.
 */
@Component
public class CatalogConfigurationGrpcAdapter implements CatalogConfigurationPort {

    private final CatalogConfigurationServiceBlockingStub stub;

    /**
     * Constructor injection of the gRPC blocking stub.
     * The stub is provided by GrpcClientConfig bean.
     *
     * @param stub the blocking stub for CatalogConfigurationService
     */
    public CatalogConfigurationGrpcAdapter(CatalogConfigurationServiceBlockingStub stub) {
        this.stub = stub;
    }

    @Override
    public CatalogProductPage listProductTemplates(int pageSize, String pageToken) {
        ListProductTemplatesRequest.Builder request = ListProductTemplatesRequest.newBuilder()
            .setPageSize(pageSize);
        if (pageToken != null && !pageToken.isBlank()) {
            request.setPageToken(pageToken);
        }
        var response = stub.listProductTemplates(request.build());
        List<CatalogProductEvent> products = response.getTemplatesList().stream()
            .map(this::toCatalogProductEvent)
            .toList();
        return new CatalogProductPage(products, response.getNextPageToken());
    }

    private CatalogProductEvent toCatalogProductEvent(ProductTemplate template) {
        return new CatalogProductEvent(
            template.getId(),
            "CATALOG_REINDEX",
            template.getId(),
            template.getName(),
            template.getDescription(),
            template.getProductFamily(),
            template.getProfileSystem(),
            template.getOpeningTypesList(),
            template.getMaterialsList(),
            template.getColorsList(),
            toMoney(template.getBasePrice()),
            toMoney(template.getMaxPrice()),
            mapStatus(template.getStatus()),
            template.getThumbnailUrl(),
            template.getPopularity(),
            template.getOptionGroupCount(),
            null,
            null
        );
    }

    private Money toMoney(com.kanokna.common.v1.Money protoMoney) {
        if (protoMoney == null 
                || protoMoney.getCurrency() == com.kanokna.common.v1.Currency.CURRENCY_UNSPECIFIED) {
            return null;
        }
        Currency domainCurrency = mapCurrency(protoMoney.getCurrency());
        return Money.ofMinor(protoMoney.getAmountMinor(), domainCurrency);
    }

    private Currency mapCurrency(com.kanokna.common.v1.Currency protoCurrency) {
        return switch (protoCurrency) {
            case CURRENCY_EUR -> Currency.EUR;
            case CURRENCY_USD -> Currency.USD;
            case CURRENCY_RUB -> Currency.RUB;
            case CURRENCY_UNSPECIFIED, UNRECOGNIZED -> Currency.RUB;
        };
    }

    private ProductStatus mapStatus(ProductTemplateStatus protoStatus) {
        return switch (protoStatus) {
            case PRODUCT_TEMPLATE_STATUS_ACTIVE -> ProductStatus.ACTIVE;
            case PRODUCT_TEMPLATE_STATUS_DRAFT -> ProductStatus.DRAFT;
            case PRODUCT_TEMPLATE_STATUS_ARCHIVED -> ProductStatus.ARCHIVED;
            case PRODUCT_TEMPLATE_STATUS_UNSPECIFIED, UNRECOGNIZED -> ProductStatus.UNSPECIFIED;
        };
    }
}
