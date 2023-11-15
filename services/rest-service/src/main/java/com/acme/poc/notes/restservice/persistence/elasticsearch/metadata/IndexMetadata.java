package com.acme.poc.notes.restservice.persistence.elasticsearch.metadata;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;


/**
 * The POJO Index metadata which is used in IndexMetadataConfiguration
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect
@AllArgsConstructor
@NoArgsConstructor
public class IndexMetadata {

    private String docType;
    private String mappingFile;
    private String templateFile;
    private String policyFile;
    private Set<String> cmd;
    private IndexType indexType;
    @JsonProperty("isEnableUpdate")
    private boolean enableUpdate;

}
