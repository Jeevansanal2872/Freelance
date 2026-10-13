package com.freelance.job;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * JobRepository - Spring Data JPA handles all SQL automatically.
 */
public interface JobRepository extends JpaRepository<Job, Long> {

    /**
     * Find all jobs posted by a specific client.
     * SQL: SELECT * FROM jobs WHERE client_id = ?
     */
    List<Job> findByClientId(Long clientId);

    /**
     * Find all jobs with a specific status.
     * SQL: SELECT * FROM jobs WHERE status = ?
     */
    List<Job> findByStatus(JobStatus status);

    /**
     * Find all jobs in a specific category.
     * SQL: SELECT * FROM jobs WHERE category = ?
     */
    List<Job> findByCategory(String category);

    /**
     * Find open jobs with a specific category (browse by category).
     */
    List<Job> findByStatusAndCategory(JobStatus status, String category);
}
