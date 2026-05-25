package com.houzicore.lobby.hub.modules.farm;

import org.bukkit.entity.Player;

public class FarmSimSession {

    public final Player player;
    public int score;
    public long startTime;
    public long lastBlockBreakTime;
    public boolean active;
    public boolean firstPlayToday;

    public FarmSimSession(Player player, boolean firstPlayToday) {
        this.player           = player;
        this.score            = 0;
        this.startTime        = System.currentTimeMillis();
        this.lastBlockBreakTime = System.currentTimeMillis();
        this.active           = true;
        this.firstPlayToday   = firstPlayToday;
    }

    public long elapsedSeconds() {
        return (System.currentTimeMillis() - startTime) / 1000L;
    }

    public long remainingSeconds(long durationSeconds) {
        return Math.max(0, durationSeconds - elapsedSeconds());
    }

    public boolean isExpired(long durationSeconds) {
        return elapsedSeconds() >= durationSeconds;
    }
}
