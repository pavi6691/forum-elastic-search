package com.acme.poc.notes.restservice.serialzation;

import com.acme.poc.notes.core.NotesConstants;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class CustomDateSerializer extends JsonSerializer<Date> {
    
    private final SimpleDateFormat formatter = new SimpleDateFormat(NotesConstants.TIMESTAMP_ISO8601);


    @Override
    public void serialize(Date value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeString(formatter.format(value));
    }

}
