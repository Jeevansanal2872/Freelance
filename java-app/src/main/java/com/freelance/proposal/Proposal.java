package com.freelance.proposal;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Proposal entity - a freelancer applies to a job by sending a proposal.
 *
 * Flow:
 *   Freelancer submits proposal → Client reviews → Client ACCEPTS or REJECTS
 *   If ACCEPTED → a Contract is automatically created
 *
 * Table: proposals
 */
@Entity
@Table(name = "proposals")
public class Proposal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Which job this proposal is for
    @Column(name = "job_id", nullable = false)
    private Long jobId;

    // Which freelancer submitted this proposal
    @Column(name = "freelancer_id", nullable = false)
    private Long freelancerId;

    // Cover letter / pitch from the freelancer
    @Column(nullable = false, columnDefinition = "TEXT")
    private String coverLetter;

    // How much the freelancer is asking for this job
    private Double bidAmount;

    // Estimated days to complete the work
    private Integer estimatedDays;

    // Current status of this proposal
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProposalStatus status;

    // When the proposal was submitted
    @Column(nullable = false)
    private LocalDateTime createdAt;

    // When the status changed (accepted/rejected)
    private LocalDateTime respondedAt;

    // JPA requires no-arg constructor
    protected Proposal() {}

    public Proposal(Long jobId, Long freelancerId, String coverLetter,
                    Double bidAmount, Integer estimatedDays) {
        this.jobId = jobId;
        this.freelancerId = freelancerId;
        this.coverLetter = coverLetter;
        this.bidAmount = bidAmount;
        this.estimatedDays = estimatedDays;
        this.status = ProposalStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    // ─── Getters ───────────────────────────────────────────────────────────────

    public Long getId() { return id; }
    public Long getJobId() { return jobId; }
    public Long getFreelancerId() { return freelancerId; }
    public String getCoverLetter() { return coverLetter; }
    public Double getBidAmount() { return bidAmount; }
    public Integer getEstimatedDays() { return estimatedDays; }
    public ProposalStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getRespondedAt() { return respondedAt; }

    // ─── Status transitions ────────────────────────────────────────────────────

    /** Client accepts this proposal → will trigger contract creation */
    public void accept() {
        this.status = ProposalStatus.ACCEPTED;
        this.respondedAt = LocalDateTime.now();
    }

    /** Client rejects this proposal */
    public void reject() {
        this.status = ProposalStatus.REJECTED;
        this.respondedAt = LocalDateTime.now();
    }

    /** Mark as withdrawn (freelancer changed their mind) */
    public void withdraw() {
        if (this.status != ProposalStatus.PENDING) {
            throw new IllegalStateException("Can only withdraw a pending proposal");
        }
        this.status = ProposalStatus.WITHDRAWN;
        this.respondedAt = LocalDateTime.now();
    }
}
