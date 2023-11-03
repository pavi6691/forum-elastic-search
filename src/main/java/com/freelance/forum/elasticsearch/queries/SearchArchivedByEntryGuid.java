package com.freelance.forum.elasticsearch.queries;

import com.freelance.forum.elasticsearch.generics.AbstractNotesOperations;
import com.freelance.forum.elasticsearch.queries.generics.AbstractQuery;
import com.freelance.forum.elasticsearch.queries.generics.enums.EsNotesFields;
import com.freelance.forum.elasticsearch.queries.generics.IQuery;

import java.util.Iterator;

/**
 * Query to search archived entries by entryGuid.
 * filter is done in {@link AbstractNotesOperations#process(IQuery, Iterator)}
 * For any entryGuid(not archived), if their threads are archived, then return them too
 */
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
        return String.format(QUERY,guid);
    }

    @Override
    public boolean getUpdateHistory() {
        return getUpdateHistory;
    }
}
