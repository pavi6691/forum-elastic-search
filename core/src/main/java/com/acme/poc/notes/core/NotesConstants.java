package com.acme.poc.notes.core;

public class NotesConstants {


    // REST path parameters
    public final static String API_ENDPOINT_PATH_PARAMETER_GUID = "guid";
    public final static String API_ENDPOINT_PATH_PARAMETER_THREADGUID = "threadGuid";
    public final static String API_ENDPOINT_PATH_PARAMETER_ENTRYGUID = "entryGuid";

    // REST API endponts
    public final static String API_ENDPOINT_PREFIX = "/api/v1";

    public final static String API_ENDPOINT_DELETE = "/DELETE";

    public final static String API_ENDPOINT_NOTES = "/notes";
    public final static String API_ENDPOINT_NOTES_CREATE = "";
    public final static String API_ENDPOINT_NOTES_UPDATE = "/guid/{" + API_ENDPOINT_PATH_PARAMETER_GUID + "}";
    public final static String API_ENDPOINT_NOTES_ARCHIVE_BY_GUID = "/archive/guid/{" + API_ENDPOINT_PATH_PARAMETER_GUID + "}";
    public final static String API_ENDPOINT_NOTES_ARCHIVE_BY_THREADGUID = "/archive/threadguid/{" + API_ENDPOINT_PATH_PARAMETER_THREADGUID + "}";
    public final static String API_ENDPOINT_NOTES_ARCHIVE_BY_ENTRYGUID = "/archive/entryguid/{" + API_ENDPOINT_PATH_PARAMETER_ENTRYGUID + "}";

}
