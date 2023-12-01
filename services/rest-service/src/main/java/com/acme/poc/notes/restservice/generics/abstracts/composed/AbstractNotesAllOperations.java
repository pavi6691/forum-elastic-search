package com.acme.poc.notes.restservice.generics.abstracts.composed;

import com.acme.poc.notes.models.INoteEntity;
import com.acme.poc.notes.restservice.generics.abstracts.disctinct.AbstractNotesAdminOperations;
import com.acme.poc.notes.restservice.generics.abstracts.disctinct.AbstractNotesClientOperations;
import com.acme.poc.notes.restservice.generics.queries.IQueryRequest;
import com.acme.poc.notes.restservice.generics.interfaces.INotesAdminOperations;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;


/**
 * This is composed of all features of admin and client. this can be extended to perform all operations
 * @param <E>
 */
public abstract class AbstractNotesAllOperations<E extends INoteEntity<E>> extends AbstractNotesClientOperations<E> implements INotesAdminOperations<E> {

    private INotesAdminOperations<E> adminService;
    
    protected abstract List<E> search(IQueryRequest query);
    

    public AbstractNotesAllOperations(CrudRepository crudRepository) {
        super(crudRepository);
        adminService = new AbstractNotesAdminOperations<E>(crudRepository) {
            @Override
            protected List<E> search(IQueryRequest query) {
                return AbstractNotesAllOperations.this.search(query);
            }
        };
    }


    @Override
    public List<E> get(String indexName) {
        return adminService.get(indexName);
    }

    @Override
    public List<E> delete(IQueryRequest queryRequest) {
        return adminService.delete(queryRequest);
    }

    @Override
    public E delete(UUID guid) {
        return adminService.delete(guid);
    }

}
