package com.acme.poc.notes.restservice.generics.queries;

import com.acme.poc.notes.models.NoteSortOrder;
import com.acme.poc.notes.restservice.generics.queries.enums.OperationStatus;
import com.acme.poc.notes.restservice.generics.queries.enums.ResultFormat;
import com.acme.poc.notes.restservice.generics.abstracts.AbstractNotesProcessor;
import com.acme.poc.notes.restservice.generics.queries.enums.Filter;
import com.acme.poc.notes.restservice.generics.queries.enums.Field;
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
    private Field searchField = Field.ENTRY;
    @Getter
    private String searchData;
    @Getter
    private long createdDateTime;
    @Getter @Singular
    private Set<Filter> filters = new HashSet<>();
    @Getter @Setter
    private Object searchAfter; // used for elasticsearch pagination
    @Getter @Setter
    private boolean allEntries = false; // default response size will be @Value("${default.db.response.size}")
    @Getter @Setter
    private int size;
    @Getter @Builder.Default
    private NoteSortOrder sortOrder = NoteSortOrder.ASCENDING;
    @Getter @Setter @Builder.Default
    private ResultFormat resultFormat = ResultFormat.TREE;
    public Set<Filter> getFilters() {
        filters = new HashSet<>(filters);
        return filters;
    }
    @Override
    public List<OperationStatus> getOperationStatuses() {
        List<OperationStatus> operationStatuses;
        if (filters.contains(Filter.INCLUDE_SOFT_DELETED)) {
            operationStatuses = List.of(OperationStatus.MARK_FOR_SOFT_DELETE,OperationStatus.SOFT_DELETED,OperationStatus.ACTIVE,OperationStatus.UPSERT);
        } else if (filters.contains(Filter.ONLY_SOFT_DELETED)) {
            operationStatuses = List.of(OperationStatus.MARK_FOR_SOFT_DELETE,OperationStatus.SOFT_DELETED);
        } else {
            operationStatuses = List.of(OperationStatus.ACTIVE,OperationStatus.UPSERT);
        }
        return operationStatuses;
    }
}
