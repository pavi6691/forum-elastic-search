package com.acme.poc.notes.restservice.service.generics.queries;

import com.acme.poc.notes.restservice.service.generics.queries.enums.Filter;
import com.acme.poc.notes.restservice.service.generics.queries.enums.Match;
import com.acme.poc.notes.restservice.service.generics.queries.enums.ResultFormat;
import org.elasticsearch.search.sort.SortOrder;

import java.util.Set;


public interface IQueryRequest {
    Set<Filter> getFilters();
    void searchAfter(Object sortValues);
    Object searchAfter();
    int getSize();
    SortOrder getSortOrder();
    ResultFormat getResultFormat();
    void setResultFormat(ResultFormat resultFormat);
    Match getSearchField();
    String getSearchData();
    long getCreatedDateTime();
    
}
