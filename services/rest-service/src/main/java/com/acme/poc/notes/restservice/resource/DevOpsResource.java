package com.acme.poc.notes.restservice.resource;

import com.acme.poc.notes.core.NotesConstants;
import com.acme.poc.notes.core.enums.NotesAPIError;
import com.acme.poc.notes.restservice.util.LogUtil;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Slf4j
@RestController
@RequestMapping(NotesConstants.API_ENDPOINT_PREFIX + NotesConstants.API_ENDPOINT_DEVOPS)
public class DevOpsResource {


    @Operation(summary = "Get errors", description = "Retrieve all possible errors for client requests", tags = { NotesConstants.OPENAPI_DEVOPS_TAG })
    @GetMapping(NotesConstants.API_ENDPOINT_DEVOPS_ERRORS)
    public ResponseEntity<Object> getErrorDefinitions() {
        log.debug("{}", LogUtil.method());

        List<Object> errors = Arrays.stream(NotesAPIError.values())
                .map(notesAPIError -> {
                    Map<String, String> enumValues = new HashMap<>();
                    enumValues.put("name", notesAPIError.name());
                    enumValues.put("httpStatusCode", Integer.toString(notesAPIError.httpStatusCode()));
                    enumValues.put("errorCode", Integer.toString(notesAPIError.errorCode()));
                    enumValues.put("errorMessage", notesAPIError.errorMessage());
                    return enumValues;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(errors);
    }

}
