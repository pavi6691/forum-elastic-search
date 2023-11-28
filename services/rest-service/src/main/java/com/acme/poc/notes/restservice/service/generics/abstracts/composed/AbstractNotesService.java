package com.acme.poc.notes.restservice.service.generics.abstracts.composed;

import com.acme.poc.notes.models.INoteEntity;
import com.acme.poc.notes.restservice.service.generics.abstracts.disctinct.AbstractNotesAdminService;
import com.acme.poc.notes.restservice.service.generics.abstracts.disctinct.AbstractNotesClientService;
import com.acme.poc.notes.restservice.service.generics.interfaces.INotesAdminService;
import com.acme.poc.notes.restservice.service.generics.queries.SearchByExternalGuid;
import com.acme.poc.notes.restservice.service.generics.queries.generics.IQuery;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;


/**
 * This is composed of all features of admin and client. this can be extended to perform all operations
 * @param <E>
 */
public abstract class AbstractNotesService<E extends INoteEntity<E>> extends AbstractNotesClientService<E> implements INotesAdminService<E> {

    private INotesAdminService<E> adminService;
    
    protected abstract List<E> search(IQuery query);
    

    public AbstractNotesService(CrudRepository crudRepository) {
        super(crudRepository);
        adminService = new AbstractNotesAdminService<E>(crudRepository) {
            @Override
            protected List<E> search(IQuery query) {
                return AbstractNotesService.this.search(query);
            }
        };
    }


    @Override
    public List<E> getAll(String indexName) {
        return adminService.getAll(indexName);
    }

    @Override
    public List<E> searchByExternalGuid(SearchByExternalGuid query) {
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
