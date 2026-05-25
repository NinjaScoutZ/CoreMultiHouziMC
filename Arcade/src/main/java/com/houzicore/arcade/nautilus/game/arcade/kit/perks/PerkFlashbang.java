package com.houzicore.arcade.nautilus.game.arcade.kit.perks;

import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.bukkit.metadata.FixedMetadataValue;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilParticle;
import com.houzicore.shared.common.util.UtilParticle.ParticleType;
import com.houzicore.shared.common.util.UtilParticle.ViewDist;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.recharge.Recharge;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;

public class PerkFlashbang extends Perk
{
	public PerkFlashbang() 
	{
		super("Smoke Bomb", new String[] 
		{ 
			C.cYellow + "Right-Click" + C.cGray + " Gunpowder to throw a",
			C.cGreen + "Smoke Bomb" + C.cGray + " that blinds and slows Hiders in a 4-block radius." 
		});
	}

	@EventHandler
	public void onThrow(PlayerInteractEvent event)
	{
		if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;

		Player player = event.getPlayer();

		if (!Kit.HasKit(player))
			return;

		if (player.getInventory().getItemInMainHand() == null || player.getInventory().getItemInMainHand().getType() != Material.GUNPOWDER)
			return;

		event.setCancelled(true);

		if (!Recharge.Instance.use(player, GetName(), 8000, true, true))
			return;

		// Deduct Item
		if (player.getInventory().getItemInMainHand().getAmount() > 1) {
			player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);
		} else {
			player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
		}

		// Throw projectile
		Snowball snowball = player.launchProjectile(Snowball.class);
		snowball.setItem(new ItemStack(Material.GUNPOWDER));
		snowball.setMetadata("Flashbang", new FixedMetadataValue(Manager.getPlugin(), player.getUniqueId().toString()));
		
		Vector velocity = player.getLocation().getDirection().multiply(1.5);
		snowball.setVelocity(velocity);

		UtilPlayer.message(player, F.main("Skill", "You threw " + F.skill(GetName()) + "."));
	}

	@EventHandler
	public void onHit(ProjectileHitEvent event)
	{
		if (!(event.getEntity() instanceof Snowball))
			return;

		Snowball snowball = (Snowball) event.getEntity();
		if (!snowball.hasMetadata("Flashbang"))
			return;

		// Blast Effect
		UtilParticle.PlayParticle(ParticleType.FIREWORKS_SPARK, snowball.getLocation(), 0.5f, 0.5f, 0.5f, 0.2f, 150, ViewDist.MAX, UtilServer.getPlayers());
		snowball.getWorld().playSound(snowball.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 2f, 0.5f);
		snowball.getWorld().playSound(snowball.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1f, 2f);

		// Blindness Application
		for (Entity other : snowball.getNearbyEntities(4, 4, 4))
		{
			if (!(other instanceof LivingEntity))
				continue;

			if (other instanceof Player)
			{
				Player target = (Player) other;
				if (Manager.isSpectator(target))
					continue;
				
				// Apply effect
				target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 0)); // 3 seconds
				target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 1)); 
				
				target.playSound(target.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 2f, 0.5f);
				com.houzicore.shared.common.util.UtilTextMiddle.display(C.cWhite + C.Bold + "BLINDED", "You were hit by a Smoke Bomb!", 5, 20, 5, target);
			}
		}
	}
}
