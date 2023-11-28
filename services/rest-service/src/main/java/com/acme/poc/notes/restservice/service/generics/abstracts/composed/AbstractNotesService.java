package com.acme.poc.notes.restservice.service.generics.abstracts.composed;

import com.acme.poc.notes.models.INoteEntity;
import com.acme.poc.notes.restservice.service.generics.queries.SearchByExternalGuid;
import com.acme.poc.notes.restservice.service.generics.queries.generics.IQuery;
import com.acme.poc.notes.restservice.service.generics.abstracts.disctinct.AbstractNotesAdminService;
import com.acme.poc.notes.restservice.service.generics.abstracts.disctinct.AbstractNotesClientService;
import com.acme.poc.notes.restservice.service.generics.interfaces.INotesAdminService;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

/**
 * This is composed of all features of admin and client. this can be extended to perform all operations
 * @param <E>
 */
public abstract class AbstractNotesService<E extends INoteEntity<E>> extends AbstractNotesClientService<E> implements INotesAdminService<E> {
    private AbstractNotesAdminService<E> abstractNotesAdminService;
    
    protected abstract List<E> search(IQuery query);
    
    public AbstractNotesService(CrudRepository crudRepository) {
        super(crudRepository);
        abstractNotesAdminService = new AbstractNotesAdminService<E>(crudRepository) {
            @Override
            protected List<E> search(IQuery query) {
                return AbstractNotesService.this.search(query);
            }
        };
    }
    
    @Override
    public List<E> getAll(String indexName) {
        return abstractNotesAdminService.getAll(indexName);
    }

    @Override
    public List<E> searchByExternalGuid(SearchByExternalGuid query) {
        return abstractNotesAdminService.searchByExternalGuid(query);
    }

    @Override
    public List<E> deleteByExternalGuid(UUID externalGuid) {
        return abstractNotesAdminService.deleteByExternalGuid(externalGuid);
    }

    @Override
    public List<E> deleteByEntryGuid(UUID entryGuid) {
        return abstractNotesAdminService.deleteByEntryGuid(entryGuid);
    }

    @Override
    public List<E> deleteByThreadGuid(UUID threadGuid) {
        return abstractNotesAdminService.deleteByThreadGuid(threadGuid);
    }

    @Override
    public E deleteByGuid(UUID guid) {
        return abstractNotesAdminService.deleteByGuid(guid);
    }
}
