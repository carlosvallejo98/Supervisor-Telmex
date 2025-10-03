package com.supervisor.supervisor_backend.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.supervisor.supervisor_backend.security.TokenRegistry;
import com.supervisor.supervisor_backend.service.ProxyService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final ProxyService proxy;
    private final TokenRegistry tokenRegistry;
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${allowed.supervisor.emails}")
    private String allowedEmailsCsv;

    public AuthController(ProxyService proxy, TokenRegistry tokenRegistry) {
        this.proxy = proxy;
        this.tokenRegistry = tokenRegistry;
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody String body) {
        ResponseEntity<String> resp = proxy.postJson(null, "/auth/login", body);

        try {
            JsonNode root = mapper.readTree(resp.getBody());
            JsonNode user = root.get("user");
            JsonNode tokenNode = root.get("token");
            if (user == null || tokenNode == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("{\"message\":\"Login inválido\"}");
            }

            String email = user.path("email").asText("");
            String token = tokenNode.asText("");

            // Validar lista blanca
            boolean allowed = false;
            for (String e : allowedEmailsCsv.split(",")) {
                if (email.equalsIgnoreCase(e.trim())) {
                    allowed = true; break;
                }
            }
            if (!allowed) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("{\"message\":\"Usuario no permitido en la app de supervisión\"}");
            }

            // Registrar token para acceso posterior
            tokenRegistry.allow(token);

            // Devolver tal cual la respuesta original (token,user,...)
            return ResponseEntity.status(resp.getStatusCode()).body(resp.getBody());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("{\"message\":\"Login inválido\"}");
        }
    }
}
