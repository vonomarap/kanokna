package com.kanokna.search.domain.model;

import com.kanokna.search.adapters.config.SearchProperties;
import com.kanokna.search.application.port.out.CatalogConfigurationPort;
import com.kanokna.search.application.port.out.DistributedLockPort;
import com.kanokna.search.application.port.out.SearchIndexAdminPort;
import com.kanokna.search.application.port.out.SearchRepository;
import com.kanokna.search.application.service.SearchApplicationService;
import com.kanokna.shared.core.DomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class AutocompleteQueryTest {
    @Mock
    private SearchRepository searchRepository;

    @Mock
    private SearchIndexAdminPort searchIndexAdminPort;

    @Mock
    private CatalogConfigurationPort catalogConfigurationPort;

    @Mock
    private DistributedLockPort distributedLockPort;

    private SearchApplicationService service;

    @BeforeEach
    void setUp() {
        service = new SearchApplicationService(
            searchRepository,
            searchIndexAdminPort,
            catalogConfigurationPort,
            distributedLockPort,
            new SearchProperties(null, null)
        );
    }

    @Test
    @DisplayName("TC-FUNC-AUTO-002: Prefix less than 2 chars returns error")
    void shortPrefix_throwsDomainException() {
        AutocompleteQuery query = new AutocompleteQuery("a", 10, null, null);

        DomainException ex = assertThrows(DomainException.class, () -> service.autocomplete(query));

        assertEquals("ERR-AUTO-PREFIX-TOO-SHORT", ex.getCode());
    }
}
