package com.freelance.forum.elasticsearch.queries;


import com.freelance.forum.elasticsearch.queries.generics.AbstractQuery;
import com.freelance.forum.elasticsearch.queries.generics.enums.EsNotesFields;

/**
 * Search query to entries by entry guid.
 */
public class SearchByEntryGuid extends AbstractQuery {
    @Override
    public String buildQuery() {
        return String.format(QUERY, EsNotesFields.ENTRY.getEsFieldName(),guid,createdDateTime);
    }
}
