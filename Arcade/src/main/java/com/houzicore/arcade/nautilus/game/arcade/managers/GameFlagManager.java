package com.houzicore.arcade.nautilus.game.arcade.managers;

import com.houzicore.shared.core.antihack.AntiHack;
import org.bukkit.Location;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilAction;
import com.houzicore.shared.common.util.UtilAlg;
import com.houzicore.shared.common.util.UtilBlock;
import com.houzicore.shared.common.util.UtilInv;
import com.houzicore.shared.common.util.UtilMath;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.common.util.UtilTextMiddle;
import com.houzicore.shared.core.itemstack.ItemStackFactory;
import com.houzicore.shared.recharge.Recharge;
import com.houzicore.shared.core.teleport.event.HouziTeleportEvent;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.bootstrap.ArcadeBootstrap;
import com.houzicore.arcade.GameType;
import com.houzicore.arcade.nautilus.game.arcade.events.GameStateChangeEvent;
import com.houzicore.arcade.nautilus.game.arcade.events.PlayerDeathOutEvent;
import com.houzicore.arcade.nautilus.game.arcade.game.Game;
import com.houzicore.arcade.nautilus.game.arcade.game.GameTeam;
import com.houzicore.arcade.nautilus.game.arcade.game.Game.GameState;
import com.houzicore.arcade.nautilus.game.arcade.game.GameTeam.PlayerState;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.event.PerkDestructorBlockEvent;
import com.houzicore.shared.core.lang.LangManager;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
//import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class GameFlagManager implements Listener
{
	ArcadeManager Manager;

	public GameFlagManager(ArcadeManager manager)
	{
		Manager = manager;

		Manager.getPluginManager().registerEvents(this, Manager.getPlugin());
	}

	private boolean ruleInventoryOpenBlock(Game game)
	{
		return game.isContextRuntime() ? game.getRules().isInventoryOpenBlock() : game.InventoryOpenBlock;
	}

	private boolean ruleInventoryClick(Game game)
	{
		return game.isContextRuntime() ? game.getRules().isInventoryClick() : game.InventoryClick;
	}

	private boolean ruleDeathOut(Game game)
	{
		return game.isContextRuntime() ? game.getRules().isDeathOut() : game.DeathOut;
	}

	private boolean ruleDeathDropItems(Game game)
	{
		return game.isContextRuntime() ? game.getRules().isDeathDropItems() : game.DeathDropItems;
	}

	private boolean ruleQuitDropItems(Game game)
	{
		return game.isContextRuntime() ? game.getRules().isQuitDropItems() : game.QuitDropItems;
	}

	private boolean rulePrepareFreeze(Game game)
	{
		return game.isContextRuntime() ? game.getRules().isPrepareFreeze() : game.PrepareFreeze;
	}

	/**
	 * Cancel ALL damage (including /kill, void, fire, etc.) when in the lobby world
	 * or when the game is not live. This prevents the ghost-player desync caused by
	 * death/respawn cycles outside of active gameplay.
	 */
	@EventHandler(priority = EventPriority.LOWEST)
	public void GlobalDamageCancel(EntityDamageEvent event)
	{
		if (!(event.getEntity() instanceof Player))
			return;

		Player p = (Player) event.getEntity();
		Game game = Manager.GetGame();

		// No game at all → lobby → cancel everything
		if (game == null)
		{
			event.setCancelled(true);
			return;
		}

		// In the lobby world → cancel everything
		if (event.getEntity().getWorld().getName().equals("world"))
		{
			event.setCancelled(true);
			return;
		}

		// Game not live → cancel everything
		if (game.GetState() != GameState.Live)
		{
			event.setCancelled(true);
			return;
		}
	}

	// onFatalDamage FAKE DEATH SYSTEM — REMOVED
	// This system was the root cause of death/respawn desync (ghost players).
	// It cancelled lethal damage and healed players instantly, but the client never saw
	// a real death, causing an irreconcilable client/server state mismatch.
	//
	// Paper 1.21's GameRule.DO_IMMEDIATE_RESPAWN (set in WorldData.java line 116
	// and GameLobbyManager.java line 131) natively handles instant respawn without
	// showing the death screen. The existing PlayerDeath handler (line 728) and
	// PlayerRespawnEvent in GamePlayerManager handle all game logic correctly.

	@EventHandler(priority = EventPriority.LOW)
	public void DamageEvent(EntityDamageByEntityEvent event)
	{ 
		Game game = Manager.GetGame();
		if (game == null)	
		{
			event.setCancelled(true);
			return;
		}

		org.bukkit.entity.Entity damagee = event.getEntity();
		org.bukkit.entity.Entity damager = event.getDamager();

		// Lobby world — cancel all damage
		if (damagee != null && damagee.getWorld().getName().equals("world"))
		{
			event.setCancelled(true);
			return;
		}

		// DESYNC FIX (Bug 1): Spectator immunity — cancel damage to/from spectators
		if (damagee != null && Manager.isSpectator(damagee))
		{
			event.setCancelled(true);
			if (damagee instanceof org.bukkit.entity.LivingEntity && ((org.bukkit.entity.LivingEntity)damagee).getFireTicks() > 0)
				((org.bukkit.entity.LivingEntity)damagee).setFireTicks(0);
			return;
		}

		if (damager != null && Manager.isSpectator(damager))
		{
			event.setCancelled(true);
			return;
		}

		if (game.isContextRuntime()) {
			if (!game.getRules().isDamage()) {
				event.setCancelled(true);
				return;
			}
		} else {
			if (!game.Damage)
			{
				event.setCancelled(true);
				return;
			}
		}

		if (game.GetState() != GameState.Live)
		{
			event.setCancelled(true);
			return; 
		}

		// Dead/eliminated player immunity
		if (damagee instanceof Player && !game.IsAlive((Player)damagee))
		{
			event.setCancelled(true);
			return;
		}

		if (damager instanceof Player && !game.IsAlive((Player)damager))
		{
			event.setCancelled(true);
			return;
		}
		
		// Entity vs Entity type routing
		if (damagee != null && damager != null)
		{
			// PvP
			if (damagee instanceof Player && damager instanceof Player)
			{
				if (!Manager.canHurt((Player)damagee, (Player)damager))
				{
					event.setCancelled(true);
					return;
				}
			}
			// PvE (player attacking mob)
			else if (damager instanceof Player)
			{
				if (game.isContextRuntime()) {
					if (!game.getRules().isDamagePvE()) {
						event.setCancelled(true);
						return;
					}
				} else {
					if (!game.DamagePvE)
					{
						event.setCancelled(true);
						return;
					}
				}
			}
			// EvP (mob attacking player)
			else if (damagee instanceof Player)
			{
				if (game.isContextRuntime()) {
					if (!game.getRules().isDamageEvP()) {
						event.setCancelled(true);
						return;
					}
				} else {
					if (!game.DamageEvP)
					{
						event.setCancelled(true);
						return;
					}
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void DamageExplosion(EntityDamageByEntityEvent event)
	{ 
		if (event.isCancelled())
			return;

		if (event.getCause() != DamageCause.ENTITY_EXPLOSION && event.getCause() != DamageCause.BLOCK_EXPLOSION)
			return;

		if (!(event.getEntity() instanceof Player)) return;
		Player damagee = (Player) event.getEntity();

		if (!(event.getDamager() instanceof Player)) return;
		Player damager = (Player) event.getDamager();

		if (Manager.canHurt(damagee, damager))
			return;

		event.setCancelled(true);
	}

	

	@EventHandler(priority = EventPriority.LOWEST)
	public void ItemPickupEvent(PlayerPickupItemEvent event)
	{
		Player player = event.getPlayer();

		Game game = Manager.GetGame();

		if (game == null || !game.IsAlive(player) || game.GetState() != GameState.Live)
		{
			event.setCancelled(true);
			return;
		}


		if (game.isContextRuntime()) {
			if (game.getRules().isItemPickup()) {
				if (game.ItemPickupDeny.contains(event.getItem().getItemStack().getType().ordinal())) {
					event.setCancelled(true);
				}
			} else {
				if (!game.ItemPickupAllow.contains(event.getItem().getItemStack().getType().ordinal())) {
					event.setCancelled(true);
				}
			}
		} else {
			if (game.ItemPickup)
			{
				if (game.ItemPickupDeny.contains(event.getItem().getItemStack().getType().ordinal()))
				{
					event.setCancelled(true);
				}
			}
			else
			{					
				if (!game.ItemPickupAllow.contains(event.getItem().getItemStack().getType().ordinal()))
				{
					event.setCancelled(true);
				}
			}
		}

	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void ItemDropEvent(PlayerDropItemEvent event)
	{
		Player player = event.getPlayer();

		Game game = Manager.GetGame();
		if (game == null || !game.IsAlive(player) || game.GetState() != GameState.Live)
		{
			//Only allow ops in creative
			if (!player.isOp() || player.getGameMode() != GameMode.CREATIVE)	
			{
				event.setCancelled(true);
			}

			return;
		}

		if (game.isContextRuntime()) {
			if (game.getRules().isItemDrop()) {
				if (game.ItemDropDeny.contains(event.getItemDrop().getItemStack().getType().ordinal())) {
					event.setCancelled(true);
				}
			} else {
				if (!game.ItemDropAllow.contains(event.getItemDrop().getItemStack().getType().ordinal())) {
					event.setCancelled(true);
				}
			}
		} else {
			if (game.ItemDrop)
			{
				if (game.ItemDropDeny.contains(event.getItemDrop().getItemStack().getType().ordinal()))
				{
					event.setCancelled(true);
				}
			}
			else
			{					
				if (!game.ItemDropAllow.contains(event.getItemDrop().getItemStack().getType().ordinal()))
				{
					event.setCancelled(true);
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void InventoryOpen(InventoryOpenEvent event)
	{
		Game game = Manager.GetGame();
		if (game == null)
			return;
		
		if (!game.InProgress())
			return;
		
		if (!ruleInventoryOpenBlock(game))
		{
			if (event.getInventory().getType() == InventoryType.ANVIL ||
					event.getInventory().getType() == InventoryType.BEACON ||
					event.getInventory().getType() == InventoryType.BREWING ||
					event.getInventory().getType() == InventoryType.DISPENSER ||
					event.getInventory().getType() == InventoryType.DROPPER ||
					event.getInventory().getType() == InventoryType.ENCHANTING ||
					event.getInventory().getType() == InventoryType.FURNACE ||
					event.getInventory().getType() == InventoryType.HOPPER ||
					event.getInventory().getType() == InventoryType.MERCHANT ||
					event.getInventory().getType() == InventoryType.ENDER_CHEST ||
					event.getInventory().getType() == InventoryType.WORKBENCH)
			{
				event.setCancelled(true);
				event.getPlayer().closeInventory();
			}
		}

		if (game.isContextRuntime() && !game.getRules().isInventoryOpenChest())
		{
			if (event.getInventory().getType() == InventoryType.CHEST)
			{
				event.setCancelled(true);
				event.getPlayer().closeInventory();
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void InventoryClick(InventoryClickEvent event)
	{
		Game game = Manager.GetGame();
		if (game == null)
			return;
		
		if (!game.InProgress())
			return;
		
		if (ruleInventoryClick(game))
			return;
		
		Player player = UtilPlayer.searchExact(event.getWhoClicked().getName());
		if (player != null && !game.IsAlive(player))
			return;
	
		if (!game.IsAlive(player))
			return;
		
		if (event.getInventory().getType() == InventoryType.CRAFTING)
		{
			event.setCancelled(true);
			event.getWhoClicked().closeInventory();
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void BlockPlaceEvent(BlockPlaceEvent event)
	{
		Player player = event.getPlayer();

		Game game = Manager.GetGame();
		if (game == null)
		{
			//Only allow ops in creative
			if (!player.isOp() || player.getGameMode() != GameMode.CREATIVE)	
				event.setCancelled(true);
		}
		else
		{
			if (!game.IsAlive(player))
			{
				//Only allow ops in creative
				if (!player.isOp() || player.getGameMode() != GameMode.CREATIVE)	
					event.setCancelled(true);
			}
			// Event Server Allowance
			else if (game.BlockPlaceCreative && player.getGameMode() == GameMode.CREATIVE) 
			{
				return;
			}
			else
			{
				if (game.isContextRuntime())
				{
					if (game.getRules().isBlockPlace())
					{
						if (game.BlockPlaceDeny.contains(event.getBlock().getType().ordinal()))
						{
							event.setCancelled(true);
						}
					}
					else
					{
						if (!game.BlockPlaceAllow.contains(event.getBlock().getType().ordinal()))
						{
							event.setCancelled(true);
						}
					}
				}
				else if (game.BlockPlace)
				{
					if (game.BlockPlaceDeny.contains(event.getBlock().getType().ordinal()))
					{
						event.setCancelled(true);
					}
				}
				else
				{					
					if (!game.BlockPlaceAllow.contains(event.getBlock().getType().ordinal()))
					{
						event.setCancelled(true);
					}
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void BlockBreakEvent(org.bukkit.event.block.BlockBreakEvent event)
	{
		Player player = event.getPlayer();

		Game game = Manager.GetGame();
		if (game == null)
		{
			//Only allow ops in creative
			if (!player.isOp() || player.getGameMode() != GameMode.CREATIVE)	
				event.setCancelled(true);
		}
		else if (game.GetState() == GameState.Live)
		{
			if (!game.IsAlive(player))
			{
				event.setCancelled(true);
			}
			// Event Server Allowance
			else if (game.BlockBreakCreative && player.getGameMode() == GameMode.CREATIVE) 
			{
				return;
			}
			else
			{
				if (game.isContextRuntime())
				{
					if (game.getRules().isBlockBreak())
					{
						if (game.BlockBreakDeny.contains(event.getBlock().getType().ordinal()))
						{
							event.setCancelled(true);
						}
					}
					else
					{
						if (!game.BlockBreakAllow.contains(event.getBlock().getType().ordinal()))
						{
							event.setCancelled(true);
						}
					}
				}
				else if (game.BlockBreak)
				{
					if (game.BlockBreakDeny.contains(event.getBlock().getType().ordinal()))
					{
						event.setCancelled(true);
					}

				}
				else
				{
					if (!game.BlockBreakAllow.contains(event.getBlock().getType().ordinal()))
					{
						event.setCancelled(true);
					}
				}
			}
		}
		else
			event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void PrivateBlockPlace(BlockPlaceEvent event)
	{
		Game game = Manager.GetGame();
		if (game == null)	return;

		if (!game.PrivateBlocks)
			return;

		if (event.isCancelled())
			return;

		if (!UtilBlock.usable(event.getBlockPlaced()))
			return;

		if (event.getBlockPlaced().getType() != Material.CHEST &&
				event.getBlockPlaced().getType() != Material.FURNACE &&
				event.getBlockPlaced().getType() != Material.FURNACE &&
				event.getBlockPlaced().getType() != Material.CRAFTING_TABLE)
			return;

		String privateKey = event.getPlayer().getName();

		//Add Empty
		if (!game.PrivateBlockCount.containsKey(privateKey))
			game.PrivateBlockCount.put(privateKey, 0);

		if (game.PrivateBlockCount.get(privateKey) == 4)
			return;

		game.PrivateBlockMap.put(event.getBlockPlaced().getLocation(), event.getPlayer());
		game.PrivateBlockCount.put(event.getPlayer().getName(), game.PrivateBlockCount.get(event.getPlayer().getName()) + 1);

		if (game.PrivateBlockCount.get(privateKey) == 4)
		{
			event.getPlayer().sendMessage(F.main(game.GetName(), "Protected block limit reached."));
		}		
	}

	@EventHandler(priority = EventPriority.LOW)
	public void PrivateBlockPlaceCancel(BlockPlaceEvent event)
	{
		Game game = Manager.GetGame();
		if (game == null)	return;

		if (!game.PrivateBlocks)
			return;

		if (event.isCancelled())
			return;

		Block block = event.getBlockPlaced();

		if (block.getType() != Material.CHEST)
			return;

		Player player = event.getPlayer();

		BlockFace[] faces = new BlockFace[] {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};

		for (BlockFace face : faces)
		{
			Block other = block.getRelative(face);

			if (other.getType() != Material.CHEST)
				continue;

			if (!game.PrivateBlockMap.containsKey(other.getLocation()))
				continue;

			Player owner = game.PrivateBlockMap.get(other.getLocation());

			if (player.equals(owner))
				continue;

			//Allow Enemy Raiding
			GameTeam ownerTeam = game.GetTeam(owner);
			GameTeam playerTeam = game.GetTeam(player);

			if (ownerTeam != null && playerTeam != null && !ownerTeam.equals(playerTeam))
				continue;

			//Disallow
			UtilPlayer.message(event.getPlayer(), F.main("Game", 
					"You cannot combine " + 
							F.elem(C.cPurple + ItemStackFactory.Instance.GetName(event.getBlock(), false)) + 
							" with " + F.elem(Manager.GetColor(owner) + owner.getName() + ".")));

			event.setCancelled(true);
			return;
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void PrivateBlockBreak(org.bukkit.event.block.BlockBreakEvent event)
	{
		Game game = Manager.GetGame();
		if (game == null)	return;

		if (!game.PrivateBlocks)
			return;

		if (event.isCancelled())
			return;

		if (!game.PrivateBlockMap.containsKey(event.getBlock().getLocation()))
			return;

		Player owner = game.PrivateBlockMap.get(event.getBlock().getLocation());
		Player player = event.getPlayer();

		//Same Team (or no team)
		if (owner.equals(player))
		{
			game.PrivateBlockMap.remove(event.getBlock().getLocation());
		}
		else
		{
			//Allow Enemy Raiding
			GameTeam ownerTeam = game.GetTeam(owner);
			GameTeam playerTeam = game.GetTeam(player);

			if (ownerTeam != null && playerTeam != null && !ownerTeam.equals(playerTeam))
				return;

			//Disallow
			UtilPlayer.message(event.getPlayer(), F.main("Game", 
					F.elem(C.cPurple + ItemStackFactory.Instance.GetName(event.getBlock(), false)) + 
					" belongs to " + F.elem(Manager.GetColor(owner) + owner.getName() + ".")));

			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void PrivateBlockUse(PlayerInteractEvent event)
	{
		Game game = Manager.GetGame();
		if (game == null)	return;

		if (!game.PrivateBlocks)
			return;

		if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;

		if (!UtilBlock.usable(event.getClickedBlock()))
			return;

		if (event.getClickedBlock().getType() != Material.CHEST &&
				event.getClickedBlock().getType() != Material.FURNACE &&
				event.getClickedBlock().getType() != Material.FURNACE)
			return;

		if (!game.PrivateBlockMap.containsKey(event.getClickedBlock().getLocation()))
			return;

		Player owner = game.PrivateBlockMap.get(event.getClickedBlock().getLocation());
		
		if (!game.IsAlive(owner))
			return;
		
		Player player = event.getPlayer();

		if (owner.equals(player))
		{
			return;
		}
		else
		{
			//Allow Enemy Raiding
			GameTeam ownerTeam = game.GetTeam(owner);
			GameTeam playerTeam = game.GetTeam(player);

			if (ownerTeam != null && playerTeam != null && !ownerTeam.equals(playerTeam))
				return;

			//Disallow
			UtilPlayer.message(event.getPlayer(), F.main("Game", 
					F.elem(C.cPurple + ItemStackFactory.Instance.GetName(event.getClickedBlock(), false)) + 
					" belongs to " + F.elem(Manager.GetColor(owner) + owner.getName() + ".")));

			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void PrivateBlockCrumble(PerkDestructorBlockEvent event)
	{
		Game game = Manager.GetGame();
		if (game == null)	return;

		if (!game.PrivateBlocks)
			return;

		if (event.isCancelled())
			return;

		if (!game.PrivateBlockMap.containsKey(event.getBlock().getLocation()))
			return;

		Player owner = game.PrivateBlockMap.get(event.getBlock().getLocation());
		Player player = event.getPlayer();

		//Same Team (or no team)
		if (owner.equals(player))
		{
			game.PrivateBlockMap.remove(event.getBlock().getLocation());
		}
		else
		{
			//Allow Enemy Raiding
			GameTeam ownerTeam = game.GetTeam(owner);
			GameTeam playerTeam = game.GetTeam(player);

			if (ownerTeam != null && playerTeam != null && !ownerTeam.equals(playerTeam))
				return;

			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void PlayerDeath(PlayerDeathEvent event)
	{
		final Game game = Manager.GetGame();
		if (game == null)	return;

		final Player player = event.getEntity();

		// Reset damage state on death
		player.setFireTicks(0);
		player.setFallDistance(0);

		//Drop Items
		if (ruleDeathDropItems(game))
			for (ItemStack stack : event.getDrops())
				player.getWorld().dropItem(player.getLocation(), stack);
		event.getDrops().clear();

		//DEATH OUT
		if (game.GetState() == GameState.Live && ruleDeathOut(game))
		{
			//Event
			PlayerDeathOutEvent outEvent = new PlayerDeathOutEvent(game, player);
			UtilServer.getServer().getPluginManager().callEvent(outEvent);

			if (!outEvent.isCancelled())
			{
				game.SetPlayerState(player, PlayerState.OUT);
			}
		}

		if (game.IsAlive(player))
		{
			// STILL ALIVE (DeathOut=false) — respawn in game
			double time = game.DeathSpectateSecs;
			if (game.GetTeam(player) != null)
				if (game.GetTeam(player).GetRespawnTime() > time)
					time = game.GetTeam(player).GetRespawnTime();

			if (time > 0)
			{
				// Delayed respawn - player awaits at base
				ArcadeBootstrap.getInstance().getPlayerStateApplier().cleanState(player);
				UtilPlayer.message(player, (LangManager.get().isThai(player) ? "\u00A7c\u00A7l\u0e04\u0e38\u0e13\u0e08\u0e30\u0e40\u0e01\u0e34\u0e14\u0e43\u0e2b\u0e21\u0e48\u0e43\u0e19 " + time + " \u0e27\u0e34\u0e19\u0e32\u0e17\u0e35..." : "\u00A7c\u00A7lYou will respawn in " + time + " seconds..."));
				UtilTextMiddle.display(null, (LangManager.get().isThai(player) ? "\u00A7c\u0e23\u0e2d\u0e40\u0e01\u0e34\u0e14\u0e43\u0e2b\u0e21\u0e48\u0e43\u0e19 " + time + " \u0e27\u0e34\u0e19\u0e32\u0e17\u0e35..." : "\u00A7cRespawning in " + time + " seconds..."), 5, 40, 5, player);

				Manager.getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(Manager.getPlugin(), new Runnable()
				{
					public void run()
					{
						if (!player.isOnline()) return;

						if (game.IsAlive(player))
						{
							game.RespawnPlayer(player);
						}
						 
						player.setFireTicks(0);
						player.setVelocity(new Vector(0,0,0));
					}
				}, (int)(time * 20d));
			}
			else
			{
				// Immediate game respawn (HideSeek, etc.)
				Manager.getPlugin().getServer().getScheduler().runTaskLater(Manager.getPlugin(), () -> {
					if (!player.isOnline()) return;
					if (!game.IsAlive(player)) return;

					game.RespawnPlayer(player);
					player.setFireTicks(0);
					player.setVelocity(new Vector(0,0,0));
				}, 1L); // Just 1 tick delay
			}
		}
		else
		{
			// Context lifecycle is centralized in ArcadeTransitionCoordinator.
		}
	}
	@EventHandler
	public void PlayerQuit(PlayerQuitEvent event)
	{
		Game game = Manager.GetGame();
		if (game == null)	return;

		//Drop Items
		if (ruleQuitDropItems(game))
			if (game.IsLive())
				if (game.IsAlive(event.getPlayer()))
					UtilInv.drop(event.getPlayer(), true);
			
		//Remove Kit
		game.RemoveTeamPreference(event.getPlayer());
		game.GetPlayerKits().remove(event.getPlayer());
		game.GetEssence().remove(event.getPlayer());
		
		if (!game.QuitOut)
			return;
		
		GameTeam team = game.GetTeam(event.getPlayer());
		
		if (team != null)
		{
			if (game.InProgress())
				team.SetPlayerState(event.getPlayer(), PlayerState.OUT);
			else
				team.RemovePlayer(event.getPlayer());
		}
	}
	
	@EventHandler
	public void PlayerMoveCancel(PlayerMoveEvent event)
	{
		Game game = Manager.GetGame();
		if (game == null || game.GetState() != GameState.Prepare)
			return;

		if (!rulePrepareFreeze(game))
			return;
		
		if (!game.IsAlive(event.getPlayer()))
			return;

		if (UtilMath.offset2d(event.getFrom(), event.getTo()) <= 0)
			return;

		event.getFrom().setPitch(event.getTo().getPitch());
		event.getFrom().setYaw(event.getTo().getYaw());

		event.setTo(event.getFrom());
	}

	@EventHandler
	public void PlayerHealthFoodUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;

		Game game = Manager.GetGame();
		
		//Not Playing
		for (Player player : UtilServer.getPlayers())
		{
			if (game == null || game.GetState() == GameState.Recruit || !game.IsAlive(player))
			{
				// Skip spectators: calling setHealth on a SPECTATOR in Paper 1.21
				// sends conflicting health packets and can cause client desync
				if (player.getGameMode() == GameMode.SPECTATOR || player.hasMetadata("spectator"))
					continue;

				// CRITICAL FIX: Do not set health on dead players, or it will 'resurrect' their body 
				// and prevent them from clicking 'Respawn' on the vanilla death screen!
				if (player.isDead())
					continue;

				player.setMaxHealth(20);
				player.setHealth(20);
				player.setFoodLevel(20);
			}
		}

		if (game == null || !game.IsLive())
			return;

		if (game.HungerSet != -1)
			for (Player player : game.GetPlayers(true))
				if (!player.isDead())
					player.setFoodLevel(game.HungerSet);

		if (game.HealthSet != -1)
			for (Player player : game.GetPlayers(true))
				if (!player.isDead())
					player.setHealth(game.HealthSet);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void PlayerBoundaryCheck(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;

		Game game = Manager.GetGame();
		if (game == null || game.GetState() != GameState.Live)
			return;
		
		for (Player player : UtilServer.getPlayers())
		{
			if (!game.isInsideMap(player))
			{	
				//Riding a Projectile, edgecase
				if (player.getVehicle() != null && player.getVehicle() instanceof Projectile)
				{
					player.getVehicle().remove();
					player.leaveVehicle();
				}
				
				if (Manager.isSpectator(player))
				{
					player.teleport(game.GetSpectatorLocation());
				}
				else if (game.IsAlive(player))
				{
					if (!game.WorldBoundaryKill)
					{
						UtilPlayer.message(player, C.cRed + C.Bold + "WARNING: " + C.cWhite + C.Bold + "RETURN TO PLAYABLE AREA!");

						if (game.GetType() != GameType.Event)
						{
							if (player.getLocation().getY() > game.WorldData.MaxY)
								UtilAction.velocity(player, UtilAlg.getTrajectory2d(player.getLocation(), game.GetSpectatorLocation()), 1, true, 0, 0, 10, true);
							else
								UtilAction.velocity(player, UtilAlg.getTrajectory2d(player.getLocation(), game.GetSpectatorLocation()), 1, true, 0.4, 0, 10, true);
						}
						
						// Border damage using vanilla API
						player.damage(4);
						player.getWorld().playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 2f, 1f);
					}
					else
					{
						// Instant kill at boundary
						player.damage(9001);
					}
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void WorldCreature(CreatureSpawnEvent event)
	{	
		Game game = Manager.GetGame();
		if (game == null)	return;

		if (game.WorldData != null && game.WorldData.World != null)
		{
			if (event.getLocation().getWorld().equals(game.WorldData.World))
			{
				if (event.getSpawnReason() == org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason.NATURAL 
					|| event.getSpawnReason() == org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason.CHUNK_GEN)
				{
					event.setCancelled(true);
					return;
				}

				if (!game.CreatureAllow && !game.CreatureAllowOverride)
				{
					event.setCancelled(true);
				}
			}
		}
	} 
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void StaffDisqualify(HouziTeleportEvent event)
	{
		if (event.isCancelled())
			return;
		
		if (Manager.GetClients().Get(event.getPlayer()).GetRank().Has(Rank.DEVELOPER))
			return;
		
		Game game = Manager.GetGame();
		if (game == null)	return;
		
		if (!game.IsLive())
			return;
		
		if (!game.TeleportsDisqualify)
			return;

		if (!game.IsAlive(event.getPlayer()))
			return;

		//Remove Kit
		game.RemoveTeamPreference(event.getPlayer());
		game.GetPlayerKits().remove(event.getPlayer());
		game.GetEssence().remove(event.getPlayer());
		
		//Remove Team
		GameTeam team = game.GetTeam(event.getPlayer());
		if (team != null)
		{
			if (game.InProgress())
				team.SetPlayerState(event.getPlayer(), PlayerState.OUT);
			else
				team.RemovePlayer(event.getPlayer());
		}
		
		// DESYNC FIX (Bug 2): Don't call addSpectator on dead player
		if (!event.getPlayer().isDead())
		{
			Manager.addSpectator(event.getPlayer(), false);
		}
	}

	@EventHandler
	public void WorldTime(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		Game game = Manager.GetGame();
		if (game == null)	return;

		if (game.WorldTimeSet != -1)
		{
			if (game.WorldData != null)
			{
				if (game.WorldData.World != null)
				{
					game.WorldData.World.setTime(game.WorldTimeSet);
				}
			}
		}
	}
	
	@EventHandler
	public void WorldWeather(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
			return;

		Game game = Manager.GetGame();
		if (game == null)	return;

		if (!game.WorldWeatherEnabled)
		{
			if (game.WorldData != null)
			{
				if (game.WorldData.World != null)
				{
					game.WorldData.World.setStorm(false);
					game.WorldData.World.setThundering(false);
				}
			}
		}
	}

	@EventHandler
	public void WorldWaterDamage(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;
		
		Game game = Manager.GetGame();
		if (game == null)	return;

		if (!game.IsLive())
			return;		
		
		if (game.WorldWaterDamage <= 0)
		{
			if (!game.WorldData.GetCustomLocs("WATER_DAMAGE").isEmpty())
			{
				game.WorldWaterDamage = 4;
			}
			else
			{
				return;
			}
		}

		for (GameTeam team : game.GetTeamList())
			for (Player player : team.GetPlayers(true))
				if (player.getLocation().getBlock().getType() == Material.WATER || player.getLocation().getBlock().getRelative(BlockFace.UP).getType() == Material.WATER)
				{
					//Damage Event
					player.damage(game.WorldWaterDamage);
					player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_SPLASH, 0.8f, 1f + (float) Math.random() / 2);
				}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void WorldSoilTrample(PlayerInteractEvent event)
	{
		if (event.getAction() != Action.PHYSICAL)
			return;
	
		Game game = Manager.GetGame();
		if (game == null)	return;
		
		if (game.WorldSoilTrample)
			return;

		if (event.getPlayer().getLocation().getBlock().getRelative(BlockFace.DOWN).getType() != Material.FARMLAND)
			// return;

		event.setCancelled(true);
	}
	
	@EventHandler
	public void WorldBlockBurn(BlockBurnEvent event)
	{
		Game game = Manager.GetGame();
		if (game == null)	return;

		if (game.WorldBlockBurn)
			return;
		
		event.setCancelled(true);
	}
	
	@EventHandler
	public void WorldFireSpread(BlockIgniteEvent event)
	{
		Game game = Manager.GetGame();
		if (game == null)	return;

		if (game.WorldFireSpread)
			return;
		
		event.setCancelled(true);
	}
	
	@EventHandler
	public void WorldLeavesDecay(LeavesDecayEvent event)
	{
		Game game = Manager.GetGame();
		if (game == null)	return;

		if (game.WorldLeavesDecay)
			return;
		
		event.setCancelled(true);
	}
	
	@EventHandler
	public void SpectatorMessage(UpdateEvent event)
	{
		if (Manager.IsTournamentServer())
			return;
		
		if (Manager.GetGame() == null)
			return;
				 
		if (!Manager.GetGame().AnnounceStay)
			return;
		
		if (!Manager.GetGame().IsLive())
			return;		
		
		if (event.getType() != UpdateType.SEC)
			return;
		
		if (Manager.GetGame().GetType() == GameType.MineStrike)
			return;
	
		for (Player player : UtilServer.getPlayers())
		{
			if (Manager.IsAlive(player))
				continue;
			
			if (Recharge.Instance.use(player, "Dont Quit Message", 30000, false, false))
			{
				UtilPlayer.message(player, " ");
				UtilPlayer.message(player, LangManager.get().isThai(player) ? "\u00A77\u0e04\u0e38\u0e13\u0e15\u0e32\u0e22\u0e41\u0e25\u0e49\u0e27\u0e41\u0e15\u0e48 \u00A76\u00A7l\u0e2d\u0e22\u0e48\u0e32\u0e40\u0e1e\u0e34\u0e48\u0e07\u0e2d\u0e2d\u0e01\u0e40\u0e01\u0e21!" : "\u00A77You died but \u00A76\u00A7ldon't leave the game!");
				UtilPlayer.message(player, LangManager.get().isThai(player) ? "\u00A77\u0e40\u0e01\u0e21\u0e16\u0e31\u0e14\u0e44\u0e1b\u0e01\u0e33\u0e25\u0e31\u0e07\u0e08\u0e30\u0e40\u0e23\u0e34\u0e48\u0e21\u0e43\u0e19\u0e44\u0e21\u0e48\u0e0a\u0e49\u0e32..." : "\u00A77The next game will start soon...");
			}
		}
	}
	
	@EventHandler
	public void AntiHackStrict(GameStateChangeEvent event) 
	{
		if (AntiHack.Instance == null)
			return;

		if (event.GetState() == GameState.Prepare || event.GetState() == GameState.Live)
			AntiHack.Instance.setStrict(event.GetGame().StrictAntiHack);
		else
			AntiHack.Instance.setStrict(true);
	}
	

	
	@EventHandler
	public void versionUpdateCheck(UpdateEvent event)
	{
		if (Manager.GetGame() == null)
			return;
				 
		if (!Manager.GetGame().VersionRequire1_8)
			return;
		
		if (event.getType() != UpdateType.SEC)
			return;

		for (Player player : UtilServer.getPlayers())
		{
			if (!UtilPlayer.is1_8(player))
				versionKick(player);
		}
	}
	
	@EventHandler
	public void versionJoinCheck(PlayerJoinEvent event)
	{
		if (Manager.GetGame() == null)
			return;
				 
		if (!Manager.GetGame().VersionRequire1_8)
			return;
		
		if (!UtilPlayer.is1_8(event.getPlayer()))
			versionKick(event.getPlayer());
	}
	
	public void versionKick(Player player)
	{
		if (Manager.GetGame() == null)
			return;
				 
		if (Manager.GetGame().GetType().getResourcePackUrl() == null)
			return;
		
		UtilPlayer.message(player, "  ");
// 		UtilPlayer.message(player, C.cGold + C.Bold + Manager.GetGame().GetType().getName() + " requires you to be using Minecraft 1.8!");
		UtilPlayer.message(player, "  ");

		player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 10f, 1f);
		Manager.GetPortal().sendPlayerToServer(player, "Lobby");
	}
	
	@EventHandler
	public void resourceInform(PlayerJoinEvent event)
	{
		if (Manager.GetGame() == null)
			return;
				 
		if (Manager.GetGame().GetType().getResourcePackUrl() == null)
			return;
		
// 		UtilTextMiddle.display(C.cGold + C.Bold + Manager.GetGame().GetType().getName(), "Make sure you accept the Resource Pack", 20, 120, 20, event.getPlayer());
	}
}
