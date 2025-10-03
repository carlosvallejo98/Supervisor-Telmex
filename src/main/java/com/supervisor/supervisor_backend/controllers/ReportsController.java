package com.supervisor.supervisor_backend.controllers;

import com.supervisor.supervisor_backend.service.ProxyService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reports")
public class ReportsController {

    private final ProxyService proxy;

    public ReportsController(ProxyService proxy) {
        this.proxy = proxy;
    }

    // Listado de reportes
    @GetMapping
    public ResponseEntity<String> list(HttpServletRequest req) {
        String auth = req.getHeader("Authorization");
        // ✅ Arreglado: era "/report/reports"
        return proxy.get(auth, "/reports", null);
    }

    // (Opcional) Generar reporte de un ticket
    @PostMapping("/{ticketId}")
    public ResponseEntity<String> generate(@PathVariable String ticketId, HttpServletRequest req) {
        String auth = req.getHeader("Authorization");
        return proxy.postJson(auth, "/tickets/" + ticketId + "/report", "{}");
    }
}
