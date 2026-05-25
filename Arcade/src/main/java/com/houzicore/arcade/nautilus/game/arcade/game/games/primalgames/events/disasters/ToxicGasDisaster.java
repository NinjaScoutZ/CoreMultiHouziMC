package com.houzicore.arcade.nautilus.game.arcade.game.games.primalgames.events.disasters;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.houzicore.arcade.nautilus.game.arcade.game.games.primalgames.PrimalGames;
import com.houzicore.arcade.nautilus.game.arcade.game.games.primalgames.lang.PrimalGamesLang;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.UtilParticle;
import com.houzicore.shared.common.util.UtilParticle.ParticleType;
import com.houzicore.shared.common.util.UtilParticle.ViewDist;
import com.houzicore.shared.common.util.UtilServer;

/**
 * Tier 1 Disaster: Toxic Gas
 *
 * A poisonous gas cloud erupts across outdoor areas.
 * 3-layer visual system: sky column (seen map-wide), ground cloud, player FX.
 *
 * - Sky column:   LARGE_SMOKE rising 40 blocks — visible from across the map
 * - Ground cloud: GREEN_DUST + SPELL_WITCH spreading 25-block radius at ground level
 * - Player FX:    SPELL_WITCH around each affected player + Poison I
 */
public class ToxicGasDisaster extends Disaster
{
    private int _tickCount = 0;

    public ToxicGasDisaster(PrimalGames game)
    {
        super(game, "Toxic Gas", "ก๊าซพิษ", "⚗", 1, 20000); // 20s duration
    }

    @Override
    public void warn(List<Player> players, Location center)
    {
        PrimalGamesLang lang = PrimalGamesLang.get();
        for (Player p : players)
        {
            p.playSound(p.getLocation(), Sound.ENTITY_ELDER_GUARDIAN_CURSE, 0.8f, 0.6f);
            p.sendMessage(lang.get(p, "primal_games.disaster.toxic_gas.warning"));
            p.sendMessage(lang.get(p, "primal_games.disaster.toxic_gas.tip"));
        }
    }

    @Override
    public void start(List<Player> players, Location center)
    {
        PrimalGamesLang lang = PrimalGamesLang.get();
        for (Player p : players)
        {
            p.playSound(p.getLocation(), Sound.AMBIENT_CAVE, 0.5f, 0.4f);
            p.sendMessage(lang.get(p, "primal_games.disaster.toxic_gas.active"));
        }
        _tickCount = 0;
    }

    @Override
    public void tick(List<Player> players)
    {
        _tickCount++;

        for (Player p : players)
        {
            if (!_game.IsAlive(p)) continue;

            Location loc = p.getLocation();
            boolean outdoor = loc.getWorld().getHighestBlockYAt(loc) <= loc.getBlockY();

            // ── Layer 1: SKY COLUMN — visible map-wide ──────────────────────────
            // Rise 40 blocks from player position — seen from far away
            if (outdoor)
            {
                for (int y = 2; y <= 40; y += 3)
                {
                    Location skyLoc = loc.clone().add(
                        (Math.random() - 0.5) * 2,
                        y,
                        (Math.random() - 0.5) * 2
                    );
                    UtilParticle.PlayParticle(ParticleType.LARGE_SMOKE,
                        skyLoc, 1.2f, 0f, 1.2f, 0.015f, 2,
                        ViewDist.LONG, UtilServer.getPlayers());
                }

                // Greenish tint every 3 ticks to distinguish from normal smoke
                if (_tickCount % 3 == 0)
                {
                    for (int y = 1; y <= 20; y += 4)
                    {
                        UtilParticle.PlayParticle(ParticleType.HAPPY_VILLAGER,
                            loc.clone().add(0, y, 0),
                            1.5f, 0f, 1.5f, 0.02f, 3,
                            ViewDist.LONG, UtilServer.getPlayers());
                    }
                }
            }

            // ── Layer 2: GROUND CLOUD — medium-range atmosphere ─────────────────
            // Dense cloud at ground level
            if (outdoor)
            {
                for (int i = 0; i < 6; i++)
                {
                    double rx = (Math.random() - 0.5) * 25;
                    double rz = (Math.random() - 0.5) * 25;
                    double ry = Math.random() * 2.5;

                    UtilParticle.PlayParticle(ParticleType.HAPPY_VILLAGER,
                        loc.clone().add(rx, ry + 0.5, rz),
                        0f, 0f, 0f, 0.1f, 1,
                        ViewDist.NORMAL, UtilServer.getPlayers());

                    UtilParticle.PlayParticle(ParticleType.SPELL,
                        loc.clone().add(rx * 0.6, ry + 0.3, rz * 0.6),
                        0f, 0f, 0f, 0.05f, 1,
                        ViewDist.NORMAL, UtilServer.getPlayers());
                }
            }

            // ── Layer 3: PLAYER FX + DAMAGE ────────────────────────────────────
            if (outdoor)
            {
                // Witch particles swirling around affected player
                UtilParticle.PlayParticle(ParticleType.SPELL,
                    loc.clone().add(0, 1, 0),
                    0.6f, 0.8f, 0.6f, 0.1f, 8,
                    ViewDist.NORMAL, p);

                // Ambient cave sound every 3 ticks (subtle gas hiss)
                if (_tickCount % 3 == 0)
                {
                    p.playSound(loc, Sound.AMBIENT_CAVE, 0.25f, 0.3f);
                }

                // Apply Poison I + 1♥ damage per tick
                p.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 60, 0, false, false, false));

                _game.Manager.GetDamage().NewDamageEvent(p, null, null,
                    org.bukkit.event.entity.EntityDamageEvent.DamageCause.CUSTOM,
                    1.0, false, true, true,
                    _game.GetName(), "Toxic Gas");
            }
        }
    }

    @Override
    public void end(List<Player> players)
    {
        PrimalGamesLang lang = PrimalGamesLang.get();
        for (Player p : players)
        {
            p.removePotionEffect(PotionEffectType.POISON);
            p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_BURP, 0.4f, 0.5f);
            p.sendMessage(lang.get(p, "primal_games.disaster.toxic_gas.ended"));
        }
        _tickCount = 0;
    }
}
