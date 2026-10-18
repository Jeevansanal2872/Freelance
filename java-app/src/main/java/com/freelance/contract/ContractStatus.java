package com.freelance.contract;

/**
 * Status of a contract between client and freelancer.
 *
 * ACTIVE    = Work is ongoing
 * COMPLETED = Client confirmed work is done, payment released
 * CANCELLED = Contract was cancelled (no payment)
 */
public enum ContractStatus {
    ACTIVE,
    COMPLETED,
    CANCELLED
}
