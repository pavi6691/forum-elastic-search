package com.acme.poc.notes.service;

import com.acme.poc.notes.elasticsearch.pojo.NotesData;

import java.util.List;
import java.util.UUID;

public interface INotesAdminService {
    List<NotesData> getAll();
    List<NotesData> getByExternalGuid(UUID externalGuid);
    List<NotesData> deleteByExternalGuid(UUID externalGuid);
    List<NotesData> deleteByEntryGuid(UUID entryGuid);
    List<NotesData> deleteByThreadGuid(UUID threadGuid);
    
}
