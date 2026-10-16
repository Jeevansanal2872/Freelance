package com.freelance.proposal;

import com.freelance.contract.Contract;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * ProposalController - REST API for proposals.
 *
 * Endpoints:
 *   POST   /api/proposals               → Freelancer submits a proposal
 *   GET    /api/proposals/job/{jobId}   → Client views proposals for their job
 *   GET    /api/proposals/my/{freelancerId} → Freelancer's own proposals
 *   GET    /api/proposals/{id}          → View a single proposal
 *   POST   /api/proposals/{id}/accept   → Client accepts (HIRES) a freelancer
 *   POST   /api/proposals/{id}/reject   → Client rejects a proposal
 *   POST   /api/proposals/{id}/withdraw → Freelancer withdraws their proposal
 */
@RestController
@RequestMapping("/api/proposals")
public class ProposalController {

    private final ProposalService proposalService;

    public ProposalController(ProposalService proposalService) {
        this.proposalService = proposalService;
    }

    // POST /api/proposals - Submit a proposal
    @PostMapping
    public ResponseEntity<ProposalResponse> submitProposal(@Valid @RequestBody SubmitProposalRequest req) {
        Proposal proposal = proposalService.submitProposal(
            req.jobId(), req.freelancerId(), req.coverLetter(), req.bidAmount(), req.estimatedDays()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(ProposalResponse.from(proposal));
    }

    // GET /api/proposals/job/{jobId} - Client sees all proposals for their job
    @GetMapping("/job/{jobId}")
    public ResponseEntity<List<ProposalResponse>> getProposalsForJob(@PathVariable Long jobId) {
        List<ProposalResponse> proposals = proposalService.getProposalsForJob(jobId)
            .stream().map(ProposalResponse::from).toList();
        return ResponseEntity.ok(proposals);
    }

    // GET /api/proposals/my/{freelancerId} - Freelancer's application history
    @GetMapping({"/my/{freelancerId}", "/freelancer/{freelancerId}"})
    public ResponseEntity<List<ProposalResponse>> getMyProposals(@PathVariable Long freelancerId) {
        List<ProposalResponse> proposals = proposalService.getProposalsByFreelancer(freelancerId)
            .stream().map(ProposalResponse::from).toList();
        return ResponseEntity.ok(proposals);
    }

    // GET /api/proposals/{id} - View a specific proposal
    @GetMapping("/{id}")
    public ResponseEntity<ProposalResponse> getProposal(@PathVariable Long id) {
        return proposalService.findById(id)
            .map(p -> ResponseEntity.ok(ProposalResponse.from(p)))
            .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/proposals/{id}/accept - HIRE a freelancer
    @PostMapping("/{id}/accept")
    public ResponseEntity<Map<String, Object>> acceptProposal(@PathVariable Long id) {
        Contract contract = proposalService.acceptProposal(id);
        return ResponseEntity.ok(Map.of(
            "message", "Freelancer hired successfully!",
            "contractId", contract.getId(),
            "status", "CONTRACT_CREATED"
        ));
    }

    // POST /api/proposals/{id}/reject - Reject a proposal
    @PostMapping("/{id}/reject")
    public ResponseEntity<ProposalResponse> rejectProposal(@PathVariable Long id) {
        Proposal proposal = proposalService.rejectProposal(id);
        return ResponseEntity.ok(ProposalResponse.from(proposal));
    }

    // POST /api/proposals/{id}/withdraw?freelancerId=X
    @PostMapping("/{id}/withdraw")
    public ResponseEntity<ProposalResponse> withdrawProposal(
            @PathVariable Long id,
            @RequestParam Long freelancerId) {
        Proposal proposal = proposalService.withdrawProposal(id, freelancerId);
        return ResponseEntity.ok(ProposalResponse.from(proposal));
    }

    // ─── Request / Response classes ───────────────────────────────────────────

    public record SubmitProposalRequest(
        @NotNull Long jobId,
        @NotNull Long freelancerId,
        @NotBlank String coverLetter,
        Double bidAmount,
        Integer estimatedDays
    ) {}

    public record ProposalResponse(
        Long id,
        Long jobId,
        Long freelancerId,
        String coverLetter,
        Double bidAmount,
        Integer estimatedDays,
        String status,
        String createdAt
    ) {
        public static ProposalResponse from(Proposal p) {
            return new ProposalResponse(
                p.getId(), p.getJobId(), p.getFreelancerId(),
                p.getCoverLetter(), p.getBidAmount(), p.getEstimatedDays(),
                p.getStatus().name(), p.getCreatedAt().toString()
            );
        }
    }
}
