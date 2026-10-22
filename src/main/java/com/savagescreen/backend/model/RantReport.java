package com.savagescreen.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

@Entity
@Table(name = "rant_reports", uniqueConstraints = @UniqueConstraint(
        name = "uk_reporter_rant", columnNames = {"rant_id", "reporter_id"}))
public class RantReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rant_id", nullable = false)
    private Rant rant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reporter_id", nullable = false)
    private UserAccount reporter;

    @Column(nullable = false, length = 32)
    private String reason;

    @Column(nullable = false, length = 1000)
    private String details;

    @Column(nullable = false, length = 16)
    private String status = "OPEN";

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    protected RantReport() {
    }

    public RantReport(Rant rant, UserAccount reporter, String reason, String details) {
        this.rant = rant;
        this.reporter = reporter;
        this.reason = reason;
        this.details = details;
    }

    public Long getId() { return id; }
    public Long getRantId() { return rant.getId(); }
    public String getRantContent() { return rant.getContent(); }
    public String getReporterName() { return reporter.getDisplayName(); }
    public String getReason() { return reason; }
    public String getDetails() { return details; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
