package com.giraone.io.copier.web.index;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class AutoIndexReader {

    private static final TypeReference<List<AutoIndexItem>> listAutoIndexItemTypeRef
        = new TypeReference<>() {
    };
    private static final ObjectMapper objectMapper = buildObjectMapper();

    public AutoIndexReader() {
    }

    public List<AutoIndexItem> read(InputStream in) throws IOException {
        return objectMapper.readValue(in, listAutoIndexItemTypeRef);
    }

    private static ObjectMapper buildObjectMapper() {

        final ObjectMapper mapper = new ObjectMapper();
        // Be tolerant in reading
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // Date/Date-Time settings
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }
}
