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
        //  antes: /user con params role=engineer (dejaba vacío)
        //  ahora: trae todos los usuarios y el front los ordena A–Z
        return proxy.get(auth, "/user", null);
    }
}
