package com.demo.security_demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {
    @GetMapping("/public")
    public String publicEndpoint() {
        return "Ceci est PUBLIC - tout le monde peut le voir.";
    }

    @GetMapping("/private")
    public String privateEndpoint() {
        return "Ceci est PRIVÉ - il faut être connecte.";
    }

    @GetMapping("/admin")
    public String adminEndpoint() {
        return "Ceci est ADMIN - il faut être admin.";
    }
}
