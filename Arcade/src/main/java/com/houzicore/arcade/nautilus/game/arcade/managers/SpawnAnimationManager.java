package com.houzicore.arcade.nautilus.game.arcade.managers;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.UtilText;
import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.events.GameStateChangeEvent;
import com.houzicore.arcade.nautilus.game.arcade.game.Game.GameState;
import com.houzicore.shared.core.lang.LangManager;

/**
 * Spawn Animation Manager (#39)
 * Plays a premium entry animation (particles + sound + title) when a player
 * teleports into a game or spawns at game start.
 */
public class SpawnAnimationManager implements Listener {

    private final ArcadeManager Manager;

    public SpawnAnimationManager(ArcadeManager manager) {
        Manager = manager;
        Manager.getPluginManager().registerEvents(this, Manager.getPlugin());
    }

    @EventHandler
    public void onGameStart(GameStateChangeEvent event) {
        if (event.GetState() != GameState.Live) return;

        // Short delay to let teleport complete
        Manager.getPlugin().getServer().getScheduler().runTaskLater(Manager.getPlugin(), () -> {
            for (Player player : Manager.GetGame().GetPlayers(false)) {
                playSpawnEffect(player);
            }
        }, 5L);
    }

    public void playSpawnEffect(Player player) {
        if (!player.isOnline()) return;
        Location loc = player.getLocation();

        // Particle burst
        loc.getWorld().spawnParticle(Particle.PORTAL, loc.clone().add(0, 1, 0), 40, 0.3, 1.0, 0.3, 0.4);
        loc.getWorld().spawnParticle(Particle.END_ROD, loc.clone().add(0, 1, 0), 15, 0.2, 0.5, 0.2, 0.1);

        // Layered sounds
        player.playSound(loc, Sound.ENTITY_ENDERMAN_TELEPORT, 0.8f, 1.0f);
        Manager.getPlugin().getServer().getScheduler().runTaskLater(Manager.getPlugin(), () -> {
            if (player.isOnline())
                player.playSound(player.getLocation(), Sound.BLOCK_END_PORTAL_SPAWN, 0.4f, 1.4f);
        }, 2L);

        boolean thai = LangManager.get().isThai(player);
        String title = thai ? "เริ่มเกม!" : UtilText.toSmallCaps("game start!");
        String subtitle = thai ? "ขอให้โชคดี, " + player.getName() + "!" : "Good luck, " + player.getName() + "!";

        player.sendMessage(C.cAqua + C.Bold + title);
        player.sendMessage(C.cGray + subtitle);
    }
}
