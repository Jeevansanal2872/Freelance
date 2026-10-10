package com.freelance.user;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

/**
 * UserService - handles all business logic for user accounts.
 *
 * This class sits between the controller and the database.
 * Controller calls Service → Service calls Repository (database).
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Spring automatically injects these via constructor (Dependency Injection)
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Register a new user.
     * Steps: validate email not taken → hash password → save to DB
     *
     * @param name     User's full name
     * @param email    User's email (must be unique)
     * @param password Plain text password (we will hash it)
     * @param role     CLIENT or FREELANCER
     * @return The newly created user
     * @throws IllegalArgumentException if email is already registered
     */
    public User register(String name, String email, String password, UserRole role) {
        // Step 1: Check if email is already taken
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email is already registered: " + email);
        }

        // Step 2: Hash the password (NEVER store plain text)
        String hashedPassword = passwordEncoder.encode(password);

        // Step 3: Create and save the user
        User user = new User(name, email, hashedPassword, role);
        return userRepository.save(user);
    }

    /**
     * Login - check email + password.
     *
     * @param email    User's email
     * @param password Plain text password to verify
     * @return The authenticated user
     * @throws IllegalArgumentException if credentials are wrong
     */
    public User login(String email, String password) {
        // Find user by email
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        // Verify password against the stored hash
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        return user;
    }

    /**
     * Get user by ID.
     *
     * @param id User's database ID
     * @return The user, or empty if not found
     */
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Update user profile (bio, skills, hourly rate).
     *
     * @param id         User's ID
     * @param bio        New bio text
     * @param skills     New skills list
     * @param hourlyRate New hourly rate
     * @return Updated user
     */
    public User updateProfile(Long id, String bio, String skills, Double hourlyRate) {
        return updateProfile(id, new UserProfileUpdate(
            null, null, bio, null, null, skills, null, null, null, null,
            null, null, null, null, null, null, hourlyRate
        ));
    }

    public User updateProfile(Long id, UserProfileUpdate profile) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));

        if (profile.bio() != null) user.setBio(profile.bio());
        if (profile.skills() != null) user.setSkills(profile.skills());
        if (profile.hourlyRate() != null) user.setHourlyRate(profile.hourlyRate());
        if (profile.firstName() != null) user.setFirstName(profile.firstName());
        if (profile.lastName() != null) user.setLastName(profile.lastName());
        if (profile.portfolioUrl() != null) user.setPortfolioUrl(profile.portfolioUrl());
        if (profile.education() != null) user.setEducation(profile.education());
        if (profile.publishedProjects() != null) user.setPublishedProjects(profile.publishedProjects());
        if (profile.languages() != null) user.setLanguages(profile.languages());
        if (profile.hoursPerWeek() != null) user.setHoursPerWeek(profile.hoursPerWeek());
        if (profile.licenses() != null) user.setLicenses(profile.licenses());
        if (profile.linkedinUrl() != null) user.setLinkedinUrl(profile.linkedinUrl());
        if (profile.githubUrl() != null) user.setGithubUrl(profile.githubUrl());
        if (profile.instagramUrl() != null) user.setInstagramUrl(profile.instagramUrl());
        if (profile.behanceUrl() != null) user.setBehanceUrl(profile.behanceUrl());
        if (profile.verificationStatus() != null) user.setVerificationStatus(profile.verificationStatus());
        if (profile.experiences() != null) user.setExperiences(profile.experiences());

        return userRepository.save(user);
    }

    public int profileCompletion(User user) {
        int completed = 0;
        if (hasText(user.getFirstName()) && hasText(user.getLastName())) completed++;
        if (hasText(user.getBio())) completed++;
        if (hasText(user.getPortfolioUrl())) completed++;
        if (hasText(user.getEducation())) completed++;
        if (hasText(user.getSkills())) completed++;
        if (hasText(user.getPublishedProjects())) completed++;
        if (hasText(user.getLanguages())) completed++;
        if (user.getHoursPerWeek() != null && user.getHoursPerWeek() > 0) completed++;
        if (hasText(user.getLicenses())) completed++;
        if (hasText(user.getLinkedinUrl()) || hasText(user.getGithubUrl())
                || hasText(user.getInstagramUrl()) || hasText(user.getBehanceUrl())) completed++;
        if ("VERIFIED".equals(user.getVerificationStatus())) completed++;
        if (hasText(user.getExperiences())) completed++;
        return Math.round(completed * 100f / 12);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    public record UserProfileUpdate(
        String firstName, String lastName, String bio, String portfolioUrl,
        String education, String skills, String publishedProjects, String languages,
        Integer hoursPerWeek, String licenses, String linkedinUrl, String githubUrl,
        String instagramUrl, String behanceUrl, String verificationStatus,
        String experiences, Double hourlyRate
    ) {}

    /**
     * List all freelancers (for clients to browse talent).
     */
    public List<User> getAllFreelancers() {
        return userRepository.findAll().stream()
            .filter(u -> u.getRole() == UserRole.FREELANCER)
            .toList();
    }

    /**
     * List all clients (for admin view).
     */
    public List<User> getAllClients() {
        return userRepository.findAll().stream()
            .filter(u -> u.getRole() == UserRole.CLIENT)
            .toList();
    }
}
