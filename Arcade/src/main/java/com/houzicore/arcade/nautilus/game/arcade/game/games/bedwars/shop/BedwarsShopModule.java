package com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.shop;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.MapUtil;
import com.houzicore.shared.common.util.UtilAlg;
import com.houzicore.shared.common.util.UtilItem;
import com.houzicore.shared.common.util.UtilMath;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.core.itemstack.ItemBuilder;
import com.houzicore.shared.recharge.Recharge;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;

import com.houzicore.arcade.nautilus.game.arcade.events.GameStateChangeEvent;
import com.houzicore.arcade.nautilus.game.arcade.game.Game.GameState;
import com.houzicore.arcade.nautilus.game.arcade.game.GameTeam;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.BedwarsModule;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.Bedwars;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.ui.BedwarsResourcePage;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.ui.BedwarsResourceShop;

public class BedwarsShopModule extends BedwarsModule
{

	private static final int MIN_BLOCK_PLACE_DIST_SQUARED = 9;
	public static final ItemStack ENDER_PEARL = new ItemBuilder(Material.ENDER_PEARL)
			.setTitle(C.cPurple + C.Bold + "Ender Pearl")
			.addLore("", "Warning! Ender Pearls have a", C.cRed + "7 second" + C.cGray + " cooldown between uses.")
			.build();
	static int getHealingStationRadius(int level)
	{
		return 5 + (3 * (level - 1));
	}

	private final Map<LivingEntity, BedwarsResource> _npcs;
	private final BedwarsResourceShop _shop;
	private final Map<BedwarsResource, List<BedwarsItem>> _items;
	private final Map<UUID, Set<BedwarsItem>> _ownedItems;
	private final Map<GameTeam, Set<BedwarsItem>> _ownedTeamItems;

	public BedwarsShopModule(Bedwars game)
	{
		super(game);

		_npcs = new HashMap<>();
		_shop = new BedwarsResourceShop(game.getArcadeManager());
		_items = new HashMap<>(BedwarsResource.values().length);
		_ownedItems = new HashMap<>();
		_ownedTeamItems = new HashMap<>(8);

		_items.put(BedwarsResource.BRICK, game.generateItems(BedwarsResource.BRICK));
		_items.put(BedwarsResource.EMERALD, game.generateItems(BedwarsResource.EMERALD));
		_items.put(BedwarsResource.STAR, game.generateItems(BedwarsResource.STAR));
	}

	public void cleanup()
	{
		_npcs.keySet().forEach(org.bukkit.entity.Entity::remove);
		_npcs.clear();
		_items.clear();
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void prepare(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Prepare)
		{
			return;
		}

		_game.CreatureAllowOverride = true;

		_game.getBedwarsTeamModule().getBedwarsTeams().forEach((team, bedTeam) ->
		{
			String teamName = team.GetName().toUpperCase();

			for (BedwarsResource resource : BedwarsResource.values())
			{
				Location location = _game.WorldData.GetCustomLocs("SHOP " + teamName + " " + resource.name()).get(0);
				location.setYaw(UtilAlg.GetYaw(UtilAlg.getTrajectory2d(location, bedTeam.getShop())));
				
				Villager villager = location.getWorld().spawn(location, Villager.class);
				villager.setCustomName(resource.getChatColor() + C.Bold + resource.getName() + " Shop");
				villager.setCustomNameVisible(true);
				villager.setAI(false);
				villager.setSilent(true);
				villager.setInvulnerable(true);
				villager.setRemoveWhenFarAway(false);

				MapUtil.QuickChangeBlockAt(location, Material.BARRIER);
				MapUtil.QuickChangeBlockAt(location.clone().add(0, 1, 0), Material.BARRIER);

				_npcs.put(villager, resource);
			}

			_ownedTeamItems.put(team, new HashSet<>());
		});

		_game.CreatureAllowOverride = false;
	}

	@EventHandler
	public void npcInteract(PlayerInteractEvent event)
	{
		if (!_game.IsLive())
		{
			return;
		}

		Block block = event.getClickedBlock();
		Player player = event.getPlayer();

		if (block == null || block.getType() != Material.BARRIER)
		{
			return;
		}

		event.setCancelled(true);
		openShop(player, event.getAction(), block.getLocation());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void entityInteract(org.bukkit.event.player.PlayerInteractEntityEvent event)
	{
		if (_npcs.containsKey(event.getRightClicked()))
		{
			event.setCancelled(true);
			openShop(event.getPlayer(), Action.RIGHT_CLICK_BLOCK, event.getRightClicked().getLocation());
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void entityDamage(org.bukkit.event.entity.EntityDamageByEntityEvent event)
	{
		if (_npcs.containsKey(event.getEntity()))
		{
			event.setCancelled(true);
			if (event.getDamager() instanceof Player)
			{
				openShop((Player) event.getDamager(), Action.LEFT_CLICK_BLOCK, event.getEntity().getLocation());
			}
		}
	}

	private void openShop(Player player, Action action, Location location)
	{
		for (Entry<LivingEntity, BedwarsResource> entry : _npcs.entrySet())
		{
			Location npcLocation = entry.getKey().getLocation();

			if (location.getBlockX() != npcLocation.getBlockX() || location.getBlockZ() != npcLocation.getBlockZ())
			{
				continue;
			}

			if (UtilPlayer.isSpectator(player) || (action == Action.LEFT_CLICK_BLOCK && player.getItemInHand() != null && player.getItemInHand().getType().name().contains("SWORD")) || !Recharge.Instance.use(player, "Interact Shop", 500, false, false))
			{
				return;
			}

			BedwarsResourcePage page = _game.getShopPage(entry.getValue(), player);

			page.refresh();
			_shop.openPageForPlayer(player, page);
			return;
		}
	}

	@EventHandler
	public void blockPlace(BlockPlaceEvent event)
	{
		if (!_game.IsLive() || UtilPlayer.isSpectator(event.getPlayer()))
		{
			return;
		}

		Location location = event.getBlock().getLocation();

		if (isNearShop(location))
		{
			event.setCancelled(true);
			event.getPlayer().sendMessage(F.main("Game", "You cannot place blocks that close to the Shop."));
		}
	}

	@EventHandler
	public void updatePassiveUpgrades(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC || !_game.IsLive())
		{
			return;
		}

		_game.getBedwarsTeamModule().getBedwarsTeams().forEach((team, bedTeam) ->
		{
			List<Player> alive = team.GetPlayers(true);

			bedTeam.getUpgrades().forEach((item, level) ->
			{
				if (_game.getBedwarsTeamModule().hasBedRot() && item == BedwarsNetherItem.REGENERATION)
				{
					return;
				}

				if (level > 0)
				{
					alive.forEach(player -> item.apply(player, level, bedTeam.getBed()));
				}
			});
		});
	}

	@EventHandler
	public void updateHealingParticles(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTER || !_game.IsLive())
		{
			return;
		}

		_game.getBedwarsTeamModule().getBedwarsTeams().forEach((team, bedTeam) ->
		{
			int level = bedTeam.getUpgrades().getOrDefault(BedwarsNetherItem.REGENERATION, 0);

			if (level == 0 || !bedTeam.canRespawn())
			{
				return;
			}

			Location location = bedTeam.getBed().clone();
			Color color = team.GetColorBase();
			int radius = getHealingStationRadius(level);
			double deltaTheta = Math.PI / (27 - (7 * level));

			for (double theta = 0; theta < 2 * Math.PI; theta += deltaTheta)
			{
				double x = radius * Math.cos(theta), z = radius * Math.sin(theta);

				location.add(x, 0, z);

				location.getWorld().spawnParticle(
						Particle.DUST,
						location,
						1,
						0, 0, 0,
						0,
						new DustOptions(color, 1.0f)
				);

				location.subtract(x, 0, z);
			}
		});
	}

	@EventHandler
	public void playerDeath(PlayerDeathEvent event)
	{
		onDeath(event.getEntity());
	}

	@EventHandler
	public void playerQuit(PlayerQuitEvent event)
	{
		onDeath(event.getPlayer());
	}

	private void onDeath(Player player)
	{
		Set<BedwarsItem> items = getOwnedItems(player);

		if (!_game.getBedwarsPlayerModule().isUsingRuneOfHolding(player))
		{
			items.removeIf(item -> !item.getItemType().isOnePerTeam());
		}
	}

	public boolean ownsItem(Player player, BedwarsItem item)
	{
		return getOwnedItems(player).contains(item);
	}

	public boolean ownsItem(GameTeam team, BedwarsItem item)
	{
		return _ownedTeamItems.get(team).contains(item);
	}

	public Set<BedwarsItem> getOwnedItems(Player player)
	{
		return _ownedItems.computeIfAbsent(player.getUniqueId(), k -> new HashSet<>());
	}

	public Set<BedwarsItem> getOwnedItems(GameTeam team)
	{
		return _ownedTeamItems.get(team);
	}

	public boolean isNearShop(Location location)
	{
		for (LivingEntity npc : _npcs.keySet())
		{
			if (UtilMath.offsetSquared(location, npc.getLocation()) < MIN_BLOCK_PLACE_DIST_SQUARED)
			{
				return true;
			}
		}

		return false;
	}

	public BedwarsResourceShop getShop()
	{
		return _shop;
	}

	public Map<BedwarsResource, List<BedwarsItem>> getItems()
	{
		return _items;
	}
}
