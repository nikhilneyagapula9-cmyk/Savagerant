package com.savagescreen.backend.repository;

import com.savagescreen.backend.model.RantComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RantCommentRepository extends JpaRepository<RantComment, Long> {
    List<RantComment> findByRantIdOrderByCreatedAtAsc(Long rantId);
}
