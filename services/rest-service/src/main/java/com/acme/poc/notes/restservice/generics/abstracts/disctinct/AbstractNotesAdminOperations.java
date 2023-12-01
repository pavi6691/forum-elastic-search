package com.acme.poc.notes.restservice.generics.abstracts.disctinct;

import com.acme.poc.notes.models.INoteEntity;
import com.acme.poc.notes.restservice.generics.queries.IQueryRequest;
import com.acme.poc.notes.restservice.generics.queries.QueryRequest;
import com.acme.poc.notes.restservice.generics.queries.enums.Filter;
import com.acme.poc.notes.restservice.generics.queries.enums.Match;
import com.acme.poc.notes.restservice.generics.queries.enums.ResultFormat;
import com.acme.poc.notes.restservice.generics.interfaces.INotesAdminOperations;
import com.acme.poc.notes.restservice.util.LogUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Set;
import java.util.UUID;


/**
 * This allows operations that only admin can perform
 * @param <E>
 */
@Slf4j
@Service
public abstract class AbstractNotesAdminOperations<E extends INoteEntity<E>> extends AbstractNotesCrudOperations<E> implements INotesAdminOperations<E> {


    public AbstractNotesAdminOperations(CrudRepository crudRepository) {
        super(crudRepository);
    }


    @Override
    public List<E> getAll(String indexName) {
        log.debug("{} index: {}", LogUtil.method(), indexName);
        return getProcessed(
                QueryRequest.builder()
                        .filters(Set.of(Filter.INCLUDE_VERSIONS, Filter.INCLUDE_ARCHIVED))
                        .build());
    }

    @Override
    public List<E> deleteByExternalGuid(UUID externalGuid) {
        log.debug("{} externalGuid: {}", LogUtil.method(), externalGuid.toString());
        return delete(QueryRequest.builder()
                .searchField(Match.EXTERNAL)
                .searchData(externalGuid.toString())
                .filters(Set.of(Filter.INCLUDE_VERSIONS, Filter.INCLUDE_ARCHIVED))
                .resultFormat(ResultFormat.FLATTEN)
                .build());
    }

    @Override
    public List<E> deleteByEntryGuid(UUID entryGuid) {
        log.debug("{} entryGuid: {}", LogUtil.method(), entryGuid.toString());
        return delete(QueryRequest.builder()
                .searchField(Match.ENTRY)
                .searchData(entryGuid.toString())
                .filters(Set.of(Filter.INCLUDE_VERSIONS, Filter.INCLUDE_ARCHIVED))
                .resultFormat(ResultFormat.FLATTEN)
                .build());
    }

    @Override
    public List<E> deleteByThreadGuid(UUID threadGuid) {
        log.debug("{} threadGuid: {}", LogUtil.method(), threadGuid.toString());
        return delete(QueryRequest.builder()
                .searchField(Match.THREAD)
                .searchData(threadGuid.toString())
                .filters(Set.of(Filter.INCLUDE_VERSIONS, Filter.INCLUDE_ARCHIVED))
                .resultFormat(ResultFormat.FLATTEN)
                .build());
    }

    @Override
    public E deleteByGuid(UUID guid) {
        log.debug("{} guid: {}", LogUtil.method(), guid.toString());
        return delete(guid);
    }

}
