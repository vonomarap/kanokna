package com.kanokna.search.adapters.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for search-service.
 */
@ConfigurationProperties(prefix = "search")
public class SearchProperties {
    private final Index index = new Index();
    private final Reindex reindex = new Reindex();

    public Index getIndex() {
        return index;
    }

    public Reindex getReindex() {
        return reindex;
    }

    public static class Index {
        private String name = "product_templates";
        private String alias = "product_templates";
        private String versionPrefix = "product_templates_v";

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getAlias() {
            return alias;
        }

        public void setAlias(String alias) {
            this.alias = alias;
        }

        public String getVersionPrefix() {
            return versionPrefix;
        }

        public void setVersionPrefix(String versionPrefix) {
            this.versionPrefix = versionPrefix;
        }
    }

    public static class Reindex {
        private int batchSize = 200;
        private String lockName = "search-service:reindex-lock";

        public int getBatchSize() {
            return batchSize;
        }

        public void setBatchSize(int batchSize) {
            this.batchSize = batchSize;
        }

        public String getLockName() {
            return lockName;
        }

        public void setLockName(String lockName) {
            this.lockName = lockName;
        }
    }
}
