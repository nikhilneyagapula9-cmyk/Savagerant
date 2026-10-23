package com.savagescreen.backend;

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
}
