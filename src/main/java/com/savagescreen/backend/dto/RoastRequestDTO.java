package com.savagescreen.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class RoastRequestDTO {

    @NotBlank(message = "App name is required")
    private String appName;

    @NotNull(message = "Duration in minutes is required")
    @Min(value = 1, message = "Duration must be at least 1 minute")
    private Integer durationMinutes;

    @NotBlank(message = "Productivity goal is required")
    private String userGoal;

    @NotBlank(message = "Personality mode is required")
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
