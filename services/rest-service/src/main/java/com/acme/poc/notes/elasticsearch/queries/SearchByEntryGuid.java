package com.acme.poc.notes.elasticsearch.queries;

import com.acme.poc.notes.elasticsearch.queries.generics.AbstractQuery;
import com.acme.poc.notes.elasticsearch.queries.generics.enums.EsNotesFields;
import lombok.experimental.SuperBuilder;

/**
 * Search query to entries by entry guid.
 */
@SuperBuilder
public class SearchByEntryGuid extends AbstractQuery {
    @Override
    public String buildQuery() {
        return String.format(QUERY, EsNotesFields.ENTRY.getEsFieldName(),searchGuid,createdDateTime);
    }
}
