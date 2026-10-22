package com.savagescreen.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RantReportRequest(
        @NotBlank @Size(max = 32) String reason,
        @Size(max = 1000) String details
) {
}
