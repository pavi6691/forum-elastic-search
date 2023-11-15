package com.acme.poc.notes.restservice.persistence.elasticsearch.metadata;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Template {

    private String name;
    private int version;
    private String pattern;
    private String aliasName;
    private int shards;
    private int replicas;
    private int maxRegexLength;

}
