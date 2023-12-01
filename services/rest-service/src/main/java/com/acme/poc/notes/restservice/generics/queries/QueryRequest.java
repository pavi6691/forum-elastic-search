package com.acme.poc.notes.restservice.generics.queries;

import com.acme.poc.notes.models.NoteSortOrder;
import com.acme.poc.notes.restservice.generics.queries.enums.ResultFormat;
import com.acme.poc.notes.restservice.generics.abstracts.AbstractNotesProcessor;
import com.acme.poc.notes.restservice.generics.queries.enums.Filter;
import com.acme.poc.notes.restservice.generics.queries.enums.Match;
import lombok.*;
import java.util.*;


/**
 * Abstraction to search by any fields. Returns entries created after passed time in millis.
 * Ability to filter out historical/archived records or to select both.
 * Filter is not done on database but in {@link AbstractNotesProcessor#process(IQueryRequest, Iterator)}
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QueryRequest implements IQueryRequest {

    @Getter
    private Match searchField = Match.ENTRY;
    @Getter
    private String searchData;
    @Getter
    private long createdDateTime;
    @Getter @Singular
    private Set<Filter> filters = new HashSet<>();
    @Getter
    private Object searchAfter;
    @Getter
    private int size;
    @Getter @Builder.Default
    private NoteSortOrder sortOrder = NoteSortOrder.ASCENDING;
    @Getter @Setter @Builder.Default
    private ResultFormat resultFormat = ResultFormat.TREE;
    @Override
    public NoteSortOrder getSortOrder() {
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
    public Set<Filter> getFilters() {
        filters = new HashSet<>(filters);
        return filters;
    }
}
