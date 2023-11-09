package com.acme.poc.notes.service;
import com.acme.poc.notes.elasticsearch.esrepo.ESNotesRepository;
import com.acme.poc.notes.elasticsearch.generics.INotesOperations;
import com.acme.poc.notes.elasticsearch.metadata.ResourceFileReaderService;
import com.acme.poc.notes.elasticsearch.pojo.NotesData;
import com.acme.poc.notes.elasticsearch.queries.SearchAll;
import com.acme.poc.notes.elasticsearch.queries.SearchByEntryGuid;
import com.acme.poc.notes.elasticsearch.queries.SearchByExternalGuid;
import com.acme.poc.notes.elasticsearch.queries.SearchByThreadGuid;
import com.acme.poc.notes.service.generics.AbstractESService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.*;

@Slf4j
@Service
public class ESAdminNotesService extends AbstractESService implements INotesAdminService {
    public ESAdminNotesService(@Qualifier("notesProcessorV3") INotesOperations iNotesOperations,
                               ESNotesRepository esNotesRepository,
                               ElasticsearchOperations elasticsearchOperations,
                               ResourceFileReaderService resourceFileReaderService) {
        super(iNotesOperations, esNotesRepository, elasticsearchOperations,resourceFileReaderService);
    }

    @Override
    public List<NotesData> getAll(String indexName) {
        log.debug("getting all entries for index ={}", indexName);
        return iNotesOperations.fetchAndProcessEsResults(
                SearchAll.builder().includeVersions(true).includeArchived(true).build());
    }

    @Override
    public List<NotesData> searchByExternalGuid(SearchByExternalGuid query) {
        log.debug("Search by external guid = {}", query.getSearchGuid());
        return search(query);
    }

    @Override
    public List<NotesData> deleteByExternalGuid(UUID externalGuid) {
        log.debug("delete by external guid = {} ", externalGuid.toString());
        return delete(SearchByExternalGuid.builder()
                .searchGuid(externalGuid.toString())
                .includeVersions(true)
                .includeArchived(true)
                .build());
    }

    @Override
    public List<NotesData> deleteByEntryGuid(UUID entryGuid) {
        log.debug("delete by entry guid = {} ", entryGuid.toString());
        return delete(SearchByEntryGuid.builder()
                .searchGuid(entryGuid.toString())
                .includeVersions(true).includeArchived(true).build());
    }

    @Override
    public List<NotesData> deleteByThreadGuid(UUID threadGuid) {
        log.debug("delete by thread guid = {} ", threadGuid.toString());
        return delete(SearchByThreadGuid.builder()
                .searchGuid(threadGuid.toString())
                .includeVersions(true).includeArchived(true).build());
    }

    @Override
    public NotesData deleteByGuid(UUID guid) {
        log.debug("delete by guid = {} ", guid.toString());
        return delete(guid.toString());
    }

    /**
     * Create new index if not exists
     * @param indexName - index name to create
     * @return indexName when successfully created, else message that says index already exists
     */
    @Override
    public String createIndex(String indexName) {
        try {
            log.debug("Creating an index if not exists. indexName = {} ", indexName);
// TODO
//            IndexMetadataConfiguration indexMetadataConfiguration =
//            resourceFileReaderService.getDocsPropertyFile(Constants.APPLICATION_YAML,this.getClass());
//            Template template = resourceFileReaderService.getTemplateFile(Constants.NOTE_V1_INDEX_TEMPLATE,this.getClass());
//            mapping = String.format(mapping, NotesConstants.TIMESTAMP_ISO8601,NotesConstants.TIMESTAMP_ISO8601);
//            String mapping  = resourceFileReaderService.getMappingFromFile(Constants.NOTE_V1_INDEX_MAPPINGS,this.getClass());
//            PolicyInfo policyInfo  = resourceFileReaderService.getPolicyFile(Constants.NOTE_V1_INDEX_POLICY,this.getClass());
            IndexOperations indexOperations = elasticsearchOperations.indexOps(IndexCoordinates.of(indexName));
            if(!indexOperations.exists()) {
                indexOperations.create();
                return new ObjectMapper().writeValueAsString(indexOperations.getInformation());
            } else {
                log.info("Index already exists, indexName= {} ", indexName);
                return String.format("Index already exists, indexName=%s", indexName);
            }
        } catch (IOException e) {
            log.error("Exception while creating an index = " + indexName, e);
            throw new RuntimeException(e);
        }
    }
}
