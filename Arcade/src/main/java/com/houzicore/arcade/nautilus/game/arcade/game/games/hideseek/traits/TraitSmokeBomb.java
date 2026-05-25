package com.houzicore.arcade.nautilus.game.arcade.game.games.hideseek.traits;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.houzicore.arcade.nautilus.game.arcade.game.games.hideseek.HideSeek;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;
import com.houzicore.shared.common.util.UtilEvent;
import com.houzicore.shared.common.util.UtilEvent.ActionType;
import com.houzicore.shared.common.util.UtilMath;
import com.houzicore.shared.common.util.UtilParticle;
import com.houzicore.shared.common.util.UtilParticle.ParticleType;
import com.houzicore.shared.common.util.UtilParticle.ViewDist;
import com.houzicore.shared.common.util.UtilServer;

public class TraitSmokeBomb extends Perk {

    public TraitSmokeBomb() {
        super("Smoke Bomb", new String[]{
                "Right-click Gunpowder to throw a Smoke Bomb.",
                "Explodes on impact, blinding and slowing",
                "nearby Hiders to set up a clean engage."
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
        if (item == null || item.getType() != Material.GUNPOWDER) return;

        event.setCancelled(true);

        if (!game.useVanillaItemCooldown(player, "Smoke Bomb", game.getAbilityCooldown(player, 20000, true)))
            return;

        player.playSound(player.getLocation(), Sound.ENTITY_SNOW_GOLEM_SHOOT, 0.5f, 0.5f);

        org.bukkit.entity.Snowball smokeBomb = player.launchProjectile(org.bukkit.entity.Snowball.class);
        smokeBomb.setCustomName("SmokeBombProjectile");
        smokeBomb.setItem(new ItemStack(Material.FIREWORK_STAR));
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (Manager.GetGame() == null || !(Manager.GetGame() instanceof HideSeek)) return;
        HideSeek game = (HideSeek) Manager.GetGame();

        if (!(event.getEntity() instanceof org.bukkit.entity.Snowball)) return;
        if (!"SmokeBombProjectile".equals(event.getEntity().getCustomName())) return;

        Location loc = event.getEntity().getLocation();
        loc.getWorld().playSound(loc, Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 1f, 1.5f);
        UtilParticle.PlayParticle(ParticleType.CAMPFIRE_COSY_SMOKE, loc, 4f, 4f, 4f, 0.05f, 200, ViewDist.MAX, UtilServer.getPlayers());

        if (event.getEntity().getShooter() instanceof Player) {
            Player shooter = (Player) event.getEntity().getShooter();
            
            if (game.getSeekers().HasPlayer(shooter) && Kit.HasKit(shooter)) {
                for (Player hider : game.getHiders().GetPlayers(true)) {
                    if (UtilMath.offset(loc, hider.getLocation()) <= 6.0) {
                        hider.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 0));
                        hider.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 0));
                        game.sendPropRushMessageKey(hider, "prop_rush.feedback.smoke_bomb_blinded");
                        hider.playSound(hider.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1f, 1f);
                    }
                }
            }
        }
    }
}
