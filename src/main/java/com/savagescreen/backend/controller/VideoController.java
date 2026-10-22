package com.savagescreen.backend.controller;

import com.savagescreen.backend.dto.VideoPost;
import com.savagescreen.backend.model.UserAccount;
import com.savagescreen.backend.service.AuthService;
import com.savagescreen.backend.service.ModerationService;
import com.savagescreen.backend.service.ServiceBridge;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.io.IOException;

@RestController
@RequestMapping("/api/videos")
public class VideoController {
    private static final int MAX_VIDEO_BYTES = 50 * 1024 * 1024;
    private static final List<MediaType> ALLOWED_TYPES = List.of(
            MediaType.valueOf("video/mp4"),
            MediaType.valueOf("video/webm"),
            MediaType.valueOf("video/quicktime"));
    private final AuthService authService;
    private final ServiceBridge serviceBridge;
    private final ModerationService moderationService;

    public VideoController(AuthService authService, ServiceBridge serviceBridge, ModerationService moderationService) {
        this.authService = authService;
        this.serviceBridge = serviceBridge;
        this.moderationService = moderationService;
    }

    @GetMapping
    public List<VideoPost> getVideos(
            @RequestParam(defaultValue = "public") String scope,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        if ("public".equals(scope)) {
            return serviceBridge.videos(scope, null, List.of());
        }
        UserAccount user = authService.requireUser(authorization);
        List<Long> authorIds = "mine".equals(scope) ? List.of(user.getId())
                : "connections".equals(scope) ? serviceBridge.connections(user.getId()) : null;
        if (authorIds == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Video scope must be public, connections, or mine.");
        }
        return serviceBridge.videos(scope, user.getId(), authorIds);
    }

    @PostMapping(consumes = {"video/mp4", "video/webm", "video/quicktime"})
    public ResponseEntity<VideoPost> uploadVideo(
            @RequestParam String caption,
            @RequestHeader(value = "Content-Type") MediaType contentType,
            @RequestHeader(value = "Authorization", required = false) String authorization,
            HttpServletRequest request) throws IOException {
        UserAccount user = authService.requireUser(authorization);
        if (caption.isBlank() || caption.length() > 160) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Caption must contain 1 to 160 characters.");
        }
        moderationService.requireAllowed(caption);
        if (!ALLOWED_TYPES.contains(contentType)) {
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Upload an MP4, WebM, or MOV video.");
        }
        if (request.getContentLengthLong() > MAX_VIDEO_BYTES) {
            throw new ResponseStatusException(HttpStatus.CONTENT_TOO_LARGE, "Video must be between 1 byte and 50 MB.");
        }
        byte[] content = request.getInputStream().readNBytes(MAX_VIDEO_BYTES + 1);
        if (content.length == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Video file cannot be empty.");
        }
        if (content.length > MAX_VIDEO_BYTES) {
            throw new ResponseStatusException(HttpStatus.CONTENT_TOO_LARGE, "Video must be between 1 byte and 50 MB.");
        }
        if (!hasVideoSignature(contentType, content)) {
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                    "The file contents do not match the selected video format.");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(serviceBridge.uploadVideo(
                user.getId(), user.getDisplayName(), caption.strip(), contentType, content));
    }

    @GetMapping("/{id}/content")
    public ResponseEntity<byte[]> getContent(@PathVariable String id) {
        ResponseEntity<byte[]> response = serviceBridge.videoContent(id);
        MediaType contentType = response.getHeaders().getContentType() == null
                ? MediaType.APPLICATION_OCTET_STREAM : response.getHeaders().getContentType();
        return ResponseEntity.status(response.getStatusCode())
                .contentType(contentType)
                .header("X-Content-Type-Options", "nosniff")
                .body(response.getBody());
    }

    private boolean hasVideoSignature(MediaType contentType, byte[] content) {
        if (contentType.equals(MediaType.valueOf("video/webm"))) {
            return content.length >= 4 && content[0] == 0x1A && content[1] == 0x45
                    && (content[2] & 0xFF) == 0xDF && (content[3] & 0xFF) == 0xA3;
        }
        return content.length >= 8 && content[4] == 'f' && content[5] == 't'
                && content[6] == 'y' && content[7] == 'p';
    }
}
