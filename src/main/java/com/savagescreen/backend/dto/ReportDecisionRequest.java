package com.savagescreen.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ReportDecisionRequest(
        @NotBlank @Pattern(regexp = "hide|dismiss") String decision
) {
}
