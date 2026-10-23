package com.savagescreen.backend.service.impl;

import com.savagescreen.backend.dto.RoastRequestDTO;
import com.savagescreen.backend.dto.RoastResponseDTO;
import com.savagescreen.backend.model.RoastHistory;
import com.savagescreen.backend.model.UserAccount;
import com.savagescreen.backend.repository.RoastHistoryRepository;
import com.savagescreen.backend.service.RoastService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
public class RoastServiceImpl implements RoastService {

    private final RoastHistoryRepository roastHistoryRepository;

    public RoastServiceImpl(RoastHistoryRepository roastHistoryRepository) {
        this.roastHistoryRepository = roastHistoryRepository;
    }

    // Using indexed arguments: %1$d = durationMinutes, %2$s = appName, %3$s = userGoal
    private final List<String> savageTemplates = List.of(
            "Warning: %1$d minutes on %2$s detected. Zero progress made towards '%3$s'.",
            "If scrolling %2$s for %1$d minutes built a career, you'd be a billionaire by now. Focus on '%3$s'!",
            "Your goal is '%3$s', yet here you are spending %1$d minutes on %2$s. Make it make sense."
    );

    private final Random random = new Random();

    @Override
    @Transactional
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

        return RoastResponseDTO.from(roastHistoryRepository.save(history));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoastResponseDTO> getAllRoasts(UserAccount user) {
        return roastHistoryRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(RoastResponseDTO::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RoastResponseDTO getRoast(Long id, UserAccount user) {
        RoastHistory history = roastHistoryRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Roast not found."));
        return RoastResponseDTO.from(history);
    }
}