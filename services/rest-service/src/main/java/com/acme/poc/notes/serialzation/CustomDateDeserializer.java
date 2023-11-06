package com.acme.poc.notes.serialzation;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.springframework.data.elasticsearch.RestStatusException;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CustomDateDeserializer extends JsonDeserializer<Date> {
    private final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX");

    @Override
    public Date deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException, JacksonException {
        if(StringUtils.isEmpty(jsonParser.getText())) {
            return null;
        }
        String dateStr = jsonParser.getText();
        try {
            return formatter.parse(dateStr);
        } catch (ParseException e) {
            throw new RestStatusException(HttpStatus.SC_BAD_REQUEST,
                    "Error parsing date: " + dateStr + " ErrorMessage : " + e.getMessage());
        }
    }
}
