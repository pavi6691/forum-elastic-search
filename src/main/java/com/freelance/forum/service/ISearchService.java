package com.freelance.forum.service;

import com.freelance.forum.elasticsearch.pojo.NotesData;
import com.freelance.forum.elasticsearch.pojo.Request;

import java.util.List;

public interface ISearchService {
    List<NotesData> search(Request request);
}
