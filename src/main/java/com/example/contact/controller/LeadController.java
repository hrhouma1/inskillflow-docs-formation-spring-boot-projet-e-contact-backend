package com.example.contact.controller;

import com.example.contact.dto.request.UpdateStatusRequest;
import com.example.contact.dto.response.LeadDto;
import com.example.contact.dto.response.LeadStatsDto;
import com.example.contact.dto.response.MessageResponse;
import com.example.contact.model.Lead;
import com.example.contact.service.LeadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/leads")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@CrossOrigin(origins = "*")
public class LeadController {

    private final LeadService leadService;

    /**
     * Liste des leads avec pagination et filtre optionnel par statut
     */
    @GetMapping
    public ResponseEntity<Page<LeadDto>> getAllLeads(
            @RequestParam(required = false) String status,
            Pageable pageable) {

        Lead.LeadStatus leadStatus = null;
        if (status != null && !status.isEmpty()) {
            leadStatus = Lead.LeadStatus.valueOf(status.toUpperCase());
        }

        return ResponseEntity.ok(leadService.getAllLeads(leadStatus, pageable));
    }

    /**
     * Détail d'un lead
     */
    @GetMapping("/{id}")
    public ResponseEntity<LeadDto> getLeadById(@PathVariable Long id) {
        return ResponseEntity.ok(leadService.getLeadById(id));
    }

    /**
     * Mettre à jour le statut d'un lead
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<LeadDto> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStatusRequest request) {

        return ResponseEntity.ok(leadService.updateStatus(id, request));
    }

    /**
     * Supprimer un lead
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteLead(@PathVariable Long id) {
        leadService.deleteLead(id);
        return ResponseEntity.ok(new MessageResponse("Lead supprimé avec succès"));
    }

    /**
     * Statistiques des leads
     */
    @GetMapping("/stats")
    public ResponseEntity<LeadStatsDto> getStats() {
        return ResponseEntity.ok(leadService.getStats());
    }
}

