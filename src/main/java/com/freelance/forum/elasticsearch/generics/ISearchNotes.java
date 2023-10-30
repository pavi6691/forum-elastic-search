package com.freelance.forum.elasticsearch.generics;

import com.freelance.forum.elasticsearch.pojo.NotesData;
import com.freelance.forum.elasticsearch.queries.IQuery;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ISearchNotes {
    List<NotesData> search(IQuery query);
    Map<UUID, Map<UUID,List<NotesData>>> getRootEntries(IQuery query);
}
