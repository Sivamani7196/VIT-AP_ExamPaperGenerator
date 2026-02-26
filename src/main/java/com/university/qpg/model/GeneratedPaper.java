package com.university.qpg.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;

@Entity
public class GeneratedPaper {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "generated_by_user_id", nullable = false)
    private AppUser generatedBy;

    @Column(nullable = false)
    private int totalMarks;

    @Column(nullable = false)
    private int easyPercentage;

    @Column(nullable = false)
    private int mediumPercentage;

    @Column(nullable = false)
    private int hardPercentage;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String paperContent;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public AppUser getGeneratedBy() {
        return generatedBy;
    }

    public void setGeneratedBy(AppUser generatedBy) {
        this.generatedBy = generatedBy;
    }

    public int getTotalMarks() {
        return totalMarks;
    }

    public void setTotalMarks(int totalMarks) {
        this.totalMarks = totalMarks;
    }

    public int getEasyPercentage() {
        return easyPercentage;
    }

    public void setEasyPercentage(int easyPercentage) {
        this.easyPercentage = easyPercentage;
    }

    public int getMediumPercentage() {
        return mediumPercentage;
    }

    public void setMediumPercentage(int mediumPercentage) {
        this.mediumPercentage = mediumPercentage;
    }

    public int getHardPercentage() {
        return hardPercentage;
    }

    public void setHardPercentage(int hardPercentage) {
        this.hardPercentage = hardPercentage;
    }

    public String getPaperContent() {
        return paperContent;
    }

    public void setPaperContent(String paperContent) {
        this.paperContent = paperContent;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
