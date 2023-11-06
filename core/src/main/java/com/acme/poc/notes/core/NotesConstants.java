package com.acme.poc.notes.core;

public class NotesConstants {


    // Date/time
    public static final String TIMESTAMP_ISO8601 = "uuuu-MM-dd'T'HH:mm:ss.SSSSSSXXX";

    // REST path parameters
    public static final String API_ENDPOINT_PATH_PARAMETER_GUID = "guid";
    public static final String API_ENDPOINT_PATH_PARAMETER_THREAD_GUID = "threadGuid";
    public static final String API_ENDPOINT_PATH_PARAMETER_EXTERNAL_GUID= "externalGuid";
    public static final String API_ENDPOINT_PATH_PARAMETER_ENTRY_GUID = "entryGuid";

    // REST API endponts
    public static final String API_ENDPOINT_PREFIX = "/api/v1";

    public static final String API_ENDPOINT_DELETE = "/DELETE";

    public static final String API_ENDPOINT_NOTES = "/notes";
    public static final String API_ENDPOINT_NOTES_CREATE = "/create";
    public static final String API_ENDPOINT_NOTES_UPDATE_BY_ENTRY_GUID = "/entryguid/{" + API_ENDPOINT_PATH_PARAMETER_ENTRY_GUID + "}";
    public static final String API_ENDPOINT_NOTES_UPDATE_BY_GUID = "/guid/{" + API_ENDPOINT_PATH_PARAMETER_GUID + "}";
    public static final String API_ENDPOINT_NOTES_ARCHIVE_BY_GUID = "/archive/guid/{" + API_ENDPOINT_PATH_PARAMETER_GUID + "}";
    public static final String API_ENDPOINT_NOTES_ARCHIVE_BY_EXTERNAL_GUID = "/archive/externalguid/{" + API_ENDPOINT_PATH_PARAMETER_EXTERNAL_GUID + "}";
    public static final String API_ENDPOINT_NOTES_ARCHIVE_BY_ENTRY_GUID = "/archive/entryguid/{" + API_ENDPOINT_PATH_PARAMETER_ENTRY_GUID + "}";

}
