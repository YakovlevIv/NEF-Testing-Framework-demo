package com.nex.test.demo.adapter.iface;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

public interface SourceService {
    String healthCheck();

    void connect();

    ResponseEntity<String> sendRequest(String host, Object requestBody,  HttpMethod method);

    ResponseEntity<String> sendRequest(String host);

    ResponseEntity<String> sendAuthRequest(String host, String userName, String password);
}
