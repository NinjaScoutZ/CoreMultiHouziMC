package com.houzicore.arcade.nautilus.game.arcade.game.games.minestrike.kits;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.core.itemstack.ItemStackFactory;
import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;
import com.houzicore.arcade.nautilus.game.arcade.kit.KitAvailability;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;

public class KitPlayer extends Kit 
{
	public KitPlayer(ArcadeManager manager)
	{
		super(manager, "Player", KitAvailability.Free, 
				// EN
				new String[] 
				{
				C.cGreen + "Right-Click" + C.cWhite + " - " + C.cYellow + "Fire Gun",
				C.cGreen + "Left-Click" + C.cWhite + " - " + C.cYellow + "Reload Gun",
				C.cGreen + "Crouch" + C.cWhite + " - " + C.cYellow + "Sniper Scope",
				"",
				C.cGreen + "Hold Right-Click with Bomb" + C.cWhite + " - " + C.cRed + "Plant Bomb",
				C.cGreen + "Hold Right-Click with Knife" + C.cWhite + " - " + C.cAqua + "Defuse Bomb",
				}, 
				// TH
				new String[] 
				{
				C.cGreen + "[TH] Right-Click" + C.cWhite + "[TH]  - " + C.cYellow + "[TH] Fire Gun",
				C.cGreen + "[TH] Left-Click" + C.cWhite + "[TH]  - " + C.cYellow + "[TH] Reload Gun",
				C.cGreen + "[TH] Crouch" + C.cWhite + "[TH]  - " + C.cYellow + "[TH] Sniper Scope",
				"",
				C.cGreen + "[TH] Hold Right-Click with Bomb" + C.cWhite + "[TH]  - " + C.cRed + "[TH] Plant Bomb",
				C.cGreen + "[TH] Hold Right-Click with Knife" + C.cWhite + "[TH]  - " + C.cAqua + "[TH] Defuse Bomb",
				}, 
				new Perk[] 
				{
				
				}, 
				EntityType.PLAYER,	
				new ItemStack(Material.AIR));
 
	}

	@Override
	public void GiveItems(Player player)
	{
		
	}
	
	@Override
	public void SpawnCustom(LivingEntity ent) 
	{
		
	}
}
