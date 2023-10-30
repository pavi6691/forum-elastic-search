package com.freelance.forum.elasticsearch.queries.generics;

import java.util.Iterator;

/**
 * Abstraction to search by any fields. returns entries created after passed time in millis.
 * ability to filter out historical/archived records or to select both. 
 * filter is not done on elastic search but in {@link com.freelance.forum.elasticsearch.generics.AbstractSearchNotes#process(IQuery, Iterator)}
 */
public abstract class AbstractQuery implements IQuery {
    protected static String QUERY = "{\n" +
            "  \"bool\": {\n" +
            "    \"must\": [\n" +
            "      {\n" +
            "        \"match_phrase\": {\n" +
            "          \"%s\": \"%s\"\n" +
            "        }\n" +
            "      },\n" +
            "      {\n" +
            "        \"range\": {\n" +
            "          \""+ESIndexNotesFields.CREATED.getEsFieldName()+"\": {\n" +
            "            \"gte\": \"%s\"\n" +
            "          }\n" +
            "        }\n" +
            "      }\n" +
            "    ]\n" +
            "  }\n" +
            "}";
    protected ESIndexNotesFields searchField = ESIndexNotesFields.ENTRY;
    protected String guid;
    protected long createdDateTime;
    protected boolean getUpdateHistory;
    protected boolean getArchived;

    public AbstractQuery setGetUpdateHistory(boolean getUpdateHistory) {
        this.getUpdateHistory = getUpdateHistory;
        return this;
    }

    public AbstractQuery setGetArchived(boolean getArchived) {
        this.getArchived = getArchived;
        return this;
    }

    @Override
    public boolean getUpdateHistory() {
        return getUpdateHistory;
    }

    @Override
    public boolean getArchived() {
        return getArchived;
    }

    public AbstractQuery setSearchBy(String guid) {
        this.guid = guid;
        return this;
    }

    public AbstractQuery setCreatedDateTime(long createdDateTime) {
        this.createdDateTime = createdDateTime;
        return this;
    }

    @Override
    public String buildQuery() {
        return String.format(QUERY,searchField.getEsFieldName(),guid,createdDateTime);
    }
}
