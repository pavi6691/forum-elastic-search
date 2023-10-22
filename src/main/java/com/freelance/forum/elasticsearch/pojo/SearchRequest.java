package com.freelance.forum.elasticsearch.pojo;

import com.freelance.forum.elasticsearch.queries.ESIndexNotesFields;
import com.freelance.forum.elasticsearch.queries.RequestType;
import org.elasticsearch.search.sort.SortOrder;

public class SearchRequest {
    
    private String search;
    private boolean updateHistory;
    private boolean archivedResponse;
    private boolean threads;
    private ESIndexNotesFields searchField;
    private RequestType requestType;
    private long timeToSearchEntriesAfter;
    private SortOrder sortOrder;

    public SearchRequest(String search, boolean getUpdateHistory, boolean getArchivedResponse,
                         boolean isSearchThreads, SortOrder sortOrder, ESIndexNotesFields searchField,
                         RequestType requestType, long timeToSearchEntriesAfter) {
        this.search = search;
        this.updateHistory = getUpdateHistory;
        this.archivedResponse = getArchivedResponse;
        this.threads = isSearchThreads;
        this.searchField = searchField;
        this.sortOrder = sortOrder;
        this.requestType = requestType;
        this.timeToSearchEntriesAfter = timeToSearchEntriesAfter;
    }

    public String getSearch() {
        return search;
    }

    public boolean getUpdateHistory() {
        return updateHistory;
    }

    public boolean getArchivedResponse() {
        return archivedResponse;
    }

    public boolean getThreads() {
        return threads;
    }

    public ESIndexNotesFields getSearchField() {
        return searchField;
    }

    public SortOrder getSortOrder() {
        return sortOrder;
    }

    public long getTimeToSearchEntriesAfter() {
        return timeToSearchEntriesAfter;
    }

    public RequestType getRequestType() {
        return requestType;
    }

    public static class Builder {

        private String search;
        private boolean getUpdateHistory;
        private boolean getArchivedResponse;
        private boolean isSearchThreads;
        private SortOrder sortOrder;
        private ESIndexNotesFields searchField;
        private RequestType requestType;
        
        private long timeToSearchEntriesAfter;

        public Builder setSearch(String search) {
            this.search = search;
            return this;
        }

        public Builder setSearchHistory(boolean getUpdateHistory) {
            this.getUpdateHistory = getUpdateHistory;
            return this;
        }

        public Builder setSearchArchived(boolean getArchivedResponse) {
            this.getArchivedResponse = getArchivedResponse;
            return this;
        }

        public Builder setSearchThreads(boolean isSearchThreads) {
            this.isSearchThreads = isSearchThreads;
            return this;
        }

        public Builder setSortOrder(SortOrder sortOrder) {
            this.sortOrder = sortOrder;
            return this;
        }

        public Builder setTimeToSearchEntriesAfter(long timeToSearchEntriesAfter) {
            this.timeToSearchEntriesAfter = timeToSearchEntriesAfter;
            return this;
        }

        public Builder setSearchField(ESIndexNotesFields searchField) {
            this.searchField = searchField;
            return this;
        }
        
        public Builder setRequestType(RequestType requestType) {
            this.requestType = requestType;
            return this;
        }

        public SearchRequest build() {
            return new SearchRequest(search,getUpdateHistory,getArchivedResponse,isSearchThreads,
                    sortOrder,searchField,requestType,timeToSearchEntriesAfter);
        }
    }
}
