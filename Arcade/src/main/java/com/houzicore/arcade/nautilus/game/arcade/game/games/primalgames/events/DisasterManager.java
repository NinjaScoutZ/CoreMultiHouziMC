package com.houzicore.arcade.nautilus.game.arcade.game.games.primalgames.events;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.houzicore.arcade.nautilus.game.arcade.events.GameStateChangeEvent;
import com.houzicore.arcade.nautilus.game.arcade.game.Game.GameState;
import com.houzicore.arcade.nautilus.game.arcade.game.games.primalgames.PrimalGames;
import com.houzicore.arcade.nautilus.game.arcade.game.games.primalgames.events.disasters.*;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.UtilMath;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;

/**
 * Manages the natural disaster system for Primal Games.
 *
 * Disasters escalate across 3 tiers:
 *   Phase 1 (0-8 min):  T1 only, every 90-150s
 *   Phase 2 (8-14 min): T1+T2, every 60-90s
 *   Phase 3 (14+ min):  All tiers, every 30-60s
 *
 * Each disaster is preceded by a 5-second warning.
 */
public class DisasterManager implements Listener
{
    private final PrimalGames _game;
    private final Random _rand = new Random();

    // All available disaster templates (one of each type)
    private final ArrayList<Disaster> _tier1 = new ArrayList<>();
    private final ArrayList<Disaster> _tier2 = new ArrayList<>();
    private final ArrayList<Disaster> _tier3 = new ArrayList<>();

    // Current state
    private Disaster _current = null;
    private Disaster _pending = null; // Warning phase
    private Location _pendingCenter = null;
    private long _pendingStartAt = 0;
    private long _nextDisasterAt = 0;
    private boolean _enabled = false;

    public DisasterManager(PrimalGames game)
    {
        _game = game;

        // Tier 1: Mild
        _tier1.add(new AcidRainDisaster(game));
        _tier1.add(new DenseFogDisaster(game));
        _tier1.add(new ToxicGasDisaster(game)); // Toxic Gas is a random disaster, not a border mechanic

        // Tier 2: Moderate
        _tier2.add(new MeteorShowerDisaster(game));
        _tier2.add(new SinkholeDisaster(game));

        // Tier 3: Severe
        _tier3.add(new TornadoDisaster(game));
        _tier3.add(new LightningStormDisaster(game));
    }

    // ── Public API ──────────────────────────────────

    public Disaster getCurrentDisaster() { return _current; }
    public Disaster getPendingDisaster() { return _pending; }
    public Disaster getActiveDisaster() { return (_current != null && _current.isActive()) ? _current : null; }
    public boolean isDisasterActive() { return _current != null && _current.isActive(); }

    public String getDisasterStatusLine(Player player)
    {
        if (_current != null && _current.isActive())
        {
            return C.cRed + C.Bold + _current.getIcon() + " " + _current.getDisplayName(player);
        }
        if (_pending != null)
        {
            return C.cYellow + "⚠ " + _pending.getDisplayName(player) + "...";
        }
        return null;
    }

    // ── Events ──────────────────────────────────────

    @EventHandler
    public void onStateChange(GameStateChangeEvent event)
    {
        if (event.GetGame() != _game) return;

        if (event.GetState() == GameState.Live)
        {
            _enabled = true;
            // First disaster after 4 minutes
            _nextDisasterAt = System.currentTimeMillis() + 240000;
            _current = null;
            _pending = null;
        }
        else if (event.GetState() == GameState.Dead || event.GetState() == GameState.End)
        {
            _enabled = false;
            cleanup();
        }
    }

    @EventHandler
    public void onUpdate(UpdateEvent event)
    {
        if (!_enabled || !_game.IsLive()) return;
        if (event.getType() != UpdateType.SEC) return;

        long now = System.currentTimeMillis();
        List<Player> players = _game.GetPlayers(true);

        // Handle active disaster tick
        if (_current != null && _current.isActive())
        {
            if (_current.isExpired())
            {
                // End disaster
                _current.deactivate(players);

                // Announce end
                for (Player p : players)
                {
                    p.sendMessage(C.cGreen + C.Bold + "✓ " + _current.getDisplayName(p) + C.cGreen + " has ended.");
                    p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.8f, 1.2f);
                }

                _current = null;
                scheduleNext(now);
            }
            else
            {
                _current.tick(players);
            }
            return;
        }

        // Handle pending disaster (warning phase)
        if (_pending != null)
        {
            if (now >= _pendingStartAt)
            {
                // Activate!
                _current = _pending;
                _current.activate(players, _pendingCenter);
                _pending = null;
                _pendingCenter = null;

                // Announce start
                for (Player p : players)
                {
                    p.sendMessage("");
                    p.sendMessage(C.cRed + C.Bold + _current.getIcon() + " " + _current.getDisplayName(p) + C.cRed + C.Bold + " has begun!");
                    p.sendMessage("");
                    p.playSound(p.getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.6f, 1.0f);
                }
            }
            return;
        }

        // Schedule new disaster
        if (now >= _nextDisasterAt)
        {
            triggerDisasterWarning(players, now);
        }
    }

    // ── Internal ────────────────────────────────────

    private void triggerDisasterWarning(List<Player> players, long now)
    {
        long elapsed = now - _game.GetStateTime();
        Disaster chosen = pickDisaster(elapsed);
        if (chosen == null) return;

        // Pick a center location near players
        Location center = pickDisasterCenter(players);
        if (center == null) return;

        _pending = chosen;
        _pendingCenter = center;
        _pendingStartAt = now + 5000; // 5s warning

        // Warn
        chosen.warn(players, center);

        // BossBar flash will be handled by PrimalGames bossbar update
    }

    private Disaster pickDisaster(long elapsedMs)
    {
        ArrayList<Disaster> pool = new ArrayList<>();

        // Phase 1: 0-8 min — T1 only
        pool.addAll(_tier1);

        // Phase 2: 8-14 min — add T2
        if (elapsedMs >= 480000)
        {
            pool.addAll(_tier2);
        }

        // Phase 3: 14+ min — add T3
        if (elapsedMs >= 840000)
        {
            pool.addAll(_tier3);
        }

        if (pool.isEmpty()) return null;
        return pool.get(_rand.nextInt(pool.size()));
    }

    private Location pickDisasterCenter(List<Player> players)
    {
        if (players.isEmpty()) return null;

        // Pick a random alive player as rough center
        Player target = players.get(_rand.nextInt(players.size()));
        Location center = target.getLocation().clone();

        // Offset randomly
        center.add(_rand.nextInt(40) - 20, 0, _rand.nextInt(40) - 20);
        if (center.getWorld() != null)
        {
            center.setY(center.getWorld().getHighestBlockYAt(center));
        }

        return center;
    }

    private void scheduleNext(long now)
    {
        long elapsed = now - _game.GetStateTime();

        long minDelay, maxDelay;

        if (elapsed < 480000) // Phase 1
        {
            minDelay = 90000;
            maxDelay = 150000;
        }
        else if (elapsed < 840000) // Phase 2
        {
            minDelay = 60000;
            maxDelay = 90000;
        }
        else // Phase 3
        {
            minDelay = 30000;
            maxDelay = 60000;
        }

        _nextDisasterAt = now + minDelay + _rand.nextInt((int) (maxDelay - minDelay));
    }

    private void cleanup()
    {
        List<Player> players = _game.GetPlayers(true);
        if (_current != null && _current.isActive())
        {
            _current.deactivate(players);
        }
        _current = null;
        _pending = null;
        _pendingCenter = null;
    }
}
