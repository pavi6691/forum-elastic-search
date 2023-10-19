package com.freelance.forum.elasticsearch.pojo;

import com.freelance.forum.elasticsearch.queries.ESIndexNotesFields;
import org.elasticsearch.search.sort.SortOrder;

public class Request {
    
    private String search;
    private boolean isUpdateHistory;
    private boolean isArchivedResponse;
    private boolean isSearchThreads;
    private ESIndexNotesFields esIndexNotesField;

    public Request(String search, boolean isUpdateHistory, boolean isArchivedResponse, 
                   boolean isSearchThreads, SortOrder sortOrder, ESIndexNotesFields esIndexNotesFields) {
        this.search = search;
        this.isUpdateHistory = isUpdateHistory;
        this.isArchivedResponse = isArchivedResponse;
        this.isSearchThreads = isSearchThreads;
        this.esIndexNotesField = esIndexNotesFields;
    }

    public String getSearch() {
        return search;
    }

    public boolean isUpdateHistory() {
        return isUpdateHistory;
    }

    public boolean isArchivedResponse() {
        return isArchivedResponse;
    }

    public boolean isSearchThreads() {
        return isSearchThreads;
    }

    public ESIndexNotesFields getEsIndexNotesField() {
        return esIndexNotesField;
    }

    public static class Builder {

        private String search;
        private boolean isUpdateHistory;
        private boolean isArchivedResponse;
        private boolean isSearchThreads;
        private SortOrder sortOrder;
        private ESIndexNotesFields esIndexNotesFields;

        public Builder setSearch(String search) {
            this.search = search;
            return this;
        }

        public Builder setUpdateHistory(boolean isUpdateHistory) {
            this.isUpdateHistory = isUpdateHistory;
            return this;
        }

        public Builder setArchivedResponse(boolean isArchivedResponse) {
            this.isArchivedResponse = isArchivedResponse;
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

        public Builder setEsIndexNotesFields(ESIndexNotesFields esIndexNotesFields) {
            this.esIndexNotesFields = esIndexNotesFields;
            return this;
        }

        public Request build() {
            return new Request(search,isUpdateHistory,isArchivedResponse,isSearchThreads,sortOrder,esIndexNotesFields);
        }
    }
}
