package com.acme.poc.notes.restservice.generics.abstracts.disctinct;

import com.acme.poc.notes.models.INoteEntity;
import com.acme.poc.notes.restservice.generics.queries.QueryRequest;
import com.acme.poc.notes.restservice.generics.queries.enums.Filter;
import com.acme.poc.notes.restservice.generics.interfaces.INotesAdminOperations;
import com.acme.poc.notes.restservice.util.LogUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Set;


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
    public List<E> get(String dsName) {
        log.debug("{} request: {}, name: {}", LogUtil.method(), "get from data store", dsName);
        return getProcessed(
                QueryRequest.builder()
                        .filters(Set.of(Filter.INCLUDE_VERSIONS, Filter.INCLUDE_ARCHIVED))
                        .build());
    }

}
