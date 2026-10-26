package com.esd.lab2.security;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * S5 — In-memory, thread-safe token denylist keyed by token ID (jti).
 */
@Component
public class TokenDenylist {

    private final Map<String, Instant> revokedTokens = new ConcurrentHashMap<>();

    /**
     * Revokes a token by storing its jti and natural expiration time.
     */
    public void revoke(String jti, Instant expiry) {
        if (jti != null && expiry != null) {
            revokedTokens.put(jti, expiry);
            cleanExpired();
        }
    }

    /**
     * Checks if a token ID (jti) has been revoked.
     */
    public boolean isRevoked(String jti) {
        if (jti == null) {
            return false;
        }
        Instant expiry = revokedTokens.get(jti);
        if (expiry == null) {
            return false;
        }
        if (Instant.now().isAfter(expiry)) {
            revokedTokens.remove(jti);
            return false;
        }
        return true;
    }

    /**
     * Removes tokens that have naturally expired past their lifetime.
     */
    public void cleanExpired() {
        Instant now = Instant.now();
        revokedTokens.entrySet().removeIf(entry -> now.isAfter(entry.getValue()));
    }
}
