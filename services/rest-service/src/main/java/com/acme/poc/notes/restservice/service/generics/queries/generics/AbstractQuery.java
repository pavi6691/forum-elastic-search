package com.acme.poc.notes.restservice.service.generics.queries.generics;

import com.acme.poc.notes.restservice.service.generics.abstracts.AbstractNotesProcessor;
import com.acme.poc.notes.restservice.service.generics.queries.generics.enums.NotesFields;
import com.acme.poc.notes.restservice.service.generics.queries.generics.enums.ResultFormat;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.elasticsearch.search.sort.SortOrder;

import java.util.Iterator;


/**
 * Abstraction to search by any fields. Returns entries created after passed time in millis.
 * Ability to filter out historical/archived records or to select both.
 * Filter is not done on elastic search but in {@link AbstractNotesProcessor#process(IQuery, Iterator)}
 */
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public abstract class AbstractQuery implements IQuery {

    protected static final String QUERY = """
            {
                  "bool": {
                        "must": [
                            {
                                "match_phrase": {
                                    "%s": "%s"
                                }
                            },
                            {
                                "range": {
                                    "{FIELDNAME}": {
                                        "gte": "%s"
                                    }
                                }
                            }
                        ]
                  }
            }
            """
            .replace("{FIELDNAME}", NotesFields.CREATED.getEsFieldName());

    @Getter
    protected NotesFields searchField = NotesFields.ENTRY;
    @Getter
    protected String searchGuid;
    @Getter
    protected long createdDateTime;
    @Getter
    protected boolean includeVersions;
    @Getter @Setter
    protected boolean includeArchived = true;
    @Getter
    protected Object searchAfter;
    @Getter
    protected int size;
    @Getter @Builder.Default
    protected SortOrder sortOrder = SortOrder.ASC;
    @Getter @Setter @Builder.Default
    protected ResultFormat resultFormat = ResultFormat.TREE;


    @Override
    public boolean includeVersions() {
        return includeVersions;
    }

    @Override
    public boolean includeArchived() {
        return includeArchived;
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
    public void searchAfter(Object sortValues) {
        this.searchAfter = sortValues;
    }

}
