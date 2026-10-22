package com.savagescreen.backend;

import com.savagescreen.backend.service.ModerationService;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ModerationServiceTests {
    private final ModerationService moderationService = new ModerationService();

    @Test
    void allowsCriticalOpinionsAndOrdinaryProfanity() {
        assertDoesNotThrow(() -> moderationService.requireAllowed("This policy is awful and the company should do better."));
        assertDoesNotThrow(() -> moderationService.requireAllowed("I hate this broken app."));
    }

    @Test
    void blocksDirectSelfHarmEncouragement() {
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> moderationService.requireAllowed("Go kill yourself."));
        assertEquals(422, exception.getStatusCode().value());
    }

    @Test
    void blocksDirectThreats() {
        assertThrows(ResponseStatusException.class,
                () -> moderationService.requireAllowed("I will hurt you."));
    }
}
