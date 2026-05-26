package com.houzicore.shared.core.treasure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerVelocityEvent;

import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilAction;
import com.houzicore.shared.common.util.UtilAlg;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilTextMiddle;
import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.core.hologram.Hologram;
import com.houzicore.shared.core.hologram.HologramManager;
import com.houzicore.shared.core.reward.Reward;
import com.houzicore.shared.core.treasure.event.TreasureFinishEvent;
import com.houzicore.shared.core.treasure.event.TreasureStartEvent;
import com.houzicore.shared.core.treasure.gui.TreasureShop;
import com.houzicore.shared.core.displayentity.DisplayEntityManager;
import com.houzicore.shared.core.displayentity.DisplayModel;
import com.houzicore.shared.core.displayentity.ModelAnimation;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class TreasureLocation implements Listener {
	private static final MiniMessage mm = MiniMessage.miniMessage();
	private static final String QI_MAIN_TITLE = "<gradient:#ffcc00:#ff5555><bold>☯ แท่นหลอมโอสถสวรรค์ ☯</bold></gradient>";
	private final TreasureManager _treasureManager;
	private final TreasureInventoryService _treasureInventoryService;
	private final Hologram _hologram;
	private final HologramManager _hologramManager;
	private final Map<Player, Hologram> _playerHolograms;
	private final Map<UUID, Integer> _lastKnownReadyCounts;
	private Treasure _currentTreasure;
	private final Block _chestBlock;
	private final TreasureShop _shop;
	private final Location _resetLocation;
	private final DisplayEntityManager _displayEntityManager;
	private final DisplayModel _idleHologramModel;
	private boolean _hologramPulse;

	public TreasureLocation(TreasureManager treasureManager, TreasureInventoryService treasureInventoryService,
			CoreClientManager clientManager, DonationManager donationManager, Block chestBlock, Block[] chestSpawns,
			Location resetLocation, HologramManager hologramManager, DisplayEntityManager displayEntityManager) {
		_treasureManager = treasureManager;
		_resetLocation = resetLocation;
		_treasureInventoryService = treasureInventoryService;
		_chestBlock = chestBlock;
		_hologramManager = hologramManager;
		_currentTreasure = null;
		_hologram = new Hologram(_hologramManager, chestBlock.getLocation().add(0.5, 2.5, 0.5),
				com.houzicore.shared.common.util.HouziColorParser.parse("<gradient:#ffcc00:#ff5555><bold>┃ คลิกขวาหลอมโอสถวิเศษ ┃</bold></gradient>"));
		_playerHolograms = new HashMap<>();
		_lastKnownReadyCounts = new HashMap<>();
		_displayEntityManager = displayEntityManager;
		
		for (org.bukkit.entity.Entity e : _resetLocation.getWorld().getNearbyEntities(_chestBlock.getLocation().add(0.5, 0.5, 0.5), 1.0, 2.0, 1.0)) {
			if (e.getType() == org.bukkit.entity.EntityType.INTERACTION || e.getType() == org.bukkit.entity.EntityType.ITEM_DISPLAY) {
				e.remove();
			}
		}
		
		com.houzicore.shared.core.displayentity.DisplayPart chestPart = com.houzicore.shared.core.displayentity.DisplayPart.item(Material.CAULDRON)
			.itemTransform(org.bukkit.entity.ItemDisplay.ItemDisplayTransform.HEAD);
		chestPart.scale(new org.joml.Vector3f(1.6f, 1.6f, 1.6f));
		_idleHologramModel = new DisplayModel("treasure_idle_" + chestBlock.hashCode(), java.util.Collections.singletonList(chestPart));
		_idleHologramModel.setAnimation(ModelAnimation.rotateY(2.5f));
		_idleHologramModel.addInteractionBox(0.0, -0.8, 0.0, 1.8f, 1.8f);
		_displayEntityManager.addModel(_idleHologramModel);
		
		setHoloChestVisible(true);
		_shop = new TreasureShop(treasureManager, _treasureInventoryService, clientManager, donationManager, this);
	}

	public void attemptOpenTreasure(Player player, TreasureType treasureType) {
		if (isTreasureInProgress()) {
			player.sendMessage(F.main("สำนักเซียน", com.houzicore.shared.core.lang.LangManager.get().get(player, "treasure.wait_current")));
			return;
		}

		if (!chargeAccount(player, treasureType)) {
			player.sendMessage(F.main("สำนักเซียน", com.houzicore.shared.core.lang.LangManager.get().get(player, "treasure.no_chests")));
			return;
		}

		final TreasureStartEvent event = new TreasureStartEvent(player);
		Bukkit.getPluginManager().callEvent(event);

		if (event.isCancelled())
			return;

		setHoloChestVisible(false);

		if (treasureType == TreasureType.ANCIENT || treasureType == TreasureType.MYTHICAL) {
			for (Player oPlayer : com.houzicore.shared.common.util.UtilServer.getPlayers()) {
				boolean oIsThai = com.houzicore.shared.core.lang.LangManager.get().isThai(oPlayer);
				String displayChestName = treasureType.getDisplay(oIsThai);
				String rarityColor = switch (treasureType) {
					case ANCIENT -> "#ffaa00";
					case MYTHICAL -> "#ff5555";
					default -> "gray";
				};
				oPlayer.sendMessage(mm.deserialize(QI_MAIN_TITLE + " <gray>ผู้บำเพ็ญพลังปราณ</gray> <green>" + player.getName() + "</green> <gray>กำลังทำพิธีเปิดคัมภีร์ลับแดนสวรรค์ เกรด:</gray> <" + rarityColor + "><bold>[" + displayChestName + "]</bold></" + rarityColor + ">"));
			}
		}

		final Reward[] rewards = _treasureManager.getRewards(player, treasureType.getRewardType());
		final Treasure treasure = new Treasure(player, new java.util.Random(), rewards, _chestBlock, new Block[0], treasureType,
				_hologramManager, _displayEntityManager);
		_currentTreasure = treasure;

		final Location teleportLocation = treasure.getCenterBlock().getLocation().add(0.5, 0, 0.5);
		teleportLocation.setPitch(player.getLocation().getPitch());
		teleportLocation.setYaw(player.getLocation().getYaw());

		for (final Entity entity : player.getNearbyEntities(3, 3, 3)) {
			if (!entity.equals(player)) {
				UtilAction.velocity(entity,
						UtilAlg.getTrajectory(entity.getLocation(), treasure.getCenterBlock().getLocation()).multiply(-1),
						1.2, true, 0.6, 0, 1.0, true);
			}
		}

		player.teleport(teleportLocation);
	}

	@EventHandler
	public void cancelMove(PlayerMoveEvent event) {
		final Player player = event.getPlayer();
		if (isTreasureInProgress()) {
			if (_currentTreasure.getPlayer().equals(player)) {
				final Treasure treasure = _currentTreasure;
				final Location centerLocation = treasure.getCenterBlock().getLocation().add(0.5, 0.5, 0.5);
				if (event.getTo().distanceSquared(centerLocation) > 49) {
					final Location newTo = event.getFrom();
					newTo.setPitch(event.getTo().getPitch());
					newTo.setYaw(event.getTo().getYaw());
					event.setTo(newTo);
				}
			} else {
				final Location fromLocation = event.getFrom();
				final Location toLocation = event.getTo();
				final Location centerLocation = _currentTreasure.getCenterBlock().getLocation().add(0.5, 1.5, 0.5);
				final double toDistanceFromCenter = centerLocation.distanceSquared(toLocation);

				if (toDistanceFromCenter <= 16) {
					final double fromDistanceFromCenter = centerLocation.distanceSquared(fromLocation);
					if (toDistanceFromCenter < fromDistanceFromCenter) {
						final Location spawnLocation = new Location(player.getWorld(), 0, 64, 0);
						UtilAction.velocity(player,
								UtilAlg.getTrajectory(player.getLocation(), spawnLocation).multiply(-1), 1.5, true, 0.8,
								0, 1.0, true);
					}
				}
			}
		}
	}

	@EventHandler
	public void cancelVelocity(PlayerVelocityEvent event) {
		final Player player = event.getPlayer();
		if (isTreasureInProgress() && _currentTreasure.getPlayer().equals(player)) {
			event.setCancelled(true);
		}
	}

	private boolean chargeAccount(Player player, TreasureType treasureType) {
		return _treasureInventoryService.consumeChest(player, treasureType);
	}

	public void cleanup() {
		if (_currentTreasure != null) {
			_currentTreasure.cleanup();
			_currentTreasure = null;
		}
		clearPlayerHolograms();
		_lastKnownReadyCounts.clear();
		_hologram.stop();
		if (_idleHologramModel != null) {
			_idleHologramModel.remove();
			_displayEntityManager.removeModel(_idleHologramModel);
		}
	}

	public Treasure getCurrentTreasure() {
		return _currentTreasure;
	}

	public Hologram getHologram() {
		return _hologram;
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void interact(PlayerInteractEvent event) {
		final Player player = event.getPlayer();
		if (isTreasureInProgress()) {
			if (_currentTreasure.getPlayer().equals(player)) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void inventoryOpen(InventoryOpenEvent event) {
		final String title = event.getView().getTitle();
		if (title != null && title.contains("Punish"))
			return;

		if (isTreasureInProgress() && event.getPlayer().equals(_currentTreasure.getPlayer())) {
			event.setCancelled(true);
		}
	}

	public boolean isTreasureInProgress() {
		return _currentTreasure != null;
	}

	@EventHandler
	public void onInteract(PlayerInteractEvent event) {
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock().equals(_chestBlock)) {
			openShop(event.getPlayer());
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onInteractEntity(org.bukkit.event.player.PlayerInteractEntityEvent event) {
		handleInteraction(event.getPlayer(), event.getRightClicked(), event);
	}

	@EventHandler
	public void onDamageEntity(org.bukkit.event.entity.EntityDamageByEntityEvent event) {
		if (event.getDamager() instanceof Player player) {
			if (handleInteraction(player, event.getEntity(), null)) {
				event.setCancelled(true);
			}
		}
	}

	private boolean handleInteraction(Player player, org.bukkit.entity.Entity clicked, org.bukkit.event.player.PlayerInteractEntityEvent event) {
		if (clicked instanceof org.bukkit.entity.Interaction interaction) {
			org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey("houzicore", "bde_interact");
			if (interaction.getPersistentDataContainer().has(key, org.bukkit.persistence.PersistentDataType.STRING)) {
				String modelId = interaction.getPersistentDataContainer().get(key, org.bukkit.persistence.PersistentDataType.STRING);
				if (("treasure_idle_" + _chestBlock.hashCode()).equals(modelId)) {
					openShop(player);
					if (event != null) event.setCancelled(true);
					return true;
				}
			}
		}
		return false;
	}

	@EventHandler
	public void onTreasureFinish(TreasureFinishEvent event) {
		if (event.getTreasure().equals(_currentTreasure)) {
			final Player player = _currentTreasure.getPlayer();
			player.teleport(_resetLocation);
			_currentTreasure = null;
			setHoloChestVisible(true);
		}
	}

	public void openShop(Player player) {
		_shop.attemptShopOpen(player);
	}

	@EventHandler
	public void quit(PlayerQuitEvent event) {
		removePlayerHologram(event.getPlayer());
		_lastKnownReadyCounts.remove(event.getPlayer().getUniqueId());
		if (isTreasureInProgress() && _currentTreasure.getPlayer().equals(event.getPlayer())) {
			reset();
		}
	}

	public void reset() {
		cleanup();
		setHoloChestVisible(true);
	}

	private void setHoloChestVisible(boolean visible) {
		if (visible) {
			_hologram.stop();
			updatePlayerHolograms();
			_chestBlock.setType(Material.AIR);
			if (_idleHologramModel != null && !_idleHologramModel.isSpawned()) {
				_idleHologramModel.spawn(_chestBlock.getLocation().add(0.5, 1.2, 0.5));
			}
		} else {
			_hologram.stop();
			clearPlayerHolograms();
			_chestBlock.setType(Material.AIR);
			if (_idleHologramModel != null) {
				_idleHologramModel.remove();
			}
		}
	}

	@EventHandler
	public void update(UpdateEvent event) {
		if (event.getType() == UpdateType.FASTER && !isTreasureInProgress()) {
			updatePlayerHolograms();
			return;
		}

		if (event.getType() != UpdateType.TICK)
			return;

		if (isTreasureInProgress()) {
			final Treasure treasure = _currentTreasure;

			treasure.update();

			if (!treasure.getPlayer().isOnline() || (treasure.isFinished() && !treasure.hasActiveAnimations() && treasure.getFinishedTickCount() >= 20)) {
				treasure.cleanup();

				final TreasureFinishEvent finishEvent = new TreasureFinishEvent(treasure.getPlayer(), treasure);
				Bukkit.getPluginManager().callEvent(finishEvent);
			}
		}
	}

	private void updatePlayerHolograms() {
		_hologramPulse = !_hologramPulse;

		for (Player player : _chestBlock.getWorld().getPlayers()) {
			updateReadyNotifications(player);

			if (player.getLocation().distanceSquared(_chestBlock.getLocation().add(0.5, 0.5, 0.5)) > 32 * 32) {
				removePlayerHologram(player);
				continue;
			}

			Hologram hologram = _playerHolograms.get(player);
			if (hologram == null || !hologram.isInUse()) {
				hologram = new Hologram(_hologramManager, _chestBlock.getLocation().add(0.5, 2.7, 0.5), "")
						.setHologramTarget(Hologram.HologramTarget.WHITELIST)
						.addPlayer(player)
						.setViewDistance(32)
						.start();
				_playerHolograms.put(player, hologram);
			}

			hologram.setText(buildHologramLines(player));
		}

		List<Player> stale = new ArrayList<Player>(_playerHolograms.keySet());
		for (Player player : stale) {
			if (!player.isOnline() || player.getWorld() != _chestBlock.getWorld()) {
				removePlayerHologram(player);
				_lastKnownReadyCounts.remove(player.getUniqueId());
			}
		}
	}

	private void updateReadyNotifications(Player player) {
		int totalOwned = getTotalOwnedChests(player);
		Integer lastKnown = _lastKnownReadyCounts.get(player.getUniqueId());

		if (lastKnown == null) {
			_lastKnownReadyCounts.put(player.getUniqueId(), totalOwned);
			return;
		}

		if (totalOwned > lastKnown) {
			sendReadyNotification(player, totalOwned);
		}

		_lastKnownReadyCounts.put(player.getUniqueId(), totalOwned);
	}

	private void sendReadyNotification(Player player, int totalOwned) {
		TreasureType spotlightType = getHighestReadyType(player);
		String spotlightName = spotlightType != null ? spotlightType.getPlainName(player) : com.houzicore.shared.common.util.UtilText.toSmallCaps(TreasureLang.get(player, "ui.notify.vault_name", "Treasure Vault"));
		String title = TreasureLang.get(player, "ui.notify.ready_title", "TREASURE READY");
		String subtitle = TreasureLang.get(player, "ui.notify.ready_subtitle", "{0} ready • Top chest: {1}")
				.replace("{0}", String.valueOf(totalOwned))
				.replace("{1}", spotlightName);
		String chatKey = totalOwned == 1 ? "ui.notify.ready_single_chat" : "ui.notify.ready_chat";
		String fallbackChat = totalOwned == 1
				? "{0} is ready to open at the Treasure Vault."
				: "{0} chests are ready to open at the Treasure Vault.";
		String chat = TreasureLang.get(player, chatKey, fallbackChat)
				.replace("{0}", totalOwned == 1 ? spotlightName : String.valueOf(totalOwned));

		player.playSound(player.getLocation(), Sound.BLOCK_ENDER_CHEST_OPEN, 0.9f, 1.15f);
		player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.7f, 1.5f);
		UtilTextMiddle.display(C.cGold + C.Bold + title, C.cYellow + subtitle, 10, 55, 10, player);
		UtilPlayer.message(player, F.main(TreasureLang.get(player, "ui.title", "Treasure"), chat));
	}

	private String[] buildHologramLines(Player player) {
		int totalOwned = getTotalOwnedChests(player);
		String localeOpen = com.houzicore.shared.common.util.HouziColorParser.parse(
				"<GRADIENT:#00AA00,#55FF55,#00AA00> [ คลิกขวาเปิดเตาหลอม ] </GRADIENT>");

		if (totalOwned <= 0) {
			return new String[] {
					_hologramPulse ? "§7🔮 ดวงจิตศิษย์สายนอกว่างเปล่า" : "§8🔮 ดวงจิตศิษย์สายนอกว่างเปล่า",
					"§7กรุณาสะสมคัมภีร์ลับแดนปราณสวรรค์ก่อนทำพิธี",
					localeOpen
			};
		}

		return new String[] {
				(_hologramPulse ? "§b⚡ " : "§3⚡ ") + "มีคัมภีร์พร้อมหลอมโอสถ: " + totalOwned + " เล่ม",
				"§fเคล็ดวิชาที่พร้อมบำเพ็ญเพียรบารมี",
				localeOpen
		};
	}

	private int getTotalOwnedChests(Player player) {
		int total = 0;
		for (TreasureType type : TreasureType.values()) {
			total += _treasureInventoryService.getOwnedCount(player, type);
		}
		return total;
	}

	private String getReadyTypesLine(Player player) {
		List<String> ready = new ArrayList<String>();

		for (TreasureType type : TreasureType.values()) {
			if (_treasureInventoryService.getOwnedCount(player, type) > 0) {
				ready.add(type.getPlainName(player));
			}
		}

		if (ready.isEmpty()) {
			return TreasureLang.get(player, "ui.hologram.none_types", "Buy a chest to begin opening.");
		}

		if (ready.size() <= 2) {
			return String.join(" §7• §f", ready);
		}

		String extra = TreasureLang.get(player, "ui.hologram.more_types", "+{0} more").replace("{0}", String.valueOf(ready.size() - 2));
		return ready.get(0) + " §7• §f" + ready.get(1) + " §7• §f" + extra;
	}

	private TreasureType getHighestReadyType(Player player) {
		TreasureType[] treasureTypes = TreasureType.values();
		for (int i = treasureTypes.length - 1; i >= 0; i--) {
			if (_treasureInventoryService.getOwnedCount(player, treasureTypes[i]) > 0) {
				return treasureTypes[i];
			}
		}
		return null;
	}

	private void removePlayerHologram(Player player) {
		Hologram hologram = _playerHolograms.remove(player);
		if (hologram != null) {
			hologram.stop();
		}
	}

	private void clearPlayerHolograms() {
		for (Hologram hologram : _playerHolograms.values()) {
			hologram.stop();
		}
		_playerHolograms.clear();
	}
}

