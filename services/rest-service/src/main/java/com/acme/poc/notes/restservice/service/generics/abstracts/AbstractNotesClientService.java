package com.acme.poc.notes.restservice.service.generics.abstracts;
import com.acme.poc.notes.core.enums.NotesAPIError;
import com.acme.poc.notes.models.INoteEntity;
import com.acme.poc.notes.restservice.service.generics.interfaces.INotesClientService;
import com.acme.poc.notes.restservice.persistence.elasticsearch.queries.*;
import com.acme.poc.notes.restservice.persistence.elasticsearch.queries.generics.AbstractQuery;
import com.acme.poc.notes.restservice.persistence.elasticsearch.queries.generics.IQuery;
import com.acme.poc.notes.restservice.persistence.elasticsearch.queries.generics.enums.ResultFormat;
import com.acme.poc.notes.restservice.util.ESUtil;
import com.acme.poc.notes.restservice.util.LogUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import java.util.*;
import static com.acme.poc.notes.restservice.util.ExceptionUtil.throwRestError;

@Slf4j
@Service
public abstract class AbstractNotesClientService<E extends INoteEntity<E>> extends AbstractNotesCrudOperations<E> 
        implements INotesClientService<E> {
    
    public AbstractNotesClientService(CrudRepository crudRepository) {
        super(crudRepository);
    }

    /**
     * Create new entry or a thread if entryGuidParent is provided
     *
     * @param entity Data for creating a new note entry
     * @return T that is created and stored in Elasticsearch
     */
    @Override
    public E create(E entity) {
        log.debug("{}", LogUtil.method());
        entity.setGuid(UUID.randomUUID());
        entity.setEntryGuid(UUID.randomUUID());
        entity.setThreadGuid(UUID.randomUUID());
        entity.setCreated(ESUtil.getCurrentDate());

        if (entity.getEntryGuidParent() != null) {  // It's a thread that needs to be created
            List<E> existingEntry = getProcessed(SearchByEntryGuid.builder()
                    .searchGuid(entity.getEntryGuidParent().toString())
                    .includeVersions(false)
                    .includeArchived(true)
                    .build());
            if (existingEntry == null || existingEntry.isEmpty()) {
                throwRestError(NotesAPIError.ERROR_NEW_RESPONSE_NO_THREAD_GUID, entity.getEntryGuidParent());
                return null;
            }
            E existingEntryFirst = existingEntry.get(0);
            if (existingEntryFirst.getArchived() != null) {
                throwRestError(NotesAPIError.ERROR_ENTRY_ARCHIVED_CANNOT_ADD_THREAD, existingEntryFirst.getExternalGuid(), existingEntryFirst.getEntryGuid());
                return null;
            }
            entity.setThreadGuid(existingEntryFirst.getThreadGuid());
            entity.setExternalGuid(existingEntryFirst.getExternalGuid());
            log.debug("Creating a thread for externalGuid: {}, entryGuid: {}", entity.getExternalGuid().toString(), entity.getEntryGuid().toString());
        } else {
            log.debug("Creating a new entry for externalGuid: {}", entity.getExternalGuid());
        }
        E newEntry = null;
        try {
            newEntry = (E) crudRepository.save(entity);
            if (newEntry == null) {
                log.error(String.format(NotesAPIError.ERROR_ON_ELASTICSEARCH.errorMessage(),LogUtil.method(),"ES returned null value"));
                throwRestError(NotesAPIError.ERROR_ON_ELASTICSEARCH,LogUtil.method(),"ES returned null value");
            }
        } catch (Exception e) {
            log.error(String.format(NotesAPIError.ERROR_ON_ELASTICSEARCH.errorMessage(),LogUtil.method(),e.getMessage()),e);
            throwRestError(NotesAPIError.ERROR_ON_ELASTICSEARCH,LogUtil.method(),e.getMessage());
        }
        log.debug("Successfully created a new entry entryGuid: {} ", newEntry.getEntryGuid());
        return newEntry;
    }

    /**
     * Search entry by guid
     *
     * @param guid
     * @return Entry from Elasticsearch for given guid
     */
    public E getByGuid(UUID guid) {
        log.debug("{} guid: {}", LogUtil.method(), guid.toString());
        return (E) crudRepository.findById(guid).orElse(null);
    }
    
    /**
     * Archive by updating existing entry. updates archived field on elastic search with current date and time.
     *
     * @param query - archive is done querying by either externalGuid / entryGuid
     * @return archived entries
     */
    @Override
    public List<E> archive(IQuery query) {
        log.debug("{} request: {}", LogUtil.method(), query.getClass().getSimpleName());
        List<E> searchHitList = getAllEntries(query);
        query.setResultFormat(ResultFormat.FLATTEN);
        List<E> processed = process(query, searchHitList.stream().iterator());
        try {
            archive(processed);
        } catch (Exception e) {
            log.error(String.format(NotesAPIError.ERROR_ON_ELASTICSEARCH.errorMessage(), LogUtil.method(), e.getMessage()), e);
            throwRestError(NotesAPIError.ERROR_ON_ELASTICSEARCH, LogUtil.method(), e.getMessage());
        }
        AbstractQuery getArchived = (AbstractQuery)query;
        getArchived.setIncludeArchived(true);
        return process(getArchived, getAllEntries(getArchived).stream().iterator());
    }

    /**
     * Archive by updating existing entry. updates archived field on elastic search with current date and time.
     *
     * @param guid - archive is done querying by guid
     * @return archived entries
     */
    @Override
    public List<E> archive(UUID guid) {
        log.debug("{} guid: {}", LogUtil.method(), guid.toString());
        Optional<E> result = crudRepository.findById(guid);
        if (!result.isPresent()) {
            throwRestError(NotesAPIError.ERROR_NOT_EXISTS_GUID, guid);
        }
        archive(List.of(result.get()));
        return List.of((E) crudRepository.findById(guid).orElse(null));
    }

    @Override
    public List<E> searchByEntryGuid(SearchByEntryGuid iQuery) {
        log.debug("{} entryGuid: {}", LogUtil.method(), iQuery.getSearchGuid());
        return search(iQuery);
    }

    @Override
    public List<E> searchByContent(SearchByContent iQuery) {
        log.debug("{} content: {}", LogUtil.method(), iQuery.getContentToSearch());
        iQuery.setResultFormat(ResultFormat.FLATTEN);
        return search(iQuery);
    }

    @Override
    public List<E> searchArchivedByExternalGuid(SearchArchivedByExternalGuid iQuery) {
        log.debug("{} externalGuid: {}", LogUtil.method(), iQuery.getSearchGuid());
        return search(iQuery);
    }

    @Override
    public List<E> searchArchivedByEntryGuid(SearchArchivedByEntryGuid iQuery) {
        log.debug("{} entryGuid: {}", LogUtil.method(), iQuery.getSearchGuid());
        return search(iQuery);
    }

    @Override
    public List<E> deleteArchivedByExternalGuid(SearchArchivedByExternalGuid iQuery) {
        log.debug("{} externalGuid: {}", LogUtil.method(), iQuery.getSearchGuid());
        return delete(iQuery);
    }

    @Override
    public List<E> deleteArchivedByEntryGuid(SearchArchivedByEntryGuid iQuery) {
        log.debug("{} entryGuid: {}", LogUtil.method(), iQuery.getSearchGuid());
        return delete(iQuery);
    }

    private void archive(List<E> entriesToArchive) {
        Date dateTime = ESUtil.getCurrentDate();
        try {
            entriesToArchive.forEach(entryToArchive -> {
                entryToArchive.setArchived(dateTime);
                crudRepository.save(entryToArchive);
            });
        } catch (Exception e) {
            log.error(String.format(NotesAPIError.ERROR_ON_ELASTICSEARCH.errorMessage(), LogUtil.method(), e), e);
            throwRestError(NotesAPIError.ERROR_ON_ELASTICSEARCH,LogUtil.method(), e.getMessage());
        }
        log.debug("Number of entries archived: {}", entriesToArchive.size());
    }
}
