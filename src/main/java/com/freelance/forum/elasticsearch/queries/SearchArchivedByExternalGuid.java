package com.freelance.forum.elasticsearch.queries;

import com.freelance.forum.elasticsearch.queries.generics.AbstractQuery;
import com.freelance.forum.elasticsearch.queries.generics.enums.EsNotesFields;
import com.freelance.forum.elasticsearch.queries.generics.IQuery;

import java.util.Iterator;

/**
 * Query to search archived entries by External Guid.
 * filter is done in {@link com.freelance.forum.elasticsearch.generics.AbstractSearchNotes#process(IQuery, Iterator)}
 * For any entryGuid(not archived), if their threads are archived, then return them too
 */
public class SearchArchivedByExternalGuid extends AbstractQuery {
    private static String QUERY = "{\n" +
            "  \"bool\": {\n" +
            "    \"must\": [\n" +
            "      {\n" +
            "        \"match_phrase\": {\n" +
            "          \""+ EsNotesFields.EXTERNAL.getEsFieldName()+"\": \"%s\"\n" +
            "        }\n" +
            "      }\n" +
            "    ],\n" +
            "    \"filter\": [\n" +
            "      {\n" +
            "        \"exists\": {\n" +
            "          \"field\": \"archived\"\n" +
            "        }\n" +
            "      }\n" +
            "    ]\n" +
            "  }\n" +
            "}";

    private String externalGuid;
    private boolean getUpdateHistory;

    public SearchArchivedByExternalGuid setExternalGuid(String externalGuid) {
        this.externalGuid = externalGuid;
        return this;
    }

    public SearchArchivedByExternalGuid setGetUpdateHistory(boolean getUpdateHistory) {
        this.getUpdateHistory = getUpdateHistory;
        return this;
    }

    @Override
    public boolean getArchived() {
        return true;
    }
    
    @Override
    public String buildQuery() {
        return String.format(QUERY,externalGuid);
    }

    @Override
    public boolean getUpdateHistory() {
        return getUpdateHistory;
    }
}
