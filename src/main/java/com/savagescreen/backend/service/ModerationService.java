package com.savagescreen.backend.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.regex.Pattern;

@Service
public class ModerationService {
    private static final int BLOCK_THRESHOLD = 4;

    private record Rule(Pattern pattern, int severity, String reason) {
    }

    private final List<Rule> blockingRules = List.of(
            new Rule(Pattern.compile("\\b(?:kill yourself|go kill yourself|you should die|you deserve to die|hurt yourself)\\b", Pattern.CASE_INSENSITIVE), 5,
                    "Encouraging self-harm or suicide is not allowed."),
            new Rule(Pattern.compile("\\b(?:i|we) (?:will|am going to|are going to) (?:kill|shoot|stab|bomb|attack|hurt) (?:you|him|her|them)\\b", Pattern.CASE_INSENSITIVE), 5,
                    "Threats of violence are not allowed."),
            new Rule(Pattern.compile("\\b(?:here is|this is) (?:their|his|her) (?:home address|phone number|password)\\b", Pattern.CASE_INSENSITIVE), 4,
                    "Sharing another person's private information is not allowed."),
            new Rule(Pattern.compile("\\b(?:dox|doxx) (?:them|him|her|you)\\b", Pattern.CASE_INSENSITIVE), 4,
                    "Threats to expose private information are not allowed.")
    );

    public void requireAllowed(String text) {
        String normalized = text == null ? "" : text.strip();
        for (Rule rule : blockingRules) {
            if (rule.severity() >= BLOCK_THRESHOLD && rule.pattern().matcher(normalized).find()) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT, rule.reason());
            }
        }
    }
}
