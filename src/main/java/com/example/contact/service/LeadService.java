package com.example.contact.service;

import com.example.contact.dto.request.ContactFormRequest;
import com.example.contact.dto.request.UpdateStatusRequest;
import com.example.contact.dto.response.LeadDto;
import com.example.contact.dto.response.LeadStatsDto;
import com.example.contact.exception.ResourceNotFoundException;
import com.example.contact.model.Lead;
import com.example.contact.repository.LeadRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LeadService {

    private final LeadRepository leadRepository;
    private final EmailService emailService;

    @Transactional
    public LeadDto createLead(ContactFormRequest request) {
        Lead lead = Lead.builder()
                .fullName(request.getFullName())
                .company(request.getCompany())
                .email(request.getEmail())
                .phone(request.getPhone())
                .requestType(request.getRequestType())
                .message(request.getMessage())
                .status(Lead.LeadStatus.NEW)
                .build();

        Lead saved = leadRepository.save(lead);

        // Envoyer les emails de manière asynchrone
        emailService.sendNotificationToAdmin(saved);
        emailService.sendConfirmationToVisitor(saved);

        log.info("Nouveau lead créé: {} ({})", saved.getFullName(), saved.getEmail());

        return mapToDto(saved);
    }

    public Page<LeadDto> getAllLeads(Lead.LeadStatus status, Pageable pageable) {
        Page<Lead> leads;
        if (status != null) {
            leads = leadRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
        } else {
            leads = leadRepository.findAllByOrderByCreatedAtDesc(pageable);
        }
        return leads.map(this::mapToDto);
    }

    public LeadDto getLeadById(Long id) {
        Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lead non trouvé avec l'id: " + id));
        return mapToDto(lead);
    }

    @Transactional
    public LeadDto updateStatus(Long id, UpdateStatusRequest request) {
        Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lead non trouvé avec l'id: " + id));

        Lead.LeadStatus oldStatus = lead.getStatus();
        lead.setStatus(request.getStatus());

        Lead updated = leadRepository.save(lead);

        log.info("Lead {} statut changé: {} → {}", id, oldStatus, request.getStatus());

        return mapToDto(updated);
    }

    @Transactional
    public void deleteLead(Long id) {
        if (!leadRepository.existsById(id)) {
            throw new ResourceNotFoundException("Lead non trouvé avec l'id: " + id);
        }
        leadRepository.deleteById(id);
        log.info("Lead {} supprimé", id);
    }

    public LeadStatsDto getStats() {
        long total = leadRepository.count();
        long converted = leadRepository.countByStatus(Lead.LeadStatus.CONVERTED);

        return LeadStatsDto.builder()
                .totalLeads(total)
                .newLeads(leadRepository.countByStatus(Lead.LeadStatus.NEW))
                .contactedLeads(leadRepository.countByStatus(Lead.LeadStatus.CONTACTED))
                .convertedLeads(converted)
                .lostLeads(leadRepository.countByStatus(Lead.LeadStatus.LOST))
                .conversionRate(total > 0 ? (double) converted / total * 100 : 0)
                .build();
    }

    private LeadDto mapToDto(Lead lead) {
        return LeadDto.builder()
                .id(lead.getId())
                .fullName(lead.getFullName())
                .company(lead.getCompany())
                .email(lead.getEmail())
                .phone(lead.getPhone())
                .requestType(lead.getRequestType().name())
                .message(lead.getMessage())
                .status(lead.getStatus().name())
                .createdAt(lead.getCreatedAt())
                .updatedAt(lead.getUpdatedAt())
                .build();
    }
}

