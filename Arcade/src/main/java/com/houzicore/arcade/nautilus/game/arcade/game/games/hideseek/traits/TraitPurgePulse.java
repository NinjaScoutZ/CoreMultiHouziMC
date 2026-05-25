package com.houzicore.arcade.nautilus.game.arcade.game.games.hideseek.traits;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.houzicore.arcade.nautilus.game.arcade.game.games.hideseek.HideSeek;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;
import com.houzicore.shared.common.util.UtilEvent;
import com.houzicore.shared.common.util.UtilEvent.ActionType;
import com.houzicore.shared.common.util.UtilMath;
import com.houzicore.shared.common.util.UtilParticle;
import com.houzicore.shared.common.util.UtilParticle.ParticleType;
import com.houzicore.shared.common.util.UtilParticle.ViewDist;
import com.houzicore.shared.common.util.UtilServer;

public class TraitPurgePulse extends Perk {

    public TraitPurgePulse() {
        super("Purge Pulse", new String[]{
                "Right-click an Amethyst Shard to unleash a Purge Pulse.",
                "Destroys nearby decoys and reveals Hiders who recently",
                "used an ability within range."
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
        if (item == null || item.getType() != Material.AMETHYST_SHARD) return;

        event.setCancelled(true);

        if (!game.useVanillaItemCooldown(player, "Purge Pulse", game.getAbilityCooldown(player, 18000, true)))
            return;

        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1f, 0.8f);
        UtilParticle.PlayParticle(ParticleType.ENCHANTMENT_TABLE, player.getLocation().add(0, 1, 0), 1.2f, 0.8f, 1.2f, 0f, 40, ViewDist.NORMAL, UtilServer.getPlayers());
        UtilParticle.PlayParticle(ParticleType.END_ROD, player.getLocation().add(0, 1, 0), 0.8f, 0.6f, 0.8f, 0.02f, 24, ViewDist.NORMAL, UtilServer.getPlayers());

        ArrayList<Block> decoyBlocks = new ArrayList<>();
        for (Block decoy : game._decoys.keySet()) {
            if (UtilMath.offset(decoy.getLocation().add(0.5, 0.5, 0.5), player.getLocation()) <= game.EXORCIST_PURGE_RADIUS) {
                decoyBlocks.add(decoy);
            }
        }

        ArrayList<org.bukkit.entity.LivingEntity> decoyMobs = new ArrayList<>();
        for (org.bukkit.entity.LivingEntity mob : game._decoyMobs.keySet()) {
            if (mob != null && mob.isValid() && UtilMath.offset(mob.getLocation(), player.getLocation()) <= game.EXORCIST_PURGE_RADIUS) {
                decoyMobs.add(mob);
            }
        }

        ArrayList<Block> bombBlocks = new ArrayList<>();
        for (Block bombBlock : game._bombBugBlocks.keySet()) {
            if (UtilMath.offset(bombBlock.getLocation().add(0.5, 0.5, 0.5), player.getLocation()) <= game.EXORCIST_PURGE_RADIUS) {
                bombBlocks.add(bombBlock);
            }
        }

        ArrayList<org.bukkit.entity.LivingEntity> bombMobs = new ArrayList<>();
        for (org.bukkit.entity.LivingEntity bombMob : game._bombBugMobs.keySet()) {
            if (bombMob != null && bombMob.isValid() && UtilMath.offset(bombMob.getLocation(), player.getLocation()) <= game.EXORCIST_PURGE_RADIUS) {
                bombMobs.add(bombMob);
            }
        }

        int purged = 0;
        for (Block decoyBlock : decoyBlocks) {
            game.BreakDecoyBlock(decoyBlock);
            purged++;
        }

        for (org.bukkit.entity.LivingEntity decoyMob : decoyMobs) {
            game._decoyMobs.remove(decoyMob);
            UtilParticle.PlayParticle(ParticleType.CLOUD, decoyMob.getLocation().add(0, 1, 0), 0.5f, 0.5f, 0.5f, 0, 10, ViewDist.NORMAL, UtilServer.getPlayers());
            decoyMob.remove();
            purged++;
        }

        for (Block bombBlock : bombBlocks) {
            game._bombBugBlocks.remove(bombBlock);
            bombBlock.setType(Material.AIR);
            UtilParticle.PlayParticle(ParticleType.CLOUD, bombBlock.getLocation().add(0.5, 0.5, 0.5), 0.5f, 0.5f, 0.5f, 0, 10, ViewDist.NORMAL, UtilServer.getPlayers());
            purged++;
        }

        for (org.bukkit.entity.LivingEntity bombMob : bombMobs) {
            game._bombBugMobs.remove(bombMob);
            UtilParticle.PlayParticle(ParticleType.CLOUD, bombMob.getLocation().add(0, 1, 0), 0.5f, 0.5f, 0.5f, 0, 10, ViewDist.NORMAL, UtilServer.getPlayers());
            bombMob.remove();
            purged++;
        }

        int revealed = 0;
        long now = System.currentTimeMillis();
        for (Player hider : game.getHiders().GetPlayers(true)) {
            long lastSkillUse = game._recentHiderSkillUse.getOrDefault(hider, 0L);
            if (now - lastSkillUse > game.EXORCIST_TRACE_WINDOW_MS)
                continue;

            if (UtilMath.offset(player, hider) > game.EXORCIST_REVEAL_RADIUS)
                continue;

            game.revealHider(hider, game.getRevealDurationTicks(100));
            hider.playSound(hider.getLocation(), Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 0.8f, 1.4f);
            game.sendPropRushMessageKey(hider, "prop_rush.feedback.exorcist_traced");
            UtilParticle.PlayParticle(ParticleType.END_ROD, hider.getLocation().add(0, 1, 0), 0.4f, 0.6f, 0.4f, 0.02f, 14, ViewDist.NORMAL, UtilServer.getPlayers());
            revealed++;
        }

        if (purged > 0 || revealed > 0) {
            game.sendPropRushNoticeKey(player, "prop_rush.notice.purge_pulse_result",
                    net.kyori.adventure.text.minimessage.tag.resolver.Placeholder.unparsed("purged", String.valueOf(purged)),
                    net.kyori.adventure.text.minimessage.tag.resolver.Placeholder.unparsed("revealed", String.valueOf(revealed)));
        } else {
            game.sendPropRushNoticeKey(player, "prop_rush.notice.purge_pulse_none");
        }
    }
}
