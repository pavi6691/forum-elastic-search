package com.acme.poc.notes.restservice.service.pgsqlservice;
import com.acme.poc.notes.restservice.generics.queries.enums.Filter;
import com.acme.poc.notes.restservice.generics.queries.IQueryRequest;
import com.acme.poc.notes.restservice.generics.queries.enums.OperationStatus;
import com.acme.poc.notes.restservice.persistence.postgresql.models.PGNoteEntity;
import com.acme.poc.notes.restservice.persistence.postgresql.repositories.PGNotesRepository;
import com.acme.poc.notes.restservice.generics.abstracts.AbstractNotesOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 * Perform crud operations on POSTGRESQL and search is done on elasticsearch
 */
@Service("PGSQLNotesService")
public class PGSQLNotesService extends AbstractNotesOperations<PGNoteEntity> {

    @Value("${default.db.response.size}")
    private int default_size_configured;
    PGNotesRepository pgNotesRepository;
    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public PGSQLNotesService(PGNotesRepository pgNotesRepository) {
        super(pgNotesRepository);
        this.pgNotesRepository = pgNotesRepository;
    }

    @Override
    protected List<PGNoteEntity> searchDb(IQueryRequest queryRequest) {
        List<OperationStatus> operationStatuses = queryRequest.getOperationStatuses();
        switch (queryRequest.getSearchField()) {
            case ENTRY:
                if (queryRequest.getFilters().contains(Filter.INCLUDE_ONLY_ARCHIVED)) {
                    // Execute query for the "ENTRY" case with archiving filter
                    return pgNotesRepository.findByEntryGuidAndOperationStatusInOrderByCreatedAsc(UUID.fromString(queryRequest.getSearchData()),operationStatuses);
                } else {
                    // Execute query for the "ENTRY" case without archiving filter
                    return pgNotesRepository.findByEntryGuidAndCreatedGreaterThanEqualAndOperationStatusInOrderByCreatedAsc(
                            UUID.fromString(queryRequest.getSearchData()), Date.from(Instant.ofEpochMilli(queryRequest.getCreatedDateTime())),operationStatuses);
                }
            case EXTERNAL:
                if (queryRequest.getFilters().contains(Filter.INCLUDE_ONLY_ARCHIVED)) {
                    // Execute query for the "EXTERNAL" case with archiving filter
                    return pgNotesRepository.findByExternalGuidAndArchivedIsNotNullAndOperationStatusInOrderByCreatedAsc(UUID.fromString(queryRequest.getSearchData()),operationStatuses);
                } else {
                    // Execute query for the "EXTERNAL" case without archiving filter
                    return pgNotesRepository.findByExternalGuidAndCreatedGreaterThanEqualAndOperationStatusInOrderByCreatedAsc(
                            UUID.fromString(queryRequest.getSearchData()), Date.from(Instant.ofEpochMilli(queryRequest.getCreatedDateTime())),operationStatuses);
                }
            case CONTENT:
                // Execute query for the "CONTENT" case
                return pgNotesRepository.findByContentContainingIgnoreCaseAndOperationStatusInOrderByCreatedAsc(queryRequest.getSearchData(),operationStatuses);
            // Add other cases as needed
            default:
                // Handle the default case
                return query(queryRequest.getSearchField().getFieldName(), UUID.fromString(queryRequest.getSearchData()),
                        Date.from(Instant.ofEpochMilli(queryRequest.getCreatedDateTime())),operationStatuses);
        }
    }
    
    private List<PGNoteEntity> query(String fieldName, UUID fieldValue, Date createdDateTime, List<OperationStatus> operationStatuses) {
        String jpql = "SELECT e FROM note e WHERE e." + fieldName + " = :fieldValue " +
                "AND e.created >= :createdDateTime " +
                "AND e.operationStatus IN :operationStatuses " +
                "ORDER BY e.created ASC";
        Query query = entityManager.createQuery(jpql, PGNoteEntity.class)
                .setParameter("fieldValue", fieldValue)
                .setParameter("createdDateTime", createdDateTime)
                .setParameter("operationStatuses", operationStatuses);
        List<PGNoteEntity> results = query.getResultList();
        entityManager.clear();
        return results;
    }

}