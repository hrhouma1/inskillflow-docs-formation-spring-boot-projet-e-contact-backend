package com.example.contact.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class LeadDto {
    private Long id;
    private String fullName;
    private String company;
    private String email;
    private String phone;
    private String requestType;
    private String message;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

