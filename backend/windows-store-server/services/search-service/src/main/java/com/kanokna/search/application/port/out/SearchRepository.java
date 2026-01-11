package com.kanokna.search.application.port.out;

import com.kanokna.search.application.dto.BulkIndexResult;
import com.kanokna.search.application.dto.DeleteResult;
import com.kanokna.search.application.dto.FacetValuesResult;
import com.kanokna.search.application.dto.GetFacetValuesQuery;
import com.kanokna.search.application.dto.IndexResult;
import com.kanokna.search.domain.model.AutocompleteQuery;
import com.kanokna.search.domain.model.AutocompleteResult;
import com.kanokna.search.domain.model.ProductSearchDocument;
import com.kanokna.search.domain.model.SearchQuery;
import com.kanokna.search.domain.model.SearchResult;

import java.util.List;

/**
 * Outbound port for Elasticsearch search operations.
 */
public interface SearchRepository {
    SearchResult search(SearchQuery query);

    AutocompleteResult autocomplete(AutocompleteQuery query);

    FacetValuesResult facetValues(GetFacetValuesQuery query);

    ProductSearchDocument getById(String productId);

    IndexResult index(ProductSearchDocument document);

    DeleteResult delete(String productId);

    BulkIndexResult bulkIndex(String indexName, List<ProductSearchDocument> documents);
}
