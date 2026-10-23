package com.savagescreen.backend.controller;

import com.savagescreen.backend.dto.AssistantChatRequest;
import com.savagescreen.backend.dto.AssistantChatResponse;
import com.savagescreen.backend.service.AssistantChatService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/assistant")
public class AssistantChatController {
    private final AssistantChatService assistantChatService;

    public AssistantChatController(AssistantChatService assistantChatService) {
        this.assistantChatService = assistantChatService;
    }

    @GetMapping("/status")
    public Map<String, Object> status() {
        return assistantChatService.status();
    }

    @PostMapping("/chat")
    public ResponseEntity<AssistantChatResponse> chat(@Valid @RequestBody AssistantChatRequest request) {
        return ResponseEntity.ok(assistantChatService.respond(request));
    }
}
