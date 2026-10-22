package com.savagescreen.backend.dto;

public record AuthResponse(String token, UserProfile user) {
}
