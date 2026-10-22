package com.savagescreen.backend.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "roast_history")
public class RoastHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "app_name", nullable = false)
    private String appName;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Column(name = "personality_mode", nullable = false)
    private String personalityMode;

    @Column(name = "roast_message", nullable = false, columnDefinition = "TEXT")
    private String roastMessage;

    @Column(name = "is_fallback")
    private Boolean isFallback;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public RoastHistory() {
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public String getPersonalityMode() {
        return personalityMode;
    }

    public void setPersonalityMode(String personalityMode) {
        this.personalityMode = personalityMode;
    }

    public String getRoastMessage() {
        return roastMessage;
    }

    public void setRoastMessage(String roastMessage) {
        this.roastMessage = roastMessage;
    }

    public Boolean getIsFallback() {
        return isFallback;
    }

    public void setIsFallback(Boolean isFallback) {
        this.isFallback = isFallback;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}