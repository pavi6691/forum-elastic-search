package com.acme.poc.notes.core;

import java.util.concurrent.TimeUnit;

public class NotesConstants {

    public static final int DIRTY_NOTES_PROCESSOR_JOB_SCHEDULE = 5/*seconds*/ * 1000/*millis*/; // Run every 30 seconds
    // Timestamps
    public static final String TIMESTAMP_ISO8601 = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX";

    // Timeouts
    public static final long TIMEOUT_ARCHIVE = TimeUnit.SECONDS.toMillis(10);
    public static final long TIMEOUT_DELETE = TimeUnit.SECONDS.toMillis(10);
    
    //PostgreSQL 
    public static final String POSTGRESQL_NOTES_TAG = "Notes API";
    public static final String POSTGRESQL_NOTES_ADMIN_TAG = "Notes ADMIN API";

    // OpenAPI
    public static final String OPENAPI_NOTES_TAG = "ES Notes API";
    public static final String OPENAPI_ADMIN_TAG = "ES Notes Admin API";
    public static final String OPENAPI_DEVOPS_TAG = "Notes DevOps API";
    public static final String OPENAPI_ELASTICSEARCH_ADMIN_TAG = "Notes Elasticsearch Admin API";

    // REST path parameters
    public static final String API_ENDPOINT_PATH_PARAMETER_GUID = "guid";
    public static final String API_ENDPOINT_PATH_PARAMETER_EXTERNAL_GUID= "externalGuid";
    public static final String API_ENDPOINT_PATH_PARAMETER_ENTRY_GUID = "entryGuid";
    public static final String API_ENDPOINT_PATH_PARAMETER_THREAD_GUID = "threadGuid";

    // REST query string parameters
    public static final String API_ENDPOINT_QUERY_PARAMETER_INDEX_NAME = "indexName";
    public static final String API_ENDPOINT_QUERY_PARAMETER_INCLUDE_VERSIONS = "includeVersions";
    public static final String API_ENDPOINT_QUERY_PARAMETER_INCLUDE_ARCHIVED = "includeArchived";
    public static final String API_ENDPOINT_QUERY_PARAMETER_INCLUDE_DELETED = "includeDeleted";

    // REST API endpoints
    public static final String API_ENDPOINT_PREFIX = "/api/v1";
    public static final String API_ENDPOINT_DELETE = "/DELETE";
    public static final String API_ENDPOINT_DEVOPS = "/devops";
    public static final String API_ENDPOINT_NOTES_ES_ADMIN = "/admin/notes/es";
    public static final String API_ENDPOINT_NOTES_ES_USER = "/notes/es";
    public static final String API_ENDPOINT_NOTES_PG_ADMIN = "/admin/notes/pg";
    public static final String API_ENDPOINT_NOTES_PG_USER = "/notes/pg";

    //
    // /api/v1/devops/...
    //
    public static final String API_ENDPOINT_DEVOPS_ERRORS = "/errors";

    //
    // /api/v1/admin/...
    //
    public static final String API_ENDPOINT_ADMIN_GET_ALL = "";
    public static final String API_ENDPOINT_ADMIN_GET_ALL_BY_EXTERNAL_GUID = "/externalguid/{" + API_ENDPOINT_PATH_PARAMETER_EXTERNAL_GUID + "}";
    public static final String API_ENDPOINT_ADMIN_DELETE_BY_EXTERNAL_GUID = "/externalguid/{" + API_ENDPOINT_PATH_PARAMETER_EXTERNAL_GUID + "}";
    public static final String API_ENDPOINT_ADMIN_DELETE_BY_ENTRY_GUID = "/entryguid/{" + API_ENDPOINT_PATH_PARAMETER_ENTRY_GUID + "}";
    public static final String API_ENDPOINT_ADMIN_DELETE_BY_THREAD_GUID = "/threadguid/{" + API_ENDPOINT_PATH_PARAMETER_THREAD_GUID + "}";

    //
    // /api/v1/admin/elasticsearch/...
    //
    public static final String API_ENDPOINT_ADMIN_ES = "/elasticsearch";
    // GET
    public static final String API_ENDPOINT_ADMIN_ES_INDEX_CREATE = "/index/create";

    //
    // /api/v1/notes/...
    // POST
    public static final String API_ENDPOINT_NOTES_CREATE = "";
    // GET
    public static final String API_ENDPOINT_NOTES_GET_BY_GUID = "/guid/{" + API_ENDPOINT_PATH_PARAMETER_GUID + "}";
    public static final String API_ENDPOINT_NOTES_GET_BY_EXTERNAL_GUID = "/externalguid/{" + API_ENDPOINT_PATH_PARAMETER_EXTERNAL_GUID + "}";
    public static final String API_ENDPOINT_NOTES_GET_BY_ENTRY_GUID = "/entryguid/{" + API_ENDPOINT_PATH_PARAMETER_ENTRY_GUID + "}";
    public static final String API_ENDPOINT_NOTES_SEARCH_CONTENT = "/search";
    public static final String API_ENDPOINT_NOTES_SEARCH_ARCHIVED_BY_ENTRY_GUID = "/search/archived/entryguid/{" + API_ENDPOINT_PATH_PARAMETER_ENTRY_GUID + "}";
    public static final String API_ENDPOINT_NOTES_SEARCH_ARCHIVED_BY_EXTERNAL_GUID = "/search/archived/externalguid/{" + API_ENDPOINT_PATH_PARAMETER_EXTERNAL_GUID + "}";
    // PUT
    public static final String API_ENDPOINT_NOTES_UPDATE = "/update";
    public static final String API_ENDPOINT_NOTES_ARCHIVE_BY_GUID = "/archive/guid/{" + API_ENDPOINT_PATH_PARAMETER_GUID + "}";
    public static final String API_ENDPOINT_NOTES_ARCHIVE_BY_EXTERNAL_GUID = "/archive/externalguid/{" + API_ENDPOINT_PATH_PARAMETER_EXTERNAL_GUID + "}";
    public static final String API_ENDPOINT_NOTES_ARCHIVE_BY_ENTRY_GUID = "/archive/entryguid/{" + API_ENDPOINT_PATH_PARAMETER_ENTRY_GUID + "}";
    // DELETE
    public static final String API_ENDPOINT_NOTES_DELETE_BY_GUID = "/delete/guid/{" + API_ENDPOINT_PATH_PARAMETER_GUID + "}";
    public static final String API_ENDPOINT_NOTES_DELETE_BY_EXTERNAL_GUID = "/delete/externalguid/{" + API_ENDPOINT_PATH_PARAMETER_EXTERNAL_GUID + "}";
    public static final String API_ENDPOINT_NOTES_DELETE_BY_ENTRY_GUID = "/delete/entryguid/{" + API_ENDPOINT_PATH_PARAMETER_ENTRY_GUID + "}";
    public static final String API_ENDPOINT_NOTES_DELETE_ARCHIVED_BY_ENTRY_GUID = "/archived/entryguid/{" + API_ENDPOINT_PATH_PARAMETER_ENTRY_GUID + "}";
    public static final String API_ENDPOINT_NOTES_DELETE_ARCHIVED_BY_EXTERNAL_GUID = "/archived/externalguid/{" + API_ENDPOINT_PATH_PARAMETER_EXTERNAL_GUID + "}";

}
