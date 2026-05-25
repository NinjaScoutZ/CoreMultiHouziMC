package com.houzicore.shared.core.shop.confirmation;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.houzicore.shared.MiniPlugin;
import com.houzicore.shared.core.shop.ShopBase;
import com.houzicore.shared.core.shop.item.IButton;
import com.houzicore.shared.core.shop.item.ShopItem;
import com.houzicore.shared.core.shop.page.ShopPageBase;
import com.houzicore.shared.core.lang.LangManager;

/**
 * HouziCore-style purchase confirmation page.
 *
 * Layout (27-slot / 3-row):
 *   Row 0 (slots 0-8):  top border — title glass panes
 *   Row 1 (slots 9-17): [confirm] [confirm] [confirm] [gap] [preview] [gap] [cancel] [cancel] [cancel]
 *   Row 2 (slots 18-26): bottom border
 *
 * Preview item is centered at slot 13.
 * CONFIRM block: slots 9, 10, 11 (green).
 * CANCEL block:  slots 15, 16, 17 (red).
 */
public class ConfirmationPage<PluginType extends MiniPlugin, ShopType extends ShopBase<PluginType>>
		extends ShopPageBase<PluginType, ShopType> implements Runnable, ConfirmationCallback {

	private int _taskId;
	private ShopPageBase<PluginType, ShopType> _returnPage;
	private ItemStack _displayItem;
	private int _progressCount;
	private ConfirmationProcessor _processor;
	private boolean _processing;

	// ── Slot constants ────────────────────────────────────────────────
	private static final int PREVIEW_SLOT = 13;
	private static final int[] CONFIRM_SLOTS = {9, 10, 11};
	private static final int[] CANCEL_SLOTS  = {15, 16, 17};

	public ConfirmationPage(Player player, ShopPageBase<PluginType, ShopType> returnPage,
			ConfirmationProcessor processor, ItemStack displayItem, String name) {
		super(returnPage.getPlugin(), returnPage.getShop(), returnPage.getClientManager(),
				returnPage.getDonationManager(), buildTitle(player, name), player, 27);
		_returnPage = returnPage;
		_displayItem = displayItem;
		_processor = processor;
		buildPage();
	}

	public ConfirmationPage(Player player, ShopPageBase<PluginType, ShopType> returnPage,
			ConfirmationProcessor processor, ItemStack displayItem) {
		this(player, returnPage, processor, displayItem, null);
	}

	private static String buildTitle(Player player, String override) {
		if (override != null && !override.isEmpty()) return override;
		return LangManager.get().isThai(player)
				? ChatColor.DARK_GRAY + "ยืนยันการซื้อ"
				: ChatColor.DARK_GRAY + "Purchase Confirmation";
	}

	@Override
	protected void buildPage() {
		// ── Blue glass border ────────────────────────────────────────
		ItemStack bluePane = buildPane(Material.LIGHT_BLUE_STAINED_GLASS_PANE,
				ChatColor.AQUA + com.houzicore.shared.core.common.BrandConfig.mainServerName());
		for (int i = 0; i < 27; i++) {
			int row = i / 9;
			int col = i % 9;
			if (row == 0 || row == 2 || col == 0 || col == 8) {
				getInventory().setItem(i, bluePane);
			}
		}

		// ── Item preview (center) ────────────────────────────────────
		if (_displayItem != null) {
			getInventory().setItem(PREVIEW_SLOT, _displayItem);
		}

		// ── CONFIRM buttons (left cluster) ───────────────────────────
		boolean isThai = LangManager.get().isThai(getPlayer());
		String confirmLabel = isThai
				? ChatColor.GREEN + "" + ChatColor.BOLD + "✔ ยืนยัน"
				: ChatColor.GREEN + "" + ChatColor.BOLD + "✔ Confirm";
		String confirmLore1 = isThai ? ChatColor.GRAY + "คลิกเพื่อซื้อ" : ChatColor.GRAY + "Click to purchase";

		ItemStack confirmItem = buildButton(Material.LIME_STAINED_GLASS_PANE, confirmLabel,
				new String[]{confirmLore1});

		for (int slot : CONFIRM_SLOTS) {
			addButton(slot, confirmItem, (player, clickType) -> okClicked(player));
		}

		// ── CANCEL buttons (right cluster) ───────────────────────────
		String cancelLabel = isThai
				? ChatColor.RED + "" + ChatColor.BOLD + "✖ ยกเลิก"
				: ChatColor.RED + "" + ChatColor.BOLD + "✖ Cancel";
		String cancelLore1 = isThai ? ChatColor.GRAY + "กลับสู่หน้าก่อนหน้า" : ChatColor.GRAY + "Return to previous page";

		ItemStack cancelItem = buildButton(Material.RED_STAINED_GLASS_PANE, cancelLabel,
				new String[]{cancelLore1});

		for (int slot : CANCEL_SLOTS) {
			addButton(slot, cancelItem, (player, clickType) -> cancelClicked(player));
		}

		// ── Gaps (center-left and center-right of row 1) ────────────
		getInventory().setItem(12, bluePane);
		getInventory().setItem(14, bluePane);

		_processor.init(this);
	}

	// ── Item builders ─────────────────────────────────────────────────

	private static ItemStack buildPane(Material material, String name) {
		ItemStack item = new ItemStack(material);
		ItemMeta meta = item.getItemMeta();
		if (meta != null) {
			meta.setDisplayName(name);
			item.setItemMeta(meta);
		}
		return item;
	}

	private static ItemStack buildButton(Material material, String name, String[] lore) {
		ShopItem item = new ShopItem(material, name, lore, 1, false, true);
		return item;
	}

	// ── Action handlers ───────────────────────────────────────────────

	protected void okClicked(Player player) {
		processTransaction();
	}

	protected void cancelClicked(Player player) {
		Bukkit.getScheduler().cancelTask(_taskId);
		if (_returnPage != null) {
			getShop().openPageForPlayer(player, _returnPage);
		} else {
			player.closeInventory();
		}
	}

	// ── Transaction processing ────────────────────────────────────────

	private void processTransaction() {
		// Lock the confirm/cancel buttons during processing
		for (int slot : CONFIRM_SLOTS) clear(slot);
		for (int slot : CANCEL_SLOTS)  clear(slot);

		// Show a processing indicator at preview slot
		getInventory().setItem(PREVIEW_SLOT, buildPane(Material.YELLOW_STAINED_GLASS_PANE,
				ChatColor.YELLOW + "" + ChatColor.BOLD + (LangManager.get().isThai(getPlayer())
						? "⏳ กำลังดำเนินการ..."
						: "⏳ Processing...")));

		_processing = true;
		_processor.process(this);
		_taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(getPlugin().getPlugin(), this, 2L, 2L);
	}

	// ── Result pages ──────────────────────────────────────────────────

	private void buildResultOverlay(Material material, ChatColor color, String label) {
		ItemStack overlay = buildPane(material, color + "" + ChatColor.BOLD + label);
		for (int i = 0; i < 27; i++) {
			addButton(i, overlay, (player, clickType) -> cancelClicked(player));
		}
	}

	private void buildErrorPage(String message) {
		boolean isThai = LangManager.get().isThai(getPlayer());
		String errorLabel = isThai ? "✖ เกิดข้อผิดพลาด" : "✖ Error";
		ItemStack overlay = new ShopItem(Material.RED_STAINED_GLASS_PANE,
				ChatColor.RED + "" + ChatColor.BOLD + errorLabel,
				new String[]{ChatColor.WHITE + message}, 1, false, true);
		for (int i = 0; i < 27; i++) {
			addButton(i, overlay, (player, clickType) -> cancelClicked(player));
		}
		if (getPlayer() != null) {
			getPlayer().playSound(getPlayer().getLocation(), Sound.ENTITY_BLAZE_DEATH, 1, 0.1f);
		}
	}

	private void buildSuccessPage(String message) {
		boolean isThai = LangManager.get().isThai(getPlayer());
		String successLabel = isThai ? "✔ สำเร็จ!" : "✔ Success!";
		ItemStack overlay = new ShopItem(Material.LIME_STAINED_GLASS_PANE,
				ChatColor.GREEN + "" + ChatColor.BOLD + successLabel,
				new String[]{ChatColor.WHITE + message, " ", ChatColor.GRAY + (isThai ? "คลิกเพื่อปิด" : "Click to close")},
				1, false, true);
		for (int i = 0; i < 27; i++) {
			addButton(i, overlay, (player, clickType) -> cancelClicked(player));
		}
		if (getPlayer() != null) {
			getPlayer().playSound(getPlayer().getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 0.9f);
		}
	}

	// ── ConfirmationCallback ──────────────────────────────────────────

	@Override
	public void resolve(String message) {
		_processing = false;
		buildSuccessPage(message);
		_progressCount = 0;
	}

	@Override
	public void reject(String message) {
		_processing = false;
		buildErrorPage(message);
		_progressCount = 0;
	}

	// ── Lifecycle ─────────────────────────────────────────────────────

	@Override
	public void playerClosed() {
		super.playerClosed();
		Bukkit.getScheduler().cancelTask(_taskId);
		if (_returnPage != null && getShop() != null) {
			getShop().setCurrentPageForPlayer(getPlayer(), _returnPage);
		}
	}

	@Override
	public void run() {
		if (_processing) {
			// Pulse the processing indicator
			boolean even = (_progressCount % 4) < 2;
			Material mat = even ? Material.YELLOW_STAINED_GLASS_PANE : Material.ORANGE_STAINED_GLASS_PANE;
			boolean isThai = (getPlayer() != null) && LangManager.get().isThai(getPlayer());
			String label = isThai ? "⏳ กำลังดำเนินการ..." : "⏳ Processing...";
			if (getPlayer() != null) {
				getInventory().setItem(PREVIEW_SLOT,
						buildPane(mat, ChatColor.YELLOW + "" + ChatColor.BOLD + label));
			}
		} else {
			if (_progressCount >= 20) {
				try {
					Bukkit.getScheduler().cancelTask(_taskId);
					if (_returnPage != null && getShop() != null) {
						getShop().openPageForPlayer(getPlayer(), _returnPage);
					} else if (getPlayer() != null) {
						getPlayer().closeInventory();
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					dispose();
				}
			}
		}
		_progressCount++;
	}

	@Override
	public void dispose() {
		super.dispose();
		Bukkit.getScheduler().cancelTask(_taskId);
	}
}
