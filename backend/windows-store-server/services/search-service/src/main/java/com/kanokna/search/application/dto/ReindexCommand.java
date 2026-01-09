package com.kanokna.search.application.dto;

/**
 * Command for reindexing the catalog.
 */
public class ReindexCommand {
    private final String sourceIndex;

    public ReindexCommand(String sourceIndex) {
        this.sourceIndex = sourceIndex;
    }

    public String getSourceIndex() {
        return sourceIndex;
    }
}
