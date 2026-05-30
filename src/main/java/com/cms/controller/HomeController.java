package com.cms.controller;

import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/")
    public Map<String, Object> home() {
        return Map.of(
                "status", "ok",
                "message", "Contact Management System API is running",
                "endpoints", List.of(
                        "POST /api/auth/register",
                        "POST /api/auth/login",
                        "PUT /api/auth/change-password"));
    }
}
