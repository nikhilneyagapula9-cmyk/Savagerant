package com.savagescreen.backend.dto;

import java.time.Instant;

public record VideoPost(String id, Long authorId, String author, String caption,
                        String contentType, long sizeBytes, Instant createdAt, String contentUrl) {
}
