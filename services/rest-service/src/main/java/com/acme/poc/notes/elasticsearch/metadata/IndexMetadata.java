package com.acme.poc.notes.elasticsearch.metadata;

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
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect
@AllArgsConstructor
@NoArgsConstructor
public class IndexMetadata {

    @Setter @Getter
    private String docType;
    @Setter @Getter
    private String mappingFile;
    @Setter @Getter
    private String templateFile;
    @Setter @Getter
    private String policyFile;
    @Setter @Getter
    private Set<String> cmd;
    @Setter @Getter
    private IndexType indexType;
    @Setter @Getter
    @JsonProperty("isEnableUpdate")
    private boolean enableUpdate;
}