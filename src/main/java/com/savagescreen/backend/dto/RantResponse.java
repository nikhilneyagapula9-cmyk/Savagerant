package com.savagescreen.backend.dto;

import com.savagescreen.backend.model.Rant;

import java.time.LocalDateTime;
import java.util.Map;

public record RantResponse(Long id, Long authorId, String author, String category, String content,
                            int likes, int shares, Map<String, Integer> reactions, LocalDateTime createdAt) {
    public static RantResponse from(Rant rant) {
        return new RantResponse(rant.getId(), rant.getAuthorId(), rant.getAuthor(), rant.getCategory(), rant.getContent(),
                rant.getLikes(), rant.getShares(), Map.copyOf(rant.getReactions()), rant.getCreatedAt());
    }
}
