package com.acme.poc.notes.core;

public class NotesConstants {


    // Date/time
    public static final String TIMESTAMP_ISO8601 = "uuuu-MM-dd'T'HH:mm:ss.SSSSSSXXX";

    // REST path parameters
    public static final String API_ENDPOINT_PATH_PARAMETER_GUID = "guid";
    public static final String API_ENDPOINT_PATH_PARAMETER_THREADGUID = "threadGuid";
    public static final String API_ENDPOINT_PATH_PARAMETER_ENTRYGUID = "entryGuid";

    // REST API endponts
    public static final String API_ENDPOINT_PREFIX = "/api/v1";

    public static final String API_ENDPOINT_DELETE = "/DELETE";

    public static final String API_ENDPOINT_NOTES = "/notes";
    public static final String API_ENDPOINT_NOTES_CREATE = "";
    public static final String API_ENDPOINT_NOTES_UPDATE = "/guid/{" + API_ENDPOINT_PATH_PARAMETER_GUID + "}";
    public static final String API_ENDPOINT_NOTES_ARCHIVE_BY_GUID = "/archive/guid/{" + API_ENDPOINT_PATH_PARAMETER_GUID + "}";
    public static final String API_ENDPOINT_NOTES_ARCHIVE_BY_THREADGUID = "/archive/threadguid/{" + API_ENDPOINT_PATH_PARAMETER_THREADGUID + "}";
    public static final String API_ENDPOINT_NOTES_ARCHIVE_BY_ENTRYGUID = "/archive/entryguid/{" + API_ENDPOINT_PATH_PARAMETER_ENTRYGUID + "}";

}
