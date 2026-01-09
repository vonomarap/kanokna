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
import com.kanokna.search.support.SearchTestFixture;
import com.kanokna.search.v1.AutocompleteRequest;
import com.kanokna.search.v1.AutocompleteResponse;
import com.kanokna.search.v1.GetFacetValuesRequest;
import com.kanokna.search.v1.GetFacetValuesResponse;
import com.kanokna.search.v1.GetProductByIdRequest;
import com.kanokna.search.v1.ProductDocument;
import com.kanokna.search.v1.SearchProductsRequest;
import com.kanokna.search.v1.SearchProductsResponse;
import com.kanokna.shared.core.DomainException;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchGrpcServiceTest {
    @Mock
    private SearchProductsUseCase searchProductsUseCase;

    @Mock
    private AutocompleteUseCase autocompleteUseCase;

    @Mock
    private GetFacetValuesUseCase getFacetValuesUseCase;

    @Mock
    private GetProductByIdUseCase getProductByIdUseCase;

    private SearchGrpcService service;

    @BeforeEach
    void setUp() {
        SearchGrpcMapper mapper = new SearchGrpcMapper();
        service = new SearchGrpcService(
            searchProductsUseCase,
            autocompleteUseCase,
            getFacetValuesUseCase,
            getProductByIdUseCase,
            mapper
        );
    }

    @Test
    @DisplayName("TC-FUNC-SEARCH-001: searchProducts_validRequest_returnsResponse")
    void searchProducts_validRequest_returnsResponse() {
        ProductSearchDocument document = SearchTestFixture.productDocument("p1", com.kanokna.search.domain.model.ProductStatus.ACTIVE);
        SearchResult result = new SearchResult(List.of(document), 1, 0, 20, 1, List.of(), 6);
        when(searchProductsUseCase.searchProducts(any(SearchQuery.class))).thenReturn(result);

        SearchProductsRequest request = SearchProductsRequest.newBuilder()
            .setQuery("window")
            .setPage(0)
            .setPageSize(20)
            .build();

        TestObserver<SearchProductsResponse> observer = new TestObserver<>();
        service.searchProducts(request, observer);

        assertTrue(observer.completed);
        assertNotNull(observer.value);
        assertEquals(1, observer.value.getProductsCount());
    }

    @Test
    @DisplayName("TC-FUNC-SEARCH-007: searchProducts_invalidPageSize_throwsInvalidArgument")
    void searchProducts_invalidPageSize_throwsInvalidArgument() {
        SearchProductsRequest request = SearchProductsRequest.newBuilder()
            .setQuery("window")
            .setPage(0)
            .setPageSize(101)
            .build();

        TestObserver<SearchProductsResponse> observer = new TestObserver<>();

        assertThrows(IllegalArgumentException.class,
            () -> service.searchProducts(request, observer));
    }

    @Test
    @DisplayName("TC-FUNC-AUTO-001: getAutocompleteSuggestions_validPrefix_returnsSuggestions")
    void getAutocompleteSuggestions_validPrefix_returnsSuggestions() {
        AutocompleteResult result = SearchTestFixture.autocompleteResult(
            List.of(SearchTestFixture.suggestion("Window", "p1")),
            4
        );
        when(autocompleteUseCase.autocomplete(any(AutocompleteQuery.class))).thenReturn(result);

        AutocompleteRequest request = AutocompleteRequest.newBuilder()
            .setPrefix("wi")
            .setLimit(5)
            .build();

        TestObserver<AutocompleteResponse> observer = new TestObserver<>();
        service.getAutocompleteSuggestions(request, observer);

        assertTrue(observer.completed);
        assertEquals(1, observer.value.getSuggestionsCount());
    }

    @Test
    @DisplayName("TC-FUNC-AUTO-002: getAutocompleteSuggestions_shortPrefix_throwsInvalidArgument")
    void getAutocompleteSuggestions_shortPrefix_throwsInvalidArgument() {
        when(autocompleteUseCase.autocomplete(any(AutocompleteQuery.class)))
            .thenThrow(new DomainException("ERR-AUTO-PREFIX-TOO-SHORT", "short"));

        AutocompleteRequest request = AutocompleteRequest.newBuilder()
            .setPrefix("w")
            .setLimit(5)
            .build();

        TestObserver<AutocompleteResponse> observer = new TestObserver<>();

        assertThrows(DomainException.class,
            () -> service.getAutocompleteSuggestions(request, observer));
    }

    @Test
    @DisplayName("TC-FUNC-FACET-001: getFacetValues_validFields_returnsFacets")
    void getFacetValues_validFields_returnsFacets() {
        FacetValuesResult result = new FacetValuesResult(
            List.of(SearchTestFixture.facetAggregation("family", "WINDOW", 3, false)),
            3
        );
        when(getFacetValuesUseCase.getFacetValues(any(GetFacetValuesQuery.class))).thenReturn(result);

        GetFacetValuesRequest request = GetFacetValuesRequest.newBuilder()
            .addFields("family")
            .build();

        TestObserver<GetFacetValuesResponse> observer = new TestObserver<>();
        service.getFacetValues(request, observer);

        assertTrue(observer.completed);
        assertEquals(1, observer.value.getFacetsCount());
    }

    @Test
    @DisplayName("TC-FUNC-GET-001: getProductById_existingId_returnsDocument")
    void getProductById_existingId_returnsDocument() {
        ProductSearchDocument document = SearchTestFixture.productDocument("p1", com.kanokna.search.domain.model.ProductStatus.ACTIVE);
        when(getProductByIdUseCase.getProductById(any(GetProductByIdQuery.class))).thenReturn(document);

        GetProductByIdRequest request = GetProductByIdRequest.newBuilder()
            .setProductId("p1")
            .build();

        TestObserver<ProductDocument> observer = new TestObserver<>();
        service.getProductById(request, observer);

        assertTrue(observer.completed);
        assertEquals("p1", observer.value.getId());
    }

    @Test
    @DisplayName("TC-FUNC-GET-002: getProductById_notFound_throwsNotFound")
    void getProductById_notFound_throwsNotFound() {
        when(getProductByIdUseCase.getProductById(any(GetProductByIdQuery.class)))
            .thenThrow(new DomainException("ERR-GET-PRODUCT-NOT-FOUND", "missing"));

        GetProductByIdRequest request = GetProductByIdRequest.newBuilder()
            .setProductId("missing")
            .build();

        TestObserver<ProductDocument> observer = new TestObserver<>();

        assertThrows(DomainException.class, () -> service.getProductById(request, observer));
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
