package com.houzicore.arcade.nautilus.game.arcade.kit.perks;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;
import com.houzicore.shared.common.util.UtilMath;

public class PerkChemist extends Perk
{
	public PerkChemist() 
	{
		super("Chemist", new String[] 
		{ 
			"Gain a random splash potion when you kill a player."
		});
	}

	@EventHandler
	public void onKill(PlayerDeathEvent event)
	{
		Player killed = event.getEntity();
		if (killed.getKiller() == null)
			return;

		Player killer = killed.getKiller();
		
		if (!Kit.HasKit(killer))
			return;
			
		if (!Manager.GetGame().IsLive())
			return;

		ItemStack potion = new ItemStack(Material.SPLASH_POTION);
		PotionMeta pMeta = (PotionMeta) potion.getItemMeta();
		
		int rand = UtilMath.r(3);
		if (rand == 0)
			pMeta.setBasePotionType(PotionType.POISON);
		else if (rand == 1)
			pMeta.setBasePotionType(PotionType.SLOWNESS);
		else
			pMeta.setBasePotionType(PotionType.WEAKNESS);
			
		potion.setItemMeta(pMeta);
		
		killer.getInventory().addItem(potion);
	}
}
