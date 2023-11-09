package com.acme.poc.notes.core;

import java.util.concurrent.TimeUnit;

public class NotesConstants {


    // Timestamps
    public static final String TIMESTAMP_ISO8601 = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX";

    // Timeouts
    public static final long TIMEOUT_ARCHIVE = TimeUnit.SECONDS.toMillis(10);
    public static final long TIMEOUT_DELETE = TimeUnit.SECONDS.toMillis(10);

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
    public static final String API_ENDPOINT_ADMIN = "/admin";

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
    //
    public static final String API_ENDPOINT_NOTES = "/notes";
    // POST
    public static final String API_ENDPOINT_NOTES_CREATE = "";
    // GET
    public static final String API_ENDPOINT_NOTES_GET_BY_GUID = "/guid/{" + API_ENDPOINT_PATH_PARAMETER_GUID + "}";
    public static final String API_ENDPOINT_NOTES_GET_BY_EXTERNAL_GUID = "/externalguid/{" + API_ENDPOINT_PATH_PARAMETER_EXTERNAL_GUID + "}";
    public static final String API_ENDPOINT_NOTES_GET_BY_ENTRY_GUID = "/entryguid/{" + API_ENDPOINT_PATH_PARAMETER_ENTRY_GUID + "}";
    public static final String API_ENDPOINT_NOTES_SEARCH_CONTENT = "/search";
    public static final String API_ENDPOINT_NOTES_SEARCH_ARCHIVED_BY_ENTRY_GUID = "/search/{" + API_ENDPOINT_PATH_PARAMETER_ENTRY_GUID + "}";
    public static final String API_ENDPOINT_NOTES_SEARCH_ARCHIVED_BY_EXTERNAL_GUID = "/search/{" + API_ENDPOINT_PATH_PARAMETER_EXTERNAL_GUID + "}";
    // PUT
    public static final String API_ENDPOINT_NOTES_UPDATE_BY_GUID = "/guid";
    public static final String API_ENDPOINT_NOTES_UPDATE_BY_ENTRY_GUID = "/entryguid";
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
