package com.freelance.proposal;

/**
 * Status of a proposal submitted by a freelancer.
 *
 * PENDING   = Submitted, waiting for client to review
 * ACCEPTED  = Client hired this freelancer → Contract is created
 * REJECTED  = Client passed on this freelancer
 * WITHDRAWN = Freelancer cancelled their own proposal
 */
public enum ProposalStatus {
    PENDING,
    ACCEPTED,
    REJECTED,
    WITHDRAWN
}
