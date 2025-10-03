package com.supervisor.supervisor_backend.config;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE) // antes que todo
public class AlwaysCorsFilter implements Filter {

    private final List<String> allowed;

    public AlwaysCorsFilter(@Value("${cors.origins:}") String corsOriginsCsv,
                            @Value("${cors.origin:http://localhost:3002}") String corsOrigin) {
        if (corsOriginsCsv != null && !corsOriginsCsv.isBlank()) {
            allowed = Arrays.stream(corsOriginsCsv.split(","))
                    .map(String::trim).filter(s -> !s.isEmpty()).toList();
        } else {
            allowed = List.of(corsOrigin);
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String origin = req.getHeader("Origin");
        if (origin != null && allowed.stream().anyMatch(origin::equalsIgnoreCase)) {
            res.setHeader("Access-Control-Allow-Origin", origin);
            res.setHeader("Vary", "Origin");
            res.setHeader("Access-Control-Allow-Credentials", "true");
            String reqHeaders = req.getHeader("Access-Control-Request-Headers");
            res.setHeader("Access-Control-Allow-Headers", (reqHeaders == null || reqHeaders.isBlank()) ? "*" : reqHeaders);
            res.setHeader("Access-Control-Allow-Methods", "GET,POST,PUT,PATCH,DELETE,OPTIONS");
        }

        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            res.setStatus(HttpServletResponse.SC_OK);
        } else {
            chain.doFilter(request, response);
        }
    }
}
