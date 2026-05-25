package com.houzicore.shared.core.cosmetic.ui.page;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.houzicore.shared.core.lang.LangManager;
import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.common.util.UtilText;
import com.houzicore.shared.common.util.UtilTextMiddle;
import com.houzicore.shared.core.cosmetic.CosmeticManager;
import com.houzicore.shared.core.cosmetic.ui.CosmeticShop;
import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.core.reward.Reward;
import com.houzicore.shared.core.reward.RewardType;
import com.houzicore.shared.core.shop.item.IButton;
import com.houzicore.shared.core.shop.item.ShopItem;
import com.houzicore.shared.core.shop.page.ShopPageBase;
import com.houzicore.shared.core.treasure.TreasureManager;

/**
 * TreasurePage — Animated chest-selection GUI
 *
 * Flow:
 *  1. A rotating border animation plays for ~140 ticks
 *  2. 5 colored chests appear one-by-one (ticks 30–120)
 *  3. At tick 140, "Select a Chest" is shown and _canSelectChest flips true
 *  4. Player clicks a chest → key consumed, rewards rolled + delivered, celebration shown
 *
 * This page must be updated externally each tick via TreasureLocation's Bukkit task.
 */
public class TreasurePage extends ShopPageBase<CosmeticManager, CosmeticShop> {

	private static final int[] ROTATION_SLOTS = new int[] {
			0, 1, 2, 3, 4, 5, 6, 7, 8,
			17, 26, 35, 34, 33, 32, 31, 30,
			29, 28, 27, 18, 9
	};
	private static final List<Integer> CHEST_SLOTS = Arrays.asList(
			3 + 9 + 9, 6 + 9 + 9, 2 + 9 + 9, 4 + 9 + 9, 5 + 9 + 9);
	private static final List<ChatColor> CHEST_COLORS = Arrays.asList(
			ChatColor.RED, ChatColor.GREEN, ChatColor.YELLOW, ChatColor.BLUE, ChatColor.AQUA, ChatColor.GOLD);

	private final TreasureManager _treasureManager;

	// Animation state
	private int _ticks;
	private final Random _random;
	private short _rotationColorOne = 0;
	private short _rotationColorTwo = 0;
	private final boolean _rotationForwardOne = true;
	private final boolean _rotationForwardTwo = false;
	private int _currentIndexOne = 4;
	private int _currentIndexTwo = 4;

	// Is the animation done — can the player select a chest?
	public boolean _canSelectChest = false;

	// Queues for Chest Colors and Slots
	private final LinkedList<ChatColor> _colors;
	private final LinkedList<Integer> _chestSlots;

	public TreasurePage(CosmeticManager plugin, CosmeticShop shop, CoreClientManager clientManager,
			DonationManager donationManager, TreasureManager treasureManager, String name, Player player) {
		super(plugin, shop, clientManager, donationManager, name, player, 9 * 4);
		_random = new Random();
		_treasureManager = treasureManager;

		// Shuffle random colors and chest positions
		_colors = new LinkedList<>(CHEST_COLORS);
		_chestSlots = new LinkedList<>(CHEST_SLOTS);
		Collections.shuffle(_colors, _random);
		Collections.shuffle(_chestSlots, _random);
	}

	@Override
	protected void buildPage() {

		_rotationColorOne = _ticks % 2 == 0 ? (short) _random.nextInt(15) : _rotationColorOne;
		_rotationColorTwo = _ticks % 20 == 0 ? (short) _random.nextInt(15) : _rotationColorTwo;

		// Border panes — grey when selecting, white-ish before
		final ItemStack borderPane = new ItemStack(Material.GLASS_PANE, 1, _canSelectChest ? (short) 7 : (short) 15);
		for (int row = 0; row < 4; row++) {
			if (row == 0 || row == 3) {
				for (int column = 0; column < 9; column++) {
					setItem(column, row, borderPane);
				}
			} else {
				setItem(0, row, borderPane);
				setItem(8, row, borderPane);
			}
		}

		if (_ticks <= 21) {
			rotateBorderPanes();
		}

		// Phase 1: Clunk sound on open
		if (_ticks == 0) {
			getPlayer().playSound(getPlayer().getEyeLocation(), Sound.BLOCK_ANVIL_USE, 4, 1);

		// Phase 2: Chest creaks open
		} else if (_ticks == 20) {
			getPlayer().playSound(getPlayer().getEyeLocation(), Sound.BLOCK_CHEST_OPEN, 4, 1);

		// Phase 3: Add colored chests one-by-one every 20 ticks
		} else if (_ticks >= 30 && _ticks <= 120 && _ticks % 20 == 0) {
			if (!_colors.isEmpty() && !_chestSlots.isEmpty()) {
				final ChatColor color = _colors.poll();
				final int slot = _chestSlots.poll();
				String colorName = color.name().toLowerCase();
				colorName = colorName.substring(0, 1).toUpperCase() + colorName.substring(1);
				final String chestName = color + colorName + " Chest";
				final String[] lore = new String[] {
						ChatColor.RESET + "" + ChatColor.GRAY + "§8──────────────────────",
						ChatColor.RESET + "" + ChatColor.WHITE + LangManager.get().get(getPlayer(), "gui.treasure.click_open_this"),
						ChatColor.RESET + "" + ChatColor.GRAY + "§8──────────────────────"
				};

				getPlayer().playSound(getPlayer().getEyeLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 4, 1);

				addButton(slot, new ShopItem(Material.CHEST, chestName, lore, 1, false), new IButton() {
					@Override
					public void onClick(Player player, ClickType clickType) {
						if (!_canSelectChest) return;

						// Roll and deliver rewards — TreasureManager handles key deduction
						Reward[] rewards = _treasureManager.getRewards(player, RewardType.OldChest);
						for (Reward reward : rewards) {
							if (reward != null) {
								reward.giveReward("Treasure", player);
							}
						}

						// Celebration sounds — layered
						player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);

						// Show reward title (UtilTextMiddle.display takes title, subtitle, player... )
						UtilTextMiddle.display("§6§l✦ " + UtilText.toSmallCaps("TREASURE OPENED"),
								"§7" + rewards.length + " rewards delivered!",
								10, 40, 20, player);

						// Prevent double-clicking
						_canSelectChest = false;
					}
				});
			}

		// Phase 4: Selection prompt
		} else if (_ticks == 140) {
			getPlayer().playSound(getPlayer().getEyeLocation(), Sound.ENTITY_PLAYER_LEVELUP, 4, 1F);

			final ItemStack is = new ItemStack(Material.BOOK);
			final ItemMeta meta = is.getItemMeta();
			meta.setDisplayName(ChatColor.RESET + "§6§l✦ " + UtilText.toSmallCaps(LangManager.get().get(getPlayer(), "gui.treasure.select_chest")));
			is.setItemMeta(meta);
			setItem(9 + 4, is);
			addGlow(9 + 4);
			_canSelectChest = true;
		}

		_ticks++;
	}

	public void rotateBorderPanes() {
		final ItemStack whitePane = new ItemStack(Material.GLASS_PANE, 1, (short) 0);
		final ItemStack paneOne = new ItemStack(Material.GLASS_PANE, 1, _rotationColorOne);
		final ItemStack paneTwo = new ItemStack(Material.GLASS_PANE, 1, _rotationColorTwo);

		_currentIndexOne = (_currentIndexOne + (_rotationForwardOne ? 1 : -1)) % ROTATION_SLOTS.length;
		if (_currentIndexOne < 0) _currentIndexOne += ROTATION_SLOTS.length;

		_currentIndexTwo = (_currentIndexTwo + (_rotationForwardTwo ? 1 : -1)) % ROTATION_SLOTS.length;
		if (_currentIndexTwo < 0) _currentIndexTwo += ROTATION_SLOTS.length;

		if (_currentIndexOne == _currentIndexTwo) {
			setItem(ROTATION_SLOTS[_currentIndexOne], whitePane);
		} else {
			setItem(ROTATION_SLOTS[_currentIndexOne], paneOne);
			setItem(ROTATION_SLOTS[_currentIndexTwo], paneTwo);
		}
	}

	public void update() {
		buildPage();
	}
}
