package com.Emporia.Configuration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class CustomObjectMapper extends ObjectMapper {
    public CustomObjectMapper() {
        super();
        this.registerModule(new JavaTimeModule());
        this.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        this.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,false);
        this.configure(MapperFeature.DEFAULT_VIEW_INCLUSION, false);
        this.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        this.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        this.disable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        this.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        this.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
    }

    public <T> String writeAsString(T value) {
        try {
            return writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Unprocessable data, cause by", e);
        }
    }

    public <T> T read(String value, Class<T> type) {
        try {
            return readValue(value, type);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Unprocessable data, cause by", e);
        }
    }

    public <T> T read(String value, TypeReference<T> reference) {
        try {
            return readerFor(reference).readValue(value);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Unprocessable data, cause by", e);
        }
    }
}
