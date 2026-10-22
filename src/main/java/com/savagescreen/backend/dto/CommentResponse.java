package com.savagescreen.backend.dto;

import com.savagescreen.backend.model.RantComment;

import java.time.LocalDateTime;

public record CommentResponse(Long id, String author, String content, LocalDateTime createdAt) {
    public static CommentResponse from(RantComment comment) {
        return new CommentResponse(comment.getId(), comment.getAuthor(), comment.getContent(), comment.getCreatedAt());
    }
}
