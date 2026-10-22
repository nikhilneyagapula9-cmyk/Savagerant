package com.savagescreen.backend.service.impl;

import com.savagescreen.backend.dto.RoastRequestDTO;
import com.savagescreen.backend.dto.RoastResponseDTO;
import com.savagescreen.backend.model.RoastHistory;
import com.savagescreen.backend.model.UserAccount;
import com.savagescreen.backend.repository.RoastHistoryRepository;
import com.savagescreen.backend.service.RoastService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
public class RoastServiceImpl implements RoastService {

    @Autowired
    private RoastHistoryRepository roastHistoryRepository;

    // Using indexed arguments: %1$d = durationMinutes, %2$s = appName, %3$s = userGoal
    private final List<String> savageTemplates = List.of(
            "Warning: %1$d minutes on %2$s detected. Zero progress made towards '%3$s'.",
            "If scrolling %2$s for %1$d minutes built a career, you'd be a billionaire by now. Focus on '%3$s'!",
            "Your goal is '%3$s', yet here you are spending %1$d minutes on %2$s. Make it make sense."
    );

    private final Random random = new Random();

    @Override
    public RoastResponseDTO generateRoast(RoastRequestDTO request, UserAccount user) {
        String template = savageTemplates.get(random.nextInt(savageTemplates.size()));

        // Arguments mapping: 1 -> durationMinutes, 2 -> appName, 3 -> userGoal
        String message = String.format(template, request.getDurationMinutes(), request.getAppName(), request.getUserGoal());

        LocalDateTime now = LocalDateTime.now();

        // Save roast into the H2 database
        RoastHistory history = new RoastHistory();
        history.setUserId(user.getId());
        history.setAppName(request.getAppName());
        history.setDurationMinutes(request.getDurationMinutes());
        history.setPersonalityMode(request.getPersonalityMode());
        history.setRoastMessage(message);
        history.setIsFallback(true);
        history.setCreatedAt(now);

        roastHistoryRepository.save(history);

        // Build and return response DTO
        RoastResponseDTO response = new RoastResponseDTO();
        response.setRoastMessage(message);
        response.setAppName(request.getAppName());
        response.setDurationMinutes(request.getDurationMinutes());
        response.setPersonalityMode(request.getPersonalityMode());
        response.setGeneratedAt(now);
        response.setFallback(true);

        return response;
    }

    @Override
    public List<RoastHistory> getAllRoasts(UserAccount user) {
        return roastHistoryRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
    }
}