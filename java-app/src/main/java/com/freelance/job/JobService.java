package com.freelance.job;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * JobService - business logic for job postings.
 *
 * Flow:
 *   1. Client posts a job (createJob)
 *   2. Job is OPEN, freelancers can apply
 *   3. Client hires someone → job becomes IN_PROGRESS
 *   4. Work is done → job becomes COMPLETED
 */
@Service
public class JobService {

    private final JobRepository jobRepository;

    public JobService(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    /**
     * Create a new job posting.
     *
     * @param clientId     ID of the client posting the job
     * @param title        Job title
     * @param description  Full job description
     * @param budget       Payment budget in dollars
     * @param category     Category (e.g., "Web Development")
     * @param requiredSkills Skills needed (e.g., "Java, React")
     * @param deadlineStr  Deadline as string (ISO format, optional)
     * @return The newly created job
     */
    public Job createJob(Long clientId, String title, String description,
                         Double budget, String category, String requiredSkills,
                         String deadlineStr) {

        LocalDateTime deadline = parseFlexibleDeadline(deadlineStr);
        Job job = new Job(clientId, title, description, budget, category, requiredSkills, deadline);
        return jobRepository.save(job);
    }

    public static LocalDateTime parseFlexibleDeadline(String str) {
        if (str == null || str.isBlank()) {
            return null;
        }
        String clean = str.trim();

        // 1. Try standard ISO-8601 LocalDateTime
        try {
            return LocalDateTime.parse(clean);
        } catch (Exception ignored) {}

        // 2. Try standard ISO-8601 LocalDate (e.g. 2027-02-28)
        try {
            return LocalDate.parse(clean).atTime(23, 59, 59);
        } catch (Exception ignored) {}

        // 3. Try common patterns: dd-MM-yyyy, d-M-yyyy, dd/MM/yyyy, d/M/yyyy, yyyy-MM-dd, yyyy/MM/dd
        String[] patterns = {
            "d-M-yyyy", "dd-MM-yyyy", "d/M/yyyy", "dd/MM/yyyy",
            "yyyy-M-d", "yyyy-MM-dd", "yyyy/M/d", "yyyy/MM/dd",
            "M-d-yyyy", "MM-dd-yyyy", "M/d/yyyy", "MM/dd/yyyy",
            "MMM d, yyyy", "MMMM d, yyyy"
        };
        for (String p : patterns) {
            try {
                DateTimeFormatter fmt = new DateTimeFormatterBuilder()
                        .parseCaseInsensitive()
                        .appendPattern(p)
                        .toFormatter(Locale.ENGLISH);
                return LocalDate.parse(clean, fmt).atTime(23, 59, 59);
            } catch (Exception ignored) {}
        }

        // 4. Try day-month-year lenient clamped parsing (e.g. 30-2-2027 clamped to Feb 28, 2027)
        try {
            String[] parts = clean.split("[-/.]");
            if (parts.length == 3) {
                int p1 = Integer.parseInt(parts[0].trim());
                int p2 = Integer.parseInt(parts[1].trim());
                int p3 = Integer.parseInt(parts[2].trim());
                int year = p3 >= 100 ? p3 : (p1 >= 100 ? p1 : 2000 + p3);
                int month = (p3 >= 100 ? p2 : (p1 >= 100 ? p2 : p2));
                int day = (p3 >= 100 ? p1 : (p1 >= 100 ? p3 : p1));
                month = Math.max(1, Math.min(12, month));
                YearMonth ym = YearMonth.of(year, month);
                day = Math.max(1, Math.min(ym.lengthOfMonth(), day));
                return LocalDate.of(year, month, day).atTime(23, 59, 59);
            }
        } catch (Exception ignored) {}

        // 5. Try relative natural strings like "2 weeks", "30 days", "1 month"
        String lower = clean.toLowerCase();
        try {
            String digits = lower.replaceAll("[^0-9]", "");
            if (!digits.isEmpty()) {
                int count = Integer.parseInt(digits);
                if (lower.contains("month")) return LocalDateTime.now().plusMonths(count);
                if (lower.contains("week")) return LocalDateTime.now().plusWeeks(count);
                if (lower.contains("day")) return LocalDateTime.now().plusDays(count);
                if (lower.contains("year")) return LocalDateTime.now().plusYears(count);
            }
        } catch (Exception ignored) {}

        // Fallback: 2 weeks from now so job creation never crashes with 500
        return LocalDateTime.now().plusWeeks(2);
    }

    /**
     * Get all open jobs (what freelancers see when browsing).
     */
    public List<Job> getAllOpenJobs() {
        return jobRepository.findByStatus(JobStatus.OPEN);
    }

    /**
     * Get all jobs posted by a specific client.
     */
    public List<Job> getJobsByClient(Long clientId) {
        return jobRepository.findByClientId(clientId);
    }

    /**
     * Get a single job by ID.
     */
    public Optional<Job> findById(Long id) {
        return jobRepository.findById(id);
    }

    /**
     * Browse open jobs by category.
     */
    public List<Job> getOpenJobsByCategory(String category) {
        return jobRepository.findByStatusAndCategory(JobStatus.OPEN, category);
    }

    /**
     * Get all jobs (admin view).
     */
    public List<Job> getAllJobs() {
        return jobRepository.findAll();
    }

    /**
     * Update job details (client can edit their posting).
     */
    public Job updateJob(Long jobId, Long clientId, String title, String description,
                         Double budget, String category, String requiredSkills) {

        Job job = jobRepository.findById(jobId)
            .orElseThrow(() -> new IllegalArgumentException("Job not found: " + jobId));

        // Only the job owner can edit it
        if (!job.getClientId().equals(clientId)) {
            throw new IllegalStateException("You can only edit your own jobs");
        }

        job.setTitle(title);
        job.setDescription(description);
        job.setBudget(budget);
        job.setCategory(category);
        job.setRequiredSkills(requiredSkills);
        return jobRepository.save(job);
    }

    /**
     * Delete a job (only owner can delete, only if OPEN).
     */
    public void deleteJob(Long jobId, Long clientId) {
        Job job = jobRepository.findById(jobId)
            .orElseThrow(() -> new IllegalArgumentException("Job not found: " + jobId));

        if (!job.getClientId().equals(clientId)) {
            throw new IllegalStateException("You can only delete your own jobs");
        }
        if (job.getStatus() != JobStatus.OPEN) {
            throw new IllegalStateException("Cannot delete a job that is already in progress");
        }

        jobRepository.delete(job);
    }

    /**
     * Mark job as in-progress (called when a proposal is accepted).
     * Called by ProposalService during the hiring flow.
     */
    public void markInProgress(Long jobId) {
        jobRepository.findById(jobId).ifPresent(job -> {
            job.markInProgress();
            jobRepository.save(job);
        });
    }

    /**
     * Mark job as completed.
     */
    public Job markCompleted(Long jobId, Long clientId) {
        Job job = jobRepository.findById(jobId)
            .orElseThrow(() -> new IllegalArgumentException("Job not found: " + jobId));

        if (!job.getClientId().equals(clientId)) {
            throw new IllegalStateException("Only the client can mark a job as completed");
        }

        job.markCompleted();
        return jobRepository.save(job);
    }
}
