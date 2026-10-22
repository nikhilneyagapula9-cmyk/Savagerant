package com.savagescreen.backend.service;

import com.savagescreen.backend.dto.VideoPost;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.function.Supplier;
import org.springframework.http.HttpStatus;

@Service
public class ServiceBridge {
    private final RestClient socialGraph;
    private final RestClient videoService;

    public ServiceBridge(
            @Value("${savagescreen.social-graph-url:http://127.0.0.1:8082}") String socialGraphUrl,
            @Value("${savagescreen.video-service-url:http://127.0.0.1:8083}") String videoServiceUrl) {
        this.socialGraph = RestClient.builder().baseUrl(socialGraphUrl).build();
        this.videoService = RestClient.builder().baseUrl(videoServiceUrl).build();
    }

    public List<Long> connections(Long userId) {
        return remote(() -> socialGraph.get().uri("/connections?userId={userId}", userId)
                .retrieve().body(new ParameterizedTypeReference<>() {}));
    }

    public void connect(Long userId, Long targetId) {
        remote(() -> socialGraph.post().uri("/connections")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ConnectionRequest(userId, targetId))
                .retrieve().toBodilessEntity());
    }

    public void disconnect(Long userId, Long targetId) {
        remote(() -> socialGraph.delete().uri("/connections/{targetId}?userId={userId}", targetId, userId)
                .retrieve().toBodilessEntity());
    }

    public List<VideoPost> videos(String scope, Long userId, List<Long> authorIds) {
        return remote(() -> videoService.get().uri(uriBuilder -> {
                    uriBuilder.path("/videos").queryParam("scope", scope);
                    if (userId != null) {
                        uriBuilder.queryParam("userId", userId);
                    }
                    if (!authorIds.isEmpty()) {
                        uriBuilder.queryParam("authorIds", authorIds.stream().map(String::valueOf).toArray());
                    }
                    return uriBuilder.build();
                }).retrieve().body(new ParameterizedTypeReference<>() {}));
    }

    public VideoPost uploadVideo(Long userId, String author, String caption, MediaType contentType, byte[] bytes) {
        return remote(() -> videoService.post().uri(uriBuilder -> uriBuilder.path("/videos")
                        .queryParam("caption", caption).queryParam("author", author).build())
                .contentType(contentType)
                .header("X-User-Id", userId.toString())
                .body(bytes)
                .retrieve().body(VideoPost.class));
    }

    public ResponseEntity<byte[]> videoContent(String id) {
        return remote(() -> videoService.get().uri("/videos/{id}/content", id)
                .retrieve().toEntity(byte[].class));
    }

    private <T> T remote(Supplier<T> request) {
        try {
            return request.get();
        } catch (ResourceAccessException exception) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "A supporting service is temporarily unavailable.", exception);
        } catch (RestClientResponseException exception) {
            throw new ResponseStatusException(exception.getStatusCode(),
                    "A supporting service could not complete the request.", exception);
        }
    }

    private record ConnectionRequest(Long userId, Long targetUserId) {
    }
}
