package com.acme.poc.notes.restservice.service;
import com.acme.poc.notes.restservice.persistence.elasticsearch.models.NotesData;
import com.acme.poc.notes.restservice.service.generics.queries.generics.IQuery;
import com.acme.poc.notes.restservice.persistence.postgresql.models.PGNoteEntity;
import com.acme.poc.notes.restservice.persistence.postgresql.repositories.PGNotesRepository;
import com.acme.poc.notes.restservice.service.generics.abstracts.disctinct.AbstractNotesCrudService;
import com.acme.poc.notes.restservice.util.DTOMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class PSQLNotesClientService extends AbstractNotesCrudService<PGNoteEntity> {

    @Value("${default.number.of.entries.to.return}")
    private int default_size_configured;

    ElasticsearchOperations elasticsearchOperations;
    ESNotesClientService esNotesClientService;
    public PSQLNotesClientService(PGNotesRepository pgNotesRepository,ElasticsearchOperations elasticsearchOperations,
                                  ESNotesClientService esNotesClientService) {
        super(pgNotesRepository);
        this.elasticsearchOperations = elasticsearchOperations;
        this.esNotesClientService = esNotesClientService;
    }
    
    @Override
    protected List<PGNoteEntity> search(IQuery query) {
        return elasticsearchOperations.search(esNotesClientService.getEsQuery(query), NotesData.class).stream()
                .map(sh -> sh.getContent()).map(e -> DTOMapper.INSTANCE.toPG(e)).collect(Collectors.toList());
    }
}
