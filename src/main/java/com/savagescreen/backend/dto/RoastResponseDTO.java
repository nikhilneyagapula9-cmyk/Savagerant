package com.savagescreen.backend.dto;

import java.time.LocalDateTime;

public class RoastResponseDTO {

    private String roastMessage;
    private String appName;
    private Integer durationMinutes;
    private String personalityMode;
    private LocalDateTime generatedAt;
    private boolean isFallback;

    public RoastResponseDTO() {
    }

    public String getRoastMessage() {
        return roastMessage;
    }

    public void setRoastMessage(String roastMessage) {
        this.roastMessage = roastMessage;
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

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }

    public boolean isFallback() {
        return isFallback;
    }

    public void setFallback(boolean fallback) {
        isFallback = fallback;
    }
}