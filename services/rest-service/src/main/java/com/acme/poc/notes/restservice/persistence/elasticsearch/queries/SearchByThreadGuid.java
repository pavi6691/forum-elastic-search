package com.acme.poc.notes.restservice.persistence.elasticsearch.queries;

import com.acme.poc.notes.restservice.persistence.elasticsearch.queries.generics.AbstractQuery;
import com.acme.poc.notes.restservice.persistence.elasticsearch.queries.generics.enums.EsNotesFields;
import lombok.experimental.SuperBuilder;

/**
 * Search query to fetch root entries by EsNotesFields.THREAD.
 */
@SuperBuilder
public class SearchByThreadGuid extends AbstractQuery {
    @Override
    public String buildQuery() {
        return String.format(QUERY, EsNotesFields.THREAD.getEsFieldName(),searchGuid,createdDateTime);
    }
}
