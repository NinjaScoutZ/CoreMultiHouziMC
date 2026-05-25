package com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.general;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilBlock;
import com.houzicore.shared.common.util.UtilInv;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.core.itemstack.ItemBuilder;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.shared.core.damage.CustomDamageEvent;

import com.houzicore.arcade.nautilus.game.arcade.events.GameStateChangeEvent;
import com.houzicore.arcade.nautilus.game.arcade.events.PlayerGameRespawnEvent;
import com.houzicore.arcade.nautilus.game.arcade.events.PlayerKitGiveEvent;
import com.houzicore.arcade.nautilus.game.arcade.game.Game.GameState;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.BedwarsModule;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.Bedwars;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.shop.BedwarsResource;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.team.BedwarsTeam;

public class BedwarsPlayerModule extends BedwarsModule
{

	public static final ItemStack RUNE_OF_HOLDING = new ItemBuilder(Material.PLAYER_HEAD)
			.setTitle(C.cPurple + C.Bold + "Rune of Holding")
			.addLore("", "Preserves your inventory on death", "Uses: " + C.cRed + "1")
			.setUnbreakable(true)
			.build();

	private final Set<Block> _placedBlocks;
	// Used to store the inventory of a player when using the rune of holding
	// Inventory[0], Armour[1]
	private final Map<Player, ItemStack[][]> _storedInventory;

	public BedwarsPlayerModule(Bedwars game)
	{
		super(game);

		_placedBlocks = new HashSet<>();
		_storedInventory = new HashMap<>();
	}

	public void cleanup()
	{
		_placedBlocks.clear();
		_storedInventory.clear();
	}

	@EventHandler
	public void prepare(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Prepare)
		{
			return;
		}

		for (Player player : _game.GetPlayers(true))
		{
			player.getEnderChest().clear();
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void blockPlaceRecord(BlockPlaceEvent event)
	{
		if (event.isCancelled())
		{
			return;
		}

		_placedBlocks.add(event.getBlock());
	}

	@EventHandler
	public void blockBreak(BlockBreakEvent event)
	{
		if (!_game.IsLive())
		{
			return;
		}

		Player player = event.getPlayer();
		Block block = event.getBlock();

		if (UtilPlayer.isSpectator(player))
		{
			event.setCancelled(true);
		}
		else if (UtilBlock.airFoliage(block) || block.getType().name().contains("LEAVES"))
		{
			event.setCancelled(true);
			block.setType(Material.AIR);
		}
		else if (!_placedBlocks.contains(block))
		{
			event.setCancelled(true);
			player.sendMessage(F.main("Game", "You cannot break blocks that have not been placed by players."));
		}
	}

	@EventHandler
	public void blockPlacePrevent(BlockPlaceEvent event)
	{
		if (!_game.IsLive())
		{
			return;
		}

		Player player = event.getPlayer();

		if (UtilPlayer.isSpectator(player))
		{
			event.setCancelled(true);
		}
		else if (event.getItemInHand().getType() == RUNE_OF_HOLDING.getType())
		{
			event.setCancelled(true);
			player.sendMessage(F.main("Game", "You cannot place a " + F.name(RUNE_OF_HOLDING.getItemMeta().getDisplayName()) + "."));
		}
		else if (_game.isNearSpawn(event.getBlock()))
		{
			event.setCancelled(true);
			player.sendMessage(F.main("Game", "You cannot place blocks that near to a player spawn."));
		}
	}

	@EventHandler
	public void pickupItem(PlayerPickupItemEvent event)
	{
		if (!_game.IsLive())
		{
			return;
		}

		if (UtilPlayer.isSpectator(event.getPlayer()))
		{
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void saveInventory(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
		{
			return;
		}

		_game.GetPlayers(true).forEach(player ->
		{
			if (UtilPlayer.isSpectator(player))
			{
				return;
			}

			PlayerInventory inventory = player.getInventory();

			inventory.remove(Material.GLASS_BOTTLE);

			if (!UtilInv.contains(player, RUNE_OF_HOLDING.getType(), (byte) 0, 1))
			{
				return;
			}

			_storedInventory.put(player, new ItemStack[][]
					{
							inventory.getContents().clone(),
							inventory.getArmorContents().clone()
					});
		});
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void playerDeath(PlayerDeathEvent event)
	{
		Player player = event.getEntity();

		if (UtilInv.contains(player, null, RUNE_OF_HOLDING.getType(), (byte) 0, 1, false, false))
		{
			_game.getArcadeManager().runSyncLater(() ->
			{
				if (!player.isOnline() || !_game.IsAlive(player))
				{
					return;
				}

				player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 0.8F);
				player.sendMessage(F.main("Game", "You consumed one " + F.name(RUNE_OF_HOLDING.getItemMeta().getDisplayName()) + ". You will respawn with your inventory."));
			}, 5);
		}
		else
		{
			_storedInventory.remove(player);
		}

		BedwarsTeam bedTeam = _game.getBedwarsTeamModule().getBedwarsTeam(_game.GetTeam(player));

		// If respawning drop nothing
		if (bedTeam == null || bedTeam.canRespawn())
		{
			event.getDrops().clear();
		}
		// If out of the game drop only resources
		else
		{
			event.getDrops().removeIf(itemStack ->
			{
				for (BedwarsResource resource : BedwarsResource.values())
				{
					if (resource.getItemStack().isSimilar(itemStack))
					{
						return false;
					}
				}

				return true;
			});
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void kitGiveItems(PlayerKitGiveEvent event)
	{
		if (!_game.IsLive())
		{
			return;
		}

		Player player = event.GetPlayer();
		ItemStack[][] storedInventory = _storedInventory.remove(player);

		if (storedInventory != null)
		{
			PlayerInventory inventory = player.getInventory();
			ItemStack[] contents = storedInventory[0];

			for (int i = 0; i < contents.length; i++)
			{
				ItemStack itemStack = contents[i];

				if (itemStack != null && itemStack.getAmount() < 1)
				{
					contents[i] = null;
				}
			}

			inventory.setContents(storedInventory[0]);
			inventory.setArmorContents(storedInventory[1]);
			player.updateInventory();

			UtilInv.remove(player, RUNE_OF_HOLDING.getType(), (byte) 0, 1);
		}
	}

	@EventHandler
	public void playerRespawn(PlayerGameRespawnEvent event)
	{
		Player player = event.GetPlayer();

		_game.getArcadeManager().runSyncLater(() -> player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, (4 + _game.getDeathsInLastMinute(player) * 2) * 20, 0)), 5);
	}

	@EventHandler
	public void spawnProtection(CustomDamageEvent event)
	{
		if (event.IsCancelled())
		{
			return;
		}

		Player damager = event.GetDamagerPlayer(true);
		Player damagee = event.GetDamageePlayer();

		if (damager != null)
		{
			damager.removePotionEffect(PotionEffectType.RESISTANCE);
		}

		if (damagee != null && damagee.hasPotionEffect(PotionEffectType.RESISTANCE))
		{
			event.SetCancelled("Damage Resistance");
		}
	}

	@EventHandler
	public void playerQuit(PlayerQuitEvent event)
	{
		_storedInventory.remove(event.getPlayer());
	}

	@EventHandler
	public void itemCraft(CraftItemEvent event)
	{
		if (!_game.IsLive())
		{
			return;
		}

		event.setCancelled(true);
	}

	@EventHandler
	public void anvilOpen(InventoryOpenEvent event)
	{
		if (!_game.IsLive() || !(event.getInventory() instanceof AnvilInventory))
		{
			return;
		}

		event.setCancelled(true);
	}

	public Set<Block> getPlacedBlocks()
	{
		return _placedBlocks;
	}

	public boolean isUsingRuneOfHolding(Player player)
	{
		return _storedInventory.containsKey(player);
	}

}
