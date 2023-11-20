package com.acme.poc.notes.restservice.persistence.elasticsearch.repositories;
import com.acme.poc.notes.restservice.persistence.elasticsearch.models.NotesData;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;


@Repository
public interface ESNotesRepository extends ElasticsearchRepository<NotesData, UUID> {
}
