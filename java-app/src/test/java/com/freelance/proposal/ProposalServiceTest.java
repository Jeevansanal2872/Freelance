package com.freelance.proposal;

import com.freelance.contract.Contract;
import com.freelance.job.Job;
import com.freelance.job.JobService;
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
 * ProposalService Tests - 15 test cases covering the entire hire flow.
 */
@SpringBootTest
@Transactional
@DisplayName("Proposal Service - 15 Test Cases (Hire Flow)")
class ProposalServiceTest {

    @Autowired private ProposalService proposalService;
    @Autowired private JobService jobService;
    @Autowired private UserService userService;

    private Long clientId;
    private Long freelancerId;
    private Long freelancer2Id;
    private Long jobId;

    @BeforeEach
    void setUp() {
        User client = userService.register("Client", "client-prop@test.com", "pass", UserRole.CLIENT);
        User freelancer = userService.register("Freelancer", "fl-prop@test.com", "pass", UserRole.FREELANCER);
        User freelancer2 = userService.register("Freelancer2", "fl2-prop@test.com", "pass", UserRole.FREELANCER);
        clientId = client.getId();
        freelancerId = freelancer.getId();
        freelancer2Id = freelancer2.getId();

        Job job = jobService.createJob(clientId, "Test Job", "Description", 500.0, "Dev", "Java", null);
        jobId = job.getId();
    }

    @Test
    @DisplayName("T1: Freelancer submits a proposal successfully")
    void testSubmitProposal() {
        Proposal p = proposalService.submitProposal(jobId, freelancerId, "I can do this!", 400.0, 7);
        assertNotNull(p.getId());
        assertEquals(ProposalStatus.PENDING, p.getStatus());
        assertEquals("I can do this!", p.getCoverLetter());
    }

    @Test
    @DisplayName("T2: Proposal has PENDING status by default")
    void testDefaultPendingStatus() {
        Proposal p = proposalService.submitProposal(jobId, freelancerId, "Cover letter", 300.0, 5);
        assertEquals(ProposalStatus.PENDING, p.getStatus());
    }

    @Test
    @DisplayName("T3: Duplicate application to same job is rejected")
    void testDuplicateApplicationRejected() {
        proposalService.submitProposal(jobId, freelancerId, "First try", 400.0, 7);
        assertThrows(IllegalStateException.class,
            () -> proposalService.submitProposal(jobId, freelancerId, "Second try", 300.0, 5));
    }

    @Test
    @DisplayName("T4: Get all proposals for a job")
    void testGetProposalsForJob() {
        proposalService.submitProposal(jobId, freelancerId, "Cover 1", 400.0, 7);
        proposalService.submitProposal(jobId, freelancer2Id, "Cover 2", 350.0, 5);
        var proposals = proposalService.getProposalsForJob(jobId);
        assertEquals(2, proposals.size());
    }

    @Test
    @DisplayName("T5: Get proposals by freelancer")
    void testGetProposalsByFreelancer() {
        proposalService.submitProposal(jobId, freelancerId, "My proposal", 400.0, 7);
        var proposals = proposalService.getProposalsByFreelancer(freelancerId);
        assertTrue(proposals.size() >= 1);
        proposals.forEach(p -> assertEquals(freelancerId, p.getFreelancerId()));
    }

    @Test
    @DisplayName("T6: Find proposal by ID")
    void testFindById() {
        Proposal p = proposalService.submitProposal(jobId, freelancerId, "Find me", 400.0, 7);
        var found = proposalService.findById(p.getId());
        assertTrue(found.isPresent());
        assertEquals("Find me", found.get().getCoverLetter());
    }

    @Test
    @DisplayName("T7: Accept proposal creates a contract (HIRE)")
    void testAcceptProposalCreatesContract() {
        Proposal p = proposalService.submitProposal(jobId, freelancerId, "Hire me!", 400.0, 7);
        Contract contract = proposalService.acceptProposal(p.getId());
        assertNotNull(contract.getId());
        assertEquals(jobId, contract.getJobId());
        assertEquals(freelancerId, contract.getFreelancerId());
        assertEquals(clientId, contract.getClientId());
    }

    @Test
    @DisplayName("T8: Accepted proposal status changes to ACCEPTED")
    void testAcceptedProposalStatus() {
        Proposal p = proposalService.submitProposal(jobId, freelancerId, "Hire me!", 400.0, 7);
        proposalService.acceptProposal(p.getId());
        var updated = proposalService.findById(p.getId());
        assertTrue(updated.isPresent());
        assertEquals(ProposalStatus.ACCEPTED, updated.get().getStatus());
    }

    @Test
    @DisplayName("T9: Accepting one proposal rejects all others for same job")
    void testAcceptRejectsOthers() {
        Proposal p1 = proposalService.submitProposal(jobId, freelancerId, "Cover 1", 400.0, 7);
        Proposal p2 = proposalService.submitProposal(jobId, freelancer2Id, "Cover 2", 350.0, 5);

        // Accept p1
        proposalService.acceptProposal(p1.getId());

        // p2 should be rejected
        var rejected = proposalService.findById(p2.getId());
        assertTrue(rejected.isPresent());
        assertEquals(ProposalStatus.REJECTED, rejected.get().getStatus());
    }

    @Test
    @DisplayName("T10: Cannot accept an already accepted proposal")
    void testCannotAcceptTwice() {
        Proposal p = proposalService.submitProposal(jobId, freelancerId, "Cover", 400.0, 7);
        proposalService.acceptProposal(p.getId());
        assertThrows(IllegalStateException.class, () -> proposalService.acceptProposal(p.getId()));
    }

    @Test
    @DisplayName("T11: Reject a proposal changes status to REJECTED")
    void testRejectProposal() {
        Proposal p = proposalService.submitProposal(jobId, freelancerId, "Cover", 400.0, 7);
        Proposal rejected = proposalService.rejectProposal(p.getId());
        assertEquals(ProposalStatus.REJECTED, rejected.getStatus());
    }

    @Test
    @DisplayName("T12: Freelancer withdraws their own proposal")
    void testWithdrawProposal() {
        Proposal p = proposalService.submitProposal(jobId, freelancerId, "Cover", 400.0, 7);
        Proposal withdrawn = proposalService.withdrawProposal(p.getId(), freelancerId);
        assertEquals(ProposalStatus.WITHDRAWN, withdrawn.getStatus());
    }

    @Test
    @DisplayName("T13: Freelancer cannot withdraw another person's proposal")
    void testCannotWithdrawOthersProposal() {
        Proposal p = proposalService.submitProposal(jobId, freelancerId, "Cover", 400.0, 7);
        assertThrows(IllegalStateException.class,
            () -> proposalService.withdrawProposal(p.getId(), freelancer2Id));
    }

    @Test
    @DisplayName("T14: Bid amount is stored correctly")
    void testBidAmount() {
        Proposal p = proposalService.submitProposal(jobId, freelancerId, "Cover", 425.75, 10);
        assertEquals(425.75, p.getBidAmount());
    }

    @Test
    @DisplayName("T15: Estimated days are stored correctly")
    void testEstimatedDays() {
        Proposal p = proposalService.submitProposal(jobId, freelancerId, "Cover", 400.0, 14);
        assertEquals(14, p.getEstimatedDays());
    }
}
