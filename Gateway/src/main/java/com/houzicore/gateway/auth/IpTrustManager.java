package com.houzicore.gateway.auth;

/**
 * Thin facade over AuthDatabase for IP trust queries.
 */
public class IpTrustManager {

    private final AuthDatabase db;

    public IpTrustManager(AuthDatabase db) {
        this.db = db;
    }

    public boolean isTrusted(String name, String ip) {
        return db.isTrustedIp(name, ip);
    }

    public void trust(String name, String ip) {
        db.addTrustedIp(name, ip);
    }

    public void clearAll(String name) {
        db.clearTrustedIps(name);
    }
}
