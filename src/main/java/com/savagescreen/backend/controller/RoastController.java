package com.savagescreen.backend.controller;

import com.savagescreen.backend.dto.RoastRequestDTO;
import com.savagescreen.backend.dto.RoastResponseDTO;
import com.savagescreen.backend.service.RoastService;
import com.savagescreen.backend.model.UserAccount;
import com.savagescreen.backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
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

    @PostMapping({"", "/generate"})
    public ResponseEntity<RoastResponseDTO> generateRoast(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody RoastRequestDTO request) {
        UserAccount user = authService.requireUser(authorization);
        RoastResponseDTO response = roastService.generateRoast(request, user);
        return ResponseEntity.created(URI.create("/api/roasts/" + response.id())).body(response);
    }

    @GetMapping({"", "/history"})
    public ResponseEntity<List<RoastResponseDTO>> getAllRoasts(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        UserAccount user = authService.requireUser(authorization);
        return ResponseEntity.ok(roastService.getAllRoasts(user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoastResponseDTO> getRoast(@PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        UserAccount user = authService.requireUser(authorization);
        return ResponseEntity.ok(roastService.getRoast(id, user));
    }
}
