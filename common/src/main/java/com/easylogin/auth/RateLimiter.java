package com.easylogin.auth;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiter for login attempts.
 * Tracks by IP address and by UUID, with configurable window and lockout.
 */
public class RateLimiter {

    private final int maxAttempts;
    private final long windowMs;
    private final long lockoutMs;

    // key → AttemptRecord
    private final Map<String, AttemptRecord> attempts = new ConcurrentHashMap<>();

    public RateLimiter(int maxAttempts, int windowMinutes, int lockoutMinutes) {
        this.maxAttempts = maxAttempts;
        this.windowMs = windowMinutes * 60_000L;
        this.lockoutMs = lockoutMinutes * 60_000L;
    }

    /**
     * Check if a key (IP or UUID string) is currently locked out.
     *
     * @return remaining lockout time in milliseconds, or 0 if not locked
     */
    public long isLocked(String key) {
        AttemptRecord rec = attempts.get(key);
        if (rec == null)
            return 0;

        long now = System.currentTimeMillis();

        // If locked out, check if lockout has expired
        if (rec.lockedUntil > 0) {
            if (now < rec.lockedUntil) {
                return rec.lockedUntil - now;
            } else {
                // Lockout expired, reset
                attempts.remove(key);
                return 0;
            }
        }

        // Clean expired window
        if (now - rec.windowStart > windowMs) {
            attempts.remove(key);
        }

        return 0;
    }

    /**
     * Record a failed attempt. Returns remaining attempts, or -1 if now locked out.
     */
    public int recordFailure(String key) {
        long now = System.currentTimeMillis();
        AttemptRecord rec = attempts.compute(key, (k, existing) -> {
            if (existing == null || now - existing.windowStart > windowMs) {
                return new AttemptRecord(now, 1, 0);
            }
            existing.count++;
            return existing;
        });

        if (rec.count >= maxAttempts) {
            rec.lockedUntil = now + lockoutMs;
            return -1;
        }

        return maxAttempts - rec.count;
    }

    /**
     * Clear attempts for a key (e.g., on successful login).
     */
    public void clear(String key) {
        attempts.remove(key);
    }

    /**
     * Cleanup expired entries. Can be called periodically.
     */
    public void cleanup() {
        long now = System.currentTimeMillis();
        attempts.entrySet().removeIf(e -> {
            AttemptRecord rec = e.getValue();
            if (rec.lockedUntil > 0) {
                return now > rec.lockedUntil;
            }
            return now - rec.windowStart > windowMs;
        });
    }

    private static class AttemptRecord {
        long windowStart;
        int count;
        long lockedUntil;

        AttemptRecord(long windowStart, int count, long lockedUntil) {
            this.windowStart = windowStart;
            this.count = count;
            this.lockedUntil = lockedUntil;
        }
    }
}
