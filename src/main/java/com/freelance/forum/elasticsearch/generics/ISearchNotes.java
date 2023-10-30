package com.freelance.forum.elasticsearch.generics;
import com.freelance.forum.elasticsearch.pojo.NotesData;
import com.freelance.forum.elasticsearch.queries.generics.IQuery;
import java.util.List;

public interface ISearchNotes {
    List<NotesData> search(IQuery query);
}
