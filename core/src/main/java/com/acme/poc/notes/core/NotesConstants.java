package com.acme.poc.notes.core;

import java.util.concurrent.TimeUnit;

public class NotesConstants {

    // Scheduled jobs
    public static final int NOTES_PROCESSOR_JOB_SCHEDULE = 5/*seconds*/ * 1000/*millis*/;  // Run every 5th second

    // Timestamps
    public static final String TIMESTAMP_ISO8601 = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX";

    // Timeouts
    public static final long TIMEOUT_ARCHIVE = TimeUnit.SECONDS.toMillis(10);
    public static final long TIMEOUT_DELETE = TimeUnit.SECONDS.toMillis(10);


    /*
     * OpenAPI
     */

    // DevOps REST APIs
    public static final String OPENAPI_NOTES_DEVOPS_TAG = "Notes DevOps API";
    // PostgreSQL REST APIs
    public static final String OPENAPI_NOTES_POSTGRESQL_TAG = "Notes CRUD API";
    public static final String OPENAPI_NOTES_POSTGRESQL_ADMIN_TAG = "Notes CRUD Admin API";
    // Elasticsearch REST APIs
    public static final String OPENAPI_NOTES_ELASTICSEARCH_TAG = "Notes Elasticsearch API";
    public static final String OPENAPI_NOTES_ELASTICSEARCH_ADMIN_TAG = "Notes Elasticsearch Admin API";


    /*
     * REST endpoints - path parameters
     */

    public static final String API_ENDPOINT_PATH_PARAMETER_GUID = "guid";
    public static final String API_ENDPOINT_PATH_PARAMETER_EXTERNAL_GUID= "externalGuid";
    public static final String API_ENDPOINT_PATH_PARAMETER_ENTRY_GUID = "entryGuid";
    public static final String API_ENDPOINT_PATH_PARAMETER_THREAD_GUID = "threadGuid";


    /*
     * REST endpoints - query parameters
     */

    public static final String API_ENDPOINT_QUERY_PARAMETER_INDEX_NAME = "indexName";
    public static final String API_ENDPOINT_QUERY_PARAMETER_INCLUDE_VERSIONS = "includeVersions";
    public static final String API_ENDPOINT_QUERY_PARAMETER_INCLUDE_ARCHIVED = "includeArchived";
    public static final String API_ENDPOINT_QUERY_PARAMETER_INCLUDE_DELETED = "includeDeleted";
    public static final String API_ENDPOINT_QUERY_PARAMETER_INCLUDE_SEARCHAFTER = "searchAfter";
    public static final String API_ENDPOINT_QUERY_PARAMETER_INCLUDE_SIZE = "size";
    public static final String API_ENDPOINT_QUERY_PARAMETER_INCLUDE_SORTORDER = "sortOrder";


    /*
     * REST endpoints - paths
     */

    public static final String API_ENDPOINT_DELETE = "/DELETE";

    public static final String API_ENDPOINT_DEVOPS = "/devops";

    public static final String API_ENDPOINT_PREFIX = "/api/v1";
    public static final String API_ENDPOINT_NOTES_POSTGRESQL_USER = "/notes";
    public static final String API_ENDPOINT_NOTES_POSTGRESQL_ADMIN = "/notes/admin";
    public static final String API_ENDPOINT_NOTES_ELASTICSEARCH_USER = "/notes/es";
    public static final String API_ENDPOINT_NOTES_ELASTICSEARCH_ADMIN = "/notes/es/admin";

    // /api/v1/devops/...
    public static final String API_ENDPOINT_DEVOPS_ERRORS = "/errors";

    // /api/v1/notes...
    // /api/v1/notes/admin...
    // /api/v1/notes/es...
    // /api/v1/notes/es/admin...
    // POST
    public static final String API_ENDPOINT_NOTES_CREATE = "";
    // GET
    public static final String API_ENDPOINT_NOTES_GET_ALL = "";

    public static final String API_ENDPOINT_NOTES_GET_ALL_SOFT_DELETED = "softdeleted";
    public static final String API_ENDPOINT_NOTES_GET_BY_GUID = "/guid/{" + API_ENDPOINT_PATH_PARAMETER_GUID + "}";
    public static final String API_ENDPOINT_NOTES_GET_BY_EXTERNAL_GUID = "/externalguid/{" + API_ENDPOINT_PATH_PARAMETER_EXTERNAL_GUID + "}";
    public static final String API_ENDPOINT_NOTES_GET_BY_ENTRY_GUID = "/entryguid/{" + API_ENDPOINT_PATH_PARAMETER_ENTRY_GUID + "}";
    public static final String API_ENDPOINT_NOTES_GET_BY_THREAD_GUID = "/threadguid/{" + API_ENDPOINT_PATH_PARAMETER_THREAD_GUID + "}";
    public static final String API_ENDPOINT_NOTES_SEARCH_CONTENT = "/search";
    // PUT
    public static final String API_ENDPOINT_NOTES_UPDATE = "";
    public static final String API_ENDPOINT_NOTES_ARCHIVE_BY_GUID = "/archive/guid/{" + API_ENDPOINT_PATH_PARAMETER_GUID + "}";
    public static final String API_ENDPOINT_NOTES_ARCHIVE_BY_EXTERNAL_GUID = "/archive/externalguid/{" + API_ENDPOINT_PATH_PARAMETER_EXTERNAL_GUID + "}";
    public static final String API_ENDPOINT_NOTES_ARCHIVE_BY_ENTRY_GUID = "/archive/entryguid/{" + API_ENDPOINT_PATH_PARAMETER_ENTRY_GUID + "}";
    // DELETE
    public static final String API_ENDPOINT_NOTES_DELETE_BY_GUID = "/guid/{" + API_ENDPOINT_PATH_PARAMETER_GUID + "}";
    public static final String API_ENDPOINT_NOTES_DELETE_BY_EXTERNAL_GUID = "/externalguid/{" + API_ENDPOINT_PATH_PARAMETER_EXTERNAL_GUID + "}";
    public static final String API_ENDPOINT_NOTES_DELETE_BY_ENTRY_GUID = "/entryguid/{" + API_ENDPOINT_PATH_PARAMETER_ENTRY_GUID + "}";
    public static final String API_ENDPOINT_NOTES_DELETE_BY_THREAD_GUID = "/threadguid/{" + API_ENDPOINT_PATH_PARAMETER_THREAD_GUID + "}";
    public static final String API_ENDPOINT_NOTES_DELETE_ARCHIVED_BY_ENTRY_GUID = "/archived/entryguid/{" + API_ENDPOINT_PATH_PARAMETER_ENTRY_GUID + "}";
    public static final String API_ENDPOINT_NOTES_DELETE_ARCHIVED_BY_EXTERNAL_GUID = "/archived/externalguid/{" + API_ENDPOINT_PATH_PARAMETER_EXTERNAL_GUID + "}";

    // RESTORE SOFT DELETED
    public static final String API_ENDPOINT_NOTES_RESTORE_BY_GUID = "/restore/guid/{" + API_ENDPOINT_PATH_PARAMETER_GUID + "}";
    public static final String API_ENDPOINT_NOTES_RESTORE_BY_EXTERNAL_GUID = "/restore/externalguid/{" + API_ENDPOINT_PATH_PARAMETER_EXTERNAL_GUID + "}";
    public static final String API_ENDPOINT_NOTES_RESTORE_BY_ENTRY_GUID = "/restore/entryguid/{" + API_ENDPOINT_PATH_PARAMETER_ENTRY_GUID + "}";
    public static final String API_ENDPOINT_NOTES_RESTORE_BY_THREAD_GUID = "/restore/threadguid/{" + API_ENDPOINT_PATH_PARAMETER_THREAD_GUID + "}";

    // GET SOFT DELETED
    public static final String API_ENDPOINT_NOTES_GET_SOFT_DELETED_BY_GUID = "/softdeleted/guid/{" + API_ENDPOINT_PATH_PARAMETER_GUID + "}";
    public static final String API_ENDPOINT_NOTES_GET_SOFT_DELETED_BY_EXTERNAL_GUID = "/softdeleted/externalguid/{" + API_ENDPOINT_PATH_PARAMETER_EXTERNAL_GUID + "}";
    public static final String API_ENDPOINT_NOTES_GET_SOFT_DELETED_BY_ENTRY_GUID = "/softdeleted/entryguid/{" + API_ENDPOINT_PATH_PARAMETER_ENTRY_GUID + "}";
    public static final String API_ENDPOINT_NOTES_GET_SOFT_DELETED_BY_THREAD_GUID = "/softdeleted/threadguid/{" + API_ENDPOINT_PATH_PARAMETER_THREAD_GUID + "}";
}
