package com.freelance.job;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * JobController - REST API for job postings.
 *
 * Endpoints:
 *   POST   /api/jobs                    → Post a new job
 *   GET    /api/jobs                    → Browse all open jobs
 *   GET    /api/jobs/{id}               → View one job
 *   GET    /api/jobs/client/{clientId}  → All jobs by a client
 *   GET    /api/jobs/category/{cat}     → Browse by category
 *   PUT    /api/jobs/{id}               → Edit a job
 *   DELETE /api/jobs/{id}               → Delete a job
 *   PUT    /api/jobs/{id}/complete      → Mark job as completed
 */
@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    // POST /api/jobs - Client posts a new job
    @PostMapping
    public ResponseEntity<JobResponse> createJob(@Valid @RequestBody CreateJobRequest req) {
        Job job = jobService.createJob(
            req.clientId(), req.title(), req.description(),
            req.budget(), req.category(), req.requiredSkills(), req.deadline()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(JobResponse.from(job));
    }

    // GET /api/jobs - Browse all open jobs
    @GetMapping
    public ResponseEntity<List<JobResponse>> getOpenJobs() {
        List<JobResponse> jobs = jobService.getAllOpenJobs()
            .stream().map(JobResponse::from).toList();
        return ResponseEntity.ok(jobs);
    }

    // GET /api/jobs/{id} - View a specific job
    @GetMapping("/{id}")
    public ResponseEntity<JobResponse> getJob(@PathVariable Long id) {
        return jobService.findById(id)
            .map(job -> ResponseEntity.ok(JobResponse.from(job)))
            .orElse(ResponseEntity.notFound().build());
    }

    // GET /api/jobs/client/{clientId} - All jobs by a client
    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<JobResponse>> getJobsByClient(@PathVariable Long clientId) {
        List<JobResponse> jobs = jobService.getJobsByClient(clientId)
            .stream().map(JobResponse::from).toList();
        return ResponseEntity.ok(jobs);
    }

    // GET /api/jobs/category/{category} - Browse by category
    @GetMapping("/category/{category}")
    public ResponseEntity<List<JobResponse>> getJobsByCategory(@PathVariable String category) {
        List<JobResponse> jobs = jobService.getOpenJobsByCategory(category)
            .stream().map(JobResponse::from).toList();
        return ResponseEntity.ok(jobs);
    }

    // PUT /api/jobs/{id} - Edit a job
    @PutMapping("/{id}")
    public ResponseEntity<JobResponse> updateJob(
            @PathVariable Long id,
            @RequestBody UpdateJobRequest req) {
        Job job = jobService.updateJob(
            id, req.clientId(), req.title(), req.description(),
            req.budget(), req.category(), req.requiredSkills()
        );
        return ResponseEntity.ok(JobResponse.from(job));
    }

    // DELETE /api/jobs/{id}?clientId=X
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteJob(
            @PathVariable Long id,
            @RequestParam Long clientId) {
        jobService.deleteJob(id, clientId);
        return ResponseEntity.ok(Map.of("message", "Job deleted successfully"));
    }

    // PUT /api/jobs/{id}/complete?clientId=X - Mark completed
    @PutMapping("/{id}/complete")
    public ResponseEntity<JobResponse> completeJob(
            @PathVariable Long id,
            @RequestParam Long clientId) {
        Job job = jobService.markCompleted(id, clientId);
        return ResponseEntity.ok(JobResponse.from(job));
    }

    // ─── Request / Response classes ───────────────────────────────────────────

    public record CreateJobRequest(
        @NotNull Long clientId,
        @NotBlank String title,
        @NotBlank String description,
        Double budget,
        @NotBlank String category,
        String requiredSkills,
        String deadline
    ) {}

    public record UpdateJobRequest(
        @NotNull Long clientId,
        String title,
        String description,
        Double budget,
        String category,
        String requiredSkills
    ) {}

    public record JobResponse(
        Long id,
        Long clientId,
        String title,
        String description,
        Double budget,
        String category,
        String requiredSkills,
        String status,
        String createdAt
    ) {
        public static JobResponse from(Job job) {
            return new JobResponse(
                job.getId(),
                job.getClientId(),
                job.getTitle(),
                job.getDescription(),
                job.getBudget(),
                job.getCategory(),
                job.getRequiredSkills(),
                job.getStatus().name(),
                job.getCreatedAt().toString()
            );
        }
    }
}
