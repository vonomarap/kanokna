package com.kanokna.search.application.port.in;

import com.kanokna.search.application.dto.FacetValuesResult;
import com.kanokna.search.application.dto.GetFacetValuesQuery;

/**
 * Inbound port for facet values retrieval.
 */
public interface GetFacetValuesUseCase {
    FacetValuesResult getFacetValues(GetFacetValuesQuery query);
}
