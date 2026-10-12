package com.freelance.user;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * UserController - handles all HTTP requests related to users.
 *
 * REST API Endpoints:
 *   POST /api/users/register  → Register a new account
 *   POST /api/users/login     → Login and get user info
 *   GET  /api/users/{id}      → Get user profile by ID
 *   PUT  /api/users/{id}      → Update user profile
 *   GET  /api/users/freelancers → List all freelancers
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // ─── POST /api/users/register ─────────────────────────────────────────────

    /**
     * Register a new user account.
     * Body example: { "name": "Alice", "email": "alice@test.com",
     *                 "password": "secret123", "role": "FREELANCER" }
     */
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest req) {
        User user = userService.register(req.name(), req.email(), req.password(), req.role());
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.from(user, userService.profileCompletion(user)));
    }

    // ─── POST /api/users/login ────────────────────────────────────────────────

    /**
     * Login with email and password.
     * Body example: { "email": "alice@test.com", "password": "secret123" }
     * Returns user info on success (in production, return a JWT token instead).
     */
    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(@Valid @RequestBody LoginRequest req) {
        User user = userService.login(req.email(), req.password());
        return ResponseEntity.ok(UserResponse.from(user, userService.profileCompletion(user)));
    }

    // ─── GET /api/users/{id} ──────────────────────────────────────────────────

    /**
     * Get public profile of a user by ID.
     * Example: GET /api/users/1
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
        return userService.findById(id)
            .map(user -> ResponseEntity.ok(UserResponse.from(user, userService.profileCompletion(user))))
            .orElse(ResponseEntity.notFound().build());
    }

    // ─── PUT /api/users/{id} ──────────────────────────────────────────────────

    /**
     * Update user profile (bio, skills, hourly rate).
     * Body example: { "bio": "Expert developer", "skills": "Java, React", "hourlyRate": 50.0 }
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateProfile(
            @PathVariable Long id,
            @RequestBody UpdateProfileRequest req) {
        User user = userService.updateProfile(id, req.toProfileUpdate());
        return ResponseEntity.ok(UserResponse.from(user, userService.profileCompletion(user)));
    }

    // ─── GET /api/users/freelancers ───────────────────────────────────────────

    /**
     * List all freelancers (for clients to browse talent).
     */
    @GetMapping("/freelancers")
    public ResponseEntity<List<UserResponse>> getFreelancers() {
        List<UserResponse> freelancers = userService.getAllFreelancers()
            .stream()
            .map(u -> UserResponse.from(u, userService.profileCompletion(u)))
            .toList();
        return ResponseEntity.ok(freelancers);
    }

    // ─── Request / Response record classes ────────────────────────────────────

    /** Data the client sends when registering */
    public record RegisterRequest(
        @NotBlank String name,
        @Email @NotBlank String email,
        @NotBlank String password,
        @NotNull UserRole role
    ) {}

    /** Data the client sends when logging in */
    public record LoginRequest(
        @Email @NotBlank String email,
        @NotBlank String password
    ) {}

    /** Data the client sends to update profile */
    public record UpdateProfileRequest(
        String firstName,
        String lastName,
        String bio,
        String portfolioUrl,
        String education,
        String skills,
        String publishedProjects,
        String languages,
        Integer hoursPerWeek,
        String licenses,
        String linkedinUrl,
        String githubUrl,
        String instagramUrl,
        String behanceUrl,
        String verificationStatus,
        String experiences,
        Double hourlyRate
    ) {
        public UserService.UserProfileUpdate toProfileUpdate() {
            return new UserService.UserProfileUpdate(
                firstName, lastName, bio, portfolioUrl, education, skills,
                publishedProjects, languages, hoursPerWeek, licenses, linkedinUrl,
                githubUrl, instagramUrl, behanceUrl, verificationStatus, experiences,
                hourlyRate
            );
        }
    }

    /**
     * What we send BACK to the client.
     * IMPORTANT: Never include passwordHash in responses!
     */
    public record UserResponse(
        Long id,
        String name,
        String email,
        UserRole role,
        String firstName,
        String lastName,
        String bio,
        String skills,
        Double hourlyRate,
        String portfolioUrl,
        String education,
        String publishedProjects,
        String languages,
        Integer hoursPerWeek,
        String licenses,
        String linkedinUrl,
        String githubUrl,
        String instagramUrl,
        String behanceUrl,
        String verificationStatus,
        String experiences,
        int profileCompletion
    ) {
        public static UserResponse from(User user, int profileCompletion) {
            return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getFirstName(),
                user.getLastName(),
                user.getBio(),
                user.getSkills(),
                user.getHourlyRate(),
                user.getPortfolioUrl(),
                user.getEducation(),
                user.getPublishedProjects(),
                user.getLanguages(),
                user.getHoursPerWeek(),
                user.getLicenses(),
                user.getLinkedinUrl(),
                user.getGithubUrl(),
                user.getInstagramUrl(),
                user.getBehanceUrl(),
                user.getVerificationStatus(),
                user.getExperiences(),
                profileCompletion
            );
        }

        public static UserResponse from(User user) {
            return from(user, 0);
        }
    }
}
