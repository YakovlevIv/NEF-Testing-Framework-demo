package com.nex.test.demo.core.service.utils;

import java.util.HashMap;
import java.util.Map;

public class ScenarioScope {

    private Map<String,Object> headers;
    private Map<String,Object> jsonPaths;

    public ScenarioScope() {
        headers = new HashMap<>();
        jsonPaths = new HashMap<>();
    }

    public Map<String, Object> getHeaders() {
        return headers;
    }

    public Map<String, Object> getJsonPaths() {
        return jsonPaths;
    }

    /**
     * Check a scenario scope variable exists and match the given value
     * @param property property to check
     * @param value expected value
     * @return true if values are matching for a given property, false otherwise
     */
    public boolean checkProperty(String property, String value) {
        Object headerValue = headers.get(property);
        boolean isHeader = headerValue != null && headerValue.equals(value);

        Object jsonPathValue = jsonPaths.get(property);
        boolean isJsonPath = jsonPathValue != null && jsonPathValue.equals(value);

        return isHeader || isJsonPath;
    }
}
