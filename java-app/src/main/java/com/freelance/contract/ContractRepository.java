package com.freelance.contract;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

/**
 * ContractRepository - auto-generated SQL queries.
 */
public interface ContractRepository extends JpaRepository<Contract, Long> {

    /** All contracts where this user is the client */
    List<Contract> findByClientId(Long clientId);

    /** All contracts where this user is the freelancer */
    List<Contract> findByFreelancerId(Long freelancerId);

    /** Find the contract for a specific job */
    Optional<Contract> findByJobId(Long jobId);

    /** Active contracts for a user (as freelancer) */
    List<Contract> findByFreelancerIdAndStatus(Long freelancerId, ContractStatus status);

    /** Active contracts for a user (as client) */
    List<Contract> findByClientIdAndStatus(Long clientId, ContractStatus status);
}
