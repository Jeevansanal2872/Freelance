package com.freelance.contract;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * ContractController - REST API for contracts.
 *
 * Endpoints:
 *   GET  /api/contracts/client/{clientId}           → Client's all contracts
 *   GET  /api/contracts/freelancer/{freelancerId}   → Freelancer's all contracts
 *   GET  /api/contracts/{id}                        → View a contract
 *   POST /api/contracts/{id}/complete?clientId=X    → Client marks work done
 *   POST /api/contracts/{id}/cancel?userId=X        → Cancel a contract
 */
@RestController
@RequestMapping("/api/contracts")
public class ContractController {

    private final ContractService contractService;

    public ContractController(ContractService contractService) {
        this.contractService = contractService;
    }

    // GET /api/contracts/client/{clientId}
    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<ContractResponse>> getClientContracts(@PathVariable Long clientId) {
        List<ContractResponse> contracts = contractService.getContractsAsClient(clientId)
            .stream().map(ContractResponse::from).toList();
        return ResponseEntity.ok(contracts);
    }

    // GET /api/contracts/freelancer/{freelancerId}
    @GetMapping("/freelancer/{freelancerId}")
    public ResponseEntity<List<ContractResponse>> getFreelancerContracts(@PathVariable Long freelancerId) {
        List<ContractResponse> contracts = contractService.getContractsAsFreelancer(freelancerId)
            .stream().map(ContractResponse::from).toList();
        return ResponseEntity.ok(contracts);
    }

    // GET /api/contracts/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ContractResponse> getContract(@PathVariable Long id) {
        return contractService.findById(id)
            .map(c -> ResponseEntity.ok(ContractResponse.from(c)))
            .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/contracts/{id}/complete?clientId=X&note=...
    @PostMapping("/{id}/complete")
    public ResponseEntity<ContractResponse> completeContract(
            @PathVariable Long id,
            @RequestParam Long clientId,
            @RequestParam(required = false) String note) {
        Contract contract = contractService.completeContract(id, clientId, note);
        return ResponseEntity.ok(ContractResponse.from(contract));
    }

    // POST /api/contracts/{id}/cancel?userId=X&reason=...
    @PostMapping("/{id}/cancel")
    public ResponseEntity<ContractResponse> cancelContract(
            @PathVariable Long id,
            @RequestParam Long userId,
            @RequestParam(required = false) String reason) {
        Contract contract = contractService.cancelContract(id, userId, reason);
        return ResponseEntity.ok(ContractResponse.from(contract));
    }

    // ─── Response record ──────────────────────────────────────────────────────

    public record ContractResponse(
        Long id,
        Long jobId,
        Long clientId,
        Long freelancerId,
        Long proposalId,
        Double amount,
        String status,
        String startedAt,
        String endedAt,
        String completionNote
    ) {
        public static ContractResponse from(Contract c) {
            return new ContractResponse(
                c.getId(), c.getJobId(), c.getClientId(), c.getFreelancerId(),
                c.getProposalId(), c.getAmount(), c.getStatus().name(),
                c.getStartedAt().toString(),
                c.getEndedAt() != null ? c.getEndedAt().toString() : null,
                c.getCompletionNote()
            );
        }
    }
}
