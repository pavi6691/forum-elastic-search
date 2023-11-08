package com.acme.poc.notes.core;

public class NotesConstants {


    // Date/time
    public static final String TIMESTAMP_ISO8601 = "uuuu-MM-dd'T'HH:mm:ss.SSSSSSXXX";
    
    // Time out for delete and archive
    
    public static final long DELETE_ENTRIES_TIME_OUT = 10000;
    public static final long ARCHIVE_ENTRIES_TIME_OUT = 10000;
    
    // REST path parameters
    public static final String API_ENDPOINT_PATH_PARAMETER_GUID = "/{guid}";
    public static final String API_ENDPOINT_PATH_PARAMETER_THREAD_GUID = "/{threadGuid}";
    public static final String API_ENDPOINT_PATH_PARAMETER_EXTERNAL_GUID= "/{externalGuid}";
    public static final String API_ENDPOINT_PATH_PARAMETER_ENTRY_GUID = "/{entryGuid}";

    // REST API endponts
    public static final String API_ENDPOINT_PREFIX = "/api/v1";
    public static final String API_ENDPOINT_DELETE = "/delete";
    public static final String API_ENDPOINT_NOTES = "/notes";
    
    // POST
    public static final String API_ENDPOINT_NOTES_CREATE = "/create";
    
    // GET
    public static final String API_ENDPOINT_NOTES_SEARCH_GUID = "/search/guid";
    public static final String API_ENDPOINT_NOTES_SEARCH_EXTERNAL = "/search/external";
    public static final String API_ENDPOINT_NOTES_SEARCH_ENTRY = "/search/entry";
    public static final String API_ENDPOINT_NOTES_SEARCH_CONTENT = "/search/content";
    public static final String API_ENDPOINT_NOTES_SEARCH_EXTERNAL_ARCHIVED = "/search/external/archived";
    public static final String API_ENDPOINT_NOTES_SEARCH_ENTRY_ARCHIVED = "/search/entry/archived";
    
    // PUT
    public static final String API_ENDPOINT_NOTES_UPDATE_BY_ENTRY_GUID = "/entry" + API_ENDPOINT_PATH_PARAMETER_ENTRY_GUID;
    public static final String API_ENDPOINT_NOTES_UPDATE_BY_GUID = "/guid" + API_ENDPOINT_PATH_PARAMETER_GUID;
    public static final String API_ENDPOINT_NOTES_ARCHIVE_BY_GUID = "/archive/guid" + API_ENDPOINT_PATH_PARAMETER_GUID;
    public static final String API_ENDPOINT_NOTES_ARCHIVE_BY_EXTERNAL_GUID = "/archive/external" + API_ENDPOINT_PATH_PARAMETER_EXTERNAL_GUID;
    public static final String API_ENDPOINT_NOTES_ARCHIVE_BY_ENTRY_GUID = "/archive/entry" + API_ENDPOINT_PATH_PARAMETER_ENTRY_GUID;

    //DELETE
    public static final String API_ENDPOINT_NOTES_DELETE_BY_EXTERNAL_GUID = "/delete/external" + API_ENDPOINT_PATH_PARAMETER_EXTERNAL_GUID;
    public static final String API_ENDPOINT_NOTES_DELETE_BY_GUID = "/delete/guid" + API_ENDPOINT_PATH_PARAMETER_ENTRY_GUID;
    public static final String API_ENDPOINT_NOTES_DELETE_BY_ENTRY_GUID = "/delete/entry" + API_ENDPOINT_PATH_PARAMETER_ENTRY_GUID;
    public static final String API_ENDPOINT_NOTES_DELETE_ARCHIVED_BY_ENTRY_GUID = "/delete/entry/archived" + API_ENDPOINT_PATH_PARAMETER_ENTRY_GUID;
    public static final String API_ENDPOINT_NOTES_DELETE_ARCHIVED_BY_EXTERNAL_GUID = "/delete/external/archived" + API_ENDPOINT_PATH_PARAMETER_EXTERNAL_GUID;
}
