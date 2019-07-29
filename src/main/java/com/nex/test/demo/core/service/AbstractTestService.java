package com.nex.test.demo.core.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import com.nex.test.demo.adapter.iface.SourceService;
import com.nex.test.demo.core.service.utils.ScenarioScope;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;

import java.io.IOException;
import java.util.*;

abstract class AbstractTestService {

        private String targetApi;
        private Map jsonBody;
        private HttpHeaders headers;
        private Map<String, String> queryParams;
        private ResponseEntity<String> responseEntity;
        private ObjectMapper objectMapper;
        protected ScenarioScope scenarioScope;
        protected final SourceService sourceService;

        AbstractTestService(SourceService sourceService) {
            this.sourceService = sourceService;
            this.scenarioScope = new ScenarioScope();
            objectMapper = new ObjectMapper();
            headers = new HttpHeaders();
            queryParams = new HashMap<>();
        }

        void setHeader(String name, String value) {
            Assert.notNull(name, "Header name must not be null");
            Assert.notNull(value, "Header value must not be null");
            headers.set(name, value);
        }

        void addQueryParameters(Map<String, String> newParams){
            Assert.notNull(newParams, "Query parameters must not be null");
            Assert.isTrue(!newParams.isEmpty(), "Query parameters must not be empty");
            queryParams.putAll(newParams);
        }

        void addHeaders(Map<String, String> newHeaders){
            Assert.notNull(newHeaders, "New header must not be null");
            Assert.isTrue(!newHeaders.isEmpty(), "new header must not be null");
            newHeaders.forEach((key, value) -> {

                List<String> headerValues = this.headers.get(key);
                if (headerValues == null) {
                    headerValues = Collections.singletonList(value);
                } else {
                    headerValues.add(value);
                }
                this.headers.put(key, headerValues);
            });
        }

        void setBody(String body) throws IOException {
            Assert.notNull(body, "Request body must not be null");
            Assert.isTrue(!body.isEmpty(), "Request body must not be empty");
            this.jsonBody = objectMapper.readValue(body, Map.class);
        }

        void request(String resource, HttpMethod method) {
            Assert.notNull(resource, "Request resource must not be null");

            Assert.notNull(method, "Request method must not be null");

            if(!resource.contains("/") && !resource.isEmpty()) {
                resource = "/" + resource;
            }

            responseEntity = this.sourceService.sendRequest(targetApi +resource, jsonBody, method);
            Assert.notNull(responseEntity, "Response must not be null");
        }


        void checkStatus(int status, boolean isNot){
            Assert.isTrue(status > 0, "Wrong status code" + status);
            Assert.isTrue(isNot == (responseEntity.getStatusCodeValue() != status), "Status not valid");
        }


        List<String> checkHeaderExists(String headerName, boolean isNot){
            Assert.notNull(headerName, "Header is null");
            Assert.isTrue(!headerName.isEmpty(), "Header is empty");
            Assert.notNull(responseEntity.getHeaders(), "Response headers is null");
            if(!isNot) {
                Assert.notNull(responseEntity.getHeaders().get(headerName), "Response headers is null");
                return responseEntity.getHeaders().get(headerName);
            } else {
                Assert.isNull(responseEntity.getHeaders().get(headerName), "Response headers is not null");
                return null;
            }
        }


        void checkHeaderEqual(String headerName, String headerValue, boolean isNot){
            Assert.notNull(headerName, "Header name is null");
            Assert.isTrue(!headerName.isEmpty(), "Header name is empty");

            Assert.notNull(headerValue, "Header value is null");
            Assert.isTrue(!headerValue.isEmpty(), "Header value is empty");

            Assert.notNull(responseEntity.getHeaders(), "Headers is null");

            if(!isNot) {
                Assert.isTrue(responseEntity.getHeaders().get(headerName).contains(headerValue), "Header is not equal");
            } else {
                Assert.isTrue(!responseEntity.getHeaders().get(headerName).contains(headerValue), "Header is equal");
            }
        }


        void checkJsonBody() throws IOException {
            String body = responseEntity.getBody();
            Assert.notNull(body, "Body is null");
            Assert.isTrue(!body.isEmpty(), "Body is empty");

            // Check body json structure is valid
            objectMapper.readValue(body,Map.class);
        }

        void checkBodyContains(String bodyValue) {
            Assert.notNull(bodyValue, "Body value is null");
            Assert.isTrue(!bodyValue.isEmpty(), "Body value is empty");

            Assert.isTrue(responseEntity.getBody().contains(bodyValue), "Body not contains "+ bodyValue);
        }


        Object checkJsonPathExists(String jsonPath){
            return getJsonPath(jsonPath);
        }

        void checkJsonPath(String jsonPath, String jsonValue, boolean isNot){
            Object pathValue = checkJsonPathExists(jsonPath);
            Assert.isTrue(!String.valueOf(pathValue).isEmpty(), "Path is empty");

            if(!isNot) {
                Assert.isTrue(pathValue.equals(jsonValue), "Json path not exist");
            } else {
                Assert.isTrue(!pathValue.equals(jsonValue), "Json path exist");
            }
        }

        void checkJsonPathIsArray(String jsonPath, int length){
            Object pathValue = getJsonPath(jsonPath);

            Assert.isTrue(pathValue instanceof Collection, "Path no array");
            if(length != -1) {
                Assert.isTrue(((Collection)pathValue).size() == length, "Wrong array size");
            }
        }

        void storeHeader(String headerName, String headerAlias){

            Assert.notNull(headerName, "Header name is null");
            Assert.isTrue(!headerName.isEmpty(), "Header name is empty");

            Assert.notNull(headerAlias, "Alias name is null");
            Assert.isTrue(!headerAlias.isEmpty(), "Alias name is empty");

            List<String> headerValues = checkHeaderExists(headerName, false);
            Assert.notNull(headerValues, "Header values is null");
            Assert.isTrue(!headerValues.isEmpty(), "Headers value is empty");

           scenarioScope.getHeaders().put(headerAlias, headerValues);
        }

        void storeJsonPath(String jsonPath, String jsonPathAlias){
            Assert.notNull(jsonPath, "Json path not exist");
            Assert.isTrue(!jsonPath.isEmpty(), "Path is empty");

            Assert.notNull(jsonPathAlias, "Alias name is null");
            Assert.isTrue(!jsonPathAlias.isEmpty(), "Alias name is empty");

            Object pathValue = getJsonPath(jsonPath);
           scenarioScope.getJsonPaths().put(jsonPathAlias, pathValue);
        }

        void checkScenarioVariable(String property, String value){
            Assert.isTrue(scenarioScope.checkProperty(property, value));
        }

        private ReadContext getBodyDocument(){
            ReadContext ctx = JsonPath.parse(responseEntity.getBody());
            Assert.notNull(ctx, "Body value is null");

            return ctx;
        }

        private Object getJsonPath(String jsonPath){

            Assert.notNull(jsonPath, "Json path not exist");
            Assert.isTrue(!jsonPath.isEmpty(), "Path is empty");

            ReadContext ctx = getBodyDocument();
            Object pathValue = ctx.read(jsonPath);

            Assert.notNull(pathValue, "Json path value is null");

            return pathValue;
        }

    protected void setTargetApi(String api){
        this.targetApi = api;
    };
}
