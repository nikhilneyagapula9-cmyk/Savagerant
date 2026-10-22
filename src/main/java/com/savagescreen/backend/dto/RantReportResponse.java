package com.savagescreen.backend.dto;

import com.savagescreen.backend.model.RantReport;

import java.time.LocalDateTime;

public record RantReportResponse(Long id, Long rantId, String rantContent, String reporterName,
                                 String reason, String details, String status, LocalDateTime createdAt) {
    public static RantReportResponse from(RantReport report) {
        return new RantReportResponse(report.getId(), report.getRantId(), report.getRantContent(),
                report.getReporterName(), report.getReason(), report.getDetails(), report.getStatus(), report.getCreatedAt());
    }
}
