package com.savagescreen.backend;

import com.savagescreen.backend.dto.AuthResponse;
import com.savagescreen.backend.dto.RegisterRequest;
import com.savagescreen.backend.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SavagescreenbackendApplicationTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthService authService;

    @Test
    void contextLoads() {
    }

    @Test
    void assistantExposesItsModeAndAnswersAppHelp() throws Exception {
        mockMvc.perform(get("/api/assistant/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mode").value("local-demo"))
                .andExpect(jsonPath("$.configured").value(false));

        mockMvc.perform(post("/api/assistant/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"message":"How do I browse videos?","history":[]}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mode").value("local-demo"))
                .andExpect(jsonPath("$.answer").value(org.hamcrest.Matchers.containsString("Rant & Roll")));
    }

    @Test
    void publicFeedIncludesIllustratedPresentationRants() throws Exception {
        mockMvc.perform(get("/api/feed?scope=public"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].author").value("One More Tab"))
                .andExpect(jsonPath("$[0].imageUrl").value("/demo/tab-collection.svg"));
    }

    @Test
    void roastRestEndpointsCreateAndKeepHistoryPrivate() throws Exception {
        AuthResponse owner = registerTestUser("roast-owner");
        AuthResponse otherUser = registerTestUser("roast-other");

        String roastPath = mockMvc.perform(post("/api/roasts")
                        .header("Authorization", "Bearer " + owner.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "appName": "Social media",
                                  "durationMinutes": 45,
                                  "userGoal": "Finish my project",
                                  "personalityMode": "Savage"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.appName").value("Social media"))
                .andExpect(jsonPath("$.fallback").value(true))
                .andReturn().getResponse().getHeader("Location");

        mockMvc.perform(get("/api/roasts")
                        .header("Authorization", "Bearer " + owner.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].appName").value("Social media"))
                .andExpect(jsonPath("$[0].userId").doesNotExist());

        mockMvc.perform(get(roastPath)
                        .header("Authorization", "Bearer " + otherUser.token()))
                .andExpect(status().isNotFound());
    }

    private AuthResponse registerTestUser(String prefix) {
        String email = prefix + "-" + java.util.UUID.randomUUID() + "@example.com";
        return authService.register(new RegisterRequest(email, "Test User", "test-password-123"));
    }
}
