package com.kanokna.search;

import com.kanokna.search.adapters.in.grpc.SearchGrpcMapper;
import com.kanokna.search.adapters.in.grpc.SearchGrpcService;
import com.kanokna.search.application.dto.FacetValuesResult;
import com.kanokna.search.application.port.in.AutocompleteUseCase;
import com.kanokna.search.application.port.in.GetFacetValuesUseCase;
import com.kanokna.search.application.port.in.GetProductByIdUseCase;
import com.kanokna.search.application.port.in.SearchProductsUseCase;
import com.kanokna.search.domain.model.AutocompleteResult;
import com.kanokna.search.domain.model.FacetAggregation;
import com.kanokna.search.domain.model.FacetBucket;
import com.kanokna.search.domain.model.FacetType;
import com.kanokna.search.domain.model.ProductSearchDocument;
import com.kanokna.search.domain.model.ProductStatus;
import com.kanokna.search.domain.model.SearchResult;
import com.kanokna.search.support.SearchTestFixture;
import com.kanokna.search.v1.AutocompleteResponse;
import com.kanokna.search.v1.GetFacetValuesResponse;
import com.kanokna.search.v1.ProductDocument;
import com.kanokna.search.v1.SearchProductsResponse;
import com.kanokna.search.v1.SearchServiceGrpc;
import com.kanokna.shared.i18n.Language;
import io.grpc.MethodDescriptor;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class SearchServiceGrpcContractTest {
    @Test
    @DisplayName("TC-FUNC-SEARCH-001: searchService_implementsAllProtoMethods")
    void searchService_implementsAllProtoMethods() throws Exception {
        List<String> methodNames = SearchServiceGrpc.getServiceDescriptor().getMethods().stream()
            .map(MethodDescriptor::getBareMethodName)
            .toList();

        assertTrue(methodNames.containsAll(List.of(
            "SearchProducts",
            "GetAutocompleteSuggestions",
            "GetFacetValues",
            "GetProductById"
        )));

        Method searchProducts = SearchGrpcService.class.getMethod(
            "searchProducts",
            com.kanokna.search.v1.SearchProductsRequest.class,
            StreamObserver.class
        );
        Method autocomplete = SearchGrpcService.class.getMethod(
            "getAutocompleteSuggestions",
            com.kanokna.search.v1.AutocompleteRequest.class,
            StreamObserver.class
        );
        Method facetValues = SearchGrpcService.class.getMethod(
            "getFacetValues",
            com.kanokna.search.v1.GetFacetValuesRequest.class,
            StreamObserver.class
        );
        Method getById = SearchGrpcService.class.getMethod(
            "getProductById",
            com.kanokna.search.v1.GetProductByIdRequest.class,
            StreamObserver.class
        );

        assertNotNull(searchProducts);
        assertNotNull(autocomplete);
        assertNotNull(facetValues);
        assertNotNull(getById);

        SearchGrpcService service = new SearchGrpcService(
            mock(SearchProductsUseCase.class),
            mock(AutocompleteUseCase.class),
            mock(GetFacetValuesUseCase.class),
            mock(GetProductByIdUseCase.class),
            new SearchGrpcMapper()
        );
        assertEquals(4, service.bindService().getServiceDescriptor().getMethods().size());
    }

    @Test
    @DisplayName("TC-FUNC-SEARCH-001: searchProducts_responseMatchesProtoSchema")
    void searchProducts_responseMatchesProtoSchema() throws Exception {
        SearchGrpcMapper mapper = new SearchGrpcMapper();
        SearchResult result = new SearchResult(
            List.of(SearchTestFixture.productDocument("p1", ProductStatus.ACTIVE)),
            1,
            0,
            20,
            1,
            List.of(),
            5
        );

        SearchProductsResponse response = mapper.toResponse(result, Language.RU);
        SearchProductsResponse parsed = SearchProductsResponse.parseFrom(response.toByteArray());

        assertEquals(response, parsed);
        assertEquals(1, parsed.getProductsCount());
    }

    @Test
    @DisplayName("TC-FUNC-AUTO-001: getAutocompleteSuggestions_responseMatchesProtoSchema")
    void getAutocompleteSuggestions_responseMatchesProtoSchema() throws Exception {
        SearchGrpcMapper mapper = new SearchGrpcMapper();
        AutocompleteResult result = SearchTestFixture.autocompleteResult(
            List.of(SearchTestFixture.suggestion("Window", "p1")),
            3
        );

        AutocompleteResponse response = mapper.toResponse(result);
        AutocompleteResponse parsed = AutocompleteResponse.parseFrom(response.toByteArray());

        assertEquals(response, parsed);
        assertEquals(1, parsed.getSuggestionsCount());
    }

    @Test
    @DisplayName("TC-FUNC-FACET-001: getFacetValues_responseMatchesProtoSchema")
    void getFacetValues_responseMatchesProtoSchema() throws Exception {
        SearchGrpcMapper mapper = new SearchGrpcMapper();
        FacetAggregation facet = new FacetAggregation(
            "family",
            "family",
            FacetType.TERMS,
            List.of(new FacetBucket("WINDOW", "WINDOW", 2, false))
        );
        FacetValuesResult result = new FacetValuesResult(List.of(facet), 3);

        GetFacetValuesResponse response = mapper.toResponse(result);
        GetFacetValuesResponse parsed = GetFacetValuesResponse.parseFrom(response.toByteArray());

        assertEquals(response, parsed);
        assertEquals(1, parsed.getFacetsCount());
    }

    @Test
    @DisplayName("TC-FUNC-GET-001: getProductById_responseMatchesProtoSchema")
    void getProductById_responseMatchesProtoSchema() throws Exception {
        SearchGrpcMapper mapper = new SearchGrpcMapper();
        ProductSearchDocument document = SearchTestFixture.productDocument("p1", ProductStatus.ACTIVE);

        ProductDocument response = mapper.toProductDocument(document, Language.RU);
        ProductDocument parsed = ProductDocument.parseFrom(response.toByteArray());

        assertEquals(response, parsed);
        assertEquals("p1", parsed.getId());
    }
}
