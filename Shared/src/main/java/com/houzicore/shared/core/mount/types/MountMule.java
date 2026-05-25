package com.houzicore.shared.core.mount.types;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Mule;

import com.houzicore.shared.core.mount.HorseMount;
import com.houzicore.shared.core.mount.MountManager;

public class MountMule extends HorseMount {

	@Override
	public void EnableCustom(org.bukkit.entity.Player player) {
		Mule horse = player.getWorld().spawn(player.getLocation(), Mule.class);
		horse.setTamed(true);
		horse.getInventory().setSaddle(new org.bukkit.inventory.ItemStack(Material.SADDLE));
		horse.setMaxHealth(40.0);
		horse.setHealth(40.0);
		horse.setJumpStrength(0.8);
		
		com.houzicore.shared.common.util.UtilEnt.Vegetate(horse);
		horse.addPassenger(player);
		
		_active.put(player, horse);
	}


	public MountMule(MountManager manager) {
		super(manager, "Mount Mule", new String[] { ChatColor.RESET + "Muley muley!" }, Material.HAY_BLOCK, (byte) 0,
				3000, null, null, null, 1.0, null);
	}
}
