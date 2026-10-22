package com.savagescreen.backend.controller;

import com.savagescreen.backend.model.UserAccount;
import com.savagescreen.backend.repository.UserAccountRepository;
import com.savagescreen.backend.service.AuthService;
import com.savagescreen.backend.service.ServiceBridge;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;

@RestController
@RequestMapping("/api/connections")
public class ConnectionController {
    private final AuthService authService;
    private final UserAccountRepository userRepository;
    private final ServiceBridge serviceBridge;

    public ConnectionController(AuthService authService, UserAccountRepository userRepository, ServiceBridge serviceBridge) {
        this.authService = authService;
        this.userRepository = userRepository;
        this.serviceBridge = serviceBridge;
    }

    @org.springframework.web.bind.annotation.GetMapping
    public List<Long> getConnections(@RequestHeader(value = "Authorization", required = false) String authorization) {
        return serviceBridge.connections(authService.requireUser(authorization).getId());
    }

    @PostMapping("/{targetId}")
    public ResponseEntity<Void> connect(@PathVariable Long targetId,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        UserAccount user = authService.requireUser(authorization);
        if (targetId.equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You are already stuck with yourself.");
        }
        if (!userRepository.existsById(targetId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Person not found.");
        }
        serviceBridge.connect(user.getId(), targetId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{targetId}")
    public ResponseEntity<Void> disconnect(@PathVariable Long targetId,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        UserAccount user = authService.requireUser(authorization);
        serviceBridge.disconnect(user.getId(), targetId);
        return ResponseEntity.noContent().build();
    }
}
