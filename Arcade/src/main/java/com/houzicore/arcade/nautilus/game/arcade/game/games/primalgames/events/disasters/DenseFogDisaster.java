package com.houzicore.arcade.nautilus.game.arcade.game.games.primalgames.events.disasters;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.houzicore.arcade.nautilus.game.arcade.game.games.primalgames.PrimalGames;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.UtilParticle;
import com.houzicore.shared.common.util.UtilParticle.ParticleType;
import com.houzicore.shared.common.util.UtilParticle.ViewDist;
import com.houzicore.shared.common.util.UtilServer;

/**
 * Tier 1 Disaster: Dense Fog
 * 
 * Applies Blindness I to all players for the duration.
 * Reduces nametag visibility range. White cloud particles.
 */
public class DenseFogDisaster extends Disaster
{
    public DenseFogDisaster(PrimalGames game)
    {
        super(game, "Dense Fog", "หมอกหนาทึบ", "🌫️", 1, 12000);
    }

    @Override
    public void warn(List<Player> players, Location center)
    {
        for (Player p : players)
        {
            p.playSound(p.getLocation(), Sound.AMBIENT_CAVE, 1.0f, 0.5f);
            p.sendMessage(C.cYellow + C.Bold + "⚠ " + _icon + " " + getDisplayName(p) + C.cYellow + " กำลังจะมา!");
        }
    }

    @Override
    public void start(List<Player> players, Location center)
    {
        for (Player p : players)
        {
            if (!_game.IsAlive(p)) continue;

            p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, (int) (_duration / 50), 0, false, false, false));
            p.playSound(p.getLocation(), Sound.ENTITY_PHANTOM_AMBIENT, 0.8f, 0.4f);
        }
    }

    @Override
    public void tick(List<Player> players)
    {
        for (Player p : players)
        {
            if (!_game.IsAlive(p)) continue;

            Location loc = p.getLocation();

            // Thick white fog particles
            UtilParticle.PlayParticle(ParticleType.CLOUD,
                    loc.clone().add(0, 1.5, 0),
                    5.0f, 2.0f, 5.0f, 0.01f, 15,
                    ViewDist.NORMAL, p);

            // Ensure blindness stays applied even if cured
            if (!p.hasPotionEffect(PotionEffectType.BLINDNESS))
            {
                long remaining = getTimeRemaining();
                if (remaining > 1000)
                {
                    p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, (int) (remaining / 50), 0, false, false, false));
                }
            }
        }
    }

    @Override
    public void end(List<Player> players)
    {
        for (Player p : players)
        {
            p.removePotionEffect(PotionEffectType.BLINDNESS);
            p.playSound(p.getLocation(), Sound.BLOCK_GLASS_BREAK, 0.5f, 1.5f);
        }
    }
}
