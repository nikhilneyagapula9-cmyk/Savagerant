package com.savagescreen.backend.service;

import com.savagescreen.backend.dto.CommentCreateRequest;
import com.savagescreen.backend.dto.CommentResponse;
import com.savagescreen.backend.dto.RantCreateRequest;
import com.savagescreen.backend.dto.RantResponse;
import com.savagescreen.backend.dto.RantReportRequest;
import com.savagescreen.backend.dto.RantReportResponse;
import com.savagescreen.backend.model.RantInteraction;
import com.savagescreen.backend.model.RantReport;
import com.savagescreen.backend.model.RantComment;
import com.savagescreen.backend.model.Rant;
import com.savagescreen.backend.model.UserAccount;
import com.savagescreen.backend.repository.RantCommentRepository;
import com.savagescreen.backend.repository.RantInteractionRepository;
import com.savagescreen.backend.repository.RantReportRepository;
import com.savagescreen.backend.repository.RantRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;
import java.util.HashMap;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;

@Service
public class RantService {
    private static final Set<String> CATEGORIES = Set.of("corporate", "tech", "politics", "life");
    private static final Set<String> REACTIONS = Set.of("👍", "❤️", "😂", "😮", "😢", "🔥");
    private static final Set<String> REPORT_REASONS = Set.of("harassment", "threats", "privacy", "hate", "other");
    private final RantRepository rantRepository;
    private final RantCommentRepository commentRepository;
    private final RantInteractionRepository interactionRepository;
    private final RantReportRepository reportRepository;
    private final ModerationService moderationService;
    private final String moderatorEmail;

    public RantService(RantRepository rantRepository, RantCommentRepository commentRepository,
                       RantInteractionRepository interactionRepository, RantReportRepository reportRepository,
                       ModerationService moderationService,
                       @Value("${savagescreen.moderator-email:}") String moderatorEmail) {
        this.rantRepository = rantRepository;
        this.commentRepository = commentRepository;
        this.interactionRepository = interactionRepository;
        this.reportRepository = reportRepository;
        this.moderationService = moderationService;
        this.moderatorEmail = moderatorEmail.strip().toLowerCase(Locale.ROOT);
    }

    @Transactional
    public RantResponse createRant(UserAccount user, RantCreateRequest request) {
        moderationService.requireAllowed(request.content());
        String category = request.category().strip().toLowerCase();
        if (!CATEGORIES.contains(category)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose a supported rant category.");
        }
        Rant rant = new Rant();
        rant.setAuthor(user.getDisplayName());
        rant.setAuthorId(user.getId());
        rant.setCategory(category);
        rant.setContent(request.content().strip());
        rant.setLikes(0);
        rant.setShares(0);
        rant.setReactions(new HashMap<>());
        return RantResponse.from(rantRepository.save(rant));
    }

    @Transactional(readOnly = true)
    public List<RantResponse> getAllRants(String category) {
        List<Rant> rants;
        if (category != null && !category.isEmpty()) {
            rants = rantRepository.findByCategoryAndHiddenFalseOrderByCreatedAtDesc(category.strip().toLowerCase(Locale.ROOT));
        } else {
            rants = rantRepository.findByHiddenFalseOrderByCreatedAtDesc();
        }
        return rants.stream().map(RantResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<RantResponse> getRantsByAuthors(Set<Long> authorIds) {
        if (authorIds.isEmpty()) {
            return List.of();
        }
        return rantRepository.findByAuthorIdInAndHiddenFalseOrderByCreatedAtDesc(authorIds)
                .stream().map(RantResponse::from).toList();
    }

    @Transactional
    public RantResponse likeRant(Long id, UserAccount user) {
        Rant rant = findRant(id);
        if (!interactionRepository.existsByRantIdAndUserIdAndKind(id, user.getId(), "LIKE")) {
            interactionRepository.save(new RantInteraction(rant, user, "LIKE", null));
        }
        rant.setLikes(Math.toIntExact(interactionRepository.countByRantIdAndKind(id, "LIKE")));
        return RantResponse.from(rantRepository.save(rant));
    }

    @Transactional
    public RantResponse shareRant(Long id) {
        Rant rant = findRant(id);
        rant.setShares(rant.getShares() + 1);
        return RantResponse.from(rantRepository.save(rant));
    }

    @Transactional
    public RantResponse reactToRant(Long id, UserAccount user, String emoji) {
        if (!REACTIONS.contains(emoji)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose one of the available reactions.");
        }
        Rant rant = findRant(id);
        RantInteraction interaction = interactionRepository.findByRantIdAndUserIdAndKind(id, user.getId(), "REACTION")
                .orElseGet(() -> new RantInteraction(rant, user, "REACTION", emoji));
        interaction.setValue(emoji);
        interactionRepository.save(interaction);
        rant.getReactions().clear();
        interactionRepository.findByRantIdAndKind(id, "REACTION").stream()
                .map(RantInteraction::getValue).filter(value -> value != null)
                .forEach(value -> rant.getReactions().merge(value, 1, Integer::sum));
        return RantResponse.from(rantRepository.save(rant));
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getComments(Long rantId) {
        findRant(rantId);
        return commentRepository.findByRantIdOrderByCreatedAtAsc(rantId).stream()
                .map(CommentResponse::from).toList();
    }

    @Transactional
    public CommentResponse addComment(Long rantId, UserAccount user, CommentCreateRequest request) {
        moderationService.requireAllowed(request.content());
        RantComment saved = commentRepository.save(new RantComment(findRant(rantId), user.getDisplayName(), request.content().strip()));
        return CommentResponse.from(saved);
    }

    @Transactional
    public RantReportResponse createReport(Long rantId, UserAccount reporter, RantReportRequest request) {
        Rant rant = findRant(rantId);
        if (reportRepository.existsByRant_IdAndReporter_Id(rantId, reporter.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You have already reported this rant.");
        }
        String reason = request.reason().strip().toLowerCase(Locale.ROOT);
        if (!REPORT_REASONS.contains(reason)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose a supported report reason.");
        }
        RantReport report = reportRepository.save(new RantReport(rant, reporter, reason,
                request.details() == null ? "" : request.details().strip()));
        return RantReportResponse.from(report);
    }

    @Transactional(readOnly = true)
    public List<RantReportResponse> getOpenReports(UserAccount user) {
        requireModerator(user);
        return reportRepository.findAllByOrderByCreatedAtAsc().stream()
                .filter(report -> "OPEN".equals(report.getStatus()))
                .map(RantReportResponse::from).toList();
    }

    @Transactional
    public void resolveReport(Long reportId, String decision, UserAccount user) {
        requireModerator(user);
        RantReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Report not found."));
        if ("hide".equals(decision)) {
            Rant rant = findRant(report.getRantId());
            rant.setHidden(true);
            rantRepository.save(rant);
            report.setStatus("HIDDEN");
        } else if ("dismiss".equals(decision)) {
            report.setStatus("DISMISSED");
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Decision must be 'hide' or 'dismiss'.");
        }
        reportRepository.save(report);
    }

    private void requireModerator(UserAccount user) {
        if (moderatorEmail.isBlank() || !moderatorEmail.equalsIgnoreCase(user.getEmail())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Moderator access is not configured for this account.");
        }
    }

    private Rant findRant(Long id) {
        return rantRepository.findById(id)
                .filter(rant -> !rant.isHidden())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rant not found."));
    }
}