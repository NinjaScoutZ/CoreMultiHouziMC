package com.houzicore.gateway.auth;

import com.houzicore.gateway.GatewayPlugin;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * IP-based login rate limiting & brute-force protection.
 */
public class RateLimiter {

    private final GatewayPlugin plugin;
    private final Map<String, Integer> ipFailures = new ConcurrentHashMap<>();
    private final Map<String, Long> ipBans = new ConcurrentHashMap<>();

    public RateLimiter(GatewayPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Checks if the given IP address is currently banned due to brute force.
     */
    public boolean isBanned(String ip) {
        if (!plugin.getGateConfig().rateLimitEnabled()) {
            return false;
        }

        Long banEndTime = ipBans.get(ip);
        if (banEndTime == null) {
            return false;
        }

        if (System.currentTimeMillis() > banEndTime) {
            // Ban expired
            ipBans.remove(ip);
            ipFailures.remove(ip);
            return false;
        }

        return true;
    }

    /**
     * Returns the remaining ban duration in seconds for an IP address.
     */
    public long getRemainingBanTime(String ip) {
        Long banEndTime = ipBans.get(ip);
        if (banEndTime == null) {
            return 0;
        }
        long diff = banEndTime - System.currentTimeMillis();
        return Math.max(0, diff / 1000);
    }

    /**
     * Records a login failure from an IP. If threshold is exceeded, bans the IP.
     */
    public void recordFailure(String ip) {
        if (!plugin.getGateConfig().rateLimitEnabled()) return;

        int failures = ipFailures.merge(ip, 1, Integer::sum);
        int max = plugin.getGateConfig().rateLimitMaxFailures();

        if (failures >= max) {
            long banDurationMs = plugin.getGateConfig().rateLimitBanDurationMinutes() * 60_000L;
            ipBans.put(ip, System.currentTimeMillis() + banDurationMs);
            plugin.getLogger().warning("[RateLimit] IP " + ip + " has been banned for " + 
                    plugin.getGateConfig().rateLimitBanDurationMinutes() + " minutes due to " + failures + " failed login attempts.");
        }
    }

    /**
     * Clears failed attempts and bans for an IP (e.g. after a successful login).
     */
    public void clear(String ip) {
        ipFailures.remove(ip);
        ipBans.remove(ip);
    }
}
