package com.nex.test.demo.adapter.nef.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nex.test.demo.adapter.iface.SourceService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.*;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Collections;

@Service
public class NefSourceServiceImpl implements SourceService {

    private static final String TOKEN_TYPE = "Bearer";
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    private final Logger log = LogManager.getLogger("NEF-TESTING");

    private final String user;
    private final String password;

    volatile private String token;
    private String nefHost;


    public NefSourceServiceImpl(
            RestTemplate restTemplate,
            ObjectMapper objectMapper,
            RestTemplate restTemplate1, @Value("${nef.user}") String user,
            @Value("${nef.password}") String password,
            @Value("${nef.Host}") String nefHost) {
        this.objectMapper = objectMapper;
        this.restTemplate = restTemplate1;
        this.user = user;
        this.password = password;
        this.nefHost = nefHost;
        connect();
    }

    @Override
    public String healthCheck() {
        StringBuilder result = new StringBuilder();
        result.append("SystemSource ").append("NEF Adapter").append("\n")
                .append("NEF connection status: ").append(checkConnection() ? "on" : "off").append("\n");
        return result.toString();
    }

    @Override
    public void connect() {
        if (checkConnection()) return;
        synchronized (NefSourceServiceImpl.class) {
            try {
                String oauthToken = "/auth/login";

                String host = nefHost + oauthToken;
                log.info("Sending authentication request to " + host);
                ResponseEntity<String> response = sendAuthRequest(host, user, password);
                JsonNode root = objectMapper.readTree(response.getBody());
                if (!root.path("token").isNull()) {
                    token = root.path("token").asText();
                    log.info("Token: " + token);
                } else {
                    throw new RuntimeException("WFM consumer settings wasn't properly set");
                }
            } catch (IOException ioe) {
                log.error("Error occurred during reading a response.", ioe);
            } catch (RuntimeException re) {
                log.error(re);
            }
        }
    }

    @Override
    public ResponseEntity<String> sendRequest(String targetApi, Object requestBody, HttpMethod method) {
        try {
            String host = nefHost + targetApi;
            HttpEntity<String> request;
            if(requestBody!=null){
                String requestSerialized = objectMapper.writeValueAsString(requestBody);
                log.debug("Sending on host: " + host + " message: " + requestSerialized);
                request = new HttpEntity<>(requestSerialized, buildHeaders(token));
            }else{
                request = new HttpEntity<>(buildHeaders(token));
            }
            return restTemplate.exchange(host, method, request, String.class);

        } catch (JsonProcessingException jpe) {
            log.error("Error occurred during building request.", jpe);
            throw new UnsupportedOperationException(jpe);
        }
    }

    @Override
    public ResponseEntity<String> sendRequest(String host){
        return sendRequest(host,null, HttpMethod.GET);
    }

    @Override
    public ResponseEntity<String> sendAuthRequest(String host, String userName, String password) {
        final String authRequestBody = buildAuthRequestBody(userName, password);
        log.info("Sending on host: " + host + " message: " + authRequestBody);
        HttpEntity<String> request = new HttpEntity<>(authRequestBody, buildAuthHeaders());
        return restTemplate.postForEntity(host, request, String.class);
    }

    private boolean checkConnection() {
        synchronized (NefSourceServiceImpl.class) {
            return token != null;
        }
    }

    private HttpHeaders buildHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        if (token != null && !token.isEmpty()){
            StringBuilder tokenBuilder = new StringBuilder();
            tokenBuilder.append(TOKEN_TYPE)
                    .append(" ")
                    .append(token);
            headers.add("authorization", tokenBuilder.toString());
        }
        return headers;
    }

    private String buildAuthRequestBody(String userName, String password) {
        StringBuilder builder = new StringBuilder();
        builder.append("&username=").append(userName)
                .append("&password=").append(password);
        return builder.toString();
    }

    private HttpHeaders buildAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        return headers;
    }

    static {
        disableSslVerification();
    }

    /**
     * Turn off SSL
     */
    private static void disableSslVerification() {
        try
        {
            // Create a trust manager that does not validate certificate chains
            TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }
            };

            // Install the all-trusting trust manager
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            // Create all-trusting host name verifier
            HostnameVerifier allHostsValid = new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };

            // Install the all-trusting host verifier
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
    }


}
