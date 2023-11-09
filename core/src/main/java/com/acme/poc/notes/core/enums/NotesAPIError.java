package com.acme.poc.notes.core.enums;


public enum NotesAPIError {

    ERROR_CLIENT_REQUEST                   (400, 4000, "Client request error"),
    ERROR_MISSING_GUID                     (400, 4001, "Missing property guid"),
    ERROR_MISSING_ENTRY_GUID               (400, 4002, "Missing property entryGuid"),
    ERROR_MISSING_THREAD_GUID              (400, 4003, "Missing property threadGuid"),
    ERROR_MISSING_THREAD_PARENT_GUID       (400, 4004, "Missing property threadParentGuid"),
    ERROR_MISSING_CREATED                  (400, 4005, "Missing property created"),
    ERROR_NOT_EXISTS_GUID                  (400, 4006, "No entry found for guid: %s"),
    ERROR_NOT_EXISTS_ENTRY_GUID            (400, 4007, "No entry found for entryGuid: %s"),
    ERROR_NEW_RESPONSE_NO_THREAD_GUID      (404, 4008, "Cannot create new response. No entry found for threadGuid: %s"),
    ERROR_ENTRY_ARCHIVED_NO_UPDATE         (400, 4009, "Entry is archived cannot be updated"),
    ERROR_ENTRY_ARCHIVED_CANNOT_ADD_THREAD (400, 4010, "Entry is archived cannot add thread (externalGuid: %s, entryGuid: %s)"),   // TODO Maybe this entry should be removed in favor of ERROR_ENTRY_ARCHIVED_NO_UPDATE ?
    ERROR_ENTRY_HAS_BEEN_MODIFIED          (409, 4011, "Entry recently updated. Please reload"),

    ERROR_SERVER                           (500, 5000, "Internal server error"),
    ERROR_ARCHIVING                        (500, 5001, "Error while archiving entries: %s");


    private final int httpStatusCode;
    private final int errorCode;
    private final String errorMessage;


    NotesAPIError(int httpStatusCode, int errorCode, String errorMessage) {
        this.httpStatusCode = httpStatusCode;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }


    public int httpStatusCode() {
        return this.httpStatusCode;
    }

    public int errorCode() {
        return this.errorCode;
    }

    public String errorMessage() {
        return this.errorMessage;
    }

}
