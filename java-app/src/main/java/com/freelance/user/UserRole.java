package com.freelance.user;

/**
 * User role - defines what a user can do in the system.
 *
 * CLIENT    = Posts jobs, reviews proposals, hires freelancers
 * FREELANCER = Browses jobs, submits proposals, completes contracts
 * ADMIN     = Has access to everything (optional, for management)
 */
public enum UserRole {
    CLIENT,
    FREELANCER,
    ADMIN
}
