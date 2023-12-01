package com.acme.poc.notes.restservice.generics.abstracts.disctinct;

import com.acme.poc.notes.core.enums.NotesAPIError;
import com.acme.poc.notes.models.INoteEntity;
import com.acme.poc.notes.restservice.generics.queries.IQueryRequest;
import com.acme.poc.notes.restservice.generics.queries.enums.Filter;
import com.acme.poc.notes.restservice.generics.queries.enums.ResultFormat;
import com.acme.poc.notes.restservice.generics.interfaces.INotesClientOperations;
import com.acme.poc.notes.restservice.util.ESUtil;
import com.acme.poc.notes.restservice.util.LogUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.acme.poc.notes.restservice.util.ExceptionUtil.throwRestError;


/**
 * This allows operations that a clients can perform
 * @param <E>
 */
@Slf4j
@Service
public abstract class AbstractNotesClientOperations<E extends INoteEntity<E>> extends AbstractNotesCrudOperations<E> implements INotesClientOperations<E> {


    public AbstractNotesClientOperations(CrudRepository crudRepository) {
        super(crudRepository);
    }


    /**
     * Archive by updating existing entry. updates archived field on database with current date and time.
     *
     * @param query - archive is done querying by either externalGuid / entryGuid
     * @return archived entries
     */
    @Override
    public List<E> archive(IQueryRequest query) {
        log.debug("{} request: {}, byField: {}, data: {}", LogUtil.method(), "archive", query.getSearchField(), query.getSearchData());
        List<E> searchHitList = getAll(query);
        query.setResultFormat(ResultFormat.FLATTEN);
        List<E> processed = process(query, searchHitList.stream().iterator());
        try {
            archive(processed);
        } catch (Exception e) {
            log.error(String.format(NotesAPIError.ERROR_ON_DB_OPERATION.errorMessage(), LogUtil.method(), e.getMessage()), e);
            throwRestError(NotesAPIError.ERROR_ON_DB_OPERATION, LogUtil.method(), e.getMessage());
        }
        query.getFilters().remove(Filter.EXCLUDE_ARCHIVED);
        query.getFilters().add(Filter.INCLUDE_ARCHIVED);
        return process(query, getAll(query).stream().iterator());
    }

    /**
     * Archive by updating existing entry. updates archived field on database with current date and time.
     *
     * @param guid - archive is done querying by guid
     * @return archived entries
     */
    @Override
    public List<E> archive(UUID guid) {
        log.debug("{} request: {}, byField: {}, data: {}", LogUtil.method(), "archive", "guid", guid);
        Optional<E> result = crudRepository.findById(guid);
        if (!result.isPresent()) {
            throwRestError(NotesAPIError.ERROR_NOT_EXISTS_GUID, guid);
        }
        archive(List.of(result.get()));
        return List.of((E) crudRepository.findById(guid).orElse(null));
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
