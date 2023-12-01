package com.acme.poc.notes.restservice.service.esservice;

import com.acme.poc.notes.restservice.persistence.elasticsearch.models.NotesData;
import com.acme.poc.notes.restservice.generics.abstracts.disctinct.AbstractNotesAdminOperations;
import com.acme.poc.notes.restservice.generics.queries.IQueryRequest;
import com.acme.poc.notes.restservice.persistence.elasticsearch.repositories.ESNotesRepository;
import com.acme.poc.notes.restservice.util.LogUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.stereotype.Service;

import java.util.List;


@Slf4j
@Service
public class ESNotesAdminOperations extends AbstractNotesAdminOperations<NotesData> {

    @Value("${default.number.of.entries.to.return}")
    private int default_size_configured;

    ElasticsearchOperations elasticsearchOperations;
    com.acme.poc.notes.restservice.service.esservice.ESNotesClientOperations esNotesClientService;


    public ESNotesAdminOperations(ESNotesRepository esNotesRepository, ElasticsearchOperations elasticsearchOperations,
                                  com.acme.poc.notes.restservice.service.esservice.ESNotesClientOperations esNotesClientService) {
        super(esNotesRepository);
        this.elasticsearchOperations = elasticsearchOperations;
        this.esNotesClientService = esNotesClientService;
    }


    /**
     * Executes IQueryRequest
     * @param query
     * @return search result from elastics search response
     */
    @Override
    protected List<NotesData> search(IQueryRequest query) {
        return esNotesClientService.search(query);
    }
    
    /**
     * Create new index if not exists
     *
     * @param indexName - index name to create
     * @return indexName when successfully created, else message that says index already exists
     */
    public String createIndex(String indexName) {
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
