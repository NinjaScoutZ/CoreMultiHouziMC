package com.houzicore.arcade.nautilus.game.arcade.game.games.snowfight.kits;

import com.houzicore.shared.common.util.UtilInv;
import com.houzicore.shared.common.util.UtilMath;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.core.itemstack.ItemStackFactory;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
//import com.houzicore.shared.condition.Condition.ConditionType;
import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;
import com.houzicore.arcade.nautilus.game.arcade.kit.KitAvailability;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.PerkFallDamage;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionType;

public class KitTactician extends Kit
{

	public KitTactician(ArcadeManager manager)
	{
		super(manager, "Tactician", KitAvailability.Free, 
					// EN
				new String[] 
						{
						"No Snowfight is complete without a tactical game!",
						" ",
						"Gets 1 Snowball every second tile.",
						"Left-Click Snow to pick up Snowballs (Max. 16)",
						"Right-Click Snowballs to throw them.",
						" ",
						"Gets 1 Barrier every 32 seconds [max. 2]",
						"Place Barriers to improve your defense.",
						"You cant place Barriers above Ice, Packed Ice or Fences.",
						" ",
						"Supports all nearby allies with RESISTANCE."
								}, 
				// TH
				new String[] 
						{
						"[TH] No Snowfight is complete without a tactical game!",
						"[TH]  ",
						"[TH] Gets 1 Snowball every second tile.",
						"[TH] Left-Click Snow to pick up Snowballs (Max. 16)",
						"[TH] Right-Click Snowballs to throw them.",
						"[TH]  ",
						"[TH] Gets 1 Barrier every 32 seconds [max. 2]",
						"[TH] Place Barriers to improve your defense.",
						"[TH] You cant place Barriers above Ice, Packed Ice or Fences.",
						"[TH]  ",
						"[TH] Supports all nearby allies with RESISTANCE."
								}, 
				new Perk[] 
								{
								}, 
								EntityType.SKELETON,
								new ItemStack(Material.OAK_FENCE));

	}

	@Override
	public void GiveItems(Player player)
	{
		player.getInventory().setItem(2, ItemStackFactory.Instance.CreateStack(Material.MUSHROOM_STEW));
		player.getInventory().setItem(3, ItemStackFactory.Instance.CreateStack(Material.MUSHROOM_STEW));
		player.getInventory().setItem(7, ItemStackFactory.Instance.CreateStack(Material.COMPASS.ordinal(), (byte) 0, 1, "§a§lTracking Compass"));
	}
	
	@Override
	public void SpawnCustom(LivingEntity ent) 
	{
		ent.getEquipment().setHelmet(new ItemStack(Material.LEATHER_HELMET));
		ent.getEquipment().setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
		ent.getEquipment().setLeggings(new ItemStack(Material.LEATHER_LEGGINGS));
		ent.getEquipment().setBoots(new ItemStack(Material.LEATHER_BOOTS));
	}
	
	@EventHandler
	public void Aura(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;
		 
		for (Player player : Manager.GetGame().GetPlayers(true))
		{
			if (!HasKit(player))
				continue;
			
			for (Player other : Manager.GetGame().GetPlayers(true))
			{
				if (other.equals(player))
					continue;
				
				if (UtilMath.offset(player, other) > 4)
					continue;
				
//				if( Manager.GetGame().GetTeam(player).equals(Manager.GetGame().GetTeam(other)))
//					Manager.GetCondition().Factory().Protection("Aura", other, player, 1.9, 0, false, false, false);
			}
		}
	}
	
	@EventHandler
	public void KitItems(UpdateEvent event)
	{
		if(!Manager.GetGame().IsLive())
			return;
		
		if (event.getType() == UpdateType.SLOWEST)
		{
			for (Player player : Manager.GetGame().GetPlayers(true))
			{
				if (!HasKit(player))
					continue;
				
				int amount = 0;
				if (player.getInventory().getItem(1) != null && UtilInv.contains(player, "", Material.OAK_FENCE, (byte) 0, 1))
					amount = 2;
				else 
					amount = 1;
				player.getInventory().setItem(1, ItemStackFactory.Instance.CreateStack(Material.OAK_FENCE, (byte) 0, amount,  "Barrier"));
			}
		}
	}

}
