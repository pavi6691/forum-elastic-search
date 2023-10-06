package com.freelance.forum.elasticsearch.pojo;

public class DeleteResponse {
    private ElasticDataModel data;
    private int totalRecordsDeleted = 0;

    public DeleteResponse(ElasticDataModel data, int totalRecordsDeleted) {
        this.data = data;
        this.totalRecordsDeleted = totalRecordsDeleted;
    }
}
