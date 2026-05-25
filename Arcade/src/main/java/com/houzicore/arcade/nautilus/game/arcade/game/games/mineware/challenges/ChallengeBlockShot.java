package com.houzicore.arcade.nautilus.game.arcade.game.games.mineware.challenges;

import java.util.ArrayList;
import java.util.HashMap;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.IdUtil;
import com.houzicore.shared.common.util.UtilAlg;
import com.houzicore.shared.common.util.UtilFirework;
import com.houzicore.shared.common.util.UtilMath;
import com.houzicore.shared.common.util.UtilParticle;
import com.houzicore.shared.common.util.UtilParticle.ParticleType;
import com.houzicore.shared.common.util.UtilParticle.ViewDist;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.common.util.UtilShapes;
import com.houzicore.shared.core.itemstack.ItemBuilder;
import com.houzicore.arcade.nautilus.game.arcade.game.games.mineware.Challenge;
import com.houzicore.arcade.nautilus.game.arcade.game.games.mineware.MineWare;

import org.bukkit.FireworkEffect.Type;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;

public class ChallengeBlockShot extends Challenge
{
	private HashMap<String, Integer> _shotBlocks = new HashMap<String, Integer>();
	private ArrayList<Location> _spawns = new ArrayList<Location>();
	private ArrayList<Entity> _arrows = new ArrayList<Entity>();

	public ChallengeBlockShot(MineWare host)
	{
		super(host, ChallengeType.FirstComplete, "Shoot down 5 blocks");
	}

	@Override
	public ArrayList<Location> getSpawns()
	{
		return _spawns;
	}

	@EventHandler
	public void onShoot(ProjectileLaunchEvent event)
	{
		_arrows.add(event.getEntity());
	}

	@Override
	public void cleanupRoom()
	{
		for (Entity arrow : _arrows)
		{
			arrow.remove();
		}
	}

	@EventHandler
	public void onProjectileHit(final ProjectileHitEvent event)
	{
		event.getEntity().remove();

		new BukkitRunnable()
		{
			public void run()
			{
				ProjectileSource shooter = event.getEntity().getShooter();

				if (shooter != null && shooter instanceof Player)
				{
					Player player = (Player) shooter;

					for (double x = -0.5; x <= 0.5; x++)
					{
						for (double y = -0.5; y <= 0.5; y++)
						{
							for (double z = -0.5; z <= 0.5; z++)
							{
								Block block = event.getEntity().getLocation().add(x, y, z).getBlock();

								if (IdUtil.getTypeId(block) == 35)
								{
									int score = _shotBlocks.get(player.getName()) + 1;

									_shotBlocks.put(player.getName(), score);

									Location sloc = player.getEyeLocation();
									sloc.add(UtilAlg.getTrajectory(sloc, block.getLocation().add(0.5, 0.5, 0.5)).multiply(
											Math.min(7, block.getLocation().distance(sloc))));

									displayCount(player, sloc, (score >= 5 ? C.cDGreen : score >= 3 ? C.cGreen
											: score >= 1 ? C.cRed : C.cDRed)
											+ score);

									if (score == 5)
									{
										SetCompleted(player);
									}

									Location loc = block.getLocation().add(0.5, 0.5, 0.5);
									UtilParticle.PlayParticle(ParticleType.LARGE_EXPLODE, loc, 0, 0, 0, 0, 1, ViewDist.MAX,
											UtilServer.getPlayers());

									spawnBlock();

									DyeColor color = DyeColor.getByWoolData(IdUtil.getData(block));
									UtilFirework.playFirework(loc, Type.BALL, color.getColor(), true, true);

									block.setType(Material.AIR);
								}
							}
						}
					}
				}
			}
		}.runTaskLater(Host.Manager.getPlugin(), 0);
	}

	private void spawnBlock()
	{
		while (true)
		{
			Block block = getCenter().add(UtilMath.r(40) - 20, 10 + UtilMath.r(10), UtilMath.r(40) - 20).getBlock();

			if (block.getType() == Material.AIR)
			{
				block.setType(IdUtil.getMaterial(35, (byte) UtilMath.r(16)));
				addBlock(block);

				break;
			}
		}
	}

	@Override
	public void setupPlayers()
	{
		setBorder(-16, 16, 0, 20, -16, 16);

		for (Player player : Host.GetPlayers(true))
		{
			player.getInventory().setItem(0,
					new ItemBuilder(Material.BOW).addEnchantment(Enchantment.INFINITY, 1).setUnbreakable(true).build());
			player.getInventory().setItem(9, new ItemStack(Material.ARROW));

			_shotBlocks.put(player.getName(), 0);
		}
	}

	@Override
	public void generateRoom()
	{
		for (Location loc : UtilShapes.getPointsInCircle(getCenter(), getChallengers().size(), 10))
		{
			loc = loc.getBlock().getLocation().add(0.5, 7.1, 0.5);
			_spawns.add(loc);

			Block block = loc.getBlock().getRelative(BlockFace.DOWN);
			block.setType(Material.STONE_SLAB);
			// block.setData((byte) 8); // Block.setData removed in 1.21
			addBlock(block);
		}

		for (int i = 0; i < 18; i++)
		{
			spawnBlock();
		}
	}

}
