package com.grid07.assignment.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HomeController {

    @GetMapping("/")
    public Map<String, String> home() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Welcome to the Grid07 Backend Engineering Assignment API");
        response.put("status", "Running");


        response.put("documentation", "Use Postman to test the endpoints under /api/posts, /api/users, and /api/bots");
        return response;
    }
}
