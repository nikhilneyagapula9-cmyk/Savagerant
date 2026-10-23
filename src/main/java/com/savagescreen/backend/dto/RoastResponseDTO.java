package com.savagescreen.backend.dto;

import com.savagescreen.backend.model.RoastHistory;

import java.time.LocalDateTime;

public record RoastResponseDTO(Long id, String roastMessage, String appName, Integer durationMinutes,
                               String personalityMode, LocalDateTime generatedAt, boolean fallback) {
    public static RoastResponseDTO from(RoastHistory history) {
        return new RoastResponseDTO(history.getId(), history.getRoastMessage(), history.getAppName(),
                history.getDurationMinutes(), history.getPersonalityMode(), history.getCreatedAt(),
                Boolean.TRUE.equals(history.getIsFallback()));
    }
    // Git practice: testing commits on a feature branch
}