package com.acme.poc.notes.restservice.service;
import com.acme.poc.notes.restservice.persistence.elasticsearch.models.NotesData;
import com.acme.poc.notes.restservice.persistence.elasticsearch.queries.generics.IQuery;
import com.acme.poc.notes.restservice.persistence.postgresql.models.PGNoteEntity;
import com.acme.poc.notes.restservice.persistence.postgresql.repositories.PGNotesRepository;
import com.acme.poc.notes.restservice.service.generics.abstracts.AbstractNotesCrudOperations;
import com.acme.poc.notes.restservice.util.DTOMapper;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
public class PSQLNotesClientService extends AbstractNotesCrudOperations<PGNoteEntity> {

    ElasticsearchOperations elasticsearchOperations;
    public PSQLNotesClientService(PGNotesRepository pgNotesRepository,ElasticsearchOperations elasticsearchOperations) {
        super(pgNotesRepository);
        this.elasticsearchOperations = elasticsearchOperations;
    }
    
    @Override
    protected List<PGNoteEntity> searchQuery(IQuery query) {
        return elasticsearchOperations.search(getEsQuery(query), NotesData.class).stream()
                .map(sh -> sh.getContent()).map(e -> DTOMapper.INSTANCE.toPG(e)).collect(Collectors.toList());
    }
}
