package com.example.contact.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LeadStatsDto {
    private long totalLeads;
    private long newLeads;
    private long contactedLeads;
    private long convertedLeads;
    private long lostLeads;
    private double conversionRate;
}

