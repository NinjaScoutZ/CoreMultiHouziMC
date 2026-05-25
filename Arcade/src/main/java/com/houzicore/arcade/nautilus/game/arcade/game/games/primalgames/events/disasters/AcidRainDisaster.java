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
 * Tier 1 Disaster: Acid Rain
 * 
 * Players without a helmet take slow damage while outdoors.
 * Green particles rain down, eerie ambient sound.
 */
public class AcidRainDisaster extends Disaster
{
    public AcidRainDisaster(PrimalGames game)
    {
        super(game, "Acid Rain", "ฝนกรด", "🌧️", 1, 15000);
    }

    @Override
    public void warn(List<Player> players, Location center)
    {
        for (Player p : players)
        {
            p.playSound(p.getLocation(), Sound.WEATHER_RAIN_ABOVE, 1.0f, 0.6f);
            p.sendMessage(C.cYellow + C.Bold + "⚠ " + _icon + " " + getDisplayName(p) + C.cYellow + " กำลังจะมา!");
        }
    }

    @Override
    public void start(List<Player> players, Location center)
    {
        for (Player p : players)
        {
            p.playSound(p.getLocation(), Sound.WEATHER_RAIN, 1.0f, 0.5f);
        }
    }

    @Override
    public void tick(List<Player> players)
    {
        for (Player p : players)
        {
            if (!_game.IsAlive(p)) continue;

            Location loc = p.getLocation();

            // Green acid particles around player
            UtilParticle.PlayParticle(ParticleType.HAPPY_VILLAGER,
                    loc.clone().add(0, 2.5, 0),
                    3.0f, 1.0f, 3.0f, 0.02f, 8,
                    ViewDist.NORMAL, UtilServer.getPlayers());

            // Check if player is exposed to sky (no blocks above)
            if (loc.getWorld().getHighestBlockYAt(loc) <= loc.getBlockY())
            {
                // Check if wearing helmet
                if (p.getInventory().getHelmet() == null)
                {
                    // Deal damage through CombatManager
                    _game.Manager.GetDamage().NewDamageEvent(p, null, null,
                            org.bukkit.event.entity.EntityDamageEvent.DamageCause.CUSTOM,
                            1.0, false, true, true,
                            _game.GetName(), "Acid Rain");

                    p.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 40, 0, false, false, false));

                    UtilParticle.PlayParticle(ParticleType.RED_DUST,
                            loc.clone().add(0, 1, 0),
                            0.5f, 0.5f, 0.5f, 0.01f, 5,
                            ViewDist.NORMAL, p);
                }
            }
        }
    }

    @Override
    public void end(List<Player> players)
    {
        for (Player p : players)
        {
            p.removePotionEffect(PotionEffectType.POISON);
        }
    }
}
