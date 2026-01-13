package com.example.contact.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    private String token;
    private String type;
    private Long expiresIn;
    private String email;
    private String role;
}

