package com.kanokna.search.application.port.in;

import com.kanokna.search.domain.model.AutocompleteQuery;
import com.kanokna.search.domain.model.AutocompleteResult;

/**
 * Inbound port for autocomplete suggestions.
 */
public interface AutocompleteUseCase {
    AutocompleteResult autocomplete(AutocompleteQuery query);
}
