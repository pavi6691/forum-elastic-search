package com.acme.poc.notes.restservice.service.generics.queries.generics;

import com.acme.poc.notes.restservice.service.generics.queries.generics.enums.ResultFormat;
import org.elasticsearch.search.sort.SortOrder;


public interface IQuery {

    String buildQuery();
    boolean includeVersions();
    boolean includeArchived();
    void searchAfter(Object sortValues);
    Object searchAfter();
    int getSize();
    SortOrder getSortOrder();
    ResultFormat getResultFormat();
    void setResultFormat(ResultFormat resultFormat);
    void setIncludeArchived(boolean includeArchived);

}
