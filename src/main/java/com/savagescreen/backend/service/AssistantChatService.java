package com.savagescreen.backend.service;

import com.savagescreen.backend.dto.AssistantChatRequest;
import com.savagescreen.backend.dto.AssistantChatResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AssistantChatService {
    private static final String SYSTEM_PROMPT = """
            You are SavageScreen's sarcastically named but genuinely helpful product guide.
            Help users navigate SavageScreen: public rants, connected-people and personal feeds,
            connecting to people, uploading public videos, replies, reactions, reporting, moderation,
            accounts, and screen-time roasts. Be concise and lightly sarcastic about software, never
            cruel to users. Do not create targeted abuse, hateful content, threats, or encouragement
            of self-harm. You are not a therapist, lawyer, doctor, or emergency service. Do not request
            passwords, access tokens, private contact details, or sensitive personal information.
            For urgent danger, tell the user to contact local emergency services or a trusted person.
            If asked about features you cannot verify, say so rather than inventing them.
            """;

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String chatCompletionsUrl;
    private final String model;
    private final String apiKey;

    public AssistantChatService(
            ObjectMapper objectMapper,
            @Value("${savagescreen.ai.chat-completions-url:}") String chatCompletionsUrl,
            @Value("${savagescreen.ai.model:}") String model,
            @Value("${savagescreen.ai.api-key:}") String apiKey) {
        this.objectMapper = objectMapper;
        this.chatCompletionsUrl = chatCompletionsUrl.strip();
        this.model = model.strip();
        this.apiKey = apiKey.strip();
        this.restClient = RestClient.builder().build();
    }

    public AssistantChatResponse respond(AssistantChatRequest request) {
        if (chatCompletionsUrl.isBlank() || model.isBlank()) {
            return new AssistantChatResponse(localHelp(request.message()), "local-demo");
        }
        return askModel(request);
    }

    public Map<String, Object> status() {
        boolean configured = !chatCompletionsUrl.isBlank() && !model.isBlank();
        return Map.of("configured", configured, "mode", configured ? "ai" : "local-demo");
    }

    private AssistantChatResponse askModel(AssistantChatRequest request) {
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", SYSTEM_PROMPT));
        if (request.history() != null) {
            request.history().stream().skip(Math.max(0, request.history().size() - 10L))
                    .forEach(turn -> messages.add(Map.of("role", turn.role(), "content", turn.content())));
        }
        messages.add(Map.of("role", "user", "content", request.message()));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model);
        body.put("messages", messages);
        body.put("temperature", 0.6);
        body.put("max_tokens", 400);

        try {
            RestClient.RequestBodySpec call = restClient.post().uri(chatCompletionsUrl)
                    .contentType(MediaType.APPLICATION_JSON).body(body);
            if (!apiKey.isBlank()) {
                call.header("Authorization", "Bearer " + apiKey);
            }
            String response = call.retrieve().body(String.class);
            JsonNode answer = objectMapper.readTree(response).path("choices").path(0).path("message").path("content");
            if (!answer.isString() || answer.stringValue().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                        "The configured AI provider returned an empty response.");
            }
            return new AssistantChatResponse(answer.stringValue().strip(), "ai");
        } catch (RestClientException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "The configured AI provider could not complete the chat request.", exception);
        } catch (JacksonException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "The configured AI provider returned an invalid response.", exception);
        }
    }

    private String localHelp(String message) {
        String question = message.toLowerCase(java.util.Locale.ROOT);
        if (containsAny(question, "feed", "posts", "rant")) {
            return "The Public Court shows public rants. “My circle of suspects” shows accounts you connected to, and “My greatest hits” filters to your own posts. Three tabs, one highly unnecessary court proceeding.";
        }
        if (containsAny(question, "video", "upload", "play")) {
            return "Open Rant & Roll to upload an MP4, WebM, or MOV up to 50 MB, or switch between public screenings, your circle’s footage, and your own press releases. Videos are public, and the demo does not automatically review video contents.";
        }
        if (containsAny(question, "connect", "friend", "follow")) {
            return "Open the connections page and add a person to your circle. Connections are one-way follows in this demo—not a mutual friendship contract notarized by the internet.";
        }
        if (containsAny(question, "report", "moderation", "blocked", "safety")) {
            return "Use Report on a rant to send it for moderator review. Recognized severe threats and some private-information threats are blocked, but phrase rules are limited and do not replace human moderation.";
        }
        if (containsAny(question, "login", "account", "password", "sign in")) {
            return "Use the Account page to create an account or sign in. Never share your password or session token in this chat; I have enough ways to embarrass myself already.";
        }
        if (containsAny(question, "roast", "screen time")) {
            return "The Roast Lab generates a screen-time roast from an app, minutes, goal, and chosen style. It is teasing about habits, not a substitute for actual productivity tools.";
        }
        return "I’m in local help mode, so I can answer questions about this app but I’m not connected to a language model yet. Ask about feeds, connections, videos, reports, accounts, or the Roast Lab.";
    }

    private boolean containsAny(String text, String... terms) {
        for (String term : terms) {
            if (text.contains(term)) {
                return true;
            }
        }
        return false;
    }
}
