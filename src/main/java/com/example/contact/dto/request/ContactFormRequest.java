package com.example.contact.dto.request;

import com.example.contact.model.Lead;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ContactFormRequest {

    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 100, message = "Le nom ne doit pas dépasser 100 caractères")
    private String fullName;

    private String company;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    private String email;

    private String phone;

    @NotNull(message = "Le type de demande est obligatoire")
    private Lead.RequestType requestType;

    @NotBlank(message = "Le message est obligatoire")
    @Size(min = 10, message = "Le message doit contenir au moins 10 caractères")
    private String message;
}

