package com.houzicore.arcade.nautilus.game.arcade.game.games.primalgames.events.disasters;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.houzicore.arcade.nautilus.game.arcade.game.games.primalgames.PrimalGames;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.UtilAlg;
import com.houzicore.shared.common.util.UtilMath;
import com.houzicore.shared.common.util.UtilParticle;
import com.houzicore.shared.common.util.UtilParticle.ParticleType;
import com.houzicore.shared.common.util.UtilParticle.ViewDist;
import com.houzicore.shared.common.util.UtilServer;

/**
 * Tier 2 Disaster: Meteor Shower
 * 
 * Fireballs rain from the sky in a ~30-block zone around a random location.
 * Players must move out of the impact zone or find shelter.
 */
public class MeteorShowerDisaster extends Disaster
{
    private final Random _rand = new Random();
    private final ArrayList<Fireball> _meteors = new ArrayList<>();
    private int _tickCount = 0;

    public MeteorShowerDisaster(PrimalGames game)
    {
        super(game, "Meteor Shower", "อุกกาบาตตก", "☄️", 2, 18000);
    }

    @Override
    public void warn(List<Player> players, Location center)
    {
        for (Player p : players)
        {
            p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.8f, 1.5f);
            p.sendMessage(C.cRed + C.Bold + "⚠ " + _icon + " " + getDisplayName(p) + C.cRed + " กำลังจะมา!");
            p.sendMessage(C.cYellow + "  ➤ " + C.cWhite + "Impact zone: X:" + center.getBlockX() + " Z:" + center.getBlockZ());
        }
    }

    @Override
    public void start(List<Player> players, Location center)
    {
        for (Player p : players)
        {
            p.playSound(p.getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.6f, 1.2f);
        }
    }

    @Override
    public void tick(List<Player> players)
    {
        _tickCount++;
        if (_center == null || getWorld() == null) return;

        // Spawn 1-3 meteors per second
        int count = 1 + _rand.nextInt(3);

        for (int i = 0; i < count; i++)
        {
            double offsetX = (_rand.nextDouble() - 0.5) * 60;
            double offsetZ = (_rand.nextDouble() - 0.5) * 60;

            Location spawn = _center.clone().add(offsetX, 50 + _rand.nextInt(20), offsetZ);

            // Target a spot near the center
            Location target = _center.clone().add(
                    (_rand.nextDouble() - 0.5) * 30,
                    0,
                    (_rand.nextDouble() - 0.5) * 30
            );
            target.setY(getWorld().getHighestBlockYAt(target));

            try
            {
                Fireball meteor = spawn.getWorld().spawn(spawn, Fireball.class);
                Vector dir = UtilAlg.getTrajectory(spawn, target);
                meteor.setDirection(dir.multiply(0.15));
                meteor.setYield(0); // No block damage (we handle damage ourselves)
                meteor.setIsIncendiary(false);
                _meteors.add(meteor);
            }
            catch (Exception ignored) { }
        }

        // Trail particles on existing meteors
        ArrayList<Fireball> toRemove = new ArrayList<>();
        for (Fireball fb : _meteors)
        {
            if (!fb.isValid() || fb.isDead())
            {
                toRemove.add(fb);
                continue;
            }

            UtilParticle.PlayParticle(ParticleType.LAVA,
                    fb.getLocation(),
                    0.5f, 0.5f, 0.5f, 0.05f, 5,
                    ViewDist.LONG, UtilServer.getPlayers());
        }
        _meteors.removeAll(toRemove);

        // Ambient rumble sound periodically
        if (_tickCount % 3 == 0)
        {
            for (Player p : players)
            {
                p.playSound(p.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.4f, 0.3f);
            }
        }
    }

    @Override
    public void end(List<Player> players)
    {
        // Clean up any remaining fireballs
        for (Fireball fb : _meteors)
        {
            if (fb.isValid()) fb.remove();
        }
        _meteors.clear();
        _tickCount = 0;

        for (Player p : players)
        {
            p.playSound(p.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 0.6f, 0.8f);
        }
    }
}
