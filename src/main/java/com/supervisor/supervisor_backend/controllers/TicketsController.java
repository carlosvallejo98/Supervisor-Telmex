package com.supervisor.supervisor_backend.controllers;

import com.supervisor.supervisor_backend.service.ProxyService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tickets")
public class TicketsController {

    private final ProxyService proxy;

    public TicketsController(ProxyService proxy) {
        this.proxy = proxy;
    }

    @GetMapping
    public ResponseEntity<String> list(
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "priority", required = false) String priority,
            @RequestParam(name = "q", required = false) String q,
            HttpServletRequest req
    ) {
        String auth = req.getHeader("Authorization");
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("all", "true"); // ✅ supervisor ve todos
        if (status != null && !status.isBlank()) params.add("status", status);
        if (priority != null && !priority.isBlank()) params.add("priority", priority);
        if (q != null && !q.isBlank()) params.add("q", q);
        return proxy.get(auth, "/tickets", params);
    }

    @GetMapping("/{id}")
    public ResponseEntity<String> detail(@PathVariable String id, HttpServletRequest req) {
        String auth = req.getHeader("Authorization");
        return proxy.get(auth, "/tickets/" + id, null);
    }
}
