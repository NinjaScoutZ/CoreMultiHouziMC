package com.houzicore.arcade.nautilus.game.arcade.game.games.snowfight.kits;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.houzicore.shared.common.util.UtilMath;
import com.houzicore.shared.core.itemstack.ItemStackFactory;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;
import com.houzicore.arcade.nautilus.game.arcade.kit.KitAvailability;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.PerkFallDamage;

public class KitSportsman extends Kit
{
	
	public KitSportsman(ArcadeManager manager)
	{
		super(manager, "Sportsman", KitAvailability.Free, 

				// EN
				new String[] 
						{
				"Trained to be the fastest on snow and ice.",
				"",
				"Gets 1 Snowball every tile",
				"Left-Click Snow to pick up Snowballs (Max. 16)",
				"Right-Click Snowballs to throw them.",
				"",
				"Supports all nearby allies with SPEED."
						}, 
				// TH
				new String[] 
						{
				"[TH] Trained to be the fastest on snow and ice.",
				"",
				"[TH] Gets 1 Snowball every tile",
				"[TH] Left-Click Snow to pick up Snowballs (Max. 16)",
				"[TH] Right-Click Snowballs to throw them.",
				"",
				"[TH] Supports all nearby allies with SPEED."
						}, 
				new Perk[] 
								{
							new PerkFallDamage(3)
								}, 
								EntityType.SKELETON,
								new ItemStack(Material.SNOWBALL));

	}

	@Override
	public void GiveItems(Player player)
	{
		player.getInventory().setItem(2, ItemStackFactory.Instance.CreateStack(Material.MUSHROOM_STEW));
		player.getInventory().setItem(3, ItemStackFactory.Instance.CreateStack(Material.MUSHROOM_STEW));
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
				
				if( Manager.GetGame().GetTeam(player).equals(Manager.GetGame().GetTeam(other))) {
					Manager.GetCondition().Factory().Speed("Aura", other, player, 1.9, 0, false, false, false);
				}
			}
		}
	}
	
}
