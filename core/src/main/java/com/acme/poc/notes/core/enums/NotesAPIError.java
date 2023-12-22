package com.acme.poc.notes.core.enums;


public enum NotesAPIError {

    ERROR_CLIENT_REQUEST                   (400, 4000, "Client request error"),
    ERROR_MISSING_PROPERTIES_FOR_UPDATE    (400, 4001, "Missing property guid and entryGuid. should be provided at least one"),
    ERROR_MISSING_THREAD_GUID              (400, 4003, "Missing property threadGuid"),
    ERROR_MISSING_THREAD_PARENT_GUID       (400, 4004, "Missing property threadParentGuid"),
    ERROR_MISSING_CREATED                  (400, 4005, "Missing property created"),
    ERROR_PARSING_TIMESTAMP                (400, 4006, "Error parsing timestamp: %s"),
    ERROR_INCORRECT_SEARCH_AFTER           (400, 4007, "Incorrect date format on searchAfter parameter. Should be %s but was: %s"),
    ERROR_NOT_EXISTS_GUID                  (400, 4008, "No entry found for guid: %s"),
    ERROR_NOT_EXISTS_ENTRY_GUID            (400, 4009, "No entry found for entryGuid: %s"),
    ERROR_NEW_RESPONSE_NO_THREAD_GUID      (404, 4010, "Cannot create new response. No entry found for threadGuid: %s"),
    ERROR_ENTRY_ARCHIVED_NO_UPDATE         (400, 4011, "Entry is archived cannot be updated"),
    ERROR_ENTRY_ARCHIVED_CANNOT_ADD_THREAD (400, 4012, "Entry is archived cannot add thread (externalGuid: %s, entryGuid: %s)"),   // TODO Maybe this entry should be removed in favor of ERROR_ENTRY_ARCHIVED_NO_UPDATE ?
    ERROR_ENTRY_HAS_BEEN_MODIFIED          (409, 4013, "Entry recently updated. Please reload"),
    ERROR_GET_ALL_ENTRIES_TIMEOUT_DELETE   (408, 4014, "Getting all entries timed out, time taken: %s ms"),
    ERROR_NOT_FOUND                        (404, 4015, "No entries found"),
    ERROR_SOFT_DELETED                     (404, 4016, "Entry soft deleted"),
    ERROR_SOFT_DELETED_ENTRIES_NOT_FOUND   (404, 4017, "Soft deleted entries not found"),
    ERROR_SERVER                           (500, 5000, "error message: %s"),
    ERROR_ON_DB_OPERATION                  (500, 5002, "Error while performing database operation. Operation: %s, error: %s");

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
