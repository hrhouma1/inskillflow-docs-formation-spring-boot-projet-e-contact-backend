package com.example.contact.dto.request;

import com.example.contact.model.Lead;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateStatusRequest {

    @NotNull(message = "Le statut est obligatoire")
    private Lead.LeadStatus status;
}

