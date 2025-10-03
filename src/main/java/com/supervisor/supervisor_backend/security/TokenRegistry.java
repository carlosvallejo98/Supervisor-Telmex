package com.supervisor.supervisor_backend.security;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Guarda tokens válidos emitidos por /auth/login del supervisor-backend */
@Component
public class TokenRegistry {
    private static final long TTL_SECONDS = 60L * 60L * 12L; // 12h
    private final Map<String, Long> tokens = new ConcurrentHashMap<>();

    public void allow(String token) {
        if (token == null || token.isBlank()) return;
        tokens.put(token, Instant.now().getEpochSecond() + TTL_SECONDS);
    }

    public boolean isAllowed(String token) {
        if (token == null || token.isBlank()) return false;
        Long exp = tokens.get(token);
        if (exp == null) return false;
        long now = Instant.now().getEpochSecond();
        if (now > exp) {
            tokens.remove(token);
            return false;
        }
        return true;
    }
}
