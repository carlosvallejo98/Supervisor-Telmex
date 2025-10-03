package com.supervisor.supervisor_backend.controllers;

import com.supervisor.supervisor_backend.service.ProxyService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UsersController {

    private final ProxyService proxy;

    public UsersController(ProxyService proxy) {
        this.proxy = proxy;
    }

    @GetMapping
    public ResponseEntity<String> listEngineers(HttpServletRequest req) {
        String auth = req.getHeader("Authorization");
        // ✅ El backend grande expone /users (plural)
        return proxy.get(auth, "/users", null);
    }
}
