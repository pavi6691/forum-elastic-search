package com.acme.poc.notes.restservice.service;

import com.acme.poc.notes.core.NotesConstants;
import com.acme.poc.notes.core.enums.NotesAPIError;
import com.acme.poc.notes.restservice.persistence.elasticsearch.models.NotesData;
import com.acme.poc.notes.restservice.persistence.elasticsearch.queries.SearchArchivedByEntryGuid;
import com.acme.poc.notes.restservice.persistence.elasticsearch.queries.SearchByEntryGuid;
import com.acme.poc.notes.restservice.persistence.elasticsearch.queries.generics.IQuery;
import com.acme.poc.notes.restservice.persistence.elasticsearch.queries.generics.enums.EsNotesFields;
import com.acme.poc.notes.restservice.persistence.elasticsearch.repositories.ESNotesRepository;
import com.acme.poc.notes.restservice.service.generics.abstracts.AbstractNotesClientService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;

import static com.acme.poc.notes.restservice.util.ExceptionUtil.throwRestError;


/**
 * Service to perform elastic search operations
 */
@Slf4j
@Service
public class ESNotesClientService extends AbstractNotesClientService<NotesData> {

    ElasticsearchOperations elasticsearchOperations;


    public ESNotesClientService(ESNotesRepository esNotesRepository,ElasticsearchOperations elasticsearchOperations) {
        super(esNotesRepository);
        this.elasticsearchOperations = elasticsearchOperations;
    }

    /**
     * Executes IQuery
     * @param query
     * @return search result from elastics search response
     */
    @Override
    protected List<NotesData> searchQuery(IQuery query) {
        return elasticsearchOperations.search(getEsQuery(query), NotesData.class).stream()
                .map(sh -> sh.getContent()).collect(Collectors.toList());
    }

}
