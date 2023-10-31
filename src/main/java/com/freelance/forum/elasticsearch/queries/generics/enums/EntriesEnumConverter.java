package com.freelance.forum.elasticsearch.queries.generics.enums;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class EntriesEnumConverter implements Converter<String, Entries> {
    @Override
    public Entries convert(String source) {
        return Entries.valueOf(source.toUpperCase());
    }
}
