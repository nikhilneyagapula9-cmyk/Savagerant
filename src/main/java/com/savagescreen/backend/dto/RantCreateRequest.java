package com.savagescreen.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RantCreateRequest(
        @NotBlank @Size(max = 40) String category,
        @NotBlank @Size(max = 1000) String content
) {
}
