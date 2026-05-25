package com.houzicore.arcade.nautilus.game.arcade.game.games.primalgames.events.disasters;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.houzicore.arcade.nautilus.game.arcade.game.games.primalgames.PrimalGames;

import java.util.List;

/**
 * Abstract base class for all natural disasters in Primal Games.
 * 
 * Each disaster has a lifecycle:
 *   1. warn()    — 5s before activation, warning FX + chat notice
 *   2. start()   — begins the disaster effect
 *   3. tick()     — called every UpdateEvent.SEC while active
 *   4. end()      — cleanup when duration expires
 */
public abstract class Disaster
{
    protected final PrimalGames _game;
    protected final String _nameEn;
    protected final String _nameTh;
    protected final String _icon;
    protected final int _tier;

    protected boolean _active = false;
    protected boolean _warned = false;
    protected long _startTime = 0;
    protected long _duration;
    protected Location _center;

    public Disaster(PrimalGames game, String nameEn, String nameTh, String icon, int tier, long durationMs)
    {
        _game = game;
        _nameEn = nameEn;
        _nameTh = nameTh;
        _icon = icon;
        _tier = tier;
        _duration = durationMs;
    }

    // ── Lifecycle ──────────────────────────────────────

    /**
     * Called 5 seconds before the disaster starts.
     * Should play warning sound + bossbar flash + chat warning.
     */
    public abstract void warn(List<Player> players, Location center);

    /**
     * Called when the disaster begins.
     */
    public abstract void start(List<Player> players, Location center);

    /**
     * Called every second while the disaster is active.
     */
    public abstract void tick(List<Player> players);

    /**
     * Called when the disaster ends. Clean up entities, particles, etc.
     */
    public abstract void end(List<Player> players);

    // ── State ──────────────────────────────────────────

    public void activate(List<Player> players, Location center)
    {
        _active = true;
        _warned = false;
        _startTime = System.currentTimeMillis();
        _center = center;
        start(players, center);
    }

    public void deactivate(List<Player> players)
    {
        _active = false;
        end(players);
    }

    public boolean isActive()
    {
        return _active;
    }

    public boolean isExpired()
    {
        return _active && (System.currentTimeMillis() - _startTime > _duration);
    }

    public long getTimeRemaining()
    {
        if (!_active) return 0;
        return Math.max(0, _duration - (System.currentTimeMillis() - _startTime));
    }

    // ── Getters ────────────────────────────────────────

    public String getNameEn() { return _nameEn; }
    public String getNameTh() { return _nameTh; }
    public String getIcon() { return _icon; }
    public int getTier() { return _tier; }
    public long getDuration() { return _duration; }
    public Location getCenter() { return _center; }

    public String getDisplayName(Player player)
    {
        if (player != null && com.houzicore.shared.core.lang.LangManager.get() != null
                && com.houzicore.shared.core.lang.LangManager.get().isThai(player))
        {
            return _nameTh;
        }
        return _nameEn;
    }

    // ── Utility ────────────────────────────────────────

    protected World getWorld()
    {
        return _game.WorldData != null ? _game.WorldData.World : null;
    }
}
