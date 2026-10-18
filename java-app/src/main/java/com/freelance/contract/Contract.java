package com.freelance.contract;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Contract entity - created when a client hires a freelancer.
 *
 * A contract ties together:
 *   - A job
 *   - The client (who posted the job)
 *   - The freelancer (who was hired)
 *   - The agreed amount
 *
 * Status flow: ACTIVE → COMPLETED or CANCELLED
 *
 * Table: contracts
 */
@Entity
@Table(name = "contracts")
public class Contract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The job this contract is for
    @Column(name = "job_id", nullable = false)
    private Long jobId;

    // The client who hired
    @Column(name = "client_id", nullable = false)
    private Long clientId;

    // The freelancer who was hired
    @Column(name = "freelancer_id", nullable = false)
    private Long freelancerId;

    // The proposal that was accepted (for reference)
    @Column(name = "proposal_id")
    private Long proposalId;

    // Agreed payment amount in dollars
    private Double amount;

    // Contract lifecycle status
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContractStatus status;

    // When the contract started
    @Column(nullable = false)
    private LocalDateTime startedAt;

    // When the contract ended (completed or cancelled)
    private LocalDateTime endedAt;

    // Notes or review from client when completing
    @Column(length = 500)
    private String completionNote;

    // JPA no-arg constructor
    protected Contract() {}

    public Contract(Long jobId, Long clientId, Long freelancerId, Long proposalId, Double amount) {
        this.jobId = jobId;
        this.clientId = clientId;
        this.freelancerId = freelancerId;
        this.proposalId = proposalId;
        this.amount = amount;
        this.status = ContractStatus.ACTIVE;
        this.startedAt = LocalDateTime.now();
    }

    // ─── Getters ───────────────────────────────────────────────────────────────

    public Long getId() { return id; }
    public Long getJobId() { return jobId; }
    public Long getClientId() { return clientId; }
    public Long getFreelancerId() { return freelancerId; }
    public Long getProposalId() { return proposalId; }
    public Double getAmount() { return amount; }
    public ContractStatus getStatus() { return status; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public LocalDateTime getEndedAt() { return endedAt; }
    public String getCompletionNote() { return completionNote; }

    // ─── Lifecycle methods ────────────────────────────────────────────────────

    /** Client marks work as completed */
    public void complete(String note) {
        if (this.status != ContractStatus.ACTIVE) {
            throw new IllegalStateException("Contract is not active");
        }
        this.status = ContractStatus.COMPLETED;
        this.endedAt = LocalDateTime.now();
        this.completionNote = note;
    }

    /** Cancel the contract (either party can cancel) */
    public void cancel(String reason) {
        if (this.status != ContractStatus.ACTIVE) {
            throw new IllegalStateException("Contract is not active");
        }
        this.status = ContractStatus.CANCELLED;
        this.endedAt = LocalDateTime.now();
        this.completionNote = reason;
    }
}
