package com.freelance.forum.service.generics;

import com.freelance.forum.elasticsearch.pojo.NotesData;
import com.freelance.forum.elasticsearch.pojo.SearchRequest;

import java.util.List;

public interface ISearchNotesService {
    List<NotesData> search(SearchRequest searchRequest);
    
}
