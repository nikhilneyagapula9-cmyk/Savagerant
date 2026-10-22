package com.savagescreen.backend.controller;

import com.savagescreen.backend.dto.RantResponse;
import com.savagescreen.backend.model.UserAccount;
import com.savagescreen.backend.service.AuthService;
import com.savagescreen.backend.service.RantService;
import com.savagescreen.backend.service.ServiceBridge;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.HashSet;
import java.util.List;

@RestController
@RequestMapping("/api/feed")
public class FeedController {
    private final RantService rantService;
    private final AuthService authService;
    private final ServiceBridge serviceBridge;

    public FeedController(RantService rantService, AuthService authService, ServiceBridge serviceBridge) {
        this.rantService = rantService;
        this.authService = authService;
        this.serviceBridge = serviceBridge;
    }

    @GetMapping
    public ResponseEntity<List<RantResponse>> getFeed(
            @RequestParam(defaultValue = "public") String scope,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        if ("public".equals(scope)) {
            return ResponseEntity.ok(rantService.getAllRants(null));
        }
        UserAccount user = authService.requireUser(authorization);
        if ("mine".equals(scope)) {
            return ResponseEntity.ok(rantService.getRantsByAuthors(java.util.Set.of(user.getId())));
        }
        if ("connections".equals(scope)) {
            return ResponseEntity.ok(rantService.getRantsByAuthors(
                    new HashSet<>(serviceBridge.connections(user.getId()))));
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Feed scope must be public, connections, or mine.");
    }
}
