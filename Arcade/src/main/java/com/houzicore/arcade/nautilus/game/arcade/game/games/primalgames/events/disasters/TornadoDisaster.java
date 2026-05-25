package com.houzicore.arcade.nautilus.game.arcade.game.games.primalgames.events.disasters;

import java.util.List;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.houzicore.arcade.nautilus.game.arcade.game.games.primalgames.PrimalGames;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.UtilAction;
import com.houzicore.shared.common.util.UtilAlg;
import com.houzicore.shared.common.util.UtilMath;
import com.houzicore.shared.common.util.UtilParticle;
import com.houzicore.shared.common.util.UtilParticle.ParticleType;
import com.houzicore.shared.common.util.UtilParticle.ViewDist;
import com.houzicore.shared.common.util.UtilServer;

/**
 * Tier 3 Disaster: Tornado
 * Pulls entities toward center, launches them upward. Spiral particles.
 */
public class TornadoDisaster extends Disaster
{
    private static final double PULL_RADIUS = 20.0;
    private static final double LAUNCH_RADIUS = 5.0;
    private final Random _rand = new Random();
    private double _angle = 0;
    private int _tickCount = 0;

    public TornadoDisaster(PrimalGames game)
    {
        super(game, "Tornado", "พายุทอร์นาโด", "🌪️", 3, 15000);
    }

    @Override
    public void warn(List<Player> players, Location center)
    {
        for (Player p : players)
        {
            p.playSound(p.getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.8f, 1.5f);
            p.sendMessage(C.cRed + C.Bold + "⚠ " + _icon + " " + getDisplayName(p) + C.cRed + " incoming!");
            p.sendMessage(C.cYellow + "  ➤ X:" + center.getBlockX() + " Z:" + center.getBlockZ());
        }
    }

    @Override
    public void start(List<Player> players, Location center)
    {
        _angle = 0;
        _tickCount = 0;
        for (Player p : players)
        {
            p.playSound(center, Sound.ENTITY_ENDER_DRAGON_FLAP, 2.0f, 0.3f);
        }
    }

    @Override
    public void tick(List<Player> players)
    {
        _tickCount++;
        if (_center == null || getWorld() == null) return;

        // Spiral particle column
        _angle += 0.5;
        for (int y = 0; y < 25; y++)
        {
            double radius = 1.0 + (y * 0.3);
            double x = Math.cos(_angle + (y * 0.3)) * radius;
            double z = Math.sin(_angle + (y * 0.3)) * radius;

            Location particleLoc = _center.clone().add(x, y * 0.8, z);

            UtilParticle.PlayParticle(ParticleType.CLOUD,
                    particleLoc, 0.2f, 0.2f, 0.2f, 0.01f, 2,
                    ViewDist.LONG, UtilServer.getPlayers());
        }

        // Pull and launch players
        for (Player p : players)
        {
            if (!_game.IsAlive(p)) continue;

            double dist = UtilMath.offset2d(p.getLocation(), _center);

            if (dist < PULL_RADIUS)
            {
                // Pull toward center
                Vector pull = UtilAlg.getTrajectory2d(p.getLocation(), _center);
                double pullStrength = (1.0 - (dist / PULL_RADIUS)) * 0.4;
                UtilAction.velocity(p, pull, pullStrength, false, 0, 0.05, 10, true);

                // Wind sound
                if (_tickCount % 2 == 0)
                {
                    p.playSound(_center, Sound.ENTITY_ENDER_DRAGON_FLAP, 0.5f, 0.5f);
                }

                // Launch if too close
                if (dist < LAUNCH_RADIUS)
                {
                    UtilAction.velocity(p, new Vector(
                            (_rand.nextDouble() - 0.5) * 0.6,
                            0.8 + _rand.nextDouble() * 0.5,
                            (_rand.nextDouble() - 0.5) * 0.6
                    ), 1.0, false, 0, 0, 10, true);

                    _game.Manager.GetDamage().NewDamageEvent(p, null, null,
                            org.bukkit.event.entity.EntityDamageEvent.DamageCause.CUSTOM,
                            2.0, false, true, true,
                            _game.GetName(), "Tornado");

                    p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_HURT, 1.0f, 0.8f);
                }
            }
        }
    }

    @Override
    public void end(List<Player> players)
    {
        _tickCount = 0;
        _angle = 0;
        for (Player p : players)
        {
            p.playSound(p.getLocation(), Sound.WEATHER_RAIN, 0.5f, 1.5f);
        }
    }
}
