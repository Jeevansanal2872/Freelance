package com.freelance.job;

import com.freelance.user.User;
import com.freelance.user.UserRole;
import com.freelance.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JobService Tests - 15 test cases covering all job operations.
 */
@SpringBootTest
@Transactional
@DisplayName("Job Service - 15 Test Cases")
class JobServiceTest {

    @Autowired private JobService jobService;
    @Autowired private UserService userService;

    private Long clientId;

    @BeforeEach
    void setUp() {
        // Create a client user for all tests
        User client = userService.register("TestClient", "client-job@test.com", "pass", UserRole.CLIENT);
        clientId = client.getId();
    }

    @Test
    @DisplayName("T1: Create a job posting successfully")
    void testCreateJob() {
        Job job = jobService.createJob(clientId, "Build Website", "Need a React website",
            500.0, "Web Development", "React, CSS", null);
        assertNotNull(job.getId());
        assertEquals("Build Website", job.getTitle());
        assertEquals(JobStatus.OPEN, job.getStatus());
    }

    @Test
    @DisplayName("T2: New job has OPEN status by default")
    void testNewJobIsOpen() {
        Job job = jobService.createJob(clientId, "Logo Design", "Need a logo", 100.0, "Design", "Photoshop", null);
        assertEquals(JobStatus.OPEN, job.getStatus());
    }

    @Test
    @DisplayName("T3: Get all open jobs returns only OPEN status jobs")
    void testGetAllOpenJobs() {
        jobService.createJob(clientId, "Job A", "Desc A", 100.0, "Dev", "Java", null);
        jobService.createJob(clientId, "Job B", "Desc B", 200.0, "Dev", "Python", null);
        List<Job> openJobs = jobService.getAllOpenJobs();
        assertTrue(openJobs.size() >= 2);
        openJobs.forEach(j -> assertEquals(JobStatus.OPEN, j.getStatus()));
    }

    @Test
    @DisplayName("T4: Get jobs by client returns only that client's jobs")
    void testGetJobsByClient() {
        jobService.createJob(clientId, "My Job", "My desc", 100.0, "Dev", "Java", null);
        List<Job> myJobs = jobService.getJobsByClient(clientId);
        assertTrue(myJobs.size() >= 1);
        myJobs.forEach(j -> assertEquals(clientId, j.getClientId()));
    }

    @Test
    @DisplayName("T5: Find job by ID returns correct job")
    void testFindById() {
        Job created = jobService.createJob(clientId, "Find Me", "Description", 300.0, "Dev", "Go", null);
        var found = jobService.findById(created.getId());
        assertTrue(found.isPresent());
        assertEquals("Find Me", found.get().getTitle());
    }

    @Test
    @DisplayName("T6: Find job by invalid ID returns empty")
    void testFindByIdNotFound() {
        assertTrue(jobService.findById(99999L).isEmpty());
    }

    @Test
    @DisplayName("T7: Browse open jobs by category")
    void testGetOpenJobsByCategory() {
        jobService.createJob(clientId, "Web Job", "Desc", 100.0, "Web", "React", null);
        jobService.createJob(clientId, "Mobile Job", "Desc", 200.0, "Mobile", "Flutter", null);
        List<Job> webJobs = jobService.getOpenJobsByCategory("Web");
        assertTrue(webJobs.size() >= 1);
        webJobs.forEach(j -> assertEquals("Web", j.getCategory()));
    }

    @Test
    @DisplayName("T8: Update job title and description")
    void testUpdateJob() {
        Job job = jobService.createJob(clientId, "Old Title", "Old desc", 100.0, "Dev", "Java", null);
        Job updated = jobService.updateJob(job.getId(), clientId, "New Title", "New desc", 200.0, "Dev", "Python");
        assertEquals("New Title", updated.getTitle());
        assertEquals("New desc", updated.getDescription());
        assertEquals(200.0, updated.getBudget());
    }

    @Test
    @DisplayName("T9: Non-owner cannot update a job")
    void testUpdateJobByNonOwner() {
        Job job = jobService.createJob(clientId, "My Job", "Desc", 100.0, "Dev", "Java", null);
        assertThrows(IllegalStateException.class,
            () -> jobService.updateJob(job.getId(), 99999L, "Hacked", "Hacked", 0.0, "Hacked", "Hacked"));
    }

    @Test
    @DisplayName("T10: Delete an open job successfully")
    void testDeleteJob() {
        Job job = jobService.createJob(clientId, "Delete Me", "Desc", 50.0, "Dev", "Java", null);
        Long jobId = job.getId();
        jobService.deleteJob(jobId, clientId);
        assertTrue(jobService.findById(jobId).isEmpty());
    }

    @Test
    @DisplayName("T11: Non-owner cannot delete a job")
    void testDeleteJobByNonOwner() {
        Job job = jobService.createJob(clientId, "Protected", "Desc", 50.0, "Dev", "Java", null);
        assertThrows(IllegalStateException.class, () -> jobService.deleteJob(job.getId(), 99999L));
    }

    @Test
    @DisplayName("T12: Mark job as completed")
    void testMarkCompleted() {
        Job job = jobService.createJob(clientId, "Complete Me", "Desc", 300.0, "Dev", "Java", null);
        // First mark in progress (simulating hire)
        jobService.markInProgress(job.getId());
        Job completed = jobService.markCompleted(job.getId(), clientId);
        assertEquals(JobStatus.COMPLETED, completed.getStatus());
    }

    @Test
    @DisplayName("T13: Non-owner cannot mark a job completed")
    void testMarkCompletedByNonOwner() {
        Job job = jobService.createJob(clientId, "Not Yours", "Desc", 300.0, "Dev", "Java", null);
        assertThrows(IllegalStateException.class, () -> jobService.markCompleted(job.getId(), 99999L));
    }

    @Test
    @DisplayName("T14: Job budget is stored correctly")
    void testJobBudget() {
        Job job = jobService.createJob(clientId, "Budget Test", "Desc", 1500.50, "Finance", "Excel", null);
        assertEquals(1500.50, job.getBudget());
    }

    @Test
    @DisplayName("T15: Job creation timestamp is recent")
    void testJobTimestamp() {
        Job job = jobService.createJob(clientId, "Time Test", "Desc", 100.0, "Dev", "Java", null);
        assertNotNull(job.getCreatedAt());
        assertTrue(job.getCreatedAt().isAfter(java.time.LocalDateTime.now().minusMinutes(1)));
    }

    @Test
    @DisplayName("T16: Flexible deadline strings parse without error")
    void testFlexibleDeadlineParsing() {
        Job job1 = jobService.createJob(clientId, "Job 1", "Desc", 300.0, "AI & Data", "Skills", "30-2-2027");
        assertNotNull(job1.getDeadline());

        Job job2 = jobService.createJob(clientId, "Job 2", "Desc", 500.0, "AI & Data", "Skills", "4 Weeks");
        assertNotNull(job2.getDeadline());

        Job job3 = jobService.createJob(clientId, "Job 3", "Desc", 600.0, "AI & Data", "Skills", "2027-12-31");
        assertNotNull(job3.getDeadline());
    }
}
