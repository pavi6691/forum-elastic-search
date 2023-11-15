package com.acme.poc.notes.persistence.elasticsearch.esrepo;

import com.acme.poc.notes.persistence.elasticsearch.pojo.NotesData;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ESNotesRepository extends ElasticsearchRepository<NotesData, UUID> {
}
