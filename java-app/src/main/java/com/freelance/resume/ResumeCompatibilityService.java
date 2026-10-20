package com.freelance.resume;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.freelance.job.Job;
import com.freelance.job.JobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.regex.Pattern;

/**
 * ResumeCompatibilityService — Intelligent, job-specific resume compatibility analyzer.
 *
 * Evaluates candidate qualifications strictly against the target job requirements.
 * Integrated with Google Gemini API (gemini-1.5-flash) and an accurate, word-bounded
 * local heuristic matching engine.
 */
@Service
public class ResumeCompatibilityService {

    private static final Logger log = LoggerFactory.getLogger(ResumeCompatibilityService.class);

    private static final Set<String> EXPERIENCE_ACTION_TERMS = Set.of(
        "built", "delivered", "architected", "developed", "led", "engineered",
        "implemented", "designed", "deployed", "scaled", "optimized", "managed"
    );

    private static final List<String> KNOWN_TECH_KEYWORDS = List.of(
        "java", "spring boot", "spring", "microservices", "redis", "kafka", "docker",
        "kubernetes", "aws", "azure", "gcp", "postgresql", "mysql", "sql", "mongodb",
        "react", "next.js", "vue", "angular", "node.js", "node", "typescript", "javascript",
        "python", "django", "flask", "pytorch", "tensorflow", "rest", "api", "graphql",
        "figma", "modern hig", "ui/ux", "design systems", "typography", "prototyping",
        "blender", "spline", "three.js", "webgl", "3d design", "prompt engineering",
        "ai agents", "llms", "google gemini", "claude", "nlp", "computer vision", "ocr",
        "machine learning", "testing", "junit", "ci/cd", "git", "database tuning"
    );

    private final JobService jobService;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    @Value("${gemini.api.key:}")
    private String configuredPropertyKey;

    private volatile String dynamicApiKey = null;

    public ResumeCompatibilityService(JobService jobService, ObjectMapper objectMapper) {
        this.jobService = jobService;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    }

    public void setDynamicApiKey(String apiKey) {
        if (apiKey != null && !apiKey.trim().isEmpty()) {
            this.dynamicApiKey = apiKey.trim();
        }
    }

    public String resolveActiveApiKey(String explicitKey) {
        if (explicitKey != null && !explicitKey.trim().isEmpty()) {
            return explicitKey.trim();
        }
        if (dynamicApiKey != null && !dynamicApiKey.trim().isEmpty()) {
            return dynamicApiKey;
        }
        if (configuredPropertyKey != null && !configuredPropertyKey.trim().isEmpty()) {
            return configuredPropertyKey.trim();
        }
        String envKey = System.getenv("GEMINI_API_KEY");
        if (envKey != null && !envKey.trim().isEmpty()) {
            return envKey.trim();
        }
        String sysProp = System.getProperty("gemini.api.key");
        if (sysProp != null && !sysProp.trim().isEmpty()) {
            return sysProp.trim();
        }
        return null;
    }

    public boolean isGeminiConfigured() {
        return resolveActiveApiKey(null) != null;
    }

    /**
     * Analyze compatibility between a specific job and candidate resume text.
     */
    public CompatibilityResult analyze(Long jobId, String resumeText) {
        return analyze(jobId, resumeText, null);
    }

    /**
     * Analyze with optional caller-provided Gemini API key.
     */
    public CompatibilityResult analyze(Long jobId, String resumeText, String candidateApiKey) {
        if (resumeText == null || resumeText.isBlank()) {
            throw new IllegalArgumentException("Resume text is required");
        }

        Job job = jobService.findById(jobId)
            .orElseThrow(() -> new IllegalArgumentException("Job not found: " + jobId));

        String activeKey = resolveActiveApiKey(candidateApiKey);
        if (activeKey != null) {
            try {
                CompatibilityResult geminiResult = callGeminiApi(job, resumeText, activeKey);
                if (geminiResult != null) {
                    return geminiResult;
                }
            } catch (Exception ex) {
                log.warn("Gemini API call failed, falling back to heuristic engine: {}", ex.getMessage());
            }
        }

        // Job-specific heuristic analysis fallback
        return analyzeHeuristically(job, resumeText);
    }

    /**
     * Deep semantic analysis using Google Gemini API (gemini-1.5-flash).
     */
    private CompatibilityResult callGeminiApi(Job job, String resumeText, String apiKey) throws IOException, InterruptedException {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiKey;

        String systemPrompt = """
            You are an expert Technical Talent Matchmaker and Executive Hiring Analyst.
            Evaluate how strongly the candidate's resume/profile matches the requirements for THIS SPECIFIC JOB.

            STRICT EVALUATION CRITERIA:
            1. Evaluate the candidate SOLELY against the skills, technologies, and responsibilities for THIS SPECIFIC JOB.
            2. IF NO SKILLS OR EXPERIENCE MATCH THIS JOB:
               - The score MUST be between 0 and 10.
               - The label MUST be "Not Good for this Job".
               - 'matchedSkills' MUST be empty [].
               - In 'editedSummary', provide kind, honest human advice stating that their background does not currently align with this role and suggest finding better-fit jobs.
            3. Do NOT hallucinate or assume skills. Only place skills in 'matchedSkills' if they appear explicitly in the resume text AND match this job's needs.
            4. In 'editedSummary':
               - If the match is poor (score < 40), provide a warm and honest heads-up that this role needs different core skills.
               - If the match is strong, write a warm, professional, and natural 2-3 sentence proposal opening written in first person ("Hi there! I saw your post for... and would love to help. I have hands-on experience with [ONLY matched skills]...").
               - Write like a thoughtful human freelancer speaking directly to a client—no robotic corporate clichés, no empty buzzwords, and NEVER claim missing skills!

            You must return a valid JSON object matching this schema:
            {
              "score": <integer 0 to 100>,
              "label": "<Not Good for this Job | Poor Fit | Partial Match | Promising Match | Strong Match>",
              "matchedSkills": ["skill1"],
              "missingSkills": ["missingSkill1"],
              "findings": [
                {
                  "type": "<STRENGTH | GAP | EXPERIENCE | ADVICE>",
                  "message": "<Specific insight about this resume vs this job>",
                  "fix": "<Concrete action recommendation>"
                }
              ],
              "suggestions": [
                "<Actionable proposal tip 1>",
                "<Actionable proposal tip 2>"
              ],
              "editedSummary": "<Honest proposal opening tailored to this role>"
            }
            """;

        String userPrompt = String.format("""
            TARGET JOB TO MATCH AGAINST:
            Title: %s
            Category: %s
            Budget: $%.0f
            Required Skills: %s
            Description:
            %s

            CANDIDATE RESUME / PROFILE:
            %s
            """,
            job.getTitle(),
            job.getCategory(),
            job.getBudget() != null ? job.getBudget() : 0.0,
            job.getRequiredSkills() != null ? job.getRequiredSkills() : "",
            job.getDescription() != null ? job.getDescription() : "",
            resumeText
        );

        Map<String, Object> textPart = Map.of("text", systemPrompt + "\n\n" + userPrompt);
        Map<String, Object> parts = Map.of("parts", List.of(textPart));
        Map<String, Object> generationConfig = Map.of(
            "temperature", 0.1,
            "responseMimeType", "application/json"
        );
        Map<String, Object> requestPayload = Map.of(
            "contents", List.of(parts),
            "generationConfig", generationConfig
        );

        String jsonBody = objectMapper.writeValueAsString(requestPayload);

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Content-Type", "application/json")
            .timeout(Duration.ofSeconds(15))
            .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
            .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            log.warn("Gemini API returned HTTP {}: {}", response.statusCode(), response.body());
            return null;
        }

        JsonNode root = objectMapper.readTree(response.body());
        JsonNode candidates = root.path("candidates");
        if (!candidates.isArray() || candidates.isEmpty()) {
            return null;
        }

        String rawContent = candidates.get(0).path("content").path("parts").get(0).path("text").asText();
        if (rawContent == null || rawContent.isBlank()) {
            return null;
        }

        JsonNode parsed = objectMapper.readTree(rawContent);

        int score = Math.max(0, Math.min(100, parsed.path("score").asInt(0)));
        String label = parsed.path("label").asText(score >= 80 ? "Strong Match" : score >= 60 ? "Promising Match" : score >= 35 ? "Partial Match" : "Not Good for this Job");

        List<String> matchedSkills = new ArrayList<>();
        parsed.path("matchedSkills").forEach(s -> {
            String val = s.asText().trim();
            if (!val.isEmpty()) matchedSkills.add(val);
        });

        List<String> missingSkills = new ArrayList<>();
        parsed.path("missingSkills").forEach(s -> {
            String val = s.asText().trim();
            if (!val.isEmpty()) missingSkills.add(val);
        });

        List<Finding> findings = new ArrayList<>();
        parsed.path("findings").forEach(f -> {
            findings.add(new Finding(
                f.path("type").asText("NOTE"),
                f.path("message").asText(""),
                f.path("fix").asText("")
            ));
        });

        List<String> suggestions = new ArrayList<>();
        parsed.path("suggestions").forEach(s -> suggestions.add(s.asText()));

        String editedSummary = parsed.path("editedSummary").asText("");

        return new CompatibilityResult(
            score,
            label,
            matchedSkills,
            missingSkills,
            findings,
            suggestions,
            editedSummary,
            "Google Gemini AI (gemini-1.5-flash)",
            true
        );
    }

    /**
     * Strict job-wise heuristic compatibility engine.
     * Evaluates whether the candidate has the skills required specifically by this job.
     */
    public CompatibilityResult analyzeHeuristically(Job job, String resumeText) {
        Set<String> requiredSkills = extractJobRequiredSkills(job);

        List<String> matched = new ArrayList<>();
        List<String> missing = new ArrayList<>();

        for (String skill : requiredSkills) {
            if (containsWordOrPhrase(resumeText, skill)) {
                matched.add(skill);
            } else {
                missing.add(skill);
            }
        }

        // ── Case 1: ZERO skills matched for this job ───────────────────────────
        if (matched.isEmpty()) {
            List<Finding> findings = new ArrayList<>();
            findings.add(new Finding(
                "NOT_SUITABLE",
                "No required skills for this job were found in your resume.",
                "This role requires demonstrated expertise in: " + String.join(", ", requiredSkills) + "."
            ));
            findings.add(new Finding(
                "RECOMMENDATION",
                "Do not submit a proposal for this position with this resume.",
                "Apply to opportunities matching your actual domain, or revise your resume to include relevant projects."
            ));

            List<String> suggestions = List.of(
                "Search for jobs matching your actual expertise and skillset.",
                "If you possess these skills, explicitly add relevant projects, tech stacks, and metrics to your resume."
            );

            String summary = "Not recommended for this role: Your resume does not show evidence of the required skills ("
                + String.join(", ", requiredSkills) + ") for this " + job.getCategory() 
                + " role. We suggest applying for positions that align with your current toolkit, or updating your resume with relevant project work first.";

            return new CompatibilityResult(
                0,
                "Not Good for this Job (0% Match)",
                Collections.emptyList(),
                new ArrayList<>(requiredSkills),
                findings,
                suggestions,
                summary,
                "Smart Heuristic Engine",
                false
            );
        }

        // ── Case 2: At least some skills matched ────────────────────────────────
        int totalRequired = Math.max(1, requiredSkills.size());
        double matchRatio = (double) matched.size() / totalRequired;

        // Base score up to 75 points from required skills ratio
        int baseSkillScore = (int) Math.round(matchRatio * 75.0);

        // Experience language bonus (only if candidate has skills)
        int expScore = 0;
        for (String term : EXPERIENCE_ACTION_TERMS) {
            if (containsWordOrPhrase(resumeText, term)) {
                expScore += 3;
                if (expScore >= 15) break;
            }
        }

        // Completeness bonus (projects / links / education)
        int compScore = 0;
        if (containsWordOrPhrase(resumeText, "project") || containsWordOrPhrase(resumeText, "portfolio")) compScore += 5;
        if (containsWordOrPhrase(resumeText, "education") || containsWordOrPhrase(resumeText, "degree")) compScore += 5;

        int rawScore = baseSkillScore + expScore + compScore;

        // Cap score strictly if match ratio is low
        int finalScore;
        String label;
        if (matchRatio < 0.35) {
            finalScore = Math.min(35, rawScore);
            label = "Poor Fit (Missing Core Skills)";
        } else if (matchRatio < 0.60) {
            finalScore = Math.min(55, Math.max(36, rawScore));
            label = "Partial Match (Needs Alignment)";
        } else if (matchRatio < 0.85) {
            finalScore = Math.min(79, Math.max(60, rawScore));
            label = "Promising Match";
        } else {
            finalScore = Math.min(100, Math.max(80, rawScore));
            label = "Strong Match for this Job";
        }

        List<Finding> findings = new ArrayList<>();
        if (!missing.isEmpty()) {
            findings.add(new Finding(
                "MISSING_SKILLS",
                "Missing required keywords: " + String.join(", ", missing),
                "If you have experience with these tools, add truthful evidence and outcomes to your resume."
            ));
        }
        if (matchRatio >= 0.70) {
            findings.add(new Finding(
                "STRONG_ALIGNMENT",
                "Your profile demonstrates strong alignment with " + matched.size() + " required skills.",
                "Emphasize your achievements in " + String.join(", ", matched) + " in your pitch."
            ));
        }

        List<String> suggestions = buildSuggestions(job, matched, missing, finalScore);
        String editedSummary = buildJobSpecificSummary(job, matched, missing, finalScore);

        return new CompatibilityResult(
            finalScore,
            label,
            matched,
            missing,
            findings,
            suggestions,
            editedSummary,
            "Smart Heuristic Engine",
            false
        );
    }

    /**
     * Extracts skills specifically required for this job (from requiredSkills + title/description).
     */
    private Set<String> extractJobRequiredSkills(Job job) {
        Set<String> skills = new LinkedHashSet<>();

        // 1. Explicit required skills on job
        if (job.getRequiredSkills() != null && !job.getRequiredSkills().isBlank()) {
            for (String s : job.getRequiredSkills().split(",")) {
                String clean = s.trim().toLowerCase(Locale.ROOT);
                if (!clean.isEmpty()) {
                    skills.add(clean);
                }
            }
        }

        // 2. Technical keywords explicitly mentioned in job title and description
        String jobCorpus = (job.getTitle() + " " + job.getDescription()).toLowerCase(Locale.ROOT);
        for (String tech : KNOWN_TECH_KEYWORDS) {
            if (containsWordOrPhrase(jobCorpus, tech)) {
                skills.add(tech);
            }
        }

        return skills;
    }

    /**
     * Checks if a skill exists in the text using strict word boundaries to eliminate
     * false positive substring matches (e.g. "git" inside "digital", "rest" inside "interest").
     */
    public static boolean containsWordOrPhrase(String text, String term) {
        if (text == null || term == null || text.isBlank() || term.isBlank()) {
            return false;
        }
        String cleanTerm = term.trim().toLowerCase(Locale.ROOT);
        String escaped = Pattern.quote(cleanTerm);
        // Match term bounded by non-alphanumeric chars or start/end of string
        String regex = "(?i)(^|[^a-zA-Z0-9])" + escaped + "([^a-zA-Z0-9]|$)";
        return Pattern.compile(regex).matcher(text).find();
    }

    private List<String> buildSuggestions(Job job, List<String> matched, List<String> missing, int score) {
        List<String> suggestions = new ArrayList<>();
        if (score < 40) {
            suggestions.add("Focus on opportunities in categories where your skills match the core requirements.");
            if (!missing.isEmpty()) {
                suggestions.add("This client specifically requires: " + String.join(", ", missing) + ".");
            }
        } else {
            if (!matched.isEmpty()) {
                suggestions.add("Highlight measurable outcomes delivered using " + String.join(", ", matched) + ".");
            }
            if (!missing.isEmpty()) {
                suggestions.add("Briefly address how you can bridge " + String.join(", ", missing) + " in your proposal.");
            }
            suggestions.add("Lead with a relevant portfolio link or GitHub repository.");
        }
        return suggestions;
    }

    private String buildJobSpecificSummary(Job job, List<String> matched, List<String> missing, int score) {
        if (matched.isEmpty()) {
            return "Honest Take: Your resume doesn't currently list the core skills needed for "
                + job.getTitle() + ". We suggest focusing on roles that match your background or adding relevant project examples to your profile.";
        }
        if (score < 40) {
            return "Hi there! I saw your post for " + job.getTitle() + ". While my core background is centered on "
                + String.join(", ", matched) + ", I learn fast, communicate clearly, and would love to discuss how I can help bring this project to life.";
        }
        return "Hi there! I saw your post for " + job.getTitle() + " and would love to help. I have hands-on experience building with "
            + String.join(", ", matched) + ", and I take pride in delivering clean, reliable work with clear, daily communication. Looking forward to discussing details!";
    }

    public record CompatibilityResult(
        int score,
        String label,
        List<String> matchedSkills,
        List<String> missingSkills,
        List<Finding> findings,
        List<String> suggestions,
        String editedSummary,
        String engine,
        boolean isGeminiPowered
    ) {
        public CompatibilityResult(
            int score,
            String label,
            List<String> matchedSkills,
            List<String> missingSkills,
            List<Finding> findings,
            List<String> suggestions,
            String editedSummary
        ) {
            this(score, label, matchedSkills, missingSkills, findings, suggestions, editedSummary, "Smart Heuristic Engine", false);
        }
    }

    public record Finding(String type, String message, String fix) {}
}
