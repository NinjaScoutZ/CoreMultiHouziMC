package com.houzicore.arcade.nautilus.game.arcade.game.games.hideseek.traits;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.houzicore.arcade.nautilus.game.arcade.game.games.hideseek.HideSeek;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;
import com.houzicore.shared.common.util.UtilEvent;
import com.houzicore.shared.common.util.UtilEvent.ActionType;
import com.houzicore.shared.common.util.UtilParticle;
import com.houzicore.shared.common.util.UtilParticle.ParticleType;
import com.houzicore.shared.common.util.UtilParticle.ViewDist;
import com.houzicore.shared.common.util.UtilServer;

public class TraitEchoSentry extends Perk {

    public TraitEchoSentry() {
        super("Echo Sentry", new String[]{
                "Right-click a Sculk Sensor to deploy an Echo Sentry.",
                "It locks down an area, slowing Hiders who try to pass."
        });
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (Manager.GetGame() == null || !(Manager.GetGame() instanceof HideSeek)) return;
        HideSeek game = (HideSeek) Manager.GetGame();

        if (game.GetState() != com.houzicore.arcade.nautilus.game.arcade.game.Game.GameState.Live)
            return;

        Player player = event.getPlayer();

        if (!Kit.HasKit(player)) return;
        if (!game.getSeekers().HasPlayer(player)) return;

        if (!UtilEvent.isAction(event, ActionType.R)) return;

        ItemStack item = player.getItemInHand();
        if (item == null || item.getType() != Material.SCULK_SENSOR) return;

        event.setCancelled(true);

        if (System.currentTimeMillis() < game._terminalDisruptionUntil) {
            game.sendPropRushNoticeKey(player, "prop_rush.notice.warden_interference");
            return;
        }

        if (!game.useVanillaItemCooldown(player, "Echo Sentry", game.getAbilityCooldown(player, 20000, true)))
            return;

        game.removeWardenSentries(player);

        Location sentryLoc = game.getWardenSentryPlacement(player);
        game._wardenSentries.add(new HideSeek.WardenSentry(player, sentryLoc, System.currentTimeMillis() + game.getWardenSentryDuration(), System.currentTimeMillis() + 1000L));

        sentryLoc.getWorld().playSound(sentryLoc, Sound.BLOCK_SCULK_SENSOR_CLICKING_STOP, 1f, 1.1f);
        UtilParticle.PlayParticle(ParticleType.ENCHANTMENT_TABLE, sentryLoc.clone().add(0, 0.3, 0), 0.25f, 0.05f, 0.25f, 0f, 14, ViewDist.NORMAL, UtilServer.getPlayers());
        game.sendPropRushNoticeKey(player, "prop_rush.notice.echo_sentry_deployed");
    }
}
