package com.houzicore.arcade.nautilus.game.arcade.game.games.mineware.challenges;

import java.util.ArrayList;
import java.util.HashMap;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.UtilMath;
import com.houzicore.shared.common.util.UtilPlayer;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import com.houzicore.arcade.nautilus.game.arcade.game.games.mineware.Challenge;
import com.houzicore.arcade.nautilus.game.arcade.game.games.mineware.MineWare;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

public class ChallengeMilkACow extends Challenge
{
	private Villager _villager;
	private HashMap<Entity, ArrayList<String>> _milked = new HashMap<Entity, ArrayList<String>>();
	private HashMap<String, Integer> _score = new HashMap<String, Integer>();

	public ChallengeMilkACow(MineWare host)
	{
		super(host, ChallengeType.FirstComplete, "Milk 5 different cows and deliver to the villager!");
	}

	@Override
	public void cleanupRoom()
	{
		for (Entity cow : _milked.keySet())
		{
			cow.remove();
		}

		_villager.remove();
	}

	@EventHandler
	public void Damage(EntityDamageByEntityEvent event)
	{
		if (event.getDamager() == null)
			return;

		if (!(event.getDamager() instanceof Player)) return;
		Player player = ((Player) event.getDamager());

		if (player == null)
			return;

		LivingEntity ent = (LivingEntity)event.getEntity();

		if (!(ent instanceof Player))
		{
			event.setCancelled(true);
		}
	}

	@Override
	public void setupPlayers()
	{
		setBorder(-16, 16, 0, 10, -16, 16);

		for (Player player : Host.GetPlayers(true))
		{
			com.houzicore.shared.api.disguise.DisguiseRequest request = new com.houzicore.shared.api.disguise.DisguiseRequest(
			player.getUniqueId(),
			com.houzicore.shared.api.disguise.DisguiseArchetype.MOB,
			"VILLAGER",
			true,
			false,
			false
		);
			// disguise.setBaby(); // not migrated
			Host.getArcadeManager().GetDisguise().getService().apply(player, request);
			player.getInventory().setItem(0, new ItemStack(Material.BUCKET));
			_score.put(player.getName(), 0);
		}
	}

	@Override
	public void generateRoom()
	{
		for (int x = -16; x <= 16; x++)
		{
			for (int z = -16; z <= 16; z++)
			{
				for (int y = 0; y <= 1; y++)
				{
					Block b = getCenter().getBlock().getRelative(x, y, z);

					if (y == 0)
					{
						b.setType(Material.GRASS_BLOCK);
					}
					else
					{
						if (Math.abs(x) == 16 || Math.abs(z) == 16)
						{
							b.setType(Material.OAK_FENCE);
						}
						else if (UtilMath.r(4) == 0)
						{
							if (UtilMath.r(8) == 0)
							{
								b.setType(UtilMath.random.nextBoolean() ? Material.DANDELION : Material.POPPY);
							}
							else
							{
								b.setType(Material.SHORT_GRASS);
								// b.setData((byte) 1); // Block.setData removed in 1.21
							}
						}
					}

					if (b.getType() != Material.AIR)
					{
						addBlock(b);
					}
				}
			}
		}

		Host.CreatureAllowOverride = true;
		_villager = (Villager) getCenter().getWorld().spawnEntity(getCenter().clone().add(0, 1, 0), EntityType.VILLAGER);
		_villager.setProfession(Profession.FARMER);
		_villager.setCustomName(C.Bold + "Farmer Joe");
		_villager.setCustomNameVisible(true);
		
		String[] names = new String[]
			{
					"Tom", "Steve", "John", "Harry", "Andrew", "Daniel", "Jorge", "Jim"
			};

		if (UtilMath.r(5) == 0)
		{
			names = new String[]
				{
						"Moosworth", "Mooington", "Mooley", "Moose", "Mooskee", "Chicken", "Mooffy", "Moozzle"
				};
		}

		for (int i = 0; i < 8; i++)
		{
			Location loc = getCenter().clone().add(UtilMath.r(26) - 13, 1, UtilMath.r(26) - 13);
			Cow cow = (Cow) loc.getWorld().spawnEntity(loc, EntityType.COW);

			cow.setCustomName(C.cWhite + names[i]);
			cow.setCustomNameVisible(true);

			_milked.put(cow, new ArrayList<String>());
		}

		Host.CreatureAllowOverride = false;
	}

	@EventHandler
	public void onEntityInteract(PlayerInteractEntityEvent event)
	{
		Player p = event.getPlayer();

		if (UtilPlayer.isSpectator(p))
		{
			return;
		}

		if (!Host.IsLive())
		{
			return;
		}

		ItemStack item = p.getItemInHand();

		if (item == null)
			return;

		Entity ent = event.getRightClicked();

		if (_milked.containsKey(ent))
		{
			if (item.getType() == Material.BUCKET)
			{
				if (!_milked.get(ent).contains(p.getName()))
				{
					p.setItemInHand(new ItemStack(Material.MILK_BUCKET));
					_milked.get(ent).add(p.getName());
				}
				else
				{
					p.updateInventory();
				}
			}

			event.setCancelled(true);
		}
		else if (ent == _villager)
		{
			if (item.getType() == Material.MILK_BUCKET)
			{
				p.setItemInHand(new ItemStack(Material.BUCKET));
				p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 2, 0);
				int score = _score.get(p.getName()) + 1;
				_score.put(p.getName(), score);

				displayCount(p, ((Villager) ent).getEyeLocation().add(0, 0.3, 0), (score >= 5 ? C.cDGreen : score >= 3 ? C.cGreen
						: score >= 2 ? C.cRed : C.cDRed) + score);

				if (score == 5)
				{
					SetCompleted(p);
				}
			}

			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onConsume(PlayerItemConsumeEvent event)
	{
		event.setCancelled(true);
		event.getPlayer().updateInventory();
	}

	@Override
	public ArrayList<Location> getSpawns()
	{
		ArrayList<Location> spawns = new ArrayList<Location>();

		for (int x = -13; x <= 13; x++)
		{
			for (int z = -13; z <= 13; z++)
			{
				if (x % 2 == 0 && z % 2 == 0)
				{
					spawns.add(getCenter().clone().add(x + 0.5, 1.1, z + 0.5));
				}
			}
		}

		return spawns;
	}
}
