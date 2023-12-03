package com.acme.poc.notes.restservice.service.esservice;

import com.acme.poc.notes.core.NotesConstants;
import com.acme.poc.notes.core.enums.NotesAPIError;
import com.acme.poc.notes.restservice.generics.abstracts.AbstractNotesOperations;
import com.acme.poc.notes.restservice.persistence.elasticsearch.models.ESNoteEntity;
import com.acme.poc.notes.restservice.generics.queries.IQueryRequest;
import com.acme.poc.notes.restservice.generics.queries.enums.Field;
import com.acme.poc.notes.restservice.persistence.elasticsearch.repositories.ESNotesRepository;
import com.acme.poc.notes.restservice.util.LogUtil;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.acme.poc.notes.restservice.util.ExceptionUtil.throwRestError;


/**
 * Service to perform elastic search operations
 */
@Slf4j
@Service("esNotesService")
@Primary
public class ESNotesService extends AbstractNotesOperations<ESNoteEntity> {
    
    @Value("${default.db.response.size}")
    private int default_response_size;

    ElasticsearchOperations elasticsearchOperations;


    public ESNotesService(ESNotesRepository esNotesRepository, ElasticsearchOperations elasticsearchOperations) {
        super(esNotesRepository);
        this.elasticsearchOperations = elasticsearchOperations;
    }


    /**
     * Executes IQueryRequest
     * @param query
     * @return search result from elastics search response
     */
    @Override
    protected List<ESNoteEntity> search(IQueryRequest query) {
        List<ESNoteEntity> searchHits = elasticsearchOperations.search(getEsQuery(query), ESNoteEntity.class).stream()
                .map(sh -> sh.getContent()).collect(Collectors.toList());
        if(query.isAllEntries()) {
            List<ESNoteEntity> searchHitList = new ArrayList<>();
            searchHitList.addAll(searchHits);
            long startTime = System.currentTimeMillis();
            boolean timeout = false;
            while (searchHits != null && !searchHits.isEmpty() && !timeout && searchHits.size() == default_response_size) {
                List<Date> sortValues = List.of(searchHits.get(searchHits.size() - 1).getCreated()); // TODO make sort value created dynamic
                query.setSearchAfter(sortValues.size() > 0 ? sortValues.get(0) : null);
                searchHits = elasticsearchOperations.search(getEsQuery(query), ESNoteEntity.class).stream()
                        .map(sh -> sh.getContent()).collect(Collectors.toList());
                timeout = (System.currentTimeMillis() - startTime) >= NotesConstants.TIMEOUT_DELETE;
                searchHitList.addAll(searchHits);
            }
            if (timeout) {
                long timeTaken = (System.currentTimeMillis() - startTime);
                log.error("getting all entries operation timed out, time taken: {} ms", timeTaken);
                throwRestError(NotesAPIError.ERROR_GET_ALL_ENTRIES_TIMEOUT_DELETE, timeTaken);
            }
            return searchHitList;
        }
        return searchHits;
    }
    
    public NativeSearchQuery getEsQuery(IQueryRequest query) {
        NativeSearchQueryBuilder searchQueryBuilder = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.wrapperQuery(ESQueryBuilder.build(query)))
                .withSort(Sort.by(Sort.Order.asc(Field.CREATED.getMatch())));
        if (query.getSearchAfter() != null && !(query.getSearchField().equals(Field.ENTRY))) {
            Object searchAfter = query.getSearchAfter();
            if(searchAfter instanceof Date) {
                searchQueryBuilder.withSearchAfter(Collections.singletonList(((Date) searchAfter).getTime()));
            } else {
                try {
                    if (getTimeUnit(Long.valueOf(searchAfter.toString()))) {
                        searchQueryBuilder.withSearchAfter(List.of(searchAfter));
                    } else {
                        SimpleDateFormat dateFormat = new SimpleDateFormat(NotesConstants.TIMESTAMP_ISO8601);
                        searchQueryBuilder.withSearchAfter(List.of(dateFormat.parse(searchAfter.toString()).toInstant().toEpochMilli()));
                    }
                } catch (ParseException e) {
                    throwRestError(NotesAPIError.ERROR_INCORRECT_SEARCH_AFTER, 
                            String.format(searchAfter.toString(),NotesConstants.TIMESTAMP_ISO8601,searchAfter));
                }
            }
        }
        NativeSearchQuery searchQuery  = searchQueryBuilder.build();
        searchQuery.setMaxResults(query.getSize() > 0 ? query.getSize() : default_response_size);
        return searchQuery;
    }
}
