package com.freelance.forum.elasticsearch.generics;

import com.freelance.forum.elasticsearch.pojo.NotesData;
import com.freelance.forum.elasticsearch.queries.IQuery;

import java.util.List;

public interface ISearchNotes {
    List<NotesData> search(IQuery query);
    
}
