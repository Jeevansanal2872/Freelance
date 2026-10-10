package com.freelance.user;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

/**
 * User entity - represents both clients (hirers) and freelancers (job seekers).
 * The "role" field tells us which type of user this is.
 *
 * Table: users
 */
@Entity
@Table(name = "users")
public class User {

    // Auto-generated primary key
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Full name of the user
    @NotBlank
    @Column(nullable = false)
    private String name;

    // Email - must be unique (used for login)
    @Email
    @NotBlank
    @Column(unique = true, nullable = false)
    private String email;

    // Hashed password (we never store plain text)
    @NotBlank
    @Column(nullable = false)
    private String passwordHash;

    // USER ROLE: "CLIENT" = posts jobs, "FREELANCER" = applies to jobs
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    // Short or long professional bio
    @Column(columnDefinition = "TEXT")
    private String bio;

    // Skills (e.g., "Java, Python, React")
    @Column(columnDefinition = "TEXT")
    private String skills;

    // Optional: hourly rate in dollars (for freelancers)
    private Double hourlyRate;

    @Column(length = 150)
    private String firstName;

    @Column(length = 150)
    private String lastName;

    @Column(columnDefinition = "TEXT")
    private String portfolioUrl;

    @Column(columnDefinition = "TEXT")
    private String education;

    @Column(columnDefinition = "TEXT")
    private String publishedProjects;

    @Column(columnDefinition = "TEXT")
    private String languages;

    private Integer hoursPerWeek;

    @Column(columnDefinition = "TEXT")
    private String licenses;

    @Column(columnDefinition = "TEXT")
    private String linkedinUrl;

    @Column(columnDefinition = "TEXT")
    private String githubUrl;

    @Column(columnDefinition = "TEXT")
    private String instagramUrl;

    @Column(columnDefinition = "TEXT")
    private String behanceUrl;

    @Column(nullable = false)
    private String verificationStatus = "NOT_STARTED";

    @Column(columnDefinition = "TEXT")
    private String experiences;

    // Account creation time
    @Column(nullable = false)
    private LocalDateTime createdAt;

    // JPA requires a no-arg constructor
    protected User() {}

    // Constructor used when registering a new user
    public User(String name, String email, String passwordHash, UserRole role) {
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.createdAt = LocalDateTime.now();
    }

    // ─── Getters ─────────────────────────────────────────────────────────────

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public UserRole getRole() { return role; }
    public String getBio() { return bio; }
    public String getSkills() { return skills; }
    public Double getHourlyRate() { return hourlyRate; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getPortfolioUrl() { return portfolioUrl; }
    public String getEducation() { return education; }
    public String getPublishedProjects() { return publishedProjects; }
    public String getLanguages() { return languages; }
    public Integer getHoursPerWeek() { return hoursPerWeek; }
    public String getLicenses() { return licenses; }
    public String getLinkedinUrl() { return linkedinUrl; }
    public String getGithubUrl() { return githubUrl; }
    public String getInstagramUrl() { return instagramUrl; }
    public String getBehanceUrl() { return behanceUrl; }
    public String getVerificationStatus() { return verificationStatus; }
    public String getExperiences() { return experiences; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // ─── Setters (only for fields that can be updated) ────────────────────────

    public void setBio(String bio) { this.bio = bio; }
    public void setSkills(String skills) { this.skills = skills; }
    public void setHourlyRate(Double hourlyRate) { this.hourlyRate = hourlyRate; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setPortfolioUrl(String portfolioUrl) { this.portfolioUrl = portfolioUrl; }
    public void setEducation(String education) { this.education = education; }
    public void setPublishedProjects(String publishedProjects) { this.publishedProjects = publishedProjects; }
    public void setLanguages(String languages) { this.languages = languages; }
    public void setHoursPerWeek(Integer hoursPerWeek) { this.hoursPerWeek = hoursPerWeek; }
    public void setLicenses(String licenses) { this.licenses = licenses; }
    public void setLinkedinUrl(String linkedinUrl) { this.linkedinUrl = linkedinUrl; }
    public void setGithubUrl(String githubUrl) { this.githubUrl = githubUrl; }
    public void setInstagramUrl(String instagramUrl) { this.instagramUrl = instagramUrl; }
    public void setBehanceUrl(String behanceUrl) { this.behanceUrl = behanceUrl; }
    public void setVerificationStatus(String verificationStatus) { this.verificationStatus = verificationStatus; }
    public void setExperiences(String experiences) { this.experiences = experiences; }
}
