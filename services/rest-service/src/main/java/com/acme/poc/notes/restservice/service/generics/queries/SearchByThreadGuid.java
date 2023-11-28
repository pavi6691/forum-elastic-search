package com.acme.poc.notes.restservice.service.generics.queries;

import com.acme.poc.notes.restservice.service.generics.queries.generics.AbstractQuery;
import com.acme.poc.notes.restservice.service.generics.queries.generics.enums.NotesFields;
import lombok.experimental.SuperBuilder;


/**
 * Search query to fetch root entries by NotesFields.THREAD.
 */
@SuperBuilder
public class SearchByThreadGuid extends AbstractQuery {


    @Override
    public String buildQuery() {
        return String.format(QUERY, NotesFields.THREAD.getEsFieldName(), searchGuid, createdDateTime);
    }

}
