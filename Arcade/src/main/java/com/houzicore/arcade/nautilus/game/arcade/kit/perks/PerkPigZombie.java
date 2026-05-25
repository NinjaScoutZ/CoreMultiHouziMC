package com.houzicore.arcade.nautilus.game.arcade.kit.perks;

import java.util.HashSet;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import com.houzicore.shared.api.disguise.DisguiseArchetype;
import com.houzicore.shared.api.disguise.DisguiseRequest;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.core.itemstack.ItemStackFactory;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.arcade.nautilus.game.arcade.kit.SmashPerk;

public class PerkPigZombie extends SmashPerk
{
	public HashSet<Player> _active = new HashSet<Player>();
	
	public PerkPigZombie() 
	{
		super("Nether Pig", new String[] 
				{ 
				C.cGray + "Become Nether Pig when HP is below 6.",
				C.cGray + "Return to Pig when HP is 10 or higher."
				});
	}
	
	@EventHandler
	public void Check(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTER)
			return;
		
		for (Player player : Manager.GetGame().GetPlayers(true))
		{
			if (!Kit.HasKit(player))
				continue;
			
			//Active
			if (_active.contains(player))
			{
				Manager.GetCondition().Factory().Speed("Pig Zombie", player, player, 0.9, 0, false, false, false);
				
				if (player.getHealth() < 10 || isSuperActive(player))
					continue;
				
				//Deactivate
				_active.remove(player);
				
				//Armor
				player.getInventory().setHelmet(null);
				player.getInventory().setChestplate(ItemStackFactory.Instance.CreateStack(Material.CHAINMAIL_CHESTPLATE));
				player.getInventory().setLeggings(ItemStackFactory.Instance.CreateStack(Material.CHAINMAIL_LEGGINGS));
				player.getInventory().setBoots(ItemStackFactory.Instance.CreateStack(Material.CHAINMAIL_BOOTS));
				
				player.getInventory().remove(Material.IRON_HELMET);
				player.getInventory().remove(Material.IRON_CHESTPLATE);
				player.getInventory().remove(Material.IRON_LEGGINGS);
				player.getInventory().remove(Material.IRON_BOOTS);
				
				//Disguise
				applyMobDisguise(player, "PIG");
				
				//Sound
				player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PIG_AMBIENT, 2f, 1f);
				player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PIG_AMBIENT, 2f, 1f);
				
				//Inform
				UtilPlayer.message(player, F.main("Skill", "You returned to " + F.skill("Pig Form") + "."));
			}
			//Not Active
			else
			{ { }
				if (player.getHealth() <= 0 || (!isSuperActive(player) && player.getHealth() > 6))
					continue;
				
				//Activate
				_active.add(player);
				
				//Armor
				player.getInventory().setHelmet(new ItemStack(Material.IRON_HELMET));
				player.getInventory().setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
				player.getInventory().setLeggings(new ItemStack(Material.IRON_LEGGINGS));
				player.getInventory().setBoots(new ItemStack(Material.IRON_BOOTS));
				
				//Disguise
				applyMobDisguise(player, "ZOMBIFIED_PIGLIN");
				
				//Sound
				player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ZOMBIFIED_PIGLIN_ANGRY, 2f, 1f);
				player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ZOMBIFIED_PIGLIN_ANGRY, 2f, 1f);
				
				//Inform
				UtilPlayer.message(player, F.main("Skill", "You transformed into " + F.skill("Nether Pig Form") + "."));
				
				player.setExp(0.99f);
			}
		}
	}
	
	@EventHandler
	public void Clean(PlayerDeathEvent event)
	{
		_active.remove(event.getEntity());
	}

	private void applyMobDisguise(Player player, String variantKey)
	{
		Manager.GetDisguise().getService().apply(player, new DisguiseRequest(
				player.getUniqueId(),
				DisguiseArchetype.MOB,
				variantKey,
				true,
				false,
				false,
				getTeamName(player),
				true));
	}

	private String getTeamName(Player player)
	{
		if (Manager.GetGame().GetTeam(player) != null)
			return Manager.GetGame().GetTeam(player).GetColor() + player.getName();

		return player.getName();
	}
}
