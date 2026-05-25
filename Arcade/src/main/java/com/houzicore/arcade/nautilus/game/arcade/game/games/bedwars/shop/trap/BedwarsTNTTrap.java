package com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.shop.trap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.UtilAction;
import com.houzicore.shared.common.util.UtilAlg;
import com.houzicore.shared.common.util.UtilParticle;
import com.houzicore.shared.common.util.UtilParticle.ParticleType;
import com.houzicore.shared.common.util.UtilParticle.ViewDist;

public class BedwarsTNTTrap extends BedwarsTrapItem
{

	public BedwarsTNTTrap(int cost)
	{
		super(new ItemStack(Material.TNT), cost, "TNT Trap",
				C.cWhite + "When a player attempts to eat your Bed.",
				C.cWhite + "They are thrown into the air!"
		);
	}

	@Override
	public void onTrapTrigger(Player player, Location bed)
	{
		player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 2, 0.6F);
		player.getWorld().spawnParticle(org.bukkit.Particle.EXPLOSION_EMITTER, player.getLocation().add(0, 1.5, 0), 1, 0.0, 0.0, 0.0, 1.0);

		Vector direction = UtilAlg.getTrajectory2d(bed, player.getLocation());
		direction.multiply(1.5);
		direction.setY(1.3 + (Math.random() / 2D));

		player.setVelocity(direction);
	}
}
