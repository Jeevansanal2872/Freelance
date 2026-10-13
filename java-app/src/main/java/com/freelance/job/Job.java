package com.freelance.job;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * Job entity - represents a job posted by a CLIENT.
 * Freelancers browse these jobs and submit proposals.
 *
 * Table: jobs
 */
@Entity
@Table(name = "jobs")
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The client (user) who posted this job
    @Column(name = "client_id", nullable = false)
    private Long clientId;

    // Job title (e.g., "Build a React Website")
    @NotBlank
    @Column(nullable = false)
    private String title;

    // Full description of the work needed
    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    // Budget in dollars (what the client is willing to pay)
    private Double budget;

    // Category (e.g., "Web Development", "Design", "Writing")
    @Column(nullable = false)
    private String category;

    // Required skills (e.g., "Java, Spring Boot, React")
    @Column(columnDefinition = "TEXT")
    private String requiredSkills;

    // Deadline for the project
    private LocalDateTime deadline;

    // Current status of the job listing
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobStatus status;

    // When this job was posted
    @Column(nullable = false)
    private LocalDateTime createdAt;

    // JPA requires no-arg constructor
    protected Job() {}

    // Constructor used when creating a new job
    public Job(Long clientId, String title, String description,
               Double budget, String category, String requiredSkills, LocalDateTime deadline) {
        this.clientId = clientId;
        this.title = title;
        this.description = description;
        this.budget = budget;
        this.category = category;
        this.requiredSkills = requiredSkills;
        this.deadline = deadline;
        this.status = JobStatus.OPEN;   // New jobs start as OPEN
        this.createdAt = LocalDateTime.now();
    }

    // ─── Getters ───────────────────────────────────────────────────────────────

    public Long getId() { return id; }
    public Long getClientId() { return clientId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Double getBudget() { return budget; }
    public String getCategory() { return category; }
    public String getRequiredSkills() { return requiredSkills; }
    public LocalDateTime getDeadline() { return deadline; }
    public JobStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // ─── Status changers ──────────────────────────────────────────────────────

    /** Mark this job as having a hired freelancer (in progress) */
    public void markInProgress() { this.status = JobStatus.IN_PROGRESS; }

    /** Mark this job as completed */
    public void markCompleted() { this.status = JobStatus.COMPLETED; }

    /** Close this job without hiring */
    public void close() { this.status = JobStatus.CLOSED; }

    // Setters for editing
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setBudget(Double budget) { this.budget = budget; }
    public void setCategory(String category) { this.category = category; }
    public void setRequiredSkills(String requiredSkills) { this.requiredSkills = requiredSkills; }
    public void setDeadline(LocalDateTime deadline) { this.deadline = deadline; }
}
