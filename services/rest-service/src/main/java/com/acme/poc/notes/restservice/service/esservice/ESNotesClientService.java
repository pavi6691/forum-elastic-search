package com.acme.poc.notes.restservice.service.esservice;

import com.acme.poc.notes.core.NotesConstants;
import com.acme.poc.notes.core.enums.NotesAPIError;
import com.acme.poc.notes.restservice.persistence.elasticsearch.models.NotesData;
import com.acme.poc.notes.restservice.service.generics.queries.IQueryRequest;
import com.acme.poc.notes.restservice.service.generics.queries.enums.Match;
import com.acme.poc.notes.restservice.persistence.elasticsearch.repositories.ESNotesRepository;
import com.acme.poc.notes.restservice.service.generics.abstracts.disctinct.AbstractNotesClientService;
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
     * Executes IQueryRequest
     * @param query
     * @return search result from elastics search response
     */
    @Override
    protected List<NotesData> search(IQueryRequest query) {
        return elasticsearchOperations.search(getEsQuery(query), NotesData.class).stream()
                .map(sh -> sh.getContent()).collect(Collectors.toList());
    }
    
    public NativeSearchQuery getEsQuery(IQueryRequest query) {
        NativeSearchQueryBuilder searchQueryBuilder = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.wrapperQuery(ESQueryBuilder.build(query)))
                .withSort(Sort.by(Sort.Order.asc(Match.CREATED.getMatch())));
        if (query.searchAfter() != null && !(query.getSearchField().equals(Match.ENTRY))) {
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
        return searchQuery;
    }

}
