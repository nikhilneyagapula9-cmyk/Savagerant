package com.savagescreen.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RoastRequestDTO {

    @NotBlank(message = "App name is required")
    @Size(max = 100, message = "App name must be at most 100 characters")
    private String appName;

    @NotNull(message = "Duration in minutes is required")
    @Min(value = 1, message = "Duration must be at least 1 minute")
    @Max(value = 1440, message = "Duration cannot exceed 1440 minutes")
    private Integer durationMinutes;

    @NotBlank(message = "Productivity goal is required")
    @Size(max = 255, message = "Productivity goal must be at most 255 characters")
    private String userGoal;

    @NotBlank(message = "Personality mode is required")
    @Pattern(regexp = "Savage|Passive-Aggressive|Corporate HR",
            message = "Choose a supported personality mode")
    private String personalityMode;

    public RoastRequestDTO() {
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

    public String getUserGoal() {
        return userGoal;
    }

    public void setUserGoal(String userGoal) {
        this.userGoal = userGoal;
    }

    public String getPersonalityMode() {
        return personalityMode;
    }

    public void setPersonalityMode(String personalityMode) {
        this.personalityMode = personalityMode;
    }
}
