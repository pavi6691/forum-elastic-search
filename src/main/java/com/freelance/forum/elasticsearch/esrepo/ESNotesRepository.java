package com.freelance.forum.elasticsearch.esrepo;
import com.freelance.forum.elasticsearch.pojo.NotesData;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ESNotesRepository extends ElasticsearchRepository<NotesData, UUID> {
}
