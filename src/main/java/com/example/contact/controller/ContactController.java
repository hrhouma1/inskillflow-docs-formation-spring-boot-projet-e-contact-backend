package com.example.contact.controller;

import com.example.contact.dto.request.ContactFormRequest;
import com.example.contact.dto.response.MessageResponse;
import com.example.contact.service.LeadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contact")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Permet les requêtes de n'importe quel frontend
public class ContactController {

    private final LeadService leadService;

    /**
     * Endpoint PUBLIC - Soumettre le formulaire de contact
     * Accessible sans authentification
     */
    @PostMapping
    public ResponseEntity<MessageResponse> submitContactForm(
            @Valid @RequestBody ContactFormRequest request) {

        leadService.createLead(request);

        return ResponseEntity.ok(
                new MessageResponse("Merci! Votre message a été envoyé. Nous vous répondrons bientôt.")
        );
    }
}

