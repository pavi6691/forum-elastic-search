package com.acme.poc.notes.restservice.service.generics;

import com.acme.poc.notes.core.NotesConstants;
import com.acme.poc.notes.core.enums.NotesAPIError;
import com.acme.poc.notes.restservice.persistence.elasticsearch.generics.INotesOperations;
import com.acme.poc.notes.restservice.persistence.elasticsearch.metadata.ResourceFileReaderService;
import com.acme.poc.notes.restservice.persistence.elasticsearch.models.NotesData;
import com.acme.poc.notes.restservice.persistence.elasticsearch.queries.generics.IQuery;
import com.acme.poc.notes.restservice.persistence.elasticsearch.queries.generics.enums.ResultFormat;
import com.acme.poc.notes.restservice.persistence.elasticsearch.repositories.ESNotesRepository;
import com.acme.poc.notes.restservice.util.ESUtil;
import com.acme.poc.notes.restservice.util.LogUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.acme.poc.notes.restservice.util.ExceptionUtil.throwRestError;


@Slf4j
@Service
public abstract class AbstractESService implements IESCommonOperations {

    @Value("${default.number.of.entries.to.return}")
    private int default_number_of_entries;
    protected INotesOperations iNotesOperations;
    protected ESNotesRepository esNotesRepository;
    protected ElasticsearchOperations elasticsearchOperations;
    protected ResourceFileReaderService resourceFileReaderService;


    public AbstractESService(@Qualifier("NotesProcessor") INotesOperations iNotesOperations, ESNotesRepository esNotesRepository, ElasticsearchOperations elasticsearchOperations, ResourceFileReaderService resourceFileReaderService) {
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
        while (searchHits != null && !searchHits.isEmpty() && !timeout && searchHits.getSearchHits().size() == default_number_of_entries) {
            searchHitList.addAll(searchHits.getSearchHits());
            List<Object> sortValues = searchHits.getSearchHits().get(searchHits.getSearchHits().size() - 1).getSortValues();
            query.searchAfter(sortValues.size() > 0 ? sortValues.get(0) : null);
            searchHits = iNotesOperations.getEsResults(query);
            timeout = (System.currentTimeMillis() - startTime) >= NotesConstants.TIMEOUT_DELETE;
        }
        if (timeout) {
            long timeTaken = (System.currentTimeMillis() - startTime);
            log.error("Delete entries operation timed out, time taken: {} ms", timeTaken);
            throwRestError(NotesAPIError.ERROR_TIMEOUT_DELETE, timeTaken);
        }
        if (searchHitList == null || searchHitList.isEmpty()) {
            log.error("No entries found for request: {}", query.getClass().getSimpleName());
            throwRestError(NotesAPIError.ERROR_NOT_FOUND);
        }
        log.debug("Number of entries found: {}",searchHitList.size());
        return searchHitList;
    }

    /**
     * Deletes entries by either externalGuid/entryGuid
     *
     * @param query search entries for given externalGuid/entryGuid
     * @return deleted entries
     */
    @Override
    public List<NotesData> delete(IQuery query) {
        log.debug("{} request: {}", LogUtil.method(), query.getClass().getSimpleName());
        List<SearchHit<NotesData>> searchHitList = getAllEntries(query);
        query.setResultFormat(ResultFormat.FLATTEN);
        List<NotesData> processed = iNotesOperations.process(query,searchHitList.stream().iterator());
        try {
            esNotesRepository.deleteAll(processed);
        } catch (Exception e) {
            log.error("Error while deleting entries for request: {} -- " + query.getClass().getSimpleName(), e.getMessage());
            throwRestError(NotesAPIError.ERROR_SERVER);
        }
        log.debug("Successfully deleted all {} entries",processed.size());
        return processed;
    }

    @Override
    public NotesData delete(UUID keyGuid) {
        log.debug("{} keyGuid: {}", LogUtil.method(), keyGuid);
        NotesData notesData = esNotesRepository.findById(keyGuid).orElse(null);
        if (notesData != null) {
            esNotesRepository.deleteById(keyGuid);
            if (!esNotesRepository.findById(keyGuid).isPresent()) {
                log.debug("Successfully deleted an entry with guid: {}", notesData.getGuid());
                return notesData;
            }
        }
        log.error("Cannot delete. No entry found for given guid: {}", keyGuid);
        throwRestError(NotesAPIError.ERROR_NOT_FOUND);
        return null;
    }

}
