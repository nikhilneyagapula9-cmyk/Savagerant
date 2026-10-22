package com.savagescreen.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RantReactionRequestDTO {
    @NotBlank(message = "Emoji is required")
    @Size(max = 16, message = "Reaction is too long")
    private String emoji;

    public RantReactionRequestDTO() {
    }

    public String getEmoji() {
        return emoji;
    }

    public void setEmoji(String emoji) {
        this.emoji = emoji;
    }
}
