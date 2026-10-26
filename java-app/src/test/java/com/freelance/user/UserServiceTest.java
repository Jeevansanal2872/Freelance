package com.freelance.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UserService Tests - 15 test cases covering all user operations.
 *
 * @SpringBootTest loads the full application context (real database).
 * @Transactional rolls back each test so database stays clean.
 */
@SpringBootTest
@Transactional
@DisplayName("User Service - 15 Test Cases")
class UserServiceTest {

    @Autowired
    private UserService userService;

    // ─── Test 1: Register a new CLIENT ────────────────────────────────────────

    @Test
    @DisplayName("T1: Register a new CLIENT user successfully")
    void testRegisterClient() {
        User user = userService.register("Alice Smith", "alice@test.com", "password123", UserRole.CLIENT);

        assertNotNull(user.getId(), "User should have an auto-generated ID");
        assertEquals("Alice Smith", user.getName());
        assertEquals("alice@test.com", user.getEmail());
        assertEquals(UserRole.CLIENT, user.getRole());
        assertNotNull(user.getCreatedAt(), "Creation timestamp should be set");
    }

    // ─── Test 2: Register a new FREELANCER ────────────────────────────────────

    @Test
    @DisplayName("T2: Register a new FREELANCER user successfully")
    void testRegisterFreelancer() {
        User user = userService.register("Bob Dev", "bob@test.com", "pass456", UserRole.FREELANCER);

        assertEquals(UserRole.FREELANCER, user.getRole());
        assertEquals("Bob Dev", user.getName());
    }

    // ─── Test 3: Password is hashed (not stored as plain text) ───────────────

    @Test
    @DisplayName("T3: Password is stored as a hash, never as plain text")
    void testPasswordIsHashed() {
        User user = userService.register("Carol", "carol@test.com", "myPassword", UserRole.CLIENT);

        // The stored hash should NOT be the plain text password
        assertNotEquals("myPassword", user.getPasswordHash());
        // The hash should start with $2a$ (BCrypt format)
        assertTrue(user.getPasswordHash().startsWith("$2a$"),
            "Password should be BCrypt hashed");
    }

    // ─── Test 4: Duplicate email rejected ─────────────────────────────────────

    @Test
    @DisplayName("T4: Registering with a duplicate email throws an error")
    void testDuplicateEmailRejected() {
        userService.register("First User", "duplicate@test.com", "pass1", UserRole.CLIENT);

        // Second registration with same email should fail
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> userService.register("Second User", "duplicate@test.com", "pass2", UserRole.FREELANCER)
        );
        assertTrue(ex.getMessage().contains("already registered"));
    }

    // ─── Test 5: Login with correct credentials ────────────────────────────────

    @Test
    @DisplayName("T5: Login succeeds with correct email and password")
    void testLoginSuccess() {
        userService.register("Dave", "dave@test.com", "correctPass", UserRole.CLIENT);
        User loggedIn = userService.login("dave@test.com", "correctPass");

        assertNotNull(loggedIn);
        assertEquals("dave@test.com", loggedIn.getEmail());
    }

    // ─── Test 6: Login fails with wrong password ──────────────────────────────

    @Test
    @DisplayName("T6: Login fails with wrong password")
    void testLoginWrongPassword() {
        userService.register("Eve", "eve@test.com", "realPassword", UserRole.FREELANCER);

        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> userService.login("eve@test.com", "wrongPassword")
        );
        assertTrue(ex.getMessage().contains("Invalid email or password"));
    }

    // ─── Test 7: Login fails with unknown email ────────────────────────────────

    @Test
    @DisplayName("T7: Login fails with an email that is not registered")
    void testLoginUnknownEmail() {
        assertThrows(
            IllegalArgumentException.class,
            () -> userService.login("nobody@test.com", "anyPassword")
        );
    }

    // ─── Test 8: Find user by ID ───────────────────────────────────────────────

    @Test
    @DisplayName("T8: Find user by ID returns the correct user")
    void testFindById() {
        User registered = userService.register("Frank", "frank@test.com", "pass", UserRole.CLIENT);
        var found = userService.findById(registered.getId());

        assertTrue(found.isPresent());
        assertEquals("Frank", found.get().getName());
    }

    // ─── Test 9: Find unknown ID returns empty ─────────────────────────────────

    @Test
    @DisplayName("T9: Finding a non-existent user ID returns empty Optional")
    void testFindByIdNotFound() {
        var found = userService.findById(99999L);
        assertTrue(found.isEmpty());
    }

    // ─── Test 10: Update user profile ─────────────────────────────────────────

    @Test
    @DisplayName("T10: Update user profile (bio, skills, hourly rate)")
    void testUpdateProfile() {
        User user = userService.register("Grace", "grace@test.com", "pass", UserRole.FREELANCER);
        User updated = userService.updateProfile(
            user.getId(), "Senior Java Developer", "Java, Spring Boot, React", 75.0
        );

        assertEquals("Senior Java Developer", updated.getBio());
        assertEquals("Java, Spring Boot, React", updated.getSkills());
        assertEquals(75.0, updated.getHourlyRate());
    }

    // ─── Test 11: Update non-existent user throws ─────────────────────────────

    @Test
    @DisplayName("T11: Updating a non-existent user throws IllegalArgumentException")
    void testUpdateNonExistentUser() {
        assertThrows(
            IllegalArgumentException.class,
            () -> userService.updateProfile(99999L, "bio", "skills", 50.0)
        );
    }

    // ─── Test 12: List all freelancers ────────────────────────────────────────

    @Test
    @DisplayName("T12: getAllFreelancers returns only FREELANCER role users")
    void testGetAllFreelancers() {
        userService.register("Freelancer1", "f1@test.com", "p", UserRole.FREELANCER);
        userService.register("Freelancer2", "f2@test.com", "p", UserRole.FREELANCER);
        userService.register("Client1", "c1@test.com", "p", UserRole.CLIENT);

        var freelancers = userService.getAllFreelancers();

        assertTrue(freelancers.size() >= 2);
        freelancers.forEach(u ->
            assertEquals(UserRole.FREELANCER, u.getRole(), "All returned users must be FREELANCER")
        );
    }

    // ─── Test 13: List all clients ────────────────────────────────────────────

    @Test
    @DisplayName("T13: getAllClients returns only CLIENT role users")
    void testGetAllClients() {
        userService.register("Client10", "cl10@test.com", "p", UserRole.CLIENT);
        userService.register("Freelancer10", "fl10@test.com", "p", UserRole.FREELANCER);

        var clients = userService.getAllClients();

        assertTrue(clients.size() >= 1);
        clients.forEach(u ->
            assertEquals(UserRole.CLIENT, u.getRole(), "All returned users must be CLIENT")
        );
    }

    // ─── Test 14: Hourly rate is null by default ───────────────────────────────

    @Test
    @DisplayName("T14: Newly registered user has no hourly rate by default")
    void testDefaultHourlyRateIsNull() {
        User user = userService.register("Henry", "henry@test.com", "pass", UserRole.FREELANCER);
        assertNull(user.getHourlyRate(), "Hourly rate should be null until set");
    }

    // ─── Test 15: User creation time is recent ────────────────────────────────

    @Test
    @DisplayName("T15: User creation timestamp is set and is recent")
    void testCreationTimestampIsRecent() {
        User user = userService.register("Ivy", "ivy@test.com", "pass", UserRole.CLIENT);
        assertNotNull(user.getCreatedAt());

        // Created within last minute
        assertTrue(
            user.getCreatedAt().isAfter(java.time.LocalDateTime.now().minusMinutes(1)),
            "User should have been created within the last minute"
        );
    }
}
