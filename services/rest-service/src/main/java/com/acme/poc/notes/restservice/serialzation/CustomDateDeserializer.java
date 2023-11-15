package com.acme.poc.notes.restservice.serialzation;

import com.acme.poc.notes.core.NotesConstants;
import com.acme.poc.notes.core.enums.NotesAPIError;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.acme.poc.notes.restservice.util.ExceptionUtil.throwRestError;


public class CustomDateDeserializer extends JsonDeserializer<Date> {

    private final SimpleDateFormat formatter = new SimpleDateFormat(NotesConstants.TIMESTAMP_ISO8601);


    @Override
    public Date deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
        if (StringUtils.isEmpty(jsonParser.getText())) {
            return null;
        }
        String dateStr = jsonParser.getText();
        try {
            return formatter.parse(dateStr);
        } catch (ParseException e) {
            throwRestError(NotesAPIError.ERROR_PARSING_TIMESTAMP, dateStr);
        }
        throwRestError(NotesAPIError.ERROR_SERVER);
        return null;
    }

}
