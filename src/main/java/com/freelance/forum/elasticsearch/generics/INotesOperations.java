package com.freelance.forum.elasticsearch.generics;
import com.freelance.forum.elasticsearch.pojo.NotesData;
import com.freelance.forum.elasticsearch.queries.generics.IQuery;
import org.springframework.data.elasticsearch.core.query.ByQueryResponse;

import java.util.List;

public interface INotesOperations {
    List<NotesData> search(IQuery query);
}
