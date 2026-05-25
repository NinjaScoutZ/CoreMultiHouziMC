package com.houzicore.lobby.hub.modules;

import java.util.ArrayList;
import java.util.HashMap;

import com.houzicore.shared.MiniPlugin;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.lobby.hub.HubManager;
import com.houzicore.lobby.hub.commands.HorseSpawn;

import org.bukkit.Material;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Donkey;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Mule;
import org.bukkit.entity.Player;
import org.bukkit.entity.SkeletonHorse;
import org.bukkit.entity.ZombieHorse;
import org.bukkit.entity.Horse.Color;
import org.bukkit.entity.Horse.Style;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public class AdminMountManager extends MiniPlugin
{
	private HubManager Manager;
	
	private HashMap<Player, AbstractHorse> _mounts = new HashMap<Player, AbstractHorse>();

	public AdminMountManager(HubManager manager)
	{
		super("Mount Manager", manager.getPlugin());
		
		Manager = manager;
	}

	@Override
	public void addCommands()
	{
		addCommand(new HorseSpawn(this));
	} 

	@EventHandler
	public void HorseInteract(PlayerInteractEntityEvent event)
	{
		if (!(event.getRightClicked() instanceof AbstractHorse))
			return;

		Player player = event.getPlayer();
		AbstractHorse horse = (AbstractHorse)event.getRightClicked();

		//Not Owner
		if (!_mounts.containsKey(player) || !_mounts.get(player).equals(horse))
		{
			UtilPlayer.message(player, F.main("Mount", "This is not your mount!"));
			event.setCancelled(true);
			return;
		}
	}

	@EventHandler
	public void PlayerQuit(PlayerQuitEvent event)
	{
		AbstractHorse horse = _mounts.remove(event.getPlayer());
		if (horse != null)
			horse.remove();
	}
	
	public void HorseCommand(Player caller, String[] args) 
	{
		if (args == null || args.length == 0)
		{
			UtilPlayer.message(caller, F.main("Mount", "Mount Commands;"));
			UtilPlayer.message(caller, "spawn / kill / leash / unleash");
			UtilPlayer.message(caller, "age / color / style / armor");
			UtilPlayer.message(caller, "Types: horse, donkey, mule, skeleton, zombie");
			return;
		}

		if (args[0].equalsIgnoreCase("spawn"))
		{
			String type = "horse";
			if (args.length > 1)
			{
				type = args[1].toLowerCase();
			}

			Spawn(caller, type);
			return;
		}

		AbstractHorse horse = _mounts.get(caller);
		if (horse == null)
		{
			UtilPlayer.message(caller, F.main("Mount", "You do not have a mount."));
			return;
		}

		//Leash
		else if (args[0].equalsIgnoreCase("leash"))
		{
			horse.setLeashHolder(caller);
		}

		//UnLeash
		else if (args[0].equalsIgnoreCase("unleash"))
		{
			horse.setLeashHolder(null);
		}

		//Kill
		else if (args[0].equalsIgnoreCase("kill"))
		{
			horse.remove();
			_mounts.remove(caller);
		}

		//Age
		else if (args[0].equalsIgnoreCase("age"))
		{
			if (args.length >= 2)
			{
				try
				{
					if (args[1].equalsIgnoreCase("adult"))
					{
						horse.setAdult();
					}
					else if (args[1].equalsIgnoreCase("baby"))
					{
						horse.setBaby();
					}
					return;
				}
				catch (Exception e)
				{

				}
			}

			UtilPlayer.message(caller, F.main("Mount", F.value("Age", "baby adult")));
		}

		//Color (only for regular Horse)
		else if (args[0].equalsIgnoreCase("color"))
		{
			if (horse instanceof Horse && args.length >= 2)
			{
				Color color = GetColor(caller, args[1]);
				if (color != null)
					((Horse) horse).setColor(color);
			}
		}

		//Style (only for regular Horse)
		else if (args[0].equalsIgnoreCase("style"))
		{
			if (horse instanceof Horse && args.length >= 2)
			{
				Style style = GetStyle(caller, args[1]);
				if (style != null)
					((Horse) horse).setStyle(style);
			}
		}

		//Armor (only for regular Horse)
		else if (args[0].equalsIgnoreCase("armor"))
		{
			if (horse instanceof Horse)
			{
				if (args.length >= 2)
				{
					try
					{
						if (args[1].equalsIgnoreCase("iron"))
						{
							((Horse) horse).getInventory().setArmor(new ItemStack(Material.IRON_HORSE_ARMOR));
							return;
						}
						if (args[1].equalsIgnoreCase("gold"))
						{
							((Horse) horse).getInventory().setArmor(new ItemStack(Material.GOLDEN_HORSE_ARMOR));
							return;
						}
						if (args[1].equalsIgnoreCase("diamond"))
						{
							((Horse) horse).getInventory().setArmor(new ItemStack(Material.DIAMOND_HORSE_ARMOR));
							return;
						}
					}
					catch (Exception e)
					{

					}
				}

				UtilPlayer.message(caller, F.main("Mount", F.value("Armor", "iron gold diamond")));
			}
		}
	}

	public Style GetStyle(Player caller, String arg)
	{
		ArrayList<Style> match = new ArrayList<Style>();

		for (Style var : Style.values())
		{
			if (var.name().equals(arg.toUpperCase()))
				return var;

			if (var.name().contains(arg.toUpperCase()))
				match.add(var);
		}

		if (match.size() == 1)
			return match.get(0);

		String valids = "";
		for (Style valid : Style.values())
			valids += valid.name() + " ";
		UtilPlayer.message(caller, F.main("Mount", F.value("Styles", valids)));

		return null;
	}

	public Color GetColor(Player caller, String arg)
	{
		ArrayList<Color> match = new ArrayList<Color>();

		for (Color var : Color.values())
		{
			if (var.name().equals(arg.toUpperCase()))
				return var;

			if (var.name().contains(arg.toUpperCase()))
				match.add(var);
		}

		if (match.size() == 1)
			return match.get(0);

		String valids = "";
		for (Color valid : Color.values())
			valids += valid.name() + " ";
		UtilPlayer.message(caller, F.main("Mount", F.value("Colors", valids)));

		return null;
	}

	public AbstractHorse Spawn(Player caller, String type) 
	{
		AbstractHorse old = _mounts.remove(caller);
		if (old != null) old.remove();

		AbstractHorse horse;
		switch (type.toLowerCase())
		{
			case "donkey":
				horse = caller.getWorld().spawn(caller.getLocation(), Donkey.class);
				break;
			case "mule":
				horse = caller.getWorld().spawn(caller.getLocation(), Mule.class);
				break;
			case "skeleton":
				horse = caller.getWorld().spawn(caller.getLocation(), SkeletonHorse.class);
				break;
			case "zombie":
				horse = caller.getWorld().spawn(caller.getLocation(), ZombieHorse.class);
				break;
			default:
				horse = caller.getWorld().spawn(caller.getLocation(), Horse.class);
				if (horse instanceof Horse)
				{
					((Horse) horse).setColor(Color.DARK_BROWN);
					((Horse) horse).setStyle(Style.WHITE_DOTS);
				}
				break;
		}

		horse.setAdult();
		horse.setAgeLock(true);
		horse.setOwner(caller);
		horse.setMaxDomestication(1);
		horse.setJumpStrength(1);
		horse.getInventory().setItem(0, new ItemStack(Material.SADDLE));

		horse.setCustomName(caller.getName() + "'s Mount");
		horse.setCustomNameVisible(true);

		_mounts.put(caller, horse);
		
		return horse;
	}
	
	@EventHandler
	public void LeashSpawn(ItemSpawnEvent event)
	{
		if (event.getEntity().getItemStack().getType() == Material.LEAD)
			event.setCancelled(true);
	}
}
