package com.savagescreen.backend.controller;

import com.savagescreen.backend.dto.RoastRequestDTO;
import com.savagescreen.backend.dto.RoastResponseDTO;
import com.savagescreen.backend.model.RoastHistory;
import com.savagescreen.backend.service.RoastService;
import com.savagescreen.backend.model.UserAccount;
import com.savagescreen.backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roasts")
public class RoastController {
    private final RoastService roastService;
    private final AuthService authService;

    public RoastController(RoastService roastService, AuthService authService) {
        this.roastService = roastService;
        this.authService = authService;
    }

    @PostMapping("/generate")
    public ResponseEntity<RoastResponseDTO> generateRoast(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody RoastRequestDTO request) {
        UserAccount user = authService.requireUser(authorization);
        RoastResponseDTO response = roastService.generateRoast(request, user);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    public ResponseEntity<List<RoastHistory>> getAllRoasts(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        UserAccount user = authService.requireUser(authorization);
        List<RoastHistory> history = roastService.getAllRoasts(user);
        return ResponseEntity.ok(history);
    }
}