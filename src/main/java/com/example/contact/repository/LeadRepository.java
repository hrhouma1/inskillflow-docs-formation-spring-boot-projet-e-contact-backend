package com.example.contact.repository;

import com.example.contact.model.Lead;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface LeadRepository extends JpaRepository<Lead, Long> {

    Optional<Lead> findByEmail(String email);

    boolean existsByEmail(String email);

    Page<Lead> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<Lead> findByStatusOrderByCreatedAtDesc(Lead.LeadStatus status, Pageable pageable);

    long countByStatus(Lead.LeadStatus status);

    @Query("SELECT COUNT(l) FROM Lead l WHERE l.status = 'NEW'")
    long countNewLeads();
}

