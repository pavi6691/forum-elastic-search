package com.acme.poc.notes.restservice.service.generics.abstracts.disctinct;

import com.acme.poc.notes.models.INoteEntity;
import com.acme.poc.notes.restservice.service.generics.interfaces.INotesAdminService;
import com.acme.poc.notes.restservice.service.generics.queries.SearchAll;
import com.acme.poc.notes.restservice.service.generics.queries.SearchByEntryGuid;
import com.acme.poc.notes.restservice.service.generics.queries.SearchByExternalGuid;
import com.acme.poc.notes.restservice.service.generics.queries.SearchByThreadGuid;
import com.acme.poc.notes.restservice.service.generics.queries.generics.enums.ResultFormat;
import com.acme.poc.notes.restservice.util.LogUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;


/**
 * This allows operations that only admin can perform
 * @param <E>
 */
@Slf4j
@Service
public abstract class AbstractNotesAdminService<E extends INoteEntity<E>> extends AbstractNotesCrudService<E> implements INotesAdminService<E> {


    public AbstractNotesAdminService(CrudRepository crudRepository) {
        super(crudRepository);
    }


    @Override
    public List<E> getAll(String indexName) {
        log.debug("{} index: {}", LogUtil.method(), indexName);
        return getProcessed(
                SearchAll.builder()
                        .includeVersions(true)
                        .includeArchived(true)
                        .build());
    }

    @Override
    public List<E> searchByExternalGuid(SearchByExternalGuid query) {
        log.debug("{} externalGuid: {}", LogUtil.method(), query.getSearchGuid());
        return get(query);
    }

    @Override
    public List<E> deleteByExternalGuid(UUID externalGuid) {
        log.debug("{} externalGuid: {}", LogUtil.method(), externalGuid.toString());
        return delete(SearchByExternalGuid.builder()
                .searchGuid(externalGuid.toString())
                .includeVersions(true)
                .includeArchived(true)
                .resultFormat(ResultFormat.FLATTEN)
                .build());
    }

    @Override
    public List<E> deleteByEntryGuid(UUID entryGuid) {
        log.debug("{} entryGuid: {}", LogUtil.method(), entryGuid.toString());
        return delete(SearchByEntryGuid.builder()
                .searchGuid(entryGuid.toString())
                .includeVersions(true)
                .includeArchived(true)
                .resultFormat(ResultFormat.FLATTEN)
                .build());
    }

    @Override
    public List<E> deleteByThreadGuid(UUID threadGuid) {
        log.debug("{} threadGuid: {}", LogUtil.method(), threadGuid.toString());
        return delete(SearchByThreadGuid.builder()
                .searchGuid(threadGuid.toString())
                .includeVersions(true)
                .includeArchived(true)
                .resultFormat(ResultFormat.FLATTEN)
                .build());
    }

    @Override
    public E deleteByGuid(UUID guid) {
        log.debug("{} guid: {}", LogUtil.method(), guid.toString());
        return delete(guid);
    }

}
