package com.freelance.job;

/**
 * Status of a job posting.
 *
 * OPEN        = Job is visible, freelancers can apply
 * IN_PROGRESS = A freelancer has been hired, work is ongoing
 * COMPLETED   = Work is done, client has confirmed
 * CLOSED      = Job was closed without hiring anyone
 */
public enum JobStatus {
    OPEN,
    IN_PROGRESS,
    COMPLETED,
    CLOSED
}
