package com.houzicore.arcade.nautilus.game.arcade.game.games.primalgames.events.disasters;

import java.util.HashSet;
import java.util.List;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.houzicore.arcade.nautilus.game.arcade.game.games.primalgames.PrimalGames;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.UtilMath;
import com.houzicore.shared.common.util.UtilParticle;
import com.houzicore.shared.common.util.UtilParticle.ParticleType;
import com.houzicore.shared.common.util.UtilParticle.ViewDist;
import com.houzicore.shared.common.util.UtilServer;

/**
 * Tier 2 Disaster: Sinkhole
 * Collapses a 5-block radius of terrain at a random location.
 */
public class SinkholeDisaster extends Disaster
{
    private static final int RADIUS = 5;
    private final Random _rand = new Random();
    private int _tickCount = 0;
    private boolean _collapsed = false;

    public SinkholeDisaster(PrimalGames game)
    {
        super(game, "Sinkhole", "หลุมยุบ", "🕳️", 2, 12000);
    }

    @Override
    public void warn(List<Player> players, Location center)
    {
        for (Player p : players)
        {
            p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.6f, 0.3f);
            p.sendMessage(C.cRed + C.Bold + "⚠ " + _icon + " " + getDisplayName(p) + C.cRed + " incoming!");
            p.sendMessage(C.cYellow + "  ➤ X:" + center.getBlockX() + " Z:" + center.getBlockZ());
        }
    }

    @Override
    public void start(List<Player> players, Location center)
    {
        _collapsed = false;
        _tickCount = 0;
        for (Player p : players)
            p.playSound(center, Sound.ENTITY_WARDEN_ROAR, 1.0f, 0.3f);
    }

    @Override
    public void tick(List<Player> players)
    {
        _tickCount++;
        if (_center == null || getWorld() == null) return;

        if (_tickCount <= 3 && !_collapsed)
        {
            for (Player p : players)
            {
                if (!_game.IsAlive(p)) continue;
                if (UtilMath.offset(p.getLocation(), _center) < RADIUS + 10)
                {
                    UtilParticle.PlayParticle(ParticleType.BLOCK_CRACK,
                            p.getLocation().add(0, 0.1, 0),
                            2.0f, 0.1f, 2.0f, 0.5f, 20,
                            ViewDist.NORMAL, p);
                    p.playSound(_center, Sound.BLOCK_ANVIL_LAND, 0.3f, 0.2f);
                }
            }
            return;
        }

        if (!_collapsed)
        {
            _collapsed = true;
            int depth = 8 + _rand.nextInt(5);
            for (int x = -RADIUS; x <= RADIUS; x++)
            {
                for (int z = -RADIUS; z <= RADIUS; z++)
                {
                    if (x * x + z * z > RADIUS * RADIUS) continue;
                    int topY = getWorld().getHighestBlockYAt(_center.getBlockX() + x, _center.getBlockZ() + z);
                    for (int y = topY; y > topY - depth && y > 1; y--)
                    {
                        Block block = getWorld().getBlockAt(_center.getBlockX() + x, y, _center.getBlockZ() + z);
                        if (block.getType() == Material.BEDROCK || block.getType() == Material.BARRIER) continue;
                        if (block.getType() != Material.AIR)
                        {
                            UtilParticle.PlayParticle(ParticleType.BLOCK_CRACK,
                                    block.getLocation().add(0.5, 0.5, 0.5),
                                    0.3f, 0.3f, 0.3f, 0.1f, 5,
                                    ViewDist.NORMAL, UtilServer.getPlayers());
                            block.setType(Material.AIR);
                        }
                    }
                }
            }
            for (Player p : players)
            {
                p.playSound(_center, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.3f);
                p.playSound(_center, Sound.ENTITY_WARDEN_SONIC_BOOM, 0.8f, 0.5f);
            }
        }
    }

    @Override
    public void end(List<Player> players)
    {
        _collapsed = false;
        _tickCount = 0;
    }
}
