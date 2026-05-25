package com.houzicore.lobby.hub.modules.arena;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class ArenaMatch {

    public enum MatchState { IDLE, OCCUPIED, COUNTDOWN, FIGHTING }

    public Player playerA;
    public Player playerB;
    
    // King of the Hill fields
    public Player king;
    public int winStreak = 0;

    public final Location center;
    public final double radius;
    public Location spawnA;  // Player A spawn point (from MapBuilder)
    public Location spawnB;  // Player B spawn point (from MapBuilder)

    public MatchState state = MatchState.IDLE;
    public long stateChangedAt = System.currentTimeMillis();
    public int countdownTick = 5;

    // Sneak-to-exit fields
    public int sneakExitTicks = 0;
    public long lastSneakTime = 0;

    // Track placed blocks for cleanup
    public final List<Block> placedBlocks = new ArrayList<>();

    public ArenaMatch(Location center, double radius) {
        this.center = center;
        this.radius = radius;
        // Default fallback spawn points: offset ±5 from center
        this.spawnA = center.clone().add(5, 0, 0);
        this.spawnB = center.clone().add(-5, 0, 0);
    }

    public boolean hasPlayer(Player p) {
        return p.equals(playerA) || p.equals(playerB);
    }

    public Player getOpponent(Player p) {
        if (p.equals(playerA)) return playerB;
        if (p.equals(playerB)) return playerA;
        return null;
    }

    public boolean isFull() {
        return playerA != null && playerB != null;
    }

    public boolean isEmpty() {
        return playerA == null && playerB == null;
    }

    public boolean inRing(Location loc) {
        return loc.getWorld().equals(center.getWorld())
            && loc.distanceSquared(center) <= radius * radius;
    }

    public void setState(MatchState newState) {
        this.state = newState;
        this.stateChangedAt = System.currentTimeMillis();
    }
}
