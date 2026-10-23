package com.savagescreen.backend.repository;

import com.savagescreen.backend.model.Rant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RantRepository extends JpaRepository<Rant, Long> {
    boolean existsByAuthorAndContent(String author, String content);
    List<Rant> findByCategory(String category);
    List<Rant> findByHiddenFalseOrderByCreatedAtDesc();
    List<Rant> findByCategoryAndHiddenFalseOrderByCreatedAtDesc(String category);
    List<Rant> findByAuthorIdInAndHiddenFalseOrderByCreatedAtDesc(java.util.Collection<Long> authorIds);
}
