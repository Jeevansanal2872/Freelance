package com.freelance.resume;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.freelance.job.Job;
import com.freelance.job.JobService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Resume Compatibility Service - Test Suite")
class ResumeCompatibilityServiceTest {

    @Mock
    private JobService jobService;

    private ResumeCompatibilityService service;
    private Job testJob;

    @BeforeEach
    void setUp() {
        service = new ResumeCompatibilityService(jobService, new ObjectMapper());
        testJob = new Job(
            1L,
            "Senior Distributed Java Architect",
            "Designing resilient microservices using Java 21, Spring Boot, Redis, and Kafka.",
            8500.0,
            "Engineering",
            "Java, Spring Boot, Redis, Kafka, Docker",
            LocalDateTime.now().plusWeeks(4)
        );
    }

    @Test
    @DisplayName("Throws IllegalArgumentException if resume text is null or blank")
    void testBlankResumeThrows() {
        assertThrows(IllegalArgumentException.class, () -> service.analyze(1L, null));
        assertThrows(IllegalArgumentException.class, () -> service.analyze(1L, "   "));
    }

    @Test
    @DisplayName("Throws IllegalArgumentException if target job is not found")
    void testJobNotFoundThrows() {
        when(jobService.findById(999L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.analyze(999L, "Java engineer with 5 years experience"));
    }

    @Test
    @DisplayName("Strict job-wise check: Zero matching skills returns 0% score and 'Not Good for this Job' label")
    void testZeroMatchingSkillsReturnsNotGoodForJob() {
        when(jobService.findById(1L)).thenReturn(Optional.of(testJob));

        String unrelatedResume = """
            Experienced Chef and Culinary Manager with 10 years in Michelin-starred restaurants.
            Expert in French cuisine, kitchen staffing, menu design, and catering operations.
            Education: Culinary Arts Institute.
            """;

        ResumeCompatibilityService.CompatibilityResult result = service.analyze(1L, unrelatedResume);

        assertNotNull(result);
        assertEquals(0, result.score(), "Score should be 0% when no skills match the job");
        assertTrue(result.label().toLowerCase().contains("not good"), "Label must indicate not good for this job: " + result.label());
        assertTrue(result.matchedSkills().isEmpty(), "Matched skills must be empty");
        assertTrue(result.editedSummary().toLowerCase().contains("not recommended"), "Pitch should advise against applying");
    }

    @Test
    @DisplayName("Eliminates false-positive substring matches (e.g. 'digital' must not match 'git', 'interest' must not match 'rest')")
    void testNoFalsePositiveSubstringMatches() {
        when(jobService.findById(1L)).thenReturn(Optional.of(testJob));

        String deceptiveResume = """
            Passionate about digital marketing, interest rates, capital investments, and springboards.
            Experienced in managing international teams.
            """;

        ResumeCompatibilityService.CompatibilityResult result = service.analyze(1L, deceptiveResume);

        assertNotNull(result);
        assertFalse(result.matchedSkills().contains("git"), "'digital' must not falsely match 'git'");
        assertFalse(result.matchedSkills().contains("rest"), "'interest' must not falsely match 'rest'");
        assertEquals(0, result.score());
    }

    @Test
    @DisplayName("Heuristic analyzer calculates high match score for aligned resume")
    void testHeuristicAnalysisHighMatch() {
        when(jobService.findById(1L)).thenReturn(Optional.of(testJob));

        String resume = """
            Senior Software Engineer with 8 years of experience.
            Built and deployed high-throughput microservices using Java, Spring Boot, Redis, and Kafka.
            Led distributed systems projects on AWS with Docker.
            Education: B.S. in Computer Science.
            Portfolio: https://github.com/developer-sample
            """;

        ResumeCompatibilityService.CompatibilityResult result = service.analyze(1L, resume);

        assertNotNull(result);
        assertTrue(result.score() >= 75, "Expected high score but got: " + result.score());
        assertTrue(result.matchedSkills().contains("java"));
        assertTrue(result.matchedSkills().contains("spring boot"));
        assertNotNull(result.editedSummary());
        assertNotNull(result.engine());
    }

    @Test
    @DisplayName("Correctly identifies missing required skills")
    void testMissingSkillsDetection() {
        when(jobService.findById(1L)).thenReturn(Optional.of(testJob));

        String resume = """
            Junior Java developer with 1 year experience writing unit tests.
            Familiar with Git and basic Docker.
            Education: B.S. Software Engineering.
            """;

        ResumeCompatibilityService.CompatibilityResult result = service.analyze(1L, resume);

        assertNotNull(result);
        assertTrue(result.matchedSkills().contains("java"));
        assertTrue(result.missingSkills().contains("kafka"));
        assertTrue(result.missingSkills().contains("redis"));
        assertTrue(result.score() < 60);
    }

    @Test
    @DisplayName("Dynamic Gemini API key configuration and status")
    void testGeminiKeyConfiguration() {
        assertFalse(service.isGeminiConfigured());
        service.setDynamicApiKey("AIzaSyDummyKeyForTestingOnly12345");
        assertTrue(service.isGeminiConfigured());
        assertEquals("AIzaSyDummyKeyForTestingOnly12345", service.resolveActiveApiKey(null));
    }

    @Test
    @DisplayName("Gracefully falls back to heuristic engine when API call fails or key is invalid")
    void testFallbackToHeuristic() {
        when(jobService.findById(1L)).thenReturn(Optional.of(testJob));

        ResumeCompatibilityService.CompatibilityResult result = service.analyze(1L, "Java and Docker developer with 4 years experience.", "invalid_key");

        assertNotNull(result);
        assertTrue(result.score() > 0);
        assertNotNull(result.label());
        assertFalse(result.isGeminiPowered());
    }
}
