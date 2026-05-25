package com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.general;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.entity.Display.Billboard;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilMath;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;

import com.houzicore.arcade.nautilus.game.arcade.events.GameStateChangeEvent;
import com.houzicore.arcade.nautilus.game.arcade.game.Game.GameState;
import com.houzicore.arcade.nautilus.game.arcade.game.GameTeam;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.BedwarsModule;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.Bedwars;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.shop.BedwarsNetherItem;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.shop.BedwarsResource;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.team.BedwarsTeam;

public class BedwarsSpawnerModule extends BedwarsModule
{

	private static final int MIN_BLOCK_PLACE_DIST_SQUARED = 4;

	private final List<ItemDisplay> _displays = new ArrayList<>();
	private final Map<BedwarsResource, Long> _lastSpawnTimes = new HashMap<>();
	private int _lastTier = 1;

	public BedwarsSpawnerModule(Bedwars game)
	{
		super(game);
	}

	@EventHandler(ignoreCancelled = true)
	public void blockPlace(BlockPlaceEvent event)
	{
		if (isNearSpawner(event.getBlock()))
		{
			event.setCancelled(true);
			event.getPlayer().sendMessage(F.main("Game", "You cannot place blocks that close to a generator."));
		}
	}

	@EventHandler
	public void onStateChange(GameStateChangeEvent event)
	{
		if (event.GetGame() != _game) return;

		if (event.GetState() == GameState.Live)
		{
			long now = System.currentTimeMillis();
			_lastSpawnTimes.put(BedwarsResource.BRICK, now);
			_lastSpawnTimes.put(BedwarsResource.STAR, now);
			_lastSpawnTimes.put(BedwarsResource.EMERALD, now);
			_lastTier = 1;
			spawnDisplays();
		}
		else if (event.GetState() == GameState.End || event.GetState() == GameState.Dead)
		{
			clearDisplays();
		}
	}

	private void spawnDisplays()
	{
		clearDisplays();
		_game.getBedwarsTeamModule().getBedwarsTeams().forEach((team, bedTeam) ->
		{
			Location loc = bedTeam.getGenerator().clone().add(0, 1.6, 0);
			ItemDisplay display = loc.getWorld().spawn(loc, ItemDisplay.class);
			display.setItemStack(new ItemStack(Material.GOLD_INGOT));
			display.setBillboard(Billboard.CENTER);
			_displays.add(display);
		});
	}

	private void clearDisplays()
	{
		for (ItemDisplay display : _displays)
		{
			if (display != null && display.isValid())
			{
				display.remove();
			}
		}
		_displays.clear();
	}

	@EventHandler
	public void rotateDisplays(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK || !_game.IsLive() || _displays.isEmpty())
		{
			return;
		}

		float yaw = (float) ((System.currentTimeMillis() / 10) % 360);
		Material expected = getTierMaterial();

		for (ItemDisplay display : _displays)
		{
			if (display != null && display.isValid())
			{
				display.setRotation(yaw, 0.0f);
				if (display.getItemStack().getType() != expected)
				{
					display.setItemStack(new ItemStack(expected));
				}
			}
		}
	}

	private Material getTierMaterial()
	{
		long elapsed = System.currentTimeMillis() - _game.GetStateTime();
		if (elapsed >= TimeUnit.MINUTES.toMillis(10))
		{
			return Material.EMERALD;
		}
		else if (elapsed >= TimeUnit.MINUTES.toMillis(5))
		{
			return Material.DIAMOND;
		}
		else
		{
			return Material.GOLD_INGOT;
		}
	}

	@EventHandler
	public void updateSpawn(UpdateEvent event)
	{
		if (!_game.IsLive())
		{
			return;
		}

		for (BedwarsResource resource : BedwarsResource.values())
		{
			if (resource.getSpawnerUpdate() != event.getType())
			{
				continue;
			}

			_lastSpawnTimes.put(resource, System.currentTimeMillis());
			_game.getBedwarsTeamModule().getBedwarsTeams().forEach((team, bedTeam) -> distributeItem(resource, getItemsToDrop(resource, team, bedTeam), team));
		}
	}

	@EventHandler
	public void checkTierUpgrade(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SLOW || !_game.IsLive())
		{
			return;
		}

		long elapsed = System.currentTimeMillis() - _game.GetStateTime();
		int currentTier = 1;
		if (elapsed >= TimeUnit.MINUTES.toMillis(15)) currentTier = 4;
		else if (elapsed >= TimeUnit.MINUTES.toMillis(10)) currentTier = 3;
		else if (elapsed >= TimeUnit.MINUTES.toMillis(5)) currentTier = 2;

		if (currentTier > _lastTier)
		{
			_lastTier = currentTier;
			String roman = currentTier == 2 ? "II" : (currentTier == 3 ? "III" : "IV");
			_game.Announce(C.cYellow + C.Bold + "GENERATORS UPGRADED TO TIER " + roman + "!");
			for (Player player : UtilServer.getPlayers())
			{
				player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
			}
		}
	}

	@EventHandler
	public void updateGeneratorHologram(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTEST || !_game.IsLive())
		{
			return;
		}

		_game.getBedwarsTeamModule().getBedwarsTeams().forEach((team, bedTeam) ->
		{
			String[] lines = new String[] {
				getGeneratorHeader(),
				getResourcesString(),
				getProgressBarString()
			};
			bedTeam.getGeneratorHologram().setText(lines);
		});
	}

	private String getGeneratorHeader()
	{
		long elapsed = System.currentTimeMillis() - _game.GetStateTime();
		if (elapsed >= TimeUnit.MINUTES.toMillis(15))
		{
			return "§e§lɢᴇɴᴇʀᴀᴛᴏʀ §6§lᴛɪᴇʀ ɪᴠ";
		}
		else if (elapsed >= TimeUnit.MINUTES.toMillis(10))
		{
			return "§e§lɢᴇɴᴇʀᴀᴛᴏʀ §6§lᴛɪᴇʀ ɪɪɪ";
		}
		else if (elapsed >= TimeUnit.MINUTES.toMillis(5))
		{
			return "§e§lɢᴇɴᴇʀᴀᴛᴏʀ §6§lᴛɪᴇʀ ɪɪ";
		}
		else
		{
			return "§e§lɢᴇɴᴇʀᴀᴛᴏʀ §6§lᴛɪᴇʀ ɪ";
		}
	}

	private String getResourcesString()
	{
		long elapsed = System.currentTimeMillis() - _game.GetStateTime();
		if (elapsed >= TimeUnit.MINUTES.toMillis(10))
		{
			return "§fɪʀᴏɴ§7, §6ɢᴏʟᴅ§7, §bᴅɪᴀᴍᴏɴᴅ §7& §aᴇᴍᴇʀᴀʟᴅ";
		}
		else if (elapsed >= TimeUnit.MINUTES.toMillis(5))
		{
			return "§fɪʀᴏɴ§7, §6ɢᴏʟᴅ §7& §bᴅɪᴀᴍᴏɴᴅ";
		}
		else
		{
			return "§fɪʀᴏɴ §7& §6ɢᴏʟᴅ";
		}
	}

	private String getProgressBarString()
	{
		BedwarsResource rarest = getRarestActiveResource();
		long lastSpawn = _lastSpawnTimes.getOrDefault(rarest, System.currentTimeMillis());
		long interval = getIntervalMillis(rarest);
		long nextSpawn = lastSpawn + interval;
		long timeLeft = nextSpawn - System.currentTimeMillis();
		if (timeLeft < 0) timeLeft = 0;

		double fraction = Math.max(0.0, Math.min(1.0, (double) (interval - timeLeft) / interval));
		int progress = (int) (fraction * 10);
		StringBuilder sb = new StringBuilder();
		sb.append("§7Spawning: §a");
		for (int i = 0; i < progress; i++) sb.append("■");
		sb.append("§7");
		for (int i = progress; i < 10; i++) sb.append("■");
		sb.append(String.format(" §e%.1fs", timeLeft / 1000.0));
		return sb.toString();
	}

	private BedwarsResource getRarestActiveResource()
	{
		long elapsed = System.currentTimeMillis() - _game.GetStateTime();
		if (elapsed >= TimeUnit.MINUTES.toMillis(10))
		{
			return BedwarsResource.EMERALD;
		}
		else if (elapsed >= TimeUnit.MINUTES.toMillis(5))
		{
			return BedwarsResource.STAR;
		}
		else
		{
			return BedwarsResource.BRICK;
		}
	}

	private long getIntervalMillis(BedwarsResource resource)
	{
		switch (resource)
		{
			case BRICK: return 2000;
			case EMERALD: return 5000;
			case STAR: return 8000;
		}
		return 2000;
	}

	private int getItemsToDrop(BedwarsResource resource, GameTeam team, BedwarsTeam bedTeam)
	{
		long elapsed = System.currentTimeMillis() - _game.GetStateTime();
		int baseRate = bedTeam.getUpgrades().get(BedwarsNetherItem.RESOURCE);

		switch (resource)
		{
			case BRICK:
				int amt = 1 + baseRate;
				double chance = 0.0;
				if (elapsed >= TimeUnit.MINUTES.toMillis(15)) chance = 0.60;
				else if (elapsed >= TimeUnit.MINUTES.toMillis(10)) chance = 0.40;
				else if (elapsed >= TimeUnit.MINUTES.toMillis(5)) chance = 0.20;
				
				if (Math.random() < chance) amt++;
				return amt;

			case STAR:
				if (elapsed < TimeUnit.MINUTES.toMillis(5)) return 0;
				int dAmt = 1;
				if (elapsed >= TimeUnit.MINUTES.toMillis(15)) dAmt = 2;
				else if (elapsed >= TimeUnit.MINUTES.toMillis(10))
				{
					if (Math.random() < 0.50) dAmt = 2;
				}
				return dAmt;

			case EMERALD:
				if (elapsed < TimeUnit.MINUTES.toMillis(10)) return 0;
				int eAmt = 1;
				if (elapsed >= TimeUnit.MINUTES.toMillis(15))
				{
					if (Math.random() < 0.50) eAmt = 2;
				}
				return eAmt;
		}

		return 0;
	}

	private void distributeItem(BedwarsResource resource, int amount, GameTeam team)
	{
		if (amount <= 0)
		{
			return;
		}

		BedwarsTeam bedTeam = _game.getBedwarsTeamModule().getBedwarsTeam(team);

		ItemStack itemStack = resource.getItemStack().clone();
		itemStack.setAmount(amount);
		Location location = bedTeam.getGenerator();
		boolean drop = true;
		List<Player> players = new ArrayList<>();
		Item item = null;

		for (Entity entity : location.getWorld().getNearbyEntities(location, 2, 2, 2))
		{
			if (entity instanceof Item)
			{
				Item itemEntity = (Item) entity;

				if (itemEntity.getItemStack().getType() == itemStack.getType())
				{
					item = itemEntity;
				}
			}
			if (entity instanceof Player)
			{
				Player player = (Player) entity;

				if (UtilPlayer.isSpectator(player))
				{
					continue;
				}

				drop = false;
				players.add((Player) entity);
			}
		}

		if (drop)
		{
			if (item != null)
			{
				item.getItemStack().setAmount(Math.min(item.getItemStack().getAmount() + itemStack.getAmount(), resource.getMaxSpawned()));
			}
			else
			{
				item = location.getWorld().dropItem(location, itemStack);
				item.setVelocity(new org.bukkit.util.Vector(0, 0, 0));
			}
		}
		else
		{
			for (Player player : players)
			{
				player.getInventory().addItem(itemStack);
				player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1, 1);
			}
		}
	}

	public boolean isNearSpawner(Block block)
	{
		return isNearSpawner(block.getLocation().add(0.5, 0, 0.5));
	}

	public boolean isNearSpawner(Location location)
	{
		for (BedwarsTeam team : _game.getBedwarsTeamModule().getBedwarsTeams().values())
		{
			if (UtilMath.offsetSquared(location, team.getGenerator()) < MIN_BLOCK_PLACE_DIST_SQUARED)
			{
				return true;
			}
		}

		return false;
	}
}
