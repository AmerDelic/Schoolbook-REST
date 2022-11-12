package com.amerd.schoolbook.common.jsonutil;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Optional;

public class JsonUtil {

    public static String getNodeStringValueOrNull(JsonNode jsonNode, String key) {
        return Optional.ofNullable(jsonNode.get(key))
                .map(JsonNode::asText)
                .orElse(null);
    }

    public static String getNodeStringValueOrThrow(JsonNode jsonNode, String key) {
        return Optional.ofNullable(jsonNode.get(key))
                .map(JsonNode::asText)
                .orElseThrow(() -> new IllegalArgumentException(String.format("Expected %s, but found null", key)));
    }

    public static Optional<String> getOptionalNodeStringValue(JsonNode jsonNode, String key) {
        return Optional.ofNullable(jsonNode.get(key)).map(JsonNode::asText);
    }
}
