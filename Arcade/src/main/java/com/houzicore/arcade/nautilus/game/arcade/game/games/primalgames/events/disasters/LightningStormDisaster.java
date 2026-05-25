package com.houzicore.arcade.nautilus.game.arcade.game.games.primalgames.events.disasters;

import java.util.List;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import com.houzicore.arcade.nautilus.game.arcade.game.games.primalgames.PrimalGames;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.UtilMath;
import com.houzicore.shared.common.util.UtilParticle;
import com.houzicore.shared.common.util.UtilParticle.ParticleType;
import com.houzicore.shared.common.util.UtilParticle.ViewDist;
import com.houzicore.shared.common.util.UtilServer;

/**
 * Tier 3 Disaster: Lightning Storm
 * Targeted lightning strikes near players with real lightning + thunder FX.
 */
public class LightningStormDisaster extends Disaster
{
    private final Random _rand = new Random();
    private int _tickCount = 0;

    public LightningStormDisaster(PrimalGames game)
    {
        super(game, "Lightning Storm", "พายุสายฟ้า", "⚡", 3, 16000);
    }

    @Override
    public void warn(List<Player> players, Location center)
    {
        for (Player p : players)
        {
            p.playSound(p.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.8f, 0.5f);
            p.sendMessage(C.cRed + C.Bold + "⚠ " + _icon + " " + getDisplayName(p) + C.cRed + " incoming!");
        }
    }

    @Override
    public void start(List<Player> players, Location center)
    {
        _tickCount = 0;
        for (Player p : players)
        {
            p.playSound(p.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 0.3f);
        }
    }

    @Override
    public void tick(List<Player> players)
    {
        _tickCount++;
        if (getWorld() == null) return;

        // Strike near random alive players
        List<Player> alive = new java.util.ArrayList<>();
        for (Player p : players)
        {
            if (_game.IsAlive(p)) alive.add(p);
        }
        if (alive.isEmpty()) return;

        // 1-2 strikes per second
        int strikes = 1 + (_tickCount % 3 == 0 ? 1 : 0);

        for (int i = 0; i < strikes; i++)
        {
            Player target = alive.get(_rand.nextInt(alive.size()));
            Location loc = target.getLocation().clone();

            // Offset slightly so it's not always a direct hit
            double offsetX = (_rand.nextDouble() - 0.5) * 12;
            double offsetZ = (_rand.nextDouble() - 0.5) * 12;
            loc.add(offsetX, 0, offsetZ);
            loc.setY(getWorld().getHighestBlockYAt(loc));

            // Strike lightning
            getWorld().strikeLightning(loc);

            // Extra particles
            UtilParticle.PlayParticle(ParticleType.FIREWORKS_SPARK,
                    loc.clone().add(0, 1, 0),
                    1.0f, 2.0f, 1.0f, 0.1f, 20,
                    ViewDist.LONG, UtilServer.getPlayers());

            // Check if player is very close to strike
            double dist = UtilMath.offset(target.getLocation(), loc);
            if (dist < 3.0)
            {
                _game.Manager.GetDamage().NewDamageEvent(target, null, null,
                        org.bukkit.event.entity.EntityDamageEvent.DamageCause.LIGHTNING,
                        4.0, false, true, true,
                        _game.GetName(), "Lightning Strike");
            }
        }

        // Ambient thunder
        if (_tickCount % 4 == 0)
        {
            for (Player p : players)
            {
                p.playSound(p.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER,
                        0.5f + (float) (_rand.nextDouble() * 0.5f),
                        0.3f + (float) (_rand.nextDouble() * 0.4f));
            }
        }
    }

    @Override
    public void end(List<Player> players)
    {
        _tickCount = 0;
        for (Player p : players)
        {
            p.playSound(p.getLocation(), Sound.WEATHER_RAIN, 0.3f, 1.2f);
        }
    }
}
