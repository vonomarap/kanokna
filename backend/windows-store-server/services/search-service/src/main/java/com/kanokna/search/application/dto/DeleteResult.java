package com.kanokna.search.application.dto;

/**
 * Result of a delete operation.
 */
public class DeleteResult {
    private final boolean deleted;

    public DeleteResult(boolean deleted) {
        this.deleted = deleted;
    }

    public boolean isDeleted() {
        return deleted;
    }
}
