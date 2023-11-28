package com.acme.poc.notes.restservice.service.generics.queries;

import com.acme.poc.notes.restservice.service.generics.queries.generics.AbstractQuery;
import com.acme.poc.notes.restservice.service.generics.queries.generics.enums.NotesFields;
import lombok.experimental.SuperBuilder;


/**
 * Search query to fetch root entries by NotesFields.PARENT_ENTRY guid
 */
@SuperBuilder
public class SearchByParentEntryGuid extends AbstractQuery {


    @Override
    public String buildQuery() {
        return String.format(QUERY, NotesFields.PARENT_ENTRY.getEsFieldName(), searchGuid, createdDateTime);
    }

}
