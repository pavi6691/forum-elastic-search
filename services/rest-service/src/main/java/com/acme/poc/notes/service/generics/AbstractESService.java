package com.acme.poc.notes.service.generics;

import com.acme.poc.notes.core.NotesConstants;
import com.acme.poc.notes.elasticsearch.esrepo.ESNotesRepository;
import com.acme.poc.notes.elasticsearch.generics.INotesOperations;
import com.acme.poc.notes.elasticsearch.metadata.ResourceFileReaderService;
import com.acme.poc.notes.elasticsearch.pojo.NotesData;
import com.acme.poc.notes.elasticsearch.queries.generics.IQuery;
import com.acme.poc.notes.util.ESUtil;
import com.acme.poc.notes.util.LogUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.elasticsearch.RestStatusException;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public abstract class AbstractESService implements IESCommonOperations {
    @Value("${default.number.of.entries.to.return}")
    private int default_number_of_entries;
    protected INotesOperations iNotesOperations;
    protected ESNotesRepository esNotesRepository;
    protected ElasticsearchOperations elasticsearchOperations;
    protected ResourceFileReaderService resourceFileReaderService;

    public AbstractESService(@Qualifier("notesProcessorV3") INotesOperations iNotesOperations, ESNotesRepository esNotesRepository,
                             ElasticsearchOperations elasticsearchOperations, ResourceFileReaderService resourceFileReaderService) {
        this.iNotesOperations = iNotesOperations;
        this.esNotesRepository = esNotesRepository;
        this.elasticsearchOperations = elasticsearchOperations;
        this.resourceFileReaderService = resourceFileReaderService;
    }

    @Override
    public List<NotesData> search(IQuery query) {
        log.debug("{}", LogUtil.method());
        return iNotesOperations.fetchAndProcessEsResults(query);
    }

    @Override
    public List<SearchHit<NotesData>> getAllEntries(IQuery query) {
        log.debug("{}", LogUtil.method());
        long startTime = System.currentTimeMillis();
        boolean timeout = false;
        List<SearchHit<NotesData>> searchHitList = new ArrayList<>();
        SearchHits<NotesData> searchHits = iNotesOperations.getEsResults(query);
        searchHitList.addAll(searchHits.getSearchHits());
        while(searchHits != null && !searchHits.isEmpty() && !timeout && searchHits.getSearchHits().size() == default_number_of_entries) {
            searchHitList.addAll(searchHits.getSearchHits());
            List<Object> sortValues = searchHits.getSearchHits().get(searchHits.getSearchHits().size()-1).getSortValues();
            query.searchAfter(sortValues.size() > 0 ? sortValues.get(0) : null);
            searchHits = iNotesOperations.getEsResults(query);
            timeout = (System.currentTimeMillis() - startTime) >= NotesConstants.TIMEOUT_DELETE;
        }
        if(timeout) {
            long timeTaken = (System.currentTimeMillis() - startTime);
            log.error("delete entries operation timed out, time taken={}", timeTaken);
            throw new RestStatusException(HttpStatus.SC_REQUEST_TIMEOUT,String.format("delete entries operation timed out, time taken =%s",timeTaken));
        }
        if(searchHitList == null || searchHitList.isEmpty()) {
            log.error("No entries found for request = {}", query.getClass().getSimpleName());
            throw new RestStatusException(HttpStatus.SC_NOT_FOUND,String.format("No entries found to perform this operation"));
        }
        log.debug("Nr of entries found = {}",searchHitList.size());
        return searchHitList;
    }

    /**
     * Deletes entries by either externalGuid/entryGuid
     * @param query - search entries for given externalGuid/entryGuid
     * @return deleted entries
     */
    @Override
    public List<NotesData> delete(IQuery query) {
        log.debug("{} request: {}", LogUtil.method(), query.getClass().getSimpleName());
        List<SearchHit<NotesData>> searchHitList = getAllEntries(query);
        List<NotesData> processed = iNotesOperations.process(query,searchHitList.stream().iterator());
        Set<NotesData> flatten = new HashSet<>();
        try {
            ESUtil.flatten(processed,flatten);
            esNotesRepository.deleteAll(flatten);
        } catch (Exception e) {
            log.error("Exception while deleting entries for request " + query.getClass().getSimpleName(),e);
            throw new RestStatusException(HttpStatus.SC_INTERNAL_SERVER_ERROR,"Error while deleting entries. error = " + e.getMessage());
        }
        log.debug("successfully deleted all entries nrOfEntriesDeleted = {}",flatten.size());
        return processed;
    }

    @Override
    public NotesData delete(String keyGuid) {   // TODO Check if this can be changed to be a UUID instead?
        log.debug("{} keyGuid: {}", LogUtil.method(), keyGuid);
        UUID guid = UUID.fromString(keyGuid);
        NotesData notesData = esNotesRepository.findById(guid).orElse(null);
        if(notesData != null) {
            esNotesRepository.deleteById(guid);
            if (!esNotesRepository.findById(guid).isPresent()) {
                log.debug("successfully deleted an entry with key guid = {}", notesData.getGuid().toString());
                return notesData;
            }
        }
        log.error("cannot delete. No entry found for given guid = {}",keyGuid);
        throw new RestStatusException(HttpStatus.SC_NOT_FOUND,"cannot delete. No entry found for given GUID");
    }

}
