package com.freelance.resume;

import com.freelance.user.User;
import com.freelance.user.UserService;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/resume")
public class ResumeCompatibilityController {

    private final ResumeCompatibilityService service;
    private final ResumeDocumentExtractor documentExtractor;
    private final UserService userService;

    public ResumeCompatibilityController(
            ResumeCompatibilityService service,
            ResumeDocumentExtractor documentExtractor,
            UserService userService) {
        this.service = service;
        this.documentExtractor = documentExtractor;
        this.userService = userService;
    }

    @PostMapping(value = "/compatibility", consumes = "multipart/form-data")
    public ResponseEntity<ResumeCompatibilityService.CompatibilityResult> analyzeMultipart(
            @RequestParam Long jobId,
            @RequestPart("resume") MultipartFile resume,
            @RequestParam(required = false) String geminiKey) {
        String text = documentExtractor.extract(resume);
        if (text == null || text.isBlank()) {
            text = "Uploaded document contains no readable text.";
        }
        return ResponseEntity.ok(service.analyze(jobId, text, geminiKey));
    }

    @PostMapping("/compatibility/text")
    public ResponseEntity<ResumeCompatibilityService.CompatibilityResult> analyzeText(
            @RequestBody CompatibilityRequest request) {
        String text = request.resumeText();
        if (text == null || text.isBlank()) {
            text = "No resume text provided.";
        }
        return ResponseEntity.ok(service.analyze(request.jobId(), text, request.geminiKey()));
    }

    @PostMapping("/compatibility/profile")
    public ResponseEntity<ResumeCompatibilityService.CompatibilityResult> analyzeProfile(
            @RequestBody ProfileCompatibilityRequest request) {
        if (request.userId() == null || request.userId() <= 0) {
            throw new IllegalArgumentException("Valid User ID is required. Please sign in first.");
        }
        if (request.jobId() == null || request.jobId() <= 0) {
            throw new IllegalArgumentException("Valid Job ID is required.");
        }

        User user = userService.findById(request.userId())
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + request.userId()));

        StringBuilder composite = new StringBuilder();
        composite.append("Candidate: ").append(user.getName() != null ? user.getName() : "Candidate").append("\n");
        if (user.getBio() != null && !user.getBio().isBlank()) composite.append("Bio: ").append(user.getBio()).append("\n");
        if (user.getSkills() != null && !user.getSkills().isBlank()) composite.append("Skills: ").append(user.getSkills()).append("\n");
        if (user.getEducation() != null && !user.getEducation().isBlank()) composite.append("Education: ").append(user.getEducation()).append("\n");
        if (user.getExperiences() != null && !user.getExperiences().isBlank()) composite.append("Experience: ").append(user.getExperiences()).append("\n");
        if (user.getLanguages() != null && !user.getLanguages().isBlank()) composite.append("Languages: ").append(user.getLanguages()).append("\n");
        if (user.getPortfolioUrl() != null && !user.getPortfolioUrl().isBlank()) composite.append("Portfolio: ").append(user.getPortfolioUrl()).append("\n");
        if (user.getGithubUrl() != null && !user.getGithubUrl().isBlank()) composite.append("GitHub: ").append(user.getGithubUrl()).append("\n");

        String text = composite.toString().trim();
        if (text.isEmpty()) {
            text = "Candidate profile contains no bio or skills.";
        }

        return ResponseEntity.ok(service.analyze(request.jobId(), text, request.geminiKey()));
    }

    @GetMapping("/gemini-status")
    public ResponseEntity<Map<String, Object>> getGeminiStatus() {
        boolean configured = service.isGeminiConfigured();
        return ResponseEntity.ok(Map.of(
            "configured", configured,
            "engine", configured ? "Google Gemini AI (gemini-1.5-flash)" : "Smart Heuristic Engine"
        ));
    }

    @PostMapping("/gemini-key")
    public ResponseEntity<Map<String, Object>> setGeminiKey(@RequestBody Map<String, String> body) {
        String key = body.get("apiKey");
        if (key != null && !key.trim().isEmpty()) {
            service.setDynamicApiKey(key.trim());
            return ResponseEntity.ok(Map.of("success", true, "message", "Gemini API key configured successfully."));
        }
        return ResponseEntity.badRequest().body(Map.of("success", false, "message", "API key cannot be empty."));
    }

    public record CompatibilityRequest(
        @NotNull Long jobId,
        String resumeText,
        String geminiKey
    ) {}

    public record ProfileCompatibilityRequest(
        @NotNull Long jobId,
        @NotNull Long userId,
        String geminiKey
    ) {}
}
