package com.savagescreen.backend.repository;

import com.savagescreen.backend.model.RantInteraction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RantInteractionRepository extends JpaRepository<RantInteraction, Long> {
    boolean existsByRantIdAndUserIdAndKind(Long rantId, Long userId, String kind);
    Optional<RantInteraction> findByRantIdAndUserIdAndKind(Long rantId, Long userId, String kind);
    long countByRantIdAndKind(Long rantId, String kind);
    List<RantInteraction> findByRantIdAndKind(Long rantId, String kind);
    void deleteByRantIdAndUserIdAndKind(Long rantId, Long userId, String kind);
}
