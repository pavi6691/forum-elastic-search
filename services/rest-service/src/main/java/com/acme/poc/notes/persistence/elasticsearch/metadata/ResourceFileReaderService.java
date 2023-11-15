package com.acme.poc.notes.persistence.elasticsearch.metadata;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.commons.io.IOUtils;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * reads template, policy or mapping json file from resource path and store. 
 * provides ability to get content of the same for multiple indexes as well.
 */
@Configuration
public class ResourceFileReaderService {
    
    public IndexMetadataConfiguration getDocsPropertyFile(String filePath, Class resourceClass) throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        IndexMetadataConfiguration indexMetadataConfiguration;
        indexMetadataConfiguration = mapper.readValue(getFileFromResources(filePath, resourceClass), IndexMetadataConfiguration.class);
        return indexMetadataConfiguration;
    }
    
    public PolicyInfo getPolicyFile(String policyFile, Class resourceClass) {
        ObjectMapper mapper = new ObjectMapper();
        PolicyInfo getPolicyInfo;
        try {
            String filePath = Constants.POLICY_DIR + policyFile;
            getPolicyInfo = mapper.readValue(getFileFromResources(filePath, resourceClass), PolicyInfo.class);
        } catch (IOException | NullPointerException e) {
            throw new RuntimeException("policy file does not exist");
        }
        return getPolicyInfo;

    }
    
    public Template getTemplateFile(String templateFile, Class resourceClass) {
        ObjectMapper mapper = new ObjectMapper();
        Template template;
        try {
            String filePath = Constants.TEMPLATE_DIR + templateFile;
            template = mapper.readValue(getFileFromResources(filePath, resourceClass), Template.class);
        } catch (IOException | NullPointerException e) {
            throw new RuntimeException("template file does not exist");
        }
        return template;

    }
    
    public String getMappingFromFile(String mappingFile, Class resourceClass) throws IOException {
        String mappingString;
        try {
            String filePath = Constants.MAPPING_DIR + mappingFile;
            mappingString =  getFileFromResources(filePath, resourceClass);
        } catch (IOException | NullPointerException e) {
            throw new RuntimeException("mapping file does not exist"+resourceClass.toString());
        }
        return mappingString;
    }
    
    private String getFileFromResources(String fileName, Class resourceClass) throws IOException {
        return IOUtils.toString(
                Objects.requireNonNull(
                        resourceClass.getClassLoader().getResourceAsStream(fileName)), StandardCharsets.UTF_8);
    }

}