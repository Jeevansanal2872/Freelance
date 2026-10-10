package com.freelance.user;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * UserRepository - Spring Data JPA automatically generates all SQL queries.
 * No need to write SQL manually - Spring handles it!
 *
 * JpaRepository gives us: save(), findById(), findAll(), delete(), etc.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find user by email (used during login).
     * Spring generates: SELECT * FROM users WHERE email = ?
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if an email is already registered.
     * Spring generates: SELECT COUNT(*) > 0 FROM users WHERE email = ?
     */
    boolean existsByEmail(String email);
}
