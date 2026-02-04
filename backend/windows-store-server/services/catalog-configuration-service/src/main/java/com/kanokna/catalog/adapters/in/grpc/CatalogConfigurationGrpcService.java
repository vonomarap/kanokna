package com.kanokna.catalog.adapters.in.grpc;

import com.kanokna.catalog.application.dto.ConfigurationResponse;
import com.kanokna.catalog.application.dto.ProductTemplateDto;
import com.kanokna.catalog.application.dto.ValidateConfigurationCommand;
import com.kanokna.catalog.application.port.in.GetProductTemplateQuery;
import com.kanokna.catalog.application.port.in.ListProductTemplatesQuery;
import com.kanokna.catalog.application.port.in.ValidateConfigurationUseCase;
import com.kanokna.catalog.application.port.out.BomTemplateRepository;
import com.kanokna.catalog.domain.model.Configuration;
import com.kanokna.catalog.domain.model.ProductFamily;
import com.kanokna.catalog.domain.model.ProductTemplateId;
import com.kanokna.catalog.domain.model.ResolvedBom;
import com.kanokna.catalog.domain.service.BomResolutionService;
import com.kanokna.catalog.v1.CatalogConfigurationServiceGrpc;
import com.kanokna.catalog.v1.GetProductTemplateRequest;
import com.kanokna.catalog.v1.GetProductTemplateResponse;
import com.kanokna.catalog.v1.ListProductTemplatesRequest;
import com.kanokna.catalog.v1.ListProductTemplatesResponse;
import com.kanokna.catalog.v1.ValidateConfigurationRequest;
import com.kanokna.catalog.v1.ValidateConfigurationResponse;
import io.grpc.stub.StreamObserver;
import org.springframework.grpc.server.service.GrpcService;

import java.util.List;

/**
 * MODULE_CONTRACT id="MC-catalog-grpc-adapter" LAYER="adapters.in.grpc"
 * INTENT="gRPC adapter for catalog configuration and validation operations"
 * LINKS="Technology.xml#TECH-grpc;RequirementsAnalysis.xml#UC-CATALOG-CONFIGURE-PRODUCT"
 *
 * gRPC service for product template and configuration operations.
 */
@GrpcService
public class CatalogConfigurationGrpcService
        extends CatalogConfigurationServiceGrpc.CatalogConfigurationServiceImplBase {

    private final ValidateConfigurationUseCase validateConfigurationUseCase;
    private final GetProductTemplateQuery getProductTemplateQuery;
    private final ListProductTemplatesQuery listProductTemplatesQuery;
    private final BomTemplateRepository bomTemplateRepository;
    private final BomResolutionService bomResolutionService;
    private final CatalogConfigurationGrpcMapper mapper;

    public CatalogConfigurationGrpcService(
            ValidateConfigurationUseCase validateConfigurationUseCase,
            GetProductTemplateQuery getProductTemplateQuery,
            ListProductTemplatesQuery listProductTemplatesQuery,
            BomTemplateRepository bomTemplateRepository,
            BomResolutionService bomResolutionService,
            CatalogConfigurationGrpcMapper mapper
    ) {
        this.validateConfigurationUseCase = validateConfigurationUseCase;
        this.getProductTemplateQuery = getProductTemplateQuery;
        this.listProductTemplatesQuery = listProductTemplatesQuery;
        this.bomTemplateRepository = bomTemplateRepository;
        this.bomResolutionService = bomResolutionService;
        this.mapper = mapper;
    }

    @Override
    public void validateConfiguration(
            ValidateConfigurationRequest request,
            StreamObserver<ValidateConfigurationResponse> responseObserver
    ) {
        ValidateConfigurationCommand command = mapper.toCommand(request);
        ConfigurationResponse validation = validateConfigurationUseCase.validate(command);

        ResolvedBom resolvedBom = null;
        if (validation.valid()) {
            Configuration configuration = mapper.toConfiguration(command);
            ProductTemplateId productTemplateId = ProductTemplateId.of(command.productTemplateId());
            resolvedBom = bomTemplateRepository.findByProductTemplateId(productTemplateId)
                    .map(template -> bomResolutionService.resolveBom(configuration, template))
                    .orElse(null);
        }

        responseObserver.onNext(mapper.toValidateConfigurationResponse(validation, resolvedBom));
        responseObserver.onCompleted();
    }

    @Override
    public void getProductTemplate(
            GetProductTemplateRequest request,
            StreamObserver<GetProductTemplateResponse> responseObserver
    ) {
        ProductTemplateDto template = getProductTemplateQuery.getById(
                ProductTemplateId.of(request.getProductTemplateId())
        );
        responseObserver.onNext(mapper.toGetProductTemplateResponse(template));
        responseObserver.onCompleted();
    }

    @Override
    public void listProductTemplates(
            ListProductTemplatesRequest request,
            StreamObserver<ListProductTemplatesResponse> responseObserver
    ) {
        ProductFamily family = mapper.toProductFamily(request.getProductFamilyFilter());
        List<ProductTemplateDto> templates = listProductTemplatesQuery.list(family, true);
        int pageSize = request.getPageSize() > 0 ? request.getPageSize() : templates.size();
        int offset = parsePageToken(request.getPageToken());
        int start = Math.min(offset, templates.size());
        int end = Math.min(start + pageSize, templates.size());
        List<ProductTemplateDto> slice = templates.subList(start, end);
        String nextToken = end < templates.size() ? String.valueOf(end) : "";
        responseObserver.onNext(mapper.toListProductTemplatesResponse(slice, nextToken));
        responseObserver.onCompleted();
    }

    private int parsePageToken(String pageToken) {
        if (pageToken == null || pageToken.isBlank()) {
            return 0;
        }
        try {
            int offset = Integer.parseInt(pageToken);
            if (offset < 0) {
                throw new IllegalArgumentException("pageToken must be non-negative");
            }
            return offset;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("pageToken must be numeric", ex);
        }
    }
}
