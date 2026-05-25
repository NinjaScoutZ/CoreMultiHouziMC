package com.houzicore.gateway.auth;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Manages 2FA PINs — stored as BCrypt hashes in gate_2fa table.
 */
public class TwoFactorManager {

    private final AuthDatabase db;

    public TwoFactorManager(AuthDatabase db) {
        this.db = db;
    }

    /** Returns true if the player has a 2FA PIN set. */
    public boolean hasPin(String name) {
        return db.hasPin(name);
    }

    /** Verifies the supplied plain-text PIN against the stored hash. */
    public boolean verifyPin(String name, String rawPin) {
        String hash = db.getPinHash(name);
        if (hash == null) return false;
        return BCrypt.checkpw(rawPin, hash);
    }

    /** Hashes and stores a new PIN for the player. */
    public void setPin(String name, String rawPin) {
        String hash = BCrypt.hashpw(rawPin, BCrypt.gensalt(10));
        db.setPin(name, hash);
    }
}
