package com.acme.poc.notes.elasticsearch.queries.generics;

import com.acme.poc.notes.elasticsearch.generics.AbstractNotesProcessor;
import com.acme.poc.notes.elasticsearch.queries.generics.enums.EsNotesFields;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.elasticsearch.search.sort.SortOrder;

import java.util.Iterator;

/**
 * Abstraction to search by any fields. returns entries created after passed time in millis.
 * ability to filter out historical/archived records or to select both. 
 * filter is not done on elastic search but in {@link AbstractNotesProcessor#process(IQuery, Iterator)}
 */
@AllArgsConstructor
@SuperBuilder
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
    @Getter
    protected EsNotesFields searchField = EsNotesFields.ENTRY;
    @Getter
    protected String searchGuid;
    @Getter
    protected long createdDateTime;
    @Getter
    protected boolean getUpdateHistory;
    @Getter
    protected boolean getArchived = true;
    @Getter
    protected Object searchAfter;
    @Getter
    protected int size;
    @Getter
    protected SortOrder sortOrder = SortOrder.ASC;

    @Override
    public boolean getUpdateHistory() {
        return getUpdateHistory;
    }

    @Override
    public boolean getArchived() {
        return getArchived;
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
    public Object searchAfter() {
        return searchAfter;
    }
    
    @Override
    public String buildQuery() {
        return String.format(QUERY,searchField.getEsFieldName(),searchGuid,createdDateTime);
    }
}
