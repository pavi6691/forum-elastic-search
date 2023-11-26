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

    @Value("${default.number.of.entries.to.return}")
    private int default_size_configured;

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
    protected List<NotesData> execSearchQuery(IQuery query) {
        NativeSearchQueryBuilder searchQueryBuilder = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.wrapperQuery(query.buildQuery()))
                .withSort(Sort.by(Sort.Order.asc(EsNotesFields.CREATED.getEsFieldName())));
        if (query.searchAfter() != null && !(query instanceof SearchByEntryGuid || query instanceof SearchArchivedByEntryGuid)) {
            SimpleDateFormat dateFormat = new SimpleDateFormat(NotesConstants.TIMESTAMP_ISO8601);
            String searchAfter = query.searchAfter().toString();
            try {
                if (getTimeUnit(Long.valueOf(searchAfter))) {
                    searchQueryBuilder.withSearchAfter(List.of(searchAfter));
                } else {
                    searchQueryBuilder.withSearchAfter(List.of(dateFormat.parse(searchAfter).toInstant().toEpochMilli()));
                }
            } catch (ParseException e) {
                throwRestError(NotesAPIError.ERROR_INCORRECT_SEARCH_AFTER, String.format(searchAfter,NotesConstants.TIMESTAMP_ISO8601,searchAfter));
            }
        }
        NativeSearchQuery searchQuery  = searchQueryBuilder.build();
        searchQuery.setMaxResults(query.getSize() > 0 ? query.getSize() : default_size_configured);
        return elasticsearchOperations.search(searchQuery, NotesData.class).stream()
                .map(sh -> sh.getContent()).collect(Collectors.toList());
    }

}
