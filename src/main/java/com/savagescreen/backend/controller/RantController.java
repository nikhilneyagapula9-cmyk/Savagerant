package com.savagescreen.backend.controller;

import com.savagescreen.backend.dto.RantReactionRequestDTO;
import com.savagescreen.backend.dto.CommentCreateRequest;
import com.savagescreen.backend.dto.CommentResponse;
import com.savagescreen.backend.dto.RantCreateRequest;
import com.savagescreen.backend.dto.RantResponse;
import com.savagescreen.backend.dto.RantReportRequest;
import com.savagescreen.backend.dto.RantReportResponse;
import com.savagescreen.backend.dto.ReportDecisionRequest;
import com.savagescreen.backend.model.Rant;
import com.savagescreen.backend.model.UserAccount;
import com.savagescreen.backend.service.AuthService;
import com.savagescreen.backend.service.RantService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rants")
public class RantController {
    private final RantService rantService;
    private final AuthService authService;

    public RantController(RantService rantService, AuthService authService) {
        this.rantService = rantService;
        this.authService = authService;
    }

    @PostMapping
    public ResponseEntity<RantResponse> createRant(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody RantCreateRequest request) {
        UserAccount user = authService.requireUser(authorization);
        return ResponseEntity.status(HttpStatus.CREATED).body(rantService.createRant(user, request));
    }

    @GetMapping
    public ResponseEntity<List<RantResponse>> getAllRants(@RequestParam(required = false) String category) {
        return ResponseEntity.ok(rantService.getAllRants(category));
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<RantResponse> likeRant(@PathVariable Long id,
                                         @RequestHeader(value = "Authorization", required = false) String authorization) {
        UserAccount user = authService.requireUser(authorization);
        return ResponseEntity.ok(rantService.likeRant(id, user));
    }

    @PostMapping("/{id}/share")
    public ResponseEntity<RantResponse> shareRant(@PathVariable Long id,
                                          @RequestHeader(value = "Authorization", required = false) String authorization) {
        authService.requireUser(authorization);
        return ResponseEntity.ok(rantService.shareRant(id));
    }

    @PostMapping("/{id}/react")
    public ResponseEntity<RantResponse> reactToRant(@PathVariable Long id,
                                            @RequestHeader(value = "Authorization", required = false) String authorization,
                                            @Valid @RequestBody RantReactionRequestDTO request) {
        UserAccount user = authService.requireUser(authorization);
        return ResponseEntity.ok(rantService.reactToRant(id, user, request.getEmoji()));
    }

    @GetMapping("/{id}/comments")
    public ResponseEntity<List<CommentResponse>> getComments(@PathVariable Long id) {
        return ResponseEntity.ok(rantService.getComments(id));
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<CommentResponse> addComment(@PathVariable Long id,
                                                      @RequestHeader(value = "Authorization", required = false) String authorization,
                                                      @Valid @RequestBody CommentCreateRequest request) {
        UserAccount user = authService.requireUser(authorization);
        return ResponseEntity.status(HttpStatus.CREATED).body(rantService.addComment(id, user, request));
    }

    @PostMapping("/{id}/reports")
    public ResponseEntity<RantReportResponse> reportRant(@PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody RantReportRequest request) {
        UserAccount user = authService.requireUser(authorization);
        return ResponseEntity.status(HttpStatus.CREATED).body(rantService.createReport(id, user, request));
    }

    @GetMapping("/moderation/reports")
    public ResponseEntity<List<RantReportResponse>> getOpenReports(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        UserAccount user = authService.requireUser(authorization);
        return ResponseEntity.ok(rantService.getOpenReports(user));
    }

    @PostMapping("/moderation/reports/{reportId}/resolve")
    public ResponseEntity<Void> resolveReport(@PathVariable Long reportId,
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody ReportDecisionRequest request) {
        UserAccount user = authService.requireUser(authorization);
        rantService.resolveReport(reportId, request.decision(), user);
        return ResponseEntity.noContent().build();
    }
}