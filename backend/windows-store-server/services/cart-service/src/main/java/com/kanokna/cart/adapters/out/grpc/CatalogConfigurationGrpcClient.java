package com.kanokna.cart.adapters.out.grpc;

import com.kanokna.cart.application.dto.BomLineDto;
import com.kanokna.cart.application.dto.SelectedOptionDto;
import com.kanokna.cart.application.port.out.CatalogConfigurationClient;
import com.kanokna.cart.application.port.out.CatalogConfigurationPort;
import com.kanokna.cart.domain.model.ConfigurationSnapshot;
import com.kanokna.catalog.v1.BillOfMaterials;
import com.kanokna.catalog.v1.BomLine;
import com.kanokna.catalog.v1.CatalogConfigurationServiceGrpc;
import com.kanokna.catalog.v1.SelectedOption;
import com.kanokna.catalog.v1.ValidateConfigurationRequest;
import com.kanokna.catalog.v1.ValidateConfigurationResponse;
import com.kanokna.common.v1.Dimensions;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class CatalogConfigurationGrpcClient implements CatalogConfigurationPort, CatalogConfigurationClient {
    private final CatalogConfigurationServiceGrpc.CatalogConfigurationServiceBlockingStub stub;

    public CatalogConfigurationGrpcClient(
        @GrpcClient("catalog-configuration-service")
        CatalogConfigurationServiceGrpc.CatalogConfigurationServiceBlockingStub stub
    ) {
        this.stub = stub;
    }

    @Override
    @CircuitBreaker(name = "catalogService", fallbackMethod = "validateConfigurationFallback")
    public ValidationResult validateConfiguration(ConfigurationSnapshot snapshot) {
        return callValidateConfiguration(snapshot);
    }

    @Override
    @CircuitBreaker(name = "catalogService", fallbackMethod = "validateConfigurationClientFallback")
    public ConfigurationValidationResult validateConfiguration(ConfigurationValidationRequest request) {
        ConfigurationSnapshot snapshot = new ConfigurationSnapshot(
            request.productTemplateId(),
            request.dimensions().widthCm(),
            request.dimensions().heightCm(),
            toOptionSnapshots(request.selectedOptions()),
            List.of()
        );
        ValidationResult result = callValidateConfiguration(snapshot);
        List<BomLineDto> resolvedBom = result.resolvedBom().stream()
            .map(line -> new BomLineDto(line.sku(), line.description(), line.quantity()))
            .toList();
        return new ConfigurationValidationResult(result.available(), result.valid(), result.errors(), resolvedBom);
    }

    private ValidationResult validateConfigurationFallback(ConfigurationSnapshot snapshot, Throwable ex) {
        return ValidationResult.unavailable();
    }

    private ConfigurationValidationResult validateConfigurationClientFallback(
        ConfigurationValidationRequest request,
        Throwable ex
    ) {
        return new ConfigurationValidationResult(false, false, List.of(), List.of());
    }

    private ValidationResult callValidateConfiguration(ConfigurationSnapshot snapshot) {
        ValidateConfigurationRequest request = ValidateConfigurationRequest.newBuilder()
            .setProductTemplateId(snapshot.productTemplateId())
            .setDimensions(Dimensions.newBuilder()
                .setWidthCm(snapshot.widthCm())
                .setHeightCm(snapshot.heightCm())
                .build())
            .addAllSelectedOptions(toSelectedOptions(snapshot.selectedOptions()))
            .build();

        ValidateConfigurationResponse response = stub
            .withDeadlineAfter(2, TimeUnit.SECONDS)
            .validateConfiguration(request);

        List<String> errors = response.getErrorsList().stream()
            .map(error -> error.getCode() + ": " + error.getMessage())
            .toList();

        List<ConfigurationSnapshot.BomLineSnapshot> resolvedBom = toBomSnapshots(response.getResolvedBom());
        return new ValidationResult(true, response.getValid(), errors, resolvedBom);
    }

    private List<SelectedOption> toSelectedOptions(List<ConfigurationSnapshot.SelectedOptionSnapshot> options) {
        if (options == null || options.isEmpty()) {
            return List.of();
        }
        return options.stream()
            .map(option -> SelectedOption.newBuilder()
                .setOptionGroupId(option.optionGroupId())
                .setOptionId(option.optionId())
                .build())
            .toList();
    }

    private List<ConfigurationSnapshot.SelectedOptionSnapshot> toOptionSnapshots(
        List<SelectedOptionDto> options
    ) {
        if (options == null || options.isEmpty()) {
            return List.of();
        }
        return options.stream()
            .map(option -> new ConfigurationSnapshot.SelectedOptionSnapshot(
                option.optionGroupId(),
                option.optionId()
            ))
            .toList();
    }

    private List<ConfigurationSnapshot.BomLineSnapshot> toBomSnapshots(BillOfMaterials bom) {
        if (bom == null || bom.getLinesCount() == 0) {
            return List.of();
        }
        return bom.getLinesList().stream()
            .map(line -> new ConfigurationSnapshot.BomLineSnapshot(
                line.getSku(),
                line.getDescription(),
                line.getQuantity()
            ))
            .toList();
    }
}
