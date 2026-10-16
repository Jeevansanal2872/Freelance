package com.freelance.proposal;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * ProposalRepository - Spring Data JPA generates SQL automatically.
 */
public interface ProposalRepository extends JpaRepository<Proposal, Long> {

    /** All proposals for a specific job (client views these) */
    List<Proposal> findByJobId(Long jobId);

    /** All proposals by a specific freelancer (freelancer's history) */
    List<Proposal> findByFreelancerId(Long freelancerId);

    /** Check if a freelancer already applied to a job */
    boolean existsByJobIdAndFreelancerId(Long jobId, Long freelancerId);

    /** Get accepted proposals for a job (should be max 1) */
    List<Proposal> findByJobIdAndStatus(Long jobId, ProposalStatus status);
}
