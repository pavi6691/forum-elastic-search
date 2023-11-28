package com.acme.poc.notes.restservice.service.generics.abstracts.disctinct;

import com.acme.poc.notes.core.enums.NotesAPIError;
import com.acme.poc.notes.models.INoteEntity;
import com.acme.poc.notes.restservice.service.generics.queries.SearchArchivedByEntryGuid;
import com.acme.poc.notes.restservice.service.generics.queries.SearchArchivedByExternalGuid;
import com.acme.poc.notes.restservice.service.generics.queries.SearchByContent;
import com.acme.poc.notes.restservice.service.generics.queries.SearchByEntryGuid;
import com.acme.poc.notes.restservice.service.generics.queries.generics.AbstractQuery;
import com.acme.poc.notes.restservice.service.generics.queries.generics.IQuery;
import com.acme.poc.notes.restservice.service.generics.queries.generics.enums.ResultFormat;
import com.acme.poc.notes.restservice.service.generics.interfaces.INotesClientService;
import com.acme.poc.notes.restservice.util.ESUtil;
import com.acme.poc.notes.restservice.util.LogUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.acme.poc.notes.restservice.util.ExceptionUtil.throwRestError;

/**
 * This allows operations that a clients can perform
 * @param <E>
 */
@Slf4j
@Service
public abstract class AbstractNotesClientService<E extends INoteEntity<E>> extends AbstractNotesCrudService<E> implements INotesClientService<E> {
    
    public AbstractNotesClientService(CrudRepository crudRepository) {
        super(crudRepository);
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
        List<E> searchHitList = getAll(query);
        query.setResultFormat(ResultFormat.FLATTEN);
        List<E> processed = process(query, searchHitList.stream().iterator());
        try {
            archive(processed);
        } catch (Exception e) {
            log.error(String.format(NotesAPIError.ERROR_ON_DB_OPERATION.errorMessage(), LogUtil.method(), e.getMessage()), e);
            throwRestError(NotesAPIError.ERROR_ON_DB_OPERATION, LogUtil.method(), e.getMessage());
        }
        AbstractQuery getArchived = (AbstractQuery)query;
        getArchived.setIncludeArchived(true);
        return process(getArchived, getAll(getArchived).stream().iterator());
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
        return get(iQuery);
    }

    @Override
    public List<E> searchByContent(SearchByContent iQuery) {
        log.debug("{} content: {}", LogUtil.method(), iQuery.getContentToSearch());
        iQuery.setResultFormat(ResultFormat.FLATTEN);
        return get(iQuery);
    }

    @Override
    public List<E> searchArchivedByExternalGuid(SearchArchivedByExternalGuid iQuery) {
        log.debug("{} externalGuid: {}", LogUtil.method(), iQuery.getSearchGuid());
        return get(iQuery);
    }

    @Override
    public List<E> searchArchivedByEntryGuid(SearchArchivedByEntryGuid iQuery) {
        log.debug("{} entryGuid: {}", LogUtil.method(), iQuery.getSearchGuid());
        return get(iQuery);
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
            log.error(String.format(NotesAPIError.ERROR_ON_DB_OPERATION.errorMessage(), LogUtil.method(), e), e);
            throwRestError(NotesAPIError.ERROR_ON_DB_OPERATION,LogUtil.method(), e.getMessage());
        }
        log.debug("Number of entries archived: {}", entriesToArchive.size());
    }

}
