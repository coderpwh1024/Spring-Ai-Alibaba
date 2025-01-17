package com.coderpwh.reader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Collections;
import java.util.stream.StreamSupport;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentReader;
import org.springframework.ai.reader.EmptyJsonMetadataGenerator;
import org.springframework.ai.reader.JsonMetadataGenerator;
import org.springframework.core.io.Resource;

public class JsonReader implements DocumentReader {


    private Resource resource;

    private JsonMetadataGenerator jsonMetadataGenerator;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * The key from the JSON that we will use as the text to parse into the Document text
     */
    private List<String> jsonKeysToUse;

    public JsonReader(Resource resource) {
        this(resource, new ArrayList<>().toArray(new String[0]));
    }

    public JsonReader(Resource resource, String... jsonKeysToUse) {
        this(resource, new EmptyJsonMetadataGenerator(), jsonKeysToUse);
    }

    public JsonReader(Resource resource, JsonMetadataGenerator jsonMetadataGenerator, String... jsonKeysToUse) {
        Objects.requireNonNull(jsonKeysToUse, "keys must not be null");
        Objects.requireNonNull(jsonMetadataGenerator, "jsonMetadataGenerator must not be null");
        Objects.requireNonNull(resource, "The Spring Resource must not be null");
        this.resource = resource;
        this.jsonMetadataGenerator = jsonMetadataGenerator;
        this.jsonKeysToUse = List.of(jsonKeysToUse);
    }

    @Override
    public List<Document> get() {
        try {
            JsonNode rootNode = objectMapper.readTree(this.resource.getInputStream());

            if (rootNode.isArray()) {
                return StreamSupport.stream(rootNode.spliterator(), true)
                        .map(jsonNode -> parseJsonNode(jsonNode, objectMapper))
                        .toList();
            }
            else {
                return Collections.singletonList(parseJsonNode(rootNode, objectMapper));
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Document parseJsonNode(JsonNode jsonNode, ObjectMapper objectMapper) {
        Map<String, Object> item = objectMapper.convertValue(jsonNode, new TypeReference<Map<String, Object>>() {
        });
        StringBuilder sb = new StringBuilder();

        jsonKeysToUse.stream().filter(item::containsKey).forEach(key -> {
            sb.append(key).append(": ").append(item.get(key)).append(System.lineSeparator());
        });

        Map<String, Object> metadata = this.jsonMetadataGenerator.generate(item);
        String content = sb.isEmpty() ? item.toString() : sb.toString();
        return new Document(content, metadata);
    }
}
