package com.supervisor.supervisor_backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Set;

@Service
public class ProxyService {

    private final RestTemplate rest;
    private final String base;

    // Headers CORS que NO debemos reenviar al navegador
    private static final Set<String> CORS_HEADERS = Set.of(
            "Access-Control-Allow-Origin",
            "Access-Control-Allow-Credentials",
            "Access-Control-Allow-Headers",
            "Access-Control-Allow-Methods",
            "Vary"
    );

    public ProxyService(RestTemplate rest, @Value("${main.api.base}") String base) {
        this.rest = rest;
        this.base = base;
    }

    private HttpHeaders headersWithAuth(String auth) {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        if (auth != null && auth.startsWith("Bearer ")) {
            h.set("Authorization", auth);
        }
        return h;
    }

    /** Quita headers CORS del backend principal para que no choquen con los de Spring */
    private HttpHeaders sanitize(HttpHeaders upstream) {
        HttpHeaders out = new HttpHeaders();
        upstream.forEach((k, v) -> {
            if (!CORS_HEADERS.contains(k)) {
                out.put(k, v);
            }
        });
        return out;
    }

    public ResponseEntity<String> postJson(String auth, String path, String body) {
        HttpHeaders h = headersWithAuth(auth);
        HttpEntity<String> req = new HttpEntity<>(body, h);
        try {
            ResponseEntity<String> resp = rest.exchange(base + path, HttpMethod.POST, req, String.class);
            return ResponseEntity.status(resp.getStatusCode())
                    .headers(sanitize(resp.getHeaders()))
                    .body(resp.getBody());
        } catch (HttpStatusCodeException ex) {
            return ResponseEntity.status(ex.getStatusCode())
                    .headers(sanitize(ex.getResponseHeaders() == null ? new HttpHeaders() : ex.getResponseHeaders()))
                    .body(ex.getResponseBodyAsString());
        }
    }

    public ResponseEntity<String> get(String auth, String path, MultiValueMap<String, String> params) {
        HttpHeaders h = headersWithAuth(auth);
        HttpEntity<Void> req = new HttpEntity<>(h);

        String url = UriComponentsBuilder
                .fromHttpUrl(base + path)
                .queryParams(params == null ? new org.springframework.util.LinkedMultiValueMap<>() : params)
                .build(true)
                .toUriString();

        try {
            ResponseEntity<String> resp = rest.exchange(url, HttpMethod.GET, req, String.class);
            return ResponseEntity.status(resp.getStatusCode())
                    .headers(sanitize(resp.getHeaders()))
                    .body(resp.getBody());
        } catch (HttpStatusCodeException ex) {
            return ResponseEntity.status(ex.getStatusCode())
                    .headers(sanitize(ex.getResponseHeaders() == null ? new HttpHeaders() : ex.getResponseHeaders()))
                    .body(ex.getResponseBodyAsString());
        }
    }
}
