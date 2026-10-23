package com.savagescreen.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public record AssistantChatRequest(
        @NotBlank @Size(max = 1000) String message,
        @Size(max = 12) List<@Valid ChatTurn> history
) {
    public record ChatTurn(
            @NotBlank @Pattern(regexp = "user|assistant") String role,
            @NotBlank @Size(max = 1000) String content
    ) {
    }
}
