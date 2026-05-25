package com.houzicore.arcade.nautilus.game.arcade.game.games.hideseek.traits;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Parrot;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import com.houzicore.arcade.nautilus.game.arcade.game.games.hideseek.HideSeek;
import com.houzicore.arcade.nautilus.game.arcade.game.Game.GameState;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;
import com.houzicore.shared.common.util.UtilEvent;
import com.houzicore.shared.common.util.UtilEvent.ActionType;
import com.houzicore.shared.common.util.UtilMath;
import com.houzicore.shared.common.util.UtilParticle;
import com.houzicore.shared.common.util.UtilParticle.ParticleType;
import com.houzicore.shared.common.util.UtilParticle.ViewDist;
import com.houzicore.shared.common.util.UtilServer;

public class TraitSkySweep extends Perk {
    private static final int SCAN_WINDOW_TICKS = 80;
    private static final int SCAN_PERIOD_TICKS = 5;

    public TraitSkySweep() {
        super("Sky Sweep", new String[]{
                "Right-click a Feather to unleash a Sky Sweep.",
                "Reveals nearby Hiders while the scout circles overhead."
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
        if (item == null || item.getType() != Material.FEATHER) return;

        event.setCancelled(true);

        if (!game.useVanillaItemCooldown(player, "Sky Sweep", game.getAbilityCooldown(player, 18000, true)))
            return;

        player.playSound(player.getLocation(), Sound.ENTITY_PARROT_FLY, 1f, 1.2f);
        UtilParticle.PlayParticle(ParticleType.CLOUD, player.getLocation().clone().add(0, 1.2, 0), 0.5f, 0.2f, 0.5f, 0.02f, 12, ViewDist.NORMAL, UtilServer.getPlayers());

        LivingEntity falcon = spawnScoutBird(player);
        runSkySweepScan(game, player, falcon);
    }

    private LivingEntity spawnScoutBird(Player player) {
        LivingEntity bird = (LivingEntity) player.getWorld().spawnEntity(player.getLocation().clone().add(0, 2.2, 0), EntityType.PARROT);
        bird.setCustomName("Falcon Scout");
        bird.setCustomNameVisible(true);
        bird.setInvulnerable(true);
        bird.setSilent(true);
        bird.setCollidable(false);
        bird.setGravity(false);
        bird.setGlowing(true);

        if (bird instanceof Parrot parrot) {
            parrot.setSitting(false);
        }

        return bird;
    }

    private void runSkySweepScan(HideSeek game, Player player, LivingEntity falcon) {
        new BukkitRunnable() {
            private int tick;
            private int revealed;
            private final java.util.Set<java.util.UUID> alreadyRevealed = new java.util.HashSet<>();

            @Override
            public void run() {
                if (!isScanStillValid(game, player) || tick >= SCAN_WINDOW_TICKS) {
                    finish();
                    return;
                }

                moveFalcon(player, falcon, tick);

                if (tick % SCAN_PERIOD_TICKS == 0) {
                    revealed += scanForHiders(game, player, alreadyRevealed);
                }

                tick++;
            }

            private void finish() {
                if (falcon != null && falcon.isValid()) {
                    UtilParticle.PlayParticle(ParticleType.CLOUD, falcon.getLocation(), 0.3f, 0.2f, 0.3f, 0.02f, 10, ViewDist.NORMAL, UtilServer.getPlayers());
                    falcon.remove();
                }

                if (player.isOnline()) {
                    if (revealed > 0) {
                        game.sendPropRushNoticeKey(player, "prop_rush.notice.sky_sweep_found",
                                net.kyori.adventure.text.minimessage.tag.resolver.Placeholder.unparsed("count", String.valueOf(revealed)));
                    } else {
                        game.sendPropRushNoticeKey(player, "prop_rush.notice.sky_sweep_none");
                    }
                }

                cancel();
            }
        }.runTaskTimer(Manager.getPlugin(), 0L, 1L);
    }

    private boolean isScanStillValid(HideSeek game, Player player) {
        return player != null
                && player.isOnline()
                && game.GetState() == GameState.Live
                && game.getSeekers().HasPlayer(player);
    }

    private void moveFalcon(Player player, LivingEntity falcon, int tick) {
        if (falcon == null || !falcon.isValid())
            return;

        double angle = Math.toRadians(tick * 18);
        double radius = 2.6 + Math.sin(tick / 6.0) * 0.35;
        Location loc = player.getLocation().clone().add(Math.cos(angle) * radius, 2.6 + Math.sin(tick / 5.0) * 0.4, Math.sin(angle) * radius);
        loc.setDirection(player.getLocation().toVector().subtract(loc.toVector()));
        falcon.teleport(loc);

        if (tick % 4 == 0) {
            UtilParticle.PlayParticle(ParticleType.CLOUD, loc.clone().add(0, 0.1, 0), 0.08f, 0.04f, 0.08f, 0.01f, 2, ViewDist.NORMAL, UtilServer.getPlayers());
        }
    }

    private int scanForHiders(HideSeek game, Player player, java.util.Set<java.util.UUID> alreadyRevealed) {
        int found = 0;
        double radius = game.getFalconerRevealRadius();

        for (Player hider : game.getHiders().GetPlayers(true)) {
            if (alreadyRevealed.contains(hider.getUniqueId()))
                continue;

            if (UtilMath.offset(player, hider) > radius)
                continue;

            alreadyRevealed.add(hider.getUniqueId());
            game.revealHider(hider, game.getRevealDurationTicks(80), player);
            UtilParticle.PlayParticle(ParticleType.END_ROD, hider.getLocation().clone().add(0, 1.2, 0), 0.35f, 0.45f, 0.35f, 0.02f, 12, ViewDist.NORMAL, UtilServer.getPlayers());
            hider.playSound(hider.getLocation(), Sound.ENTITY_PARROT_AMBIENT, 0.8f, 1.5f);
            found++;
        }

        return found;
    }
}
