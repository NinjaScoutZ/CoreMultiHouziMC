package com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.shop.trap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.UtilParticle;
import com.houzicore.shared.common.util.UtilParticle.ParticleType;
import com.houzicore.shared.common.util.UtilParticle.ViewDist;

public class BedwarsBearTrap extends BedwarsTrapItem
{

	public BedwarsBearTrap(int cost)
	{
		super(new ItemStack(Material.TRIPWIRE_HOOK), cost, "Bear Trap",
				C.cWhite + "When a player gets near your Bed.",
				C.cWhite + "They are blinded and slowed for " + C.cGreen + "4 Seconds" + C.cWhite + "."
		);

		_trapTrigger = TrapTrigger.BED_NEAR;
	}

	@Override
	public void onTrapTrigger(Player player, Location bed)
	{
		player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 2, 1);
		player.getWorld().spawnParticle(org.bukkit.Particle.WITCH, player.getLocation().add(0, 1.5, 0), 30, 0.25, 0.25, 0.25, 0.5);
		player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 80, 4));
		player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 80, 0, false, false));
	}
}
