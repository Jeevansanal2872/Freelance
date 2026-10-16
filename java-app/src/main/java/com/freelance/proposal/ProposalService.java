package com.freelance.proposal;

import com.freelance.contract.Contract;
import com.freelance.contract.ContractRepository;
import com.freelance.job.JobService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * ProposalService - core hiring logic.
 *
 * KEY FLOW (Upwork-style hiring):
 *   1. Freelancer calls submitProposal()
 *   2. Client views proposals with getProposalsForJob()
 *   3. Client calls acceptProposal() → this:
 *      a. Sets proposal status to ACCEPTED
 *      b. Rejects all other proposals for the same job
 *      c. Creates a Contract between client and freelancer
 *      d. Marks the job as IN_PROGRESS
 */
@Service
public class ProposalService {

    private final ProposalRepository proposalRepository;
    private final ContractRepository contractRepository;
    private final JobService jobService;

    public ProposalService(ProposalRepository proposalRepository,
                           ContractRepository contractRepository,
                           JobService jobService) {
        this.proposalRepository = proposalRepository;
        this.contractRepository = contractRepository;
        this.jobService = jobService;
    }

    /**
     * Freelancer submits a proposal for a job.
     *
     * @param jobId         ID of the job to apply to
     * @param freelancerId  ID of the freelancer applying
     * @param coverLetter   Freelancer's pitch/message
     * @param bidAmount     How much the freelancer is asking
     * @param estimatedDays How many days to complete
     */
    public Proposal submitProposal(Long jobId, Long freelancerId,
                                   String coverLetter, Double bidAmount, Integer estimatedDays) {

        // Prevent duplicate applications
        if (proposalRepository.existsByJobIdAndFreelancerId(jobId, freelancerId)) {
            throw new IllegalStateException("You have already applied to this job");
        }

        Proposal proposal = new Proposal(jobId, freelancerId, coverLetter, bidAmount, estimatedDays);
        return proposalRepository.save(proposal);
    }

    /**
     * Get all proposals for a specific job (client views these).
     */
    public List<Proposal> getProposalsForJob(Long jobId) {
        return proposalRepository.findByJobId(jobId);
    }

    /**
     * Get all proposals submitted by a freelancer (their application history).
     */
    public List<Proposal> getProposalsByFreelancer(Long freelancerId) {
        return proposalRepository.findByFreelancerId(freelancerId);
    }

    /**
     * Get a single proposal by ID.
     */
    public Optional<Proposal> findById(Long id) {
        return proposalRepository.findById(id);
    }

    /**
     * Client accepts a proposal → creates a contract and rejects others.
     * This is the HIRE action.
     *
     * @Transactional means: if anything fails, ALL changes are rolled back.
     */
    @Transactional
    public Contract acceptProposal(Long proposalId) {
        // Step 1: Find and accept the proposal
        Proposal proposal = proposalRepository.findById(proposalId)
            .orElseThrow(() -> new IllegalArgumentException("Proposal not found: " + proposalId));

        if (proposal.getStatus() != ProposalStatus.PENDING) {
            throw new IllegalStateException("This proposal is no longer pending");
        }

        proposal.accept();
        proposalRepository.save(proposal);

        // Step 2: Reject all other proposals for the same job
        List<Proposal> others = proposalRepository.findByJobIdAndStatus(
            proposal.getJobId(), ProposalStatus.PENDING
        );
        for (Proposal other : others) {
            if (!other.getId().equals(proposalId)) {
                other.reject();
                proposalRepository.save(other);
            }
        }

        // Step 3: Mark the job as in progress
        jobService.markInProgress(proposal.getJobId());

        // Step 4: Create a contract
        Contract contract = new Contract(
            proposal.getJobId(),
            // We need the client ID - get it from the job
            jobService.findById(proposal.getJobId())
                .orElseThrow(() -> new IllegalStateException("Job not found"))
                .getClientId(),
            proposal.getFreelancerId(),
            proposal.getId(),
            proposal.getBidAmount()
        );
        return contractRepository.save(contract);
    }

    /**
     * Client rejects a specific proposal.
     */
    public Proposal rejectProposal(Long proposalId) {
        Proposal proposal = proposalRepository.findById(proposalId)
            .orElseThrow(() -> new IllegalArgumentException("Proposal not found: " + proposalId));

        proposal.reject();
        return proposalRepository.save(proposal);
    }

    /**
     * Freelancer withdraws their own proposal.
     */
    public Proposal withdrawProposal(Long proposalId, Long freelancerId) {
        Proposal proposal = proposalRepository.findById(proposalId)
            .orElseThrow(() -> new IllegalArgumentException("Proposal not found: " + proposalId));

        if (!proposal.getFreelancerId().equals(freelancerId)) {
            throw new IllegalStateException("You can only withdraw your own proposals");
        }

        proposal.withdraw();
        return proposalRepository.save(proposal);
    }
}
