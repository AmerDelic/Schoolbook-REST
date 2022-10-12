package com.amerd.schoolbook.domain.user;

import java.util.Map;

@FunctionalInterface
public interface FieldSetter<T> {
    void setFieldValue(T targetObj, Map<String, String> updateValues);

    default String getValue(Map<String, String> updateValues, String targetField) {
        if (!updateValues.containsKey(targetField)) return null;
        return updateValues.get(targetField);
    }
}
