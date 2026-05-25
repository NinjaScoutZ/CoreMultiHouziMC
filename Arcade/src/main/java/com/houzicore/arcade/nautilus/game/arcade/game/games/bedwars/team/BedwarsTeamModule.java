package com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.team;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.MapUtil;
import com.houzicore.shared.common.util.UtilAlg;
import com.houzicore.shared.common.util.UtilBlock;
import com.houzicore.shared.common.util.UtilEvent;
import com.houzicore.shared.common.util.UtilEvent.ActionType;
import com.houzicore.shared.common.util.UtilItem;
import com.houzicore.shared.common.util.UtilMath;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.common.util.UtilTextMiddle;
import com.houzicore.shared.common.util.UtilTime;
import com.houzicore.shared.core.hologram.Hologram;
import com.houzicore.shared.core.preferences.PreferencesManager;
import com.houzicore.shared.core.itemstack.ItemBuilder;
import com.houzicore.shared.recharge.Recharge;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.shared.core.combat.event.CombatDeathEvent;

import com.houzicore.arcade.nautilus.game.arcade.events.GameStateChangeEvent;
import com.houzicore.arcade.nautilus.game.arcade.events.PlayerDeathOutEvent;
import com.houzicore.arcade.nautilus.game.arcade.events.PlayerKitGiveEvent;
import com.houzicore.arcade.nautilus.game.arcade.game.Game.GameState;
import com.houzicore.arcade.nautilus.game.arcade.game.GameTeam;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.BedwarsModule;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.Bedwars;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.event.BedRotEvent;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.event.BedwarsBreakBedEvent;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.shop.BedwarsItem;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.shop.BedwarsShopModule;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.shop.trap.BedwarsTrapItem;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.shop.trap.BedwarsTrapItem.TrapTrigger;
import com.houzicore.arcade.nautilus.game.arcade.game.modules.CompassModule;
import com.houzicore.arcade.nautilus.game.arcade.world.WorldData;

public class BedwarsTeamModule extends BedwarsModule
{

	private static final int HOLOGRAM_VIEW_SQUARED = 16;
	private static final long BED_ROT_TIME = TimeUnit.MINUTES.toMillis(20);
	private static final long BED_WARNING_TIME = TimeUnit.MINUTES.toMillis(5);
	private static final long TRAP_COOLDOWN = TimeUnit.SECONDS.toMillis(10);
	private static final ItemStack[] STARTING_ITEMS =
			{
					new ItemBuilder(Material.WOODEN_SWORD)
							.setUnbreakable(true)
							.build()
			};

	private final Map<GameTeam, BedwarsTeam> _teams;

	private Player _lastPlayer;
	private boolean _samePlayer;

	private boolean _announcedWarning;
	private boolean _bedsRotten;

	public BedwarsTeamModule(Bedwars game)
	{
		super(game);

		_teams = new HashMap<>();
	}

	public void cleanup()
	{
		_teams.clear();
	}

	@EventHandler
	public void prepare(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Prepare)
		{
			return;
		}

		WorldData worldData = _game.WorldData;
		ArrayList<Location> edgeHolograms = worldData.GetDataLocs("BLACK");
		ArrayList<Location> shopHolograms = worldData.GetDataLocs("GRAY");
		ArrayList<Location> chestLocations = worldData.GetCustomLocs("54");

		_game.GetTeamList().forEach(team ->
				{
					if (!team.IsTeamAlive())
					{
						return;
					}

					Location average = _game.getAverageLocation(team);

					_teams.put(team,
							new BedwarsTeam(
									_game,
									team,
									UtilAlg.findClosest(
											average,
											edgeHolograms
									),
									UtilAlg.findClosest(
											average,
											shopHolograms
									),
									UtilAlg.findClosest(
											average,
											chestLocations
									),
									worldData.GetCustomLocs("GEN " + team.GetName().toUpperCase()).get(0)
							)
					);
				}
		);
	}

	@EventHandler
	public void updateHologramVisibility(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST || !_game.InProgress())
		{
			return;
		}

		_teams.forEach((team, bedTeam) ->
		{
			updateVisibility(bedTeam, bedTeam.getTipHolograms(), true);
			updateVisibility(bedTeam, bedTeam.getOtherHolograms(), false);
		});
	}

	private void updateVisibility(BedwarsTeam bedTeam, List<Hologram> holograms, boolean checkPreference)
	{
		GameTeam team = bedTeam.getGameTeam();
		List<Player> players = team.GetPlayers(true);
		PreferencesManager preferences = _game.getArcadeManager().getPreferences();

		holograms.forEach(hologram ->
		{
			if (!hologram.isInUse())
			{
				return;
			}

			Location location = hologram.getLocation();

			for (Player player : players)
			{
				if (!player.isOnline() || !team.IsAlive(player) || UtilMath.offsetSquared(player.getLocation(), location) < HOLOGRAM_VIEW_SQUARED)
				{
					hologram.removePlayer(player);
				}
				else
				{
					hologram.addPlayer(player);
				}
			}
		});
	}

	private boolean isBedBlock(Block block)
	{
		if (block == null || !(block.getBlockData() instanceof org.bukkit.block.data.type.Bed))
		{
			return false;
		}
		for (BedwarsTeam bedTeam : _teams.values())
		{
			Block mainBed = bedTeam.getBed().getBlock();
			Block otherBed = bedTeam.getOtherBedBlock();
			if (block.equals(mainBed) || (otherBed != null && block.equals(otherBed)))
			{
				return true;
			}
		}
		return false;
	}

	@EventHandler
	public void onBlockPhysics(org.bukkit.event.block.BlockPhysicsEvent event)
	{
		if (isBedBlock(event.getBlock()))
		{
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onBlockFromTo(org.bukkit.event.block.BlockFromToEvent event)
	{
		if (isBedBlock(event.getToBlock()))
		{
			event.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onBlockPlace(org.bukkit.event.block.BlockPlaceEvent event)
	{
		if (isBedBlock(event.getBlockPlaced()))
		{
			event.setCancelled(true);
			event.getPlayer().sendMessage(F.main("Game", "You cannot place blocks inside the bed!"));
		}
	}

	@EventHandler
	public void onExplode(org.bukkit.event.entity.EntityExplodeEvent event)
	{
		event.blockList().removeIf(this::isBedBlock);
	}

	@EventHandler
	public void onBlockExplode(org.bukkit.event.block.BlockExplodeEvent event)
	{
		event.blockList().removeIf(this::isBedBlock);
	}

	@EventHandler
	public void bedBreak(BlockBreakEvent event)
	{
		Player player = event.getPlayer();
		Block block = event.getBlock();

		if (UtilPlayer.isSpectator(player) || block == null || !(block.getBlockData() instanceof org.bukkit.block.data.type.Bed))
		{
			return;
		}

		event.setCancelled(true); // Cancel standard drop, we handle it custom

		GameTeam playerTeam = _game.GetTeam(player);
		if (playerTeam == null)
		{
			return;
		}

		_teams.forEach((team, bedTeam) ->
		{
			org.bukkit.block.data.type.Bed bedData = (org.bukkit.block.data.type.Bed) block.getBlockData();
			Block otherPart = bedData.getPart() == org.bukkit.block.data.type.Bed.Part.FOOT 
				? block.getRelative(bedData.getFacing()) 
				: block.getRelative(bedData.getFacing().getOppositeFace());

			boolean isTargetBed = block.equals(bedTeam.getBed().getBlock()) 
				|| (otherPart != null && otherPart.equals(bedTeam.getBed().getBlock()));

			if (!isTargetBed)
			{
				return;
			}

			if (team.equals(playerTeam))
			{
				player.sendMessage(F.main("Game", "You cannot break your own bed!"));
				return;
			}

			BedwarsShopModule module = _game.getBedwarsShopModule();
			module.getOwnedItems(team).removeIf(item ->
			{
				if (!(item instanceof BedwarsTrapItem))
				{
					return false;
				}

				BedwarsTrapItem trapItem = (BedwarsTrapItem) item;

				if (trapItem.getTrapTrigger() != TrapTrigger.BED_INTERACT || !Recharge.Instance.use(player, "Trap", TRAP_COOLDOWN, false, false))
				{
					return false;
				}

				triggerTrap(player, team, bedTeam, trapItem);
				return true;
			});

			_game.AddGems(player, 10, "Bed Broken", true, true);
			_game.AddStat(player, "BrokeBeds", 1, false, false);

			Location location = block.getLocation();
			block.getWorld().playEffect(location, Effect.STEP_SOUND, block.getType());
			block.getWorld().playSound(location, Sound.BLOCK_ANVIL_BREAK, 1f, 1f);

			// Break both parts of the bed
			block.setType(Material.AIR);
			if (otherPart != null && otherPart.getBlockData() instanceof org.bukkit.block.data.type.Bed)
			{
				otherPart.setType(Material.AIR);
			}

			org.bukkit.Bukkit.getPluginManager().callEvent(new BedwarsBreakBedEvent(player, bedTeam));
			bedTeam.getBedHologram().stop();

			_game.Announce(F.main("Game", F.name(team.GetFormattedName()) + "'s Bed was broken by " + F.name(playerTeam.GetColor() + player.getName()) + "! They can no longer respawn."));
			
			// Play dragon growl for bed break notification
			for (Player onlinePlayer : UtilServer.getPlayers())
			{
				onlinePlayer.playSound(onlinePlayer.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1f, 1f);
			}

			UtilTextMiddle.display(team.GetColor() + "BED BROKEN", "You can no longer respawn", 10, 40, 10, team.GetPlayers(true).toArray(new Player[0]));
		});
	}

	private void destroyBed(BedwarsTeam bedTeam)
	{
		Block bedBlock = bedTeam.getBed().getBlock();
		if (bedBlock.getBlockData() instanceof org.bukkit.block.data.type.Bed)
		{
			org.bukkit.block.data.type.Bed bedData = (org.bukkit.block.data.type.Bed) bedBlock.getBlockData();
			Block otherPart = bedData.getPart() == org.bukkit.block.data.type.Bed.Part.FOOT 
				? bedBlock.getRelative(bedData.getFacing()) 
				: bedBlock.getRelative(bedData.getFacing().getOppositeFace());
			
			bedBlock.setType(Material.AIR);
			if (otherPart != null && otherPart.getBlockData() instanceof org.bukkit.block.data.type.Bed)
			{
				otherPart.setType(Material.AIR);
			}
		}
		else
		{
			MapUtil.QuickChangeBlockAt(bedTeam.getBed(), Material.AIR);
		}
	}

	@EventHandler
	public void updateIslandTraps(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
		{
			return;
		}

		BedwarsShopModule module = _game.getBedwarsShopModule();

		_teams.forEach((team, bedTeam) ->
		{
			if (!bedTeam.canRespawn())
			{
				return;
			}

			module.getOwnedItems(team).removeIf(item ->
			{
				if (!(item instanceof BedwarsTrapItem))
				{
					return false;
				}

				BedwarsTrapItem trapItem = (BedwarsTrapItem) item;

				if (trapItem.getTrapTrigger() != TrapTrigger.BED_NEAR)
				{
					return false;
				}

				for (Player player : UtilPlayer.getNearby(bedTeam.getBed(), 3))
				{
					if (!team.HasPlayer(player) && Recharge.Instance.use(player, "Trap", TRAP_COOLDOWN, false, false))
					{
						triggerTrap(player, team, bedTeam, trapItem);
						return true;
					}
				}

				return false;
			});
		});
	}

	private void triggerTrap(Player player, GameTeam team, BedwarsTeam bedTeam, BedwarsTrapItem trapItem)
	{
		Player damager = null;

		for (Player teamMember : team.GetPlayers(true))
		{
			Set<BedwarsItem> items = _game.getBedwarsShopModule().getOwnedItems(teamMember);
			Iterator<BedwarsItem> iterator = items.iterator();

			while (iterator.hasNext())
			{
				if (iterator.next().equals(trapItem))
				{
					damager = teamMember;
					iterator.remove();
				}
			}
		}

		_game.getArcadeManager().GetDamage().NewDamageEvent(player, damager, null, DamageCause.CUSTOM, 2, false, true, false, "Trap", trapItem.getName());
		trapItem.onTrapTrigger(player, bedTeam.getBed());
		UtilTextMiddle.display(team.GetColor() + "TRAP SET OFF", "One of your traps has been set off!", 5, 20, 5, team.GetPlayers(true).toArray(new Player[0]));
		UtilTextMiddle.display("", C.cRed + C.Bold + "TRAPPED", 5, 20, 5, player);
	}

	@EventHandler
	public void playerDeathOut(PlayerDeathOutEvent event)
	{
		GameTeam team = _game.GetTeam(event.GetPlayer());

		if (team == null)
		{
			return;
		}
		else if (!_teams.get(team).canRespawn())
		{
			if (team.GetPlayers(true).size() == 1)
			{
				_game.Announce(F.main("Game", "The " + F.name(team.GetFormattedName()) + " team has been eliminated!"));
			}

			Player killer = event.GetPlayer().getKiller();

			if (killer != null)
			{
				_game.AddGems(killer, 2, "Final Kills", true, true);
				_game.AddStat(killer, "FinalKills", 1, false, false);
			}

			return;
		}

		event.setCancelled(true);
	}

	@EventHandler
	public void chestInteract(PlayerInteractEvent event)
	{
		if (!UtilEvent.isAction(event, ActionType.R_BLOCK))
		{
			return;
		}

		Player player = event.getPlayer();
		Location block = event.getClickedBlock().getLocation();

		if (UtilPlayer.isSpectator(player))
		{
			return;
		}

		GameTeam team = _game.GetTeam(player);

		for (BedwarsTeam bedTeam : _teams.values())
		{
			if (!bedTeam.canRespawn() || !bedTeam.getChest().equals(block) || team.equals(bedTeam.getGameTeam()))
			{
				continue;
			}

			event.setCancelled(true);
			player.sendMessage(F.main("Game", "You cannot open another team's chest while their Bed hasn't been broken."));
			return;
		}
	}

	@EventHandler
	public void kitGiveItems(PlayerKitGiveEvent event)
	{
		if (!_game.InProgress())
		{
			return;
		}

		Player player = event.GetPlayer();
		PlayerInventory inventory = player.getInventory();
		Color colour = _game.GetTeam(player).GetColorBase();

		inventory.addItem(STARTING_ITEMS);
		inventory.setArmorContents(new ItemStack[]
				{
						createColouredArmour(Material.LEATHER_BOOTS, colour),
						createColouredArmour(Material.LEATHER_LEGGINGS, colour),
						createColouredArmour(Material.LEATHER_CHESTPLATE, colour),
						createColouredArmour(Material.LEATHER_HELMET, colour),
				});
	}

	private ItemStack createColouredArmour(Material material, Color colour)
	{
		return new ItemBuilder(material)
				.setColor(colour)
				.setUnbreakable(true)
				.build();
	}

	@EventHandler
	public void playerDropItem(PlayerDropItemEvent event)
	{
		if (!_game.IsLive())
		{
			return;
		}

		ItemStack itemStack = event.getItemDrop().getItemStack();

		if (itemStack != null && itemStack.getType().name().contains("LEATHER_"))
		{
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void playerQuit(PlayerQuitEvent event)
	{
		GameTeam team = _game.GetTeam(event.getPlayer());

		if (team == null || team.GetPlayers(true).size() > 1)
		{
			return;
		}

		BedwarsTeam bedTeam = _teams.get(team);

		if (bedTeam == null || !bedTeam.canRespawn())
		{
			return;
		}

		destroyBed(bedTeam);
		_game.Announce(F.main("Game", F.name(team.GetFormattedName()) + "'s Bed has been broken! All their players have quit."));
	}

	@EventHandler
	public void playerDeath(CombatDeathEvent event)
	{
		if (!_game.IsLive())
		{
			return;
		}

		Player player = (Player) event.GetEvent().getEntity();
		GameTeam team = _game.GetTeam(player);

		if (team == null)
		{
			return;
		}

		BedwarsTeam bedTeam = _teams.get(team);

		if (bedTeam.canRespawn())
		{
			Player killer = player.getKiller();
			GameTeam killerTeam = _game.GetTeam(killer);

			// event.getPlayersToInform().removeIf(other -> (killerTeam == null || !killerTeam.HasPlayer(other)) && !team.HasPlayer(other));
		}
		else
		{
			// event.setSuffix(C.cAquaB + " ELIMINATION");
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void updateRotHologram(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST || !_game.IsLive())
		{
			return;
		}

		String bedRotString = getBedRotString();

		if (bedRotString != null)
		{
			String finalBedRotString = C.cRed + bedRotString + " until Bed Rot";

			_teams.values().forEach(bedTeam ->
			{
				if (!bedTeam.canRespawn())
				{
					bedTeam.getBedHologram().stop();
					return;
				}

				bedTeam.getBedHologram().setText(finalBedRotString);
			});
		}
	}

	public String getBedRotString()
	{
		long time = Math.max(0, _game.GetStateTime() + BED_ROT_TIME - System.currentTimeMillis());

		if (time > BED_WARNING_TIME)
		{
			return null;
		}
		else if (!_announcedWarning)
		{
			_announcedWarning = true;
			_game.Announce(F.main("Game", "Beds will rot in " + F.time(UtilTime.MakeStr(BED_WARNING_TIME)) + "!"));
		}
		else if (time == 0)
		{
			if (!_bedsRotten)
			{
				_bedsRotten = true;
				_game.Announce(F.main("Game", "All Beds have rotted away. No one can respawn!"));

				_teams.values().forEach(bedTeam ->
				{
					if (bedTeam.canRespawn())
					{
						destroyBed(bedTeam);
					}

					bedTeam.getBedHologram().stop();
				});

				_game.getModule(CompassModule.class)
						.setGiveItem(true);

				org.bukkit.Bukkit.getPluginManager().callEvent(new BedRotEvent());
			}

			return null;
		}

		return UtilTime.MakeStr(time);
	}

	public boolean hasBedRot()
	{
		return UtilTime.elapsed(_game.GetStateTime(), BED_ROT_TIME);
	}

	public BedwarsTeam getBedwarsTeam(GameTeam team)
	{
		return _teams.get(team);
	}

	public Map<GameTeam, BedwarsTeam> getBedwarsTeams()
	{
		return _teams;
	}
}
