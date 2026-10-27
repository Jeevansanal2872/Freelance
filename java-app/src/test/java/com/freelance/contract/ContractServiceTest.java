package com.freelance.contract;

import com.freelance.job.Job;
import com.freelance.job.JobService;
import com.freelance.proposal.Proposal;
import com.freelance.proposal.ProposalService;
import com.freelance.user.User;
import com.freelance.user.UserRole;
import com.freelance.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ContractService Tests - 15 test cases covering contract lifecycle.
 */
@SpringBootTest
@Transactional
@DisplayName("Contract Service - 15 Test Cases")
class ContractServiceTest {

    @Autowired private ContractService contractService;
    @Autowired private ProposalService proposalService;
    @Autowired private JobService jobService;
    @Autowired private UserService userService;

    private Long clientId;
    private Long freelancerId;
    private Long contractId;

    @BeforeEach
    void setUp() {
        User client = userService.register("Client", "client-ct@test.com", "pass", UserRole.CLIENT);
        User freelancer = userService.register("Freelancer", "fl-ct@test.com", "pass", UserRole.FREELANCER);
        clientId = client.getId();
        freelancerId = freelancer.getId();

        // Create a job, submit proposal, accept it → creates contract
        Job job = jobService.createJob(clientId, "Contract Test Job", "Desc", 500.0, "Dev", "Java", null);
        Proposal proposal = proposalService.submitProposal(job.getId(), freelancerId, "Cover", 450.0, 7);
        Contract contract = proposalService.acceptProposal(proposal.getId());
        contractId = contract.getId();
    }

    @Test
    @DisplayName("T1: Contract is created with ACTIVE status after hiring")
    void testContractIsActive() {
        var contract = contractService.findById(contractId);
        assertTrue(contract.isPresent());
        assertEquals(ContractStatus.ACTIVE, contract.get().getStatus());
    }

    @Test
    @DisplayName("T2: Contract has correct client ID")
    void testContractClientId() {
        var contract = contractService.findById(contractId).orElseThrow();
        assertEquals(clientId, contract.getClientId());
    }

    @Test
    @DisplayName("T3: Contract has correct freelancer ID")
    void testContractFreelancerId() {
        var contract = contractService.findById(contractId).orElseThrow();
        assertEquals(freelancerId, contract.getFreelancerId());
    }

    @Test
    @DisplayName("T4: Contract amount matches bid amount")
    void testContractAmount() {
        var contract = contractService.findById(contractId).orElseThrow();
        assertEquals(450.0, contract.getAmount());
    }

    @Test
    @DisplayName("T5: Get contracts as client returns the contract")
    void testGetContractsAsClient() {
        var contracts = contractService.getContractsAsClient(clientId);
        assertTrue(contracts.size() >= 1);
        contracts.forEach(c -> assertEquals(clientId, c.getClientId()));
    }

    @Test
    @DisplayName("T6: Get contracts as freelancer returns the contract")
    void testGetContractsAsFreelancer() {
        var contracts = contractService.getContractsAsFreelancer(freelancerId);
        assertTrue(contracts.size() >= 1);
        contracts.forEach(c -> assertEquals(freelancerId, c.getFreelancerId()));
    }

    @Test
    @DisplayName("T7: Complete a contract successfully")
    void testCompleteContract() {
        Contract completed = contractService.completeContract(contractId, clientId, "Great work!");
        assertEquals(ContractStatus.COMPLETED, completed.getStatus());
        assertEquals("Great work!", completed.getCompletionNote());
        assertNotNull(completed.getEndedAt());
    }

    @Test
    @DisplayName("T8: Non-client cannot complete a contract")
    void testNonClientCannotComplete() {
        assertThrows(IllegalStateException.class,
            () -> contractService.completeContract(contractId, freelancerId, "Not allowed"));
    }

    @Test
    @DisplayName("T9: Cancel a contract as client")
    void testCancelAsClient() {
        Contract cancelled = contractService.cancelContract(contractId, clientId, "Budget cut");
        assertEquals(ContractStatus.CANCELLED, cancelled.getStatus());
    }

    @Test
    @DisplayName("T10: Cancel a contract as freelancer")
    void testCancelAsFreelancer() {
        Contract cancelled = contractService.cancelContract(contractId, freelancerId, "Personal reasons");
        assertEquals(ContractStatus.CANCELLED, cancelled.getStatus());
    }

    @Test
    @DisplayName("T11: Non-party cannot cancel a contract")
    void testNonPartyCannotCancel() {
        User outsider = userService.register("Outsider", "outsider@test.com", "pass", UserRole.CLIENT);
        assertThrows(IllegalStateException.class,
            () -> contractService.cancelContract(contractId, outsider.getId(), "Hacking attempt"));
    }

    @Test
    @DisplayName("T12: Cannot complete an already completed contract")
    void testCannotCompleteTwice() {
        contractService.completeContract(contractId, clientId, "Done");
        assertThrows(IllegalStateException.class,
            () -> contractService.completeContract(contractId, clientId, "Again"));
    }

    @Test
    @DisplayName("T13: Get active contracts for freelancer")
    void testGetActiveContractsForFreelancer() {
        var active = contractService.getActiveContractsForFreelancer(freelancerId);
        assertTrue(active.size() >= 1);
        active.forEach(c -> assertEquals(ContractStatus.ACTIVE, c.getStatus()));
    }

    @Test
    @DisplayName("T14: Contract start time is set")
    void testContractStartTime() {
        var contract = contractService.findById(contractId).orElseThrow();
        assertNotNull(contract.getStartedAt());
        assertTrue(contract.getStartedAt().isAfter(java.time.LocalDateTime.now().minusMinutes(1)));
    }

    @Test
    @DisplayName("T15: Finding non-existent contract returns empty")
    void testFindNonExistent() {
        assertTrue(contractService.findById(99999L).isEmpty());
    }
}
