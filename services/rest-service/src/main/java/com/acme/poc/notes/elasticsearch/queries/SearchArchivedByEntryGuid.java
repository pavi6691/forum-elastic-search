package com.acme.poc.notes.elasticsearch.queries;

import com.acme.poc.notes.elasticsearch.generics.AbstractNotesProcessor;
import com.acme.poc.notes.elasticsearch.queries.generics.AbstractQuery;
import com.acme.poc.notes.elasticsearch.queries.generics.IQuery;
import com.acme.poc.notes.elasticsearch.queries.generics.enums.EsNotesFields;
import lombok.experimental.SuperBuilder;

import java.util.Iterator;

/**
 * Query to search archived entries by entryGuid.
 * filter is done in {@link AbstractNotesProcessor#process(IQuery, Iterator)}
 * For any entryGuid(not archived), if their threads are archived, then return them too
 */
@SuperBuilder
public class SearchArchivedByEntryGuid extends AbstractQuery {
    private static String QUERY = "{\n" +
            "  \"bool\": {\n" +
            "    \"must\": [\n" +
            "      {\n" +
            "        \"match_phrase\": {\n" +
            "          \""+ EsNotesFields.ENTRY.getEsFieldName()+"\": \"%s\"\n" +
            "        }\n" +
            "      }\n" +
            "    ]\n" +
            "  }\n" +
            "}";
    
    @Override
    public boolean getArchived() {
        return true;
    }

    @Override
    public String buildQuery() {
        return String.format(QUERY,searchGuid);
    }

    @Override
    public boolean getUpdateHistory() {
        return getUpdateHistory;
    }
}
