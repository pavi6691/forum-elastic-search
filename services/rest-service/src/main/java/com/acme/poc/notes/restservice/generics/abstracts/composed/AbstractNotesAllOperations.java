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
    public List<E> getAll(String indexName) {
        return adminService.getAll(indexName);
    }

    @Override
    public List<E> searchByExternalGuid(IQueryRequest query) {
        return adminService.searchByExternalGuid(query);
    }

    @Override
    public List<E> deleteByExternalGuid(UUID externalGuid) {
        return adminService.deleteByExternalGuid(externalGuid);
    }

    @Override
    public List<E> deleteByEntryGuid(UUID entryGuid) {
        return adminService.deleteByEntryGuid(entryGuid);
    }

    @Override
    public List<E> deleteByThreadGuid(UUID threadGuid) {
        return adminService.deleteByThreadGuid(threadGuid);
    }

    @Override
    public E deleteByGuid(UUID guid) {
        return adminService.deleteByGuid(guid);
    }

}
