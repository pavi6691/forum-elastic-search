package com.acme.poc.notes.restservice.service.esservice;

import com.acme.poc.notes.core.NotesConstants;
import com.acme.poc.notes.core.enums.NotesAPIError;
import com.acme.poc.notes.restservice.generics.abstracts.AbstractNotesOperations;
import com.acme.poc.notes.restservice.persistence.elasticsearch.models.NotesData;
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
import java.util.List;
import java.util.stream.Collectors;

import static com.acme.poc.notes.restservice.util.ExceptionUtil.throwRestError;


/**
 * Service to perform elastic search operations
 */
@Slf4j
@Service("esNotesService")
@Primary
public class ESNotesService extends AbstractNotesOperations<NotesData> {
    
    @Value("${default.number.of.entries.to.return}")
    private int default_size_configured;

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
    protected List<NotesData> search(IQueryRequest query) {
        return elasticsearchOperations.search(getEsQuery(query), NotesData.class).stream()
                .map(sh -> sh.getContent()).collect(Collectors.toList());
    }
    
    public NativeSearchQuery getEsQuery(IQueryRequest query) {
        NativeSearchQueryBuilder searchQueryBuilder = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.wrapperQuery(com.acme.poc.notes.restservice.service.esservice.ESQueryBuilder.build(query)))
                .withSort(Sort.by(Sort.Order.asc(Field.CREATED.getMatch())));
        if (query.searchAfter() != null && !(query.getSearchField().equals(Field.ENTRY))) {
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

    /**
     * Create new index if not exists
     *
     * @param indexName - index name to create
     * @return indexName when successfully created, else message that says index already exists
     */
    public Object createDataStore(String indexName) {
        log.debug("{} index: {}", LogUtil.method(), indexName);
        try {
// TODO
//            IndexMetadataConfiguration indexMetadataConfiguration =
//            resourceFileReaderService.getDocsPropertyFile(Constants.APPLICATION_YAML,this.getClass());
//            Template template = resourceFileReaderService.getTemplateFile(Constants.NOTE_V1_INDEX_TEMPLATE,this.getClass());
//            mapping = String.format(mapping, NotesConstants.TIMESTAMP_ISO8601,NotesConstants.TIMESTAMP_ISO8601);
//            String mapping  = resourceFileReaderService.getMappingFromFile(Constants.NOTE_V1_INDEX_MAPPINGS,this.getClass());
//            PolicyInfo policyInfo  = resourceFileReaderService.getPolicyFile(Constants.NOTE_V1_INDEX_POLICY,this.getClass());
            IndexOperations indexOperations = elasticsearchOperations.indexOps(IndexCoordinates.of(indexName));
            if (!indexOperations.exists()) {
                indexOperations.create();
//                return new ObjectMapper().writeValueAsString(indexOperations.getInformation());
                return indexOperations.getInformation().get(0).getName();
            } else {
                log.info("Index already exists: {} ", indexName);
                return String.format("Index already exists: %s", indexName);
            }
        } catch (Exception e) {
            log.error("Exception while creating index: " + indexName, e);
            throw new RuntimeException(e);
        }
    }

}
