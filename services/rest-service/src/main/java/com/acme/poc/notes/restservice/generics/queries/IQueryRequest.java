package com.acme.poc.notes.restservice.generics.queries;

import com.acme.poc.notes.models.NoteSortOrder;
import com.acme.poc.notes.restservice.generics.queries.enums.ResultFormat;
import com.acme.poc.notes.restservice.generics.queries.enums.Filter;
import com.acme.poc.notes.restservice.generics.queries.enums.Match;
import java.util.Set;


public interface IQueryRequest {
    Set<Filter> getFilters();
    void searchAfter(Object sortValues);
    Object searchAfter();
    int getSize();
    NoteSortOrder getSortOrder();
    ResultFormat getResultFormat();
    void setResultFormat(ResultFormat resultFormat);
    Match getSearchField();
    String getSearchData();
    long getCreatedDateTime();
    
}
