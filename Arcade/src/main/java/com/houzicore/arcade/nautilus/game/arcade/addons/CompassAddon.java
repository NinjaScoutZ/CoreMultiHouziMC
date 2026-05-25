package com.houzicore.arcade.nautilus.game.arcade.addons;

import java.util.HashSet;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import com.houzicore.shared.MiniPlugin;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilGear;
import com.houzicore.shared.common.util.UtilInv;
import com.houzicore.shared.common.util.UtilMath;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.common.util.UtilTextBottom;
import com.houzicore.shared.recharge.Recharge;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.events.GameStateChangeEvent;
import com.houzicore.arcade.nautilus.game.arcade.game.Game;
import com.houzicore.arcade.nautilus.game.arcade.game.GameTeam;
import com.houzicore.arcade.nautilus.game.arcade.gui.spectatorMenu.SpectatorShop;

public class CompassAddon extends MiniPlugin
{
	public ArcadeManager Manager;

	private SpectatorShop _spectatorShop;

	public CompassAddon(JavaPlugin plugin, ArcadeManager manager)
	{
		super("Compass Addon", plugin);

		Manager = manager;

		_spectatorShop = new SpectatorShop(this, manager, manager.GetClients(), manager.GetDonation());
	}

	@EventHandler
	public void Update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTER)
			return;

		if (Manager.GetGame() == null)
			return;

		if (!Manager.GetGame().IsLive())
			return;

		// Give Spectators the Spectator Menu/Teleport compass
		for (Player player : UtilServer.getPlayers())
		{
			if (Manager.isSpectator(player))
			{
				if (!player.getInventory().contains(Material.COMPASS))
				{
					if (player.getOpenInventory() == null || player.getOpenInventory().getCursor() == null || player.getOpenInventory().getCursor().getType() != Material.COMPASS)
					{
						ItemStack stack = new ItemStack(Material.COMPASS);
						ItemMeta itemMeta = stack.getItemMeta();
						itemMeta.setDisplayName("§b🔮 §7" + com.houzicore.shared.common.util.UtilText.toSmallCaps("Spectator Menu"));
						stack.setItemMeta(itemMeta);
						player.getInventory().setItem(0, stack);
					}
				}
			}
		}
	}

	@EventHandler
	public void SpectatorTeleport(PlayerInteractEvent event)
	{
		if (Manager.GetGame() == null)
			return;
		
		if (event.getAction() == Action.PHYSICAL)
		    return;

		Player player = event.getPlayer();

		if (!UtilGear.isMat(player.getItemInHand(), Material.COMPASS))
			return;

		if (Manager.GetGame().IsAlive(player))
			return;

		event.setCancelled(true);

		if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK)
		{
			// Teleport to nearest player when you left click compass

			if (!Recharge.Instance.use(player, "Spectate", 5000, true, false))
			{
				return;
			}

			spectateNearestPlayer(player);
		}
		else
		{
			// Right click - open spectator menu

			_spectatorShop.attemptShopOpen(player);
		}
	}

	private void spectateNearestPlayer(Player spectator)
	{
		GameTeam team = Manager.GetGame().GetTeam(spectator);

		Player target = null;
		double bestDist = 0;

		for (Player other : Manager.GetGame().GetPlayers(true))
		{
			GameTeam otherTeam = Manager.GetGame().GetTeam(other);

			//Same Team (Not Solo Game) && Alive
			if (Manager.GetGame().GetTeamList().size() > 1 && (team != null && team.equals(otherTeam)) && Manager.GetGame().IsAlive(spectator))
				continue;

			double dist = UtilMath.offset(spectator, other);

			if (target == null || dist < bestDist)
			{
				target = other;
				bestDist = dist;
			}
		}

		if (target != null)
		{
			spectator.teleport(target.getLocation().add(0, 1, 0));
		}
	}

	@EventHandler
	public void closeShop(GameStateChangeEvent event)
	{
		// Close shop when a game ends
		if (event.GetState().equals(Game.GameState.End))
		{
			for (Player player : UtilServer.getPlayers())
			{
				if (_spectatorShop.isPlayerInShop(player))
					player.closeInventory();
			}
		}
	}

	@EventHandler
	public void updateShop(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
			return;

		_spectatorShop.update();
	}

	private String getDirectionArrow(org.bukkit.Location from, org.bukkit.Location to)
	{
		if (from == null || to == null) return "•";
		org.bukkit.util.Vector direction = to.toVector().subtract(from.toVector());
		if (direction.lengthSquared() == 0) return "•";
		
		double angle = Math.atan2(direction.getZ(), direction.getX());
		double yaw = (angle * 180 / Math.PI) - 90;
		double relativeYaw = yaw - from.getYaw();
		relativeYaw = (relativeYaw % 360 + 360) % 360;
		
		if (relativeYaw >= 337.5 || relativeYaw < 22.5) return "↑";
		if (relativeYaw >= 22.5 && relativeYaw < 67.5) return "↗";
		if (relativeYaw >= 67.5 && relativeYaw < 112.5) return "→";
		if (relativeYaw >= 112.5 && relativeYaw < 157.5) return "↘";
		if (relativeYaw >= 157.5 && relativeYaw < 202.5) return "↓";
		if (relativeYaw >= 202.5 && relativeYaw < 247.5) return "↙";
		if (relativeYaw >= 247.5 && relativeYaw < 292.5) return "←";
		if (relativeYaw >= 292.5 && relativeYaw < 337.5) return "↖";
		
		return "•";
	}

}
