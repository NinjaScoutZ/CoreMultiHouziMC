package com.houzicore.arcade.nautilus.game.arcade.managers;

import com.houzicore.arcade.ArcadeManager;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.GameMode;
import org.bukkit.event.block.Action;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilMath;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.common.util.UtilTextBottom;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.arcade.nautilus.game.arcade.game.Game;

public class GameSpectatorManager implements Listener
{
	ArcadeManager Manager;

	public GameSpectatorManager(ArcadeManager manager)
	{
		Manager = manager;

		Manager.getPluginManager().registerEvents(this, Manager.getPlugin());
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void interactCancel(PlayerInteractEvent event)
	{
		if (Manager.GetGame() == null || !Manager.GetGame().InProgress())
			return;

		Player player = event.getPlayer();

		if (!Manager.GetGame().IsAlive(player))
			event.setCancelled(true);
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void interactEntityCancel(PlayerInteractEntityEvent event)
	{
		if (Manager.GetGame() == null || !Manager.GetGame().InProgress())
			return;

		Player player = event.getPlayer();

		if (!Manager.GetGame().IsAlive(player))
			event.setCancelled(true);
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void vehicleDamage(VehicleDamageEvent event)
	{
		if (Manager.GetGame() == null || !Manager.GetGame().InProgress())
			return;

		if (!(event.getAttacker() instanceof Player))
			return;
		
		Player player = (Player)event.getAttacker();

		if (!Manager.GetGame().IsAlive(player))
			event.setCancelled(true);
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void pickupCancel(org.bukkit.event.entity.EntityPickupItemEvent event)
	{
		if (Manager.GetGame() == null || !Manager.GetGame().InProgress())
			return;

		if (!(event.getEntity() instanceof Player))
			return;

		Player player = (Player)event.getEntity();

		if (!Manager.GetGame().IsAlive(player))
			event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.LOW)
	public void dropCancel(org.bukkit.event.player.PlayerDropItemEvent event)
	{
		if (Manager.GetGame() == null || !Manager.GetGame().InProgress())
			return;

		Player player = event.getPlayer();

		if (!Manager.GetGame().IsAlive(player))
			event.setCancelled(true);
	}

	@EventHandler
	public void giveSpectatorTools(UpdateEvent event) {
		if (event.getType() != UpdateType.FASTER) return;
		Game game = Manager.GetGame();
		if (game == null || !game.IsLive()) return;

		for (Player player : UtilServer.getPlayers()) {
			if (Manager.isSpectator(player)) {
				// Speed Clock (Slot 8)
				if (!player.getInventory().contains(Material.CLOCK)) {
					if (player.getOpenInventory() == null || player.getOpenInventory().getCursor() == null || player.getOpenInventory().getCursor().getType() != Material.CLOCK) {
						ItemStack stack = new ItemStack(Material.CLOCK);
						ItemMeta itemMeta = stack.getItemMeta();
						itemMeta.setDisplayName(C.cGreen + "♦ " + C.cGray + com.houzicore.shared.common.util.UtilText.toSmallCaps("Speed Toggle"));
						stack.setItemMeta(itemMeta);
						player.getInventory().setItem(8, stack);
					}
				}
			}
		}
	}

	@EventHandler
	public void interactTools(PlayerInteractEvent event) {
		Game game = Manager.GetGame();
		if (game == null || !game.IsLive()) return;

		Player player = event.getPlayer();
		if (game.IsAlive(player)) return;

		if (event.getAction() == Action.PHYSICAL) return;

		if (com.houzicore.shared.common.util.UtilGear.isMat(player.getItemInHand(), Material.CLOCK)) {
			event.setCancelled(true);
			if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
				float speed = player.getFlySpeed();
				if (speed >= 0.8f) {
					player.setFlySpeed(0.2f);
					UtilPlayer.message(player, F.main("Spectator", "Speed set to " + F.elem("Normal")));
				} else if (speed >= 0.4f) {
					player.setFlySpeed(0.8f);
					UtilPlayer.message(player, F.main("Spectator", "Speed set to " + F.elem("Maximum")));
				} else {
					player.setFlySpeed(0.4f);
					UtilPlayer.message(player, F.main("Spectator", "Speed set to " + F.elem("Fast")));
				}
				player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 1f, 1.5f);
			}
		}
	}

	@EventHandler
	public void trackPlayers(UpdateEvent event) {
		if (event.getType() != UpdateType.FAST) return;
		Game game = Manager.GetGame();
		if (game == null || !game.IsLive()) return;

		for (Player spec : UtilServer.getPlayers()) {
			if (!Manager.isSpectator(spec)) continue;

			Player target = null;
			double bestDist = 25.0; // Max distance to display stats automatically

			for (Player alive : game.GetPlayers(true)) {
				if (!alive.getWorld().equals(spec.getWorld())) continue;
				double dist = UtilMath.offset(spec, alive);
				if (dist < bestDist) {
					target = alive;
					bestDist = dist;
				}
			}

			if (target != null) {
				String name = target.getName();
				java.util.HashMap<String, Integer> stats = game.GetStats().containsKey(target) ? game.GetStats().get(target) : null;
				int kills = stats != null && stats.containsKey("Kills") ? stats.get("Kills") : 0;
				double hp = Math.ceil(target.getHealth() / 2.0);
				double maxHp = target.getMaxHealth() / 2.0;

				String hpCol = C.cGreen;
				if (hp <= maxHp * 0.3) hpCol = C.cRed;
				else if (hp <= maxHp * 0.6) hpCol = C.cYellow;

				String overlay = "§e" + com.houzicore.shared.common.util.UtilText.toSmallCaps("spectating") + " §d" + name;
				overlay += " §8| §c♥ " + hpCol + (int)hp + "§7/§c" + (int)maxHp;
				overlay += " §8| §c⚔ §f" + kills;

				UtilTextBottom.display(com.houzicore.shared.common.actionbar.ActionBarChannel.GAME_STATUS, overlay, spec);
			}
		}
	}
}
