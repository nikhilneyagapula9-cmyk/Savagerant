package com.savagescreen.backend;

import com.savagescreen.backend.dto.AssistantChatRequest;
import com.savagescreen.backend.service.AssistantChatService;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AssistantChatServiceTests {
    private final AssistantChatService service =
            new AssistantChatService(JsonMapper.builder().build(), "", "", "");

    @Test
    void reportsLocalDemoModeWhenNoModelIsConfigured() {
        assertEquals("local-demo", service.status().get("mode"));
        assertEquals(Boolean.FALSE, service.status().get("configured"));
    }

    @Test
    void answersAppHelpWithoutClaimingItIsGenerativeAi() {
        var response = service.respond(new AssistantChatRequest(
                "How do I find videos?", List.of()));

        assertEquals("local-demo", response.mode());
        assertTrue(response.answer().contains("Rant & Roll"));
        assertFalse(response.answer().isBlank());
    }
}
