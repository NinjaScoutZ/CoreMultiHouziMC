package com.houzicore.arcade.nautilus.game.arcade.game.games.mineware.challenges;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.UtilAlg;
import com.houzicore.shared.common.util.UtilMath;
import com.houzicore.shared.core.itemstack.ItemBuilder;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import com.houzicore.arcade.nautilus.game.arcade.game.games.mineware.Challenge;
import com.houzicore.arcade.nautilus.game.arcade.game.games.mineware.MineWare;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;

public class ChallengeShootChickens extends Challenge
{
	private ArrayList<Chicken> _chickens = new ArrayList<Chicken>();
	private HashMap<String, Integer> _killedChickens = new HashMap<String, Integer>();
	private ArrayList<Projectile> _arrows = new ArrayList<Projectile>();

	public ChallengeShootChickens(MineWare host)
	{
		super(host, ChallengeType.FirstComplete, "Shoot 6 chickens");
	}

	@Override
	public void cleanupRoom()
	{
		for (Chicken chicken : _chickens)
		{
			chicken.remove();
		}

		for (Projectile arrow : _arrows)
		{
			arrow.remove();
		}

		Host.DamagePvE = false;
	}

	@EventHandler
	public void onShoot(ProjectileLaunchEvent event)
	{
		_arrows.add(event.getEntity());
	}

	@EventHandler
	public void onDeath(EntityDeathEvent event)
	{
		event.getDrops().clear();
		event.setDroppedExp(0);
	}

	@EventHandler
	public void Damage(EntityDamageByEntityEvent event)
	{
		if (event.getDamager() == null)
		{
			event.setCancelled(true);
			return;
		}

		if (!(event.getDamager() instanceof Player)) return;
		Player player = ((Player) event.getDamager());

		if (player == null)
			return;

		if (event.isCancelled())
			return;

		LivingEntity ent = (LivingEntity)event.getEntity();

		if (!_chickens.remove(ent))
		{
			event.setCancelled(true);
			return;
		}

		int score = _killedChickens.get(player.getName()) + 1;

		Location sloc = player.getEyeLocation();
		sloc.add(UtilAlg.getTrajectory(sloc, ent.getEyeLocation()).multiply(Math.min(7, ent.getLocation().distance(sloc))));

		displayCount(player, sloc, (score >= 6 ? C.cDGreen : score >= 4 ? C.cGreen : score >= 2 ? C.cRed : C.cDRed) + score);

		_killedChickens.put(player.getName(), score);

		if (score == 6)
		{
			SetCompleted(player);
		}
	}

	@EventHandler
	public void onHalfSecond(UpdateEvent event)
	{
		if (!Host.IsLive())
		{
			return;
		}

		if (event.getType() != UpdateType.FAST)
		{
			return;
		}

		Iterator<Chicken> itel = _chickens.iterator();

		while (itel.hasNext())
		{
			Chicken chicken = itel.next();

			if (chicken.isOnGround() || !chicken.isValid())
			{
				chicken.remove();
				itel.remove();
			}
		}

		if (_chickens.size() < 11 + (getChallengers().size() * 2))
		{
			Location loc = getCenter().clone().add(UtilMath.r(20) - 10, 15, UtilMath.r(20) - 10);

			Host.CreatureAllowOverride = true;
			Chicken chicken = (Chicken) loc.getWorld().spawnEntity(loc, EntityType.CHICKEN);
			Host.CreatureAllowOverride = false;
			chicken.setMaxHealth(0.1);
			chicken.setHealth(0.1);

			_chickens.add(chicken);
		}
	}

	@Override
	public void setupPlayers()
	{
		setBorder(-10, 10, 0, 10, -10, 10);

		for (Player player : Host.GetPlayers(true))
		{
			player.getInventory().setItem(0,
					new ItemBuilder(Material.BOW).addEnchantment(Enchantment.INFINITY, 1).setUnbreakable(true).build());
			player.getInventory().setItem(9, new ItemStack(Material.ARROW));

			_killedChickens.put(player.getName(), 0);
		}

		Host.DamagePvE = true;
	}

	@Override
	public void generateRoom()
	{
		for (int x = -10; x <= 10; x++)
		{
			for (int z = -10; z <= 10; z++)
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
						if (Math.abs(x) == 10 || Math.abs(z) == 10)
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
	}

	@Override
	public ArrayList<Location> getSpawns()
	{
		ArrayList<Location> spawns = new ArrayList<Location>();

		for (int x = -8; x <= 8; x++)
		{
			for (int z = -8; z <= 8; z++)
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
