package com.university.qpg.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.university.qpg.model.GeneratedPaper;

public interface GeneratedPaperRepository extends JpaRepository<GeneratedPaper, Long> {
    List<GeneratedPaper> findAllByOrderByCreatedAtDesc();
}
