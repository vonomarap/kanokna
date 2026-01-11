package com.kanokna.search.adapters.in.grpc;

import com.kanokna.search.application.dto.FacetValuesResult;
import com.kanokna.search.application.dto.GetFacetValuesQuery;
import com.kanokna.search.application.dto.GetProductByIdQuery;
import com.kanokna.search.application.port.in.AutocompleteUseCase;
import com.kanokna.search.application.port.in.GetFacetValuesUseCase;
import com.kanokna.search.application.port.in.GetProductByIdUseCase;
import com.kanokna.search.application.port.in.SearchProductsUseCase;
import com.kanokna.search.domain.model.AutocompleteQuery;
import com.kanokna.search.domain.model.AutocompleteResult;
import com.kanokna.search.domain.model.ProductSearchDocument;
import com.kanokna.search.domain.model.SearchQuery;
import com.kanokna.search.domain.model.SearchResult;
import com.kanokna.search.v1.AutocompleteRequest;
import com.kanokna.search.v1.AutocompleteResponse;
import com.kanokna.search.v1.GetFacetValuesRequest;
import com.kanokna.search.v1.GetFacetValuesResponse;
import com.kanokna.search.v1.GetProductByIdRequest;
import com.kanokna.search.v1.ProductDocument;
import com.kanokna.search.v1.SearchProductsRequest;
import com.kanokna.search.v1.SearchProductsResponse;
import com.kanokna.search.v1.SearchServiceGrpc;
import com.kanokna.shared.i18n.Language;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

/**
 * gRPC service for search operations.
 */
@GrpcService
public class SearchGrpcService extends SearchServiceGrpc.SearchServiceImplBase {
    private final SearchProductsUseCase searchProductsUseCase;
    private final AutocompleteUseCase autocompleteUseCase;
    private final GetFacetValuesUseCase getFacetValuesUseCase;
    private final GetProductByIdUseCase getProductByIdUseCase;
    private final SearchGrpcMapper mapper;

    public SearchGrpcService(
        SearchProductsUseCase searchProductsUseCase,
        AutocompleteUseCase autocompleteUseCase,
        GetFacetValuesUseCase getFacetValuesUseCase,
        GetProductByIdUseCase getProductByIdUseCase,
        SearchGrpcMapper mapper
    ) {
        this.searchProductsUseCase = searchProductsUseCase;
        this.autocompleteUseCase = autocompleteUseCase;
        this.getFacetValuesUseCase = getFacetValuesUseCase;
        this.getProductByIdUseCase = getProductByIdUseCase;
        this.mapper = mapper;
    }

    @Override
    public void searchProducts(SearchProductsRequest request,
                               StreamObserver<SearchProductsResponse> responseObserver) {
        SearchQuery query = mapper.toQuery(request);
        SearchResult result = searchProductsUseCase.searchProducts(query);
        Language language = query.language() == null ? Language.RU : query.language();
        responseObserver.onNext(mapper.toResponse(result, language));
        responseObserver.onCompleted();
    }

    @Override
    public void getAutocompleteSuggestions(AutocompleteRequest request,
                                           StreamObserver<AutocompleteResponse> responseObserver) {
        AutocompleteQuery query = mapper.toQuery(request);
        AutocompleteResult result = autocompleteUseCase.autocomplete(query);
        responseObserver.onNext(mapper.toResponse(result));
        responseObserver.onCompleted();
    }

    @Override
    public void getFacetValues(GetFacetValuesRequest request,
                               StreamObserver<GetFacetValuesResponse> responseObserver) {
        GetFacetValuesQuery query = mapper.toQuery(request);
        FacetValuesResult result = getFacetValuesUseCase.getFacetValues(query);
        responseObserver.onNext(mapper.toResponse(result));
        responseObserver.onCompleted();
    }

    @Override
    public void getProductById(GetProductByIdRequest request,
                               StreamObserver<ProductDocument> responseObserver) {
        GetProductByIdQuery query = mapper.toQuery(request);
        ProductSearchDocument document = getProductByIdUseCase.getProductById(query);
        Language language = query.language() == null ? Language.RU : query.language();
        responseObserver.onNext(mapper.toProductDocument(document, language));
        responseObserver.onCompleted();
    }
}
