package com.acme.poc.notes.restservice.service.pgsqlservice;
import com.acme.poc.notes.restservice.persistence.elasticsearch.models.NotesData;
import com.acme.poc.notes.restservice.service.esservice.ESNotesClientOperations;
import com.acme.poc.notes.restservice.generics.queries.IQueryRequest;
import com.acme.poc.notes.restservice.persistence.postgresql.models.PGNoteEntity;
import com.acme.poc.notes.restservice.persistence.postgresql.repositories.PGNotesRepository;
import com.acme.poc.notes.restservice.generics.abstracts.disctinct.AbstractNotesCrudOperations;
import com.acme.poc.notes.restservice.util.DTOMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Perform crud operations on POSTGRESQL and search is done on elasticsearch
 */
@Service
public class PSQLNotesClientOperations extends AbstractNotesCrudOperations<PGNoteEntity> {

    @Value("${default.number.of.entries.to.return}")
    private int default_size_configured;

    ElasticsearchOperations elasticsearchOperations;
    ESNotesClientOperations esNotesClientService;


    public PSQLNotesClientOperations(PGNotesRepository pgNotesRepository, ElasticsearchOperations elasticsearchOperations, ESNotesClientOperations esNotesClientService) {
        super(pgNotesRepository);
        this.elasticsearchOperations = elasticsearchOperations;
        this.esNotesClientService = esNotesClientService;
    }


    @Override
    protected List<PGNoteEntity> search(IQueryRequest query) {
        return elasticsearchOperations.search(esNotesClientService.getEsQuery(query), NotesData.class).stream()
                .map(sh -> sh.getContent())
                .map(e -> DTOMapper.INSTANCE.toPG(e))
                .collect(Collectors.toList());
    }

}
