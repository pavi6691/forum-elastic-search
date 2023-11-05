package com.acme.poc.notes.elasticsearch.queries;

import com.acme.poc.notes.elasticsearch.queries.generics.AbstractQuery;
import com.acme.poc.notes.elasticsearch.queries.generics.enums.EsNotesFields;

/**
 * Search query to fetch root entries by EsNotesFields.PARENT_THREAD guid
 */
public class SearchByParentThreadGuid extends AbstractQuery {
    @Override
    public String buildQuery() {
        return String.format(QUERY, EsNotesFields.PARENT_THREAD.getEsFieldName(),guid,createdDateTime);
    }
}
