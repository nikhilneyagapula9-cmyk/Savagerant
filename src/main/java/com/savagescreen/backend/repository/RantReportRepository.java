package com.savagescreen.backend.repository;

import com.savagescreen.backend.model.RantReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RantReportRepository extends JpaRepository<RantReport, Long> {
    boolean existsByRant_IdAndReporter_Id(Long rantId, Long reporterId);
    List<RantReport> findAllByOrderByCreatedAtAsc();
}
