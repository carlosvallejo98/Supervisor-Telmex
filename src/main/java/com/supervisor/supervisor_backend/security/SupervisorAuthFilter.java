package com.supervisor.supervisor_backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class SupervisorAuthFilter extends OncePerRequestFilter {

    private final TokenRegistry tokenRegistry;
    private final AntPathMatcher matcher = new AntPathMatcher();

    public SupervisorAuthFilter(TokenRegistry tokenRegistry) {
        this.tokenRegistry = tokenRegistry;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // Permitir login y OPTIONS sin filtro
        return "OPTIONS".equalsIgnoreCase(request.getMethod())
                || matcher.match("/auth/**", path);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        // Manejo explícito de preflight para que lleve CORS siempre
        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            addCorsHeaders(req, res);
            res.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        String auth = req.getHeader("Authorization");
        String token = (auth != null && auth.startsWith("Bearer ")) ? auth.substring(7) : null;

        if (!tokenRegistry.isAllowed(token)) {
            addCorsHeaders(req, res); // 👈 agrega CORS también en 403
            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
            res.setContentType("application/json");
            res.getWriter().write("{\"message\":\"No autorizado para la app de supervisión\"}");
            return;
        }

        // Si está autorizado, continúa
        chain.doFilter(req, res);
    }

    private void addCorsHeaders(HttpServletRequest req, HttpServletResponse res) {
        String origin = req.getHeader("Origin");
        if (origin != null && !res.containsHeader("Access-Control-Allow-Origin")) {
            res.setHeader("Access-Control-Allow-Origin", origin);
            res.setHeader("Vary", "Origin");
            res.setHeader("Access-Control-Allow-Credentials", "true");
            String reqHeaders = req.getHeader("Access-Control-Request-Headers");
            res.setHeader("Access-Control-Allow-Headers", (reqHeaders == null || reqHeaders.isBlank()) ? "*" : reqHeaders);
            res.setHeader("Access-Control-Allow-Methods", "GET,POST,PUT,PATCH,DELETE,OPTIONS");
        }
    }
}
