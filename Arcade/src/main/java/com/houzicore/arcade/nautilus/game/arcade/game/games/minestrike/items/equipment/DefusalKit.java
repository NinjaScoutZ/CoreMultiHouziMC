package com.houzicore.arcade.nautilus.game.arcade.game.games.minestrike.items.equipment;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.arcade.nautilus.game.arcade.game.games.minestrike.MineStrike;
import com.houzicore.arcade.nautilus.game.arcade.game.games.minestrike.items.StrikeItem;
import com.houzicore.arcade.nautilus.game.arcade.game.games.minestrike.items.StrikeItemType;
import com.houzicore.shared.core.lang.LangManager;


public class DefusalKit extends StrikeItem
{
	public DefusalKit()
	{
		super(StrikeItemType.EQUIPMENT, "Defusal Kit",  new String[] 
				{
				"Halves the time it takes to defuse."
				},
				400, 0, Material.SHEARS);
	}

	@Override
	public boolean pickup(MineStrike game, Player player)
	{
		return false;
	}
	
	public void giveToPlayer(Player player, int slot)
	{
		fixStackName();
		
		player.getInventory().setItem(slot, getStack());
		
		UtilPlayer.message(player, F.main("Game", LangManager.get().isThai(player) ? "\u00A77\u0e04\u0e38\u0e13\u0e2a\u0e27\u0e21\u0e43\u0e2a\u0e48 " + getName() + "." : "\u00A77You equipped " + getName() + "."));
		
		player.getWorld().playSound(player.getLocation(), Sound.ENTITY_HORSE_ARMOR, 1.5f, 1f);
	}
	
	@Override
	public String getShopItemType()
	{
		return C.cDGreen + C.Bold + "Equipment" + ChatColor.RESET;
	}
}
