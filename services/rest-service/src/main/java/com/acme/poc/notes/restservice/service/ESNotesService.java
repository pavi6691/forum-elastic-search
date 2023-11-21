package com.acme.poc.notes.restservice.service;
import com.acme.poc.notes.core.enums.NotesAPIError;
import com.acme.poc.notes.restservice.persistence.elasticsearch.models.NotesData;
import com.acme.poc.notes.restservice.persistence.elasticsearch.queries.generics.enums.ResultFormat;
import com.acme.poc.notes.restservice.persistence.elasticsearch.repositories.ESNotesRepository;
import com.acme.poc.notes.restservice.persistence.elasticsearch.generics.INotesProcessor;
import com.acme.poc.notes.restservice.persistence.elasticsearch.queries.SearchArchivedByEntryGuid;
import com.acme.poc.notes.restservice.persistence.elasticsearch.queries.SearchArchivedByExternalGuid;
import com.acme.poc.notes.restservice.persistence.elasticsearch.queries.SearchByContent;
import com.acme.poc.notes.restservice.persistence.elasticsearch.queries.SearchByEntryGuid;
import com.acme.poc.notes.restservice.persistence.elasticsearch.queries.generics.AbstractQuery;
import com.acme.poc.notes.restservice.persistence.elasticsearch.queries.generics.IQuery;
import com.acme.poc.notes.restservice.service.generics.AbstractService;
import com.acme.poc.notes.restservice.util.ESUtil;
import com.acme.poc.notes.restservice.util.LogUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.stereotype.Service;
import java.util.*;
import static com.acme.poc.notes.restservice.util.ExceptionUtil.throwRestError;


/**
 * Service to perform elastic search operations
 */
@Slf4j
@Service
public class ESNotesService extends AbstractService<NotesData> implements INotesService<NotesData> {
    public ESNotesService(INotesProcessor iNotesProcessor,
                          ESNotesRepository esNotesRepository) {
        super(iNotesProcessor, esNotesRepository);
    }

    /**
     * Archive by updating existing entry. updates archived field on elastic search with current date and time.
     *
     * @param query - archive is done querying by either externalGuid / entryGuid
     * @return archived entries
     */
    @Override
    public List<NotesData> archive(IQuery query) {
        log.debug("{} request: {}", LogUtil.method(), query.getClass().getSimpleName());
        List<NotesData> searchHitList = getAllEntries(query);
        query.setResultFormat(ResultFormat.FLATTEN);
        List<NotesData> processed = iNotesProcessor.process(query, searchHitList.stream().iterator());
        try {
            archive(processed);
        } catch (Exception e) {
            log.error(String.format(NotesAPIError.ERROR_ON_ELASTICSEARCH.errorMessage(), LogUtil.method(), e.getMessage()), e);
            throwRestError(NotesAPIError.ERROR_ON_ELASTICSEARCH, LogUtil.method(), e.getMessage());
        }
        AbstractQuery getArchived = (AbstractQuery)query;
        getArchived.setIncludeArchived(true);
        return iNotesProcessor.process(getArchived, getAllEntries(getArchived).stream().iterator());
    }

    /**
     * Archive by updating existing entry. updates archived field on elastic search with current date and time.
     *
     * @param guid - archive is done querying by guid
     * @return archived entries
     */
    @Override
    public List<NotesData> archive(UUID guid) {
        log.debug("{} guid: {}", LogUtil.method(), guid.toString());
        Optional<NotesData> result = crudRepository.findById(guid);
        if (!result.isPresent()) {
            throwRestError(NotesAPIError.ERROR_NOT_EXISTS_GUID, guid);
        }
        archive(List.of(result.get()));
        return List.of((NotesData) crudRepository.findById(guid).orElse(null));
    }

    @Override
    public List<NotesData> searchByEntryGuid(SearchByEntryGuid iQuery) {
        log.debug("{} entryGuid: {}", LogUtil.method(), iQuery.getSearchGuid());
        return search(iQuery);
    }

    @Override
    public List<NotesData> searchByContent(SearchByContent iQuery) {
        log.debug("{} content: {}", LogUtil.method(), iQuery.getContentToSearch());
        iQuery.setResultFormat(ResultFormat.FLATTEN);
        return search(iQuery);
    }

    @Override
    public List<NotesData> searchArchivedByExternalGuid(SearchArchivedByExternalGuid iQuery) {
        log.debug("{} externalGuid: {}", LogUtil.method(), iQuery.getSearchGuid());
        return search(iQuery);
    }

    @Override
    public List<NotesData> searchArchivedByEntryGuid(SearchArchivedByEntryGuid iQuery) {
        log.debug("{} entryGuid: {}", LogUtil.method(), iQuery.getSearchGuid());
        return search(iQuery);
    }

    @Override
    public List<NotesData> deleteArchivedByExternalGuid(SearchArchivedByExternalGuid iQuery) {
        log.debug("{} externalGuid: {}", LogUtil.method(), iQuery.getSearchGuid());
        return delete(iQuery);
    }

    @Override
    public List<NotesData> deleteArchivedByEntryGuid(SearchArchivedByEntryGuid iQuery) {
        log.debug("{} entryGuid: {}", LogUtil.method(), iQuery.getSearchGuid());
        return delete(iQuery);
    }

    private void archive(List<NotesData> entriesToArchive) {
        Date dateTime = ESUtil.getCurrentDate();
        try {
            entriesToArchive.forEach(entryToArchive -> crudRepository.save(NotesData.builder()
                    .archived(dateTime)
                    .externalGuid(entryToArchive.getExternalGuid())
                    .entryGuid(entryToArchive.getEntryGuid())
                    .guid(entryToArchive.getGuid())
                    .threadGuid(entryToArchive.getThreadGuid())
                    .entryGuidParent(entryToArchive.getEntryGuidParent())
                    .type(entryToArchive.getType())
                    .content(entryToArchive.getContent())
                    .created(entryToArchive.getCreated())
                    .build())
            );
        } catch (Exception e) {
            log.error(String.format(NotesAPIError.ERROR_ON_ELASTICSEARCH.errorMessage(), LogUtil.method(), e), e);
            throwRestError(NotesAPIError.ERROR_ON_ELASTICSEARCH,LogUtil.method(), e.getMessage());
        }
        log.debug("Number of entries archived: {}", entriesToArchive.size());
    }
}
