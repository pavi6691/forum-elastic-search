package com.acme.poc.notes.elasticsearch.queries.generics;

import com.acme.poc.notes.elasticsearch.generics.AbstractNotesOperations;
import com.acme.poc.notes.elasticsearch.queries.generics.enums.EsNotesFields;
import org.elasticsearch.search.sort.SortOrder;

import java.util.Iterator;

/**
 * Abstraction to search by any fields. returns entries created after passed time in millis.
 * ability to filter out historical/archived records or to select both. 
 * filter is not done on elastic search but in {@link AbstractNotesOperations#process(IQuery, Iterator)}
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
            "          \""+ EsNotesFields.CREATED.getEsFieldName()+"\": {\n" +
            "            \"gte\": \"%s\"\n" +
            "          }\n" +
            "        }\n" +
            "      }\n" +
            "    ]\n" +
            "  }\n" +
            "}";
    protected EsNotesFields searchField = EsNotesFields.ENTRY;
    protected String guid;
    protected long createdDateTime;
    protected boolean getUpdateHistory;
    protected boolean getArchived = true;
    protected Object searchAfter;
    protected int size;
    protected SortOrder sortOrder = SortOrder.ASC;

    public AbstractQuery setGetUpdateHistory(boolean getUpdateHistory) {
        this.getUpdateHistory = getUpdateHistory;
        return this;
    }
    public AbstractQuery setSearchAfter(Object searchAfter) {
        this.searchAfter = searchAfter;
        return this;
    }

    public AbstractQuery setGetArchived(boolean getArchived) {
        this.getArchived = getArchived;
        return this;
    }

    public AbstractQuery setSize(int size) {
        this.size = size;
        return this;
    }

    public AbstractQuery setSortOrder(SortOrder sortOrder) {
        this.sortOrder = sortOrder;
        return this;
    }

    @Override
    public SortOrder getSortOrder() {
        return sortOrder;
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public boolean getUpdateHistory() {
        return getUpdateHistory;
    }

    @Override
    public boolean getArchived() {
        return getArchived;
    }

    @Override
    public Object searchAfter() {
        return searchAfter;
    }

    @Override
    public String getSearchId() {
        return guid;
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
