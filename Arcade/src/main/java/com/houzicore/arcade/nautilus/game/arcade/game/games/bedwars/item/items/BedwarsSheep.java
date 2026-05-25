package com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.item.items;

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilAlg;
import com.houzicore.shared.common.util.UtilBlock;
import com.houzicore.shared.common.util.UtilEnt;
import com.houzicore.shared.common.util.UtilMath;
import com.houzicore.shared.common.util.UtilParticle;
import com.houzicore.shared.common.util.UtilParticle.ParticleType;
import com.houzicore.shared.common.util.UtilParticle.ViewDist;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.core.itemstack.ItemBuilder;
import com.houzicore.shared.core.damage.CustomDamageEvent;

import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.Bedwars;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.item.BedwarsSpecialItem;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.team.BedwarsTeam;

public class BedwarsSheep extends BedwarsSpecialItem implements Listener
{

	public static final ItemStack ITEM_STACK = new ItemBuilder(Material.SHEEP_SPAWN_EGG)
			.setTitle(C.cPurple + C.Bold + "Polly The Sheep")
			.addLore(
					"",
					"Spawns Polly The Sheep...",
					"After " + C.cRed + "4 seconds" + C.cGray + " she explodes destroying",
					"nearby player placed blocks.",
					"If she is killed she does not explode.",
					"Warning! Polly has a", C.cRed + "20 second" + C.cGray + " cooldown between uses."
			)
			.build();
	private static final long EXPLOSION_TIME = TimeUnit.SECONDS.toMillis(4);
	private static final int EXPLOSION_RADIUS = 7;
	private static final int NO_PLACE_RADIUS_SQUARED = 225;

	public BedwarsSheep(Bedwars game)
	{
		super(game, ITEM_STACK, "Polly The Sheep", TimeUnit.SECONDS.toMillis(20));
	}

	@Override
	protected void setup()
	{
		org.bukkit.Bukkit.getPluginManager().registerEvents(this, _game.getArcadeManager().getPlugin());
	}

	@Override
	protected void cleanup()
	{
		org.bukkit.event.HandlerList.unregisterAll(this);
	}

	@Override
	protected boolean onClick(PlayerInteractEvent event, BedwarsTeam bedTeam)
	{
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
		{
			return false;
		}

		Player player = event.getPlayer();
		Location location = event.getClickedBlock().getLocation().add(0.5, 1, 0.5);

		double dx = location.getX() - bedTeam.getBed().getX();
		double dz = location.getZ() - bedTeam.getBed().getZ();
		double distSq = dx * dx + dz * dz;
		if (distSq < NO_PLACE_RADIUS_SQUARED)
		{
			player.sendMessage(F.main("Game", "You cannot place " + F.name(getName()) + " this close to your bed."));
			return false;
		}

		location.setYaw(UtilAlg.GetYaw(UtilAlg.getTrajectory(location, player.getLocation())));

		DecimalFormat format = new DecimalFormat("0.0");
		long start = System.currentTimeMillis();

		_game.CreatureAllowOverride = true;

		Sheep sheep = location.getWorld().spawn(location, Sheep.class);
		sheep.setColor(getDyeColor(bedTeam.getGameTeam().GetColor()));
		sheep.setCustomNameVisible(true);
		location.getWorld().playSound(location, Sound.ENTITY_SHEEP_SHEAR, 2, 0.8F);
		sheep.setAI(false);

		_game.getArcadeManager().runSyncTimer(new BukkitRunnable()
		{
			int tick;

			@Override
			public void run()
			{
				if (!sheep.isValid())
				{
					cancel();
					return;
				}

				long left = start + EXPLOSION_TIME - System.currentTimeMillis();
				Location sheepLocation = sheep.getLocation();

				if (left <= 0)
				{
					Map<Block, Double> blocks = UtilBlock.getInRadius(sheepLocation, EXPLOSION_RADIUS);
					Collection<Block> placedBlocks = _game.getBedwarsPlayerModule().getPlacedBlocks();

					location.getWorld().playSound(sheepLocation, Sound.ENTITY_GENERIC_EXPLODE, 2, 1);
					UtilParticle.PlayParticle(ParticleType.HUGE_EXPLOSION, sheepLocation.add(0, 0.5, 0), 0.5f, 0.5f, 0.5f, 0, 1, ViewDist.LONG, UtilServer.getPlayers());
					blocks.entrySet().removeIf(entry ->
					{
						Block block = entry.getKey();
						double scale = entry.getValue();

						if (placedBlocks.contains(block))
						{
							double chance;

							// I originally was going to use the NMS way to get the "hardness" of a block
							// but I decided against it as the values were way too varied for this game.
							// The value for chance should be taken from if the block was right next to the sheep.
							String blockName = block.getType().name();
							if (blockName.contains("WOOL"))
							{
								chance = 1;
							}
							else if (blockName.contains("TERRACOTTA") || blockName.contains("CLAY"))
							{
								chance = 0.7;
							}
							else if (blockName.contains("PLANKS") || blockName.contains("WOOD"))
							{
								chance = 0.7;
							}
							else if (blockName.contains("END_STONE"))
							{
								chance = 0.4;
							}
							else if (blockName.contains("OBSIDIAN"))
							{
								chance = 0.05;
							}
							else
							{
								chance = 0.5;
							}

							chance *= scale * 2;

							if (Math.random() < chance)
							{
								placedBlocks.remove(block);
								return false;
							}
						}

						return true;
					});

					_game.getArcadeManager().GetExplosion().BlockExplosion(blocks.keySet(), sheepLocation, false);
					UtilPlayer.getInRadius(sheepLocation, EXPLOSION_RADIUS).forEach((nearby, scale) ->
					{
						_game.getArcadeManager().GetDamage().NewDamageEvent(nearby, player, null, DamageCause.CUSTOM, 40 * scale, true, true, false, player.getName(), getName());
					});

					sheep.remove();
					cancel();
					return;
				}

				sheep.setCustomName((tick % 2 == 0 ? bedTeam.getGameTeam().GetColor() + C.Bold : C.cWhite + C.Bold) + format.format(left / 1000D));

				if (tick % 8 == 0)
				{
					location.getWorld().playSound(sheepLocation, Sound.ENTITY_SHEEP_AMBIENT, 2, 0.8F);
				}

				tick++;
			}
		}, 0, 2);

		_game.CreatureAllowOverride = false;

		return true;
	}

	@EventHandler
	public void sheepDeath(EntityDeathEvent event)
	{
		if (event.getEntity() instanceof Sheep)
		{
			event.getDrops().clear();
			event.setDroppedExp(0);
		}
	}

	@EventHandler
	public void sheepDamage(CustomDamageEvent event)
	{
		if (event.GetDamageeEntity() instanceof Sheep && event.GetDamagerPlayer(true) == null)
		{
			event.SetCancelled("Sheep World Damage");
		}
	}

	@EventHandler
	public void sheepShear(PlayerInteractEntityEvent event)
	{
		if (event.getRightClicked() instanceof Sheep)
		{
			event.setCancelled(true);
		}
	}

	private org.bukkit.DyeColor getDyeColor(org.bukkit.ChatColor chatColor)
	{
		if (chatColor == org.bukkit.ChatColor.WHITE) return org.bukkit.DyeColor.WHITE;
		if (chatColor == org.bukkit.ChatColor.GOLD) return org.bukkit.DyeColor.ORANGE;
		if (chatColor == org.bukkit.ChatColor.LIGHT_PURPLE) return org.bukkit.DyeColor.PINK;
		if (chatColor == org.bukkit.ChatColor.AQUA) return org.bukkit.DyeColor.LIGHT_BLUE;
		if (chatColor == org.bukkit.ChatColor.YELLOW) return org.bukkit.DyeColor.YELLOW;
		if (chatColor == org.bukkit.ChatColor.GREEN) return org.bukkit.DyeColor.LIME;
		if (chatColor == org.bukkit.ChatColor.DARK_GRAY) return org.bukkit.DyeColor.GRAY;
		if (chatColor == org.bukkit.ChatColor.GRAY) return org.bukkit.DyeColor.LIGHT_GRAY;
		if (chatColor == org.bukkit.ChatColor.DARK_AQUA) return org.bukkit.DyeColor.CYAN;
		if (chatColor == org.bukkit.ChatColor.DARK_PURPLE) return org.bukkit.DyeColor.PURPLE;
		if (chatColor == org.bukkit.ChatColor.BLUE || chatColor == org.bukkit.ChatColor.DARK_BLUE) return org.bukkit.DyeColor.BLUE;
		if (chatColor == org.bukkit.ChatColor.DARK_GREEN) return org.bukkit.DyeColor.GREEN;
		if (chatColor == org.bukkit.ChatColor.RED || chatColor == org.bukkit.ChatColor.DARK_RED) return org.bukkit.DyeColor.RED;
		return org.bukkit.DyeColor.WHITE;
	}
}
