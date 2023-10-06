package com.freelance.forum.elasticsearch.configuration;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Set;

/**
 * The POJO Index metadata which is used in IndexMetadataConfiguration
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect
public class IndexMetadata {

    private String docType;
    private String mappingFile;
    private String templateFile;
    private String policyFile;
    private Set<String> cmd;
    private IndexType indexType;
    @JsonProperty("isEnableUpdate")
    private boolean enableUpdate;

    public IndexMetadata(String docType, String mappingFile, String templateFile, String policyFile, Set<String> cmd,
                         IndexType indexType, boolean enableUpdate) {
        this.docType = docType;
        this.mappingFile = mappingFile;
        this.templateFile = templateFile;
        this.policyFile = policyFile;
        this.cmd = cmd;
        this.indexType = indexType;
        this.enableUpdate = enableUpdate;
    }

    public IndexMetadata() {
    }

    public String getDocType() {
        return docType;
    }

    public void setDocType(String docType) {
        this.docType = docType;
    }

    public String getMappingFile() {
        return mappingFile;
    }

    public void setMappingFile(String mappingFile) {
        this.mappingFile = mappingFile;
    }

    public String getTemplateFile() {
        return templateFile;
    }

    public void setTemplateFile(String templateFile) {
        this.templateFile = templateFile;
    }

    public String getPolicyFile() {
        return policyFile;
    }

    public void setPolicyFile(String policyFile) {
        this.policyFile = policyFile;
    }

    public Set<String> getCmd() {
        return cmd;
    }

    public void setCmd(Set<String> cmd) {
        this.cmd = cmd;
    }

    public IndexType getIndexType() {
        return indexType;
    }

    public void setIndexType(IndexType indexType) {
        this.indexType = indexType;
    }

    public boolean isEnableUpdate() {
        return enableUpdate;
    }

    public void setEnableUpdate(boolean enableUpdate) {
        this.enableUpdate = enableUpdate;
    }


}