package com.acme.poc.notes.data;

import com.acme.poc.notes.models.NoteEntry;
import com.acme.poc.notes.models.NoteType;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.UUID;

public class PostgreSQLData {

    public static final UUID TEST_GUID = UUID.randomUUID();
    public static final UUID TEST_EXTERNAL_GUID = UUID.randomUUID();
    public static final UUID TEST_THREAD_GUID = UUID.randomUUID();
    public static final UUID TEST_ENTRY_GUID = UUID.randomUUID();
    public static final UUID TEST_ENTRY_GUID_PARENT = UUID.randomUUID();
    public static final NoteType TEST_TYPE = NoteType.REMARK;
    public static final String TEST_CONTENT = UUID.randomUUID().toString();
    public static final ZonedDateTime TEST_ZDT_CREATED = ZonedDateTime.now();
    public static final ZonedDateTime TEST_ZDT_ARCHIVED = ZonedDateTime.now();

    public static final NoteEntry TEST_NOTE_ALL_NULLS = NoteEntry.builder().build();
    public static final NoteEntry TEST_NOTE = NoteEntry.builder()
            .guid(TEST_GUID)
            .externalGuid(TEST_EXTERNAL_GUID)
            .threadGuid(TEST_THREAD_GUID)
            .entryGuid(TEST_ENTRY_GUID)
            .entryGuidParent(TEST_ENTRY_GUID_PARENT)
            .type(TEST_TYPE)
            .content(TEST_CONTENT)
            .created(Date.from(TEST_ZDT_CREATED.toInstant()))
            .archived(Date.from(TEST_ZDT_ARCHIVED.toInstant()))
            .build();

}
