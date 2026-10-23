package com.savagescreen.backend.repository;

import com.savagescreen.backend.model.RoastHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoastHistoryRepository extends JpaRepository<RoastHistory, Long> {
    java.util.List<RoastHistory> findByUserIdOrderByCreatedAtDesc(Long userId);
    java.util.Optional<RoastHistory> findByIdAndUserId(Long id, Long userId);
}