package com.freelance.contract;

import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

/**
 * ContractService - manages active contracts between clients and freelancers.
 */
@Service
public class ContractService {

    private final ContractRepository contractRepository;

    public ContractService(ContractRepository contractRepository) {
        this.contractRepository = contractRepository;
    }

    /**
     * Get all contracts where this user is the client.
     */
    public List<Contract> getContractsAsClient(Long clientId) {
        return contractRepository.findByClientId(clientId);
    }

    /**
     * Get all contracts where this user is the freelancer.
     */
    public List<Contract> getContractsAsFreelancer(Long freelancerId) {
        return contractRepository.findByFreelancerId(freelancerId);
    }

    /**
     * Get a contract by ID.
     */
    public Optional<Contract> findById(Long id) {
        return contractRepository.findById(id);
    }

    /**
     * Get the contract for a specific job.
     */
    public Optional<Contract> getContractForJob(Long jobId) {
        return contractRepository.findByJobId(jobId);
    }

    /**
     * Client marks a contract as completed (work is done, payment approved).
     *
     * @param contractId    ID of the contract
     * @param clientId      Must match the contract's clientId (security check)
     * @param completionNote  Optional note or review
     */
    public Contract completeContract(Long contractId, Long clientId, String completionNote) {
        Contract contract = contractRepository.findById(contractId)
            .orElseThrow(() -> new IllegalArgumentException("Contract not found: " + contractId));

        if (!contract.getClientId().equals(clientId)) {
            throw new IllegalStateException("Only the hiring client can mark a contract as completed");
        }

        contract.complete(completionNote);
        return contractRepository.save(contract);
    }

    /**
     * Cancel a contract (either client or freelancer can cancel).
     */
    public Contract cancelContract(Long contractId, Long userId, String reason) {
        Contract contract = contractRepository.findById(contractId)
            .orElseThrow(() -> new IllegalArgumentException("Contract not found: " + contractId));

        // Either party can cancel
        boolean isClient = contract.getClientId().equals(userId);
        boolean isFreelancer = contract.getFreelancerId().equals(userId);

        if (!isClient && !isFreelancer) {
            throw new IllegalStateException("You are not a party to this contract");
        }

        contract.cancel(reason);
        return contractRepository.save(contract);
    }

    /**
     * Get active contracts for a freelancer (their ongoing work).
     */
    public List<Contract> getActiveContractsForFreelancer(Long freelancerId) {
        return contractRepository.findByFreelancerIdAndStatus(freelancerId, ContractStatus.ACTIVE);
    }

    /**
     * Get active contracts for a client (jobs they've hired for).
     */
    public List<Contract> getActiveContractsForClient(Long clientId) {
        return contractRepository.findByClientIdAndStatus(clientId, ContractStatus.ACTIVE);
    }
}
