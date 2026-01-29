package com.kanokna.search.adapters.out.elasticsearch;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.kanokna.search.application.port.out.SearchIndexAdminPort;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;

/**
 * Elasticsearch adapter for index administration operations.
 */
@Component
public class ElasticsearchIndexAdminAdapter implements SearchIndexAdminPort {
    private final ElasticsearchClient client;

    public ElasticsearchIndexAdminAdapter(ElasticsearchClient client) {
        this.client = client;
    }

    @Override
    public boolean indexExists(String indexName) {
        try {
            return client.indices().exists(e -> e.index(indexName)).value();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void createIndex(String indexName) {
        try {
            client.indices().create(c -> c
                .index(indexName)
                .settings(s -> s
                    .numberOfShards("1")
                    .numberOfReplicas("1")
                    .refreshInterval(t -> t.time("1s")))
                .mappings(m -> m
                    .properties("id", p -> p.keyword(k -> k))
                    .properties("name", p -> p.text(t -> t
                        .fields("keyword", f -> f.keyword(k -> k))))
                    .properties("description", p -> p.text(t -> t))
                    .properties("family", p -> p.keyword(k -> k))
                    .properties("profileSystem", p -> p.keyword(k -> k))
                    .properties("openingTypes", p -> p.keyword(k -> k))
                    .properties("materials", p -> p.keyword(k -> k))
                    .properties("colors", p -> p.keyword(k -> k))
                    .properties("minPrice", p -> p.long_(l -> l))
                    .properties("maxPrice", p -> p.long_(l -> l))
                    .properties("currency", p -> p.keyword(k -> k))
                    .properties("popularity", p -> p.integer(i -> i))
                    .properties("status", p -> p.keyword(k -> k))
                    .properties("publishedAt", p -> p.date(d -> d.format("strict_date_optional_time")))
                    .properties("thumbnailUrl", p -> p.keyword(k -> k.index(false)))
                    .properties("optionCount", p -> p.integer(i -> i))
                    .properties("suggest", p -> p.completion(cmp -> cmp
                        .contexts(ctx -> ctx.name("family").type("category").path("family"))))));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public List<String> resolveAlias(String alias) {
        try {
            Map<String, ?> result = client.indices().getAlias(a -> a.name(alias)).result();
            return new ArrayList<>(result.keySet());
        } catch (ElasticsearchException ex) {
            if (ex.status() == 404) {
                return List.of();
            }
            throw ex;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void swapAlias(String alias, String newIndex, List<String> removeIndices) {
        try {
            client.indices().updateAliases(a -> {
                if (removeIndices != null) {
                    for (String index : removeIndices) {
                        if (index != null && !index.isBlank()) {
                            a.actions(action -> action.remove(r -> r.index(index).alias(alias)));
                        }
                    }
                }
                a.actions(action -> action.add(add -> add.index(newIndex).alias(alias)));
                return a;
            });
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
