package com.houzicore.shared.core.shop.page;

import com.houzicore.shared.MiniPlugin;
import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.server.util.TransactionResponse;
import com.houzicore.shared.core.shop.ShopBase;
import com.houzicore.shared.core.shop.item.IButton;
import com.houzicore.shared.core.shop.item.SalesPackageBase;
import com.houzicore.shared.core.shop.item.ShopItem;
import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.Callback;
import com.houzicore.shared.core.lang.LangManager;
import com.houzicore.shared.common.CurrencyType;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * HouziCore purchase confirmation page (legacy path — used by cosmetic shop, treasure, etc.)
 *
 * Modern 27-slot layout:
 *   Row 0:  blue glass border
 *   Row 1:  [CONFIRM×3] [gap] [item preview] [gap] [CANCEL×3]
 *   Row 2:  blue glass border
 *
 * Preview slot: 13. Confirm: 9,10,11. Cancel: 15,16,17.
 */
public class ConfirmationPage<PluginType extends MiniPlugin, ShopType extends ShopBase<PluginType>>
		extends ShopPageBase<PluginType, ShopType> implements Runnable {

	private final Runnable _runnable;
	private final ShopPageBase<PluginType, ShopType> _returnPage;
	private final SalesPackageBase _salesItem;
	private boolean _processing;
	private int _progressCount;
	private int _taskId;

	// ── Slot layout ───────────────────────────────────────────────────
	private static final int   PREVIEW_SLOT  = 13;
	private static final int[] CONFIRM_SLOTS = {9, 10, 11};
	private static final int[] CANCEL_SLOTS  = {15, 16, 17};

	public ConfirmationPage(PluginType plugin, ShopType shop, CoreClientManager clientManager,
			DonationManager donationManager, Runnable runnable,
			ShopPageBase<PluginType, ShopType> returnPage,
			SalesPackageBase salesItem, CurrencyType currencyType, Player player) {
		super(plugin, shop, clientManager, donationManager, buildTitle(player), player, 27);

		_runnable    = runnable;
		_returnPage  = returnPage;
		_salesItem   = salesItem;
		setCurrencyType(currencyType);

		if (getShop().canPlayerAttemptPurchase(player)) {
			buildPage();
		} else {
			buildErrorPage(ChatColor.RED + LangManager.get().get(player, "shop.error_too_many_invalid") + "\n"
					+ ChatColor.RED + LangManager.get().get(player, "shop.error_wait_retry"));
			_taskId = plugin.getScheduler().scheduleSyncRepeatingTask(plugin.getPlugin(), this, 2L, 2L);
		}
	}

	private static String buildTitle(Player player) {
		return LangManager.get().isThai(player)
				? ChatColor.DARK_GRAY + "ยืนยันการซื้อ"
				: ChatColor.DARK_GRAY + "Purchase Confirmation";
	}

	// ── Page builders ─────────────────────────────────────────────────

	@Override
	protected void buildPage() {
		boolean isThai = LangManager.get().isThai(getPlayer());

		// Blue glass border
		ItemStack bluePane = buildPane(Material.LIGHT_BLUE_STAINED_GLASS_PANE,
				ChatColor.AQUA + com.houzicore.shared.core.common.BrandConfig.mainServerName());
		for (int i = 0; i < 27; i++) {
			int row = i / 9;
			int col = i % 9;
			if (row == 0 || row == 2 || col == 0 || col == 8) {
				getInventory().setItem(i, bluePane);
			}
		}
		// Inner gaps in row 1
		getInventory().setItem(12, bluePane);
		getInventory().setItem(14, bluePane);

		// Item preview
		getInventory().setItem(PREVIEW_SLOT, new ShopItem(
				_salesItem.GetDisplayMaterial(), (byte) 0,
				_salesItem.GetDisplayName(),
				buildPreviewLore(isThai),
				1, false, true));

		// CONFIRM
		String confirmLabel = isThai
				? ChatColor.GREEN + "" + ChatColor.BOLD + "✔ ยืนยัน"
				: ChatColor.GREEN + "" + ChatColor.BOLD + "✔ Confirm";
		String confirmSub = isThai
				? ChatColor.GRAY + "คลิกเพื่อซื้อ"
				: ChatColor.GRAY + "Click to purchase";
		ItemStack confirmItem = new ShopItem(Material.LIME_STAINED_GLASS_PANE,
				confirmLabel, new String[]{confirmSub}, 1, false, true);
		for (int slot : CONFIRM_SLOTS) {
			addButton(slot, confirmItem, (player, clickType) -> okClicked(player));
		}

		// CANCEL
		String cancelLabel = isThai
				? ChatColor.RED + "" + ChatColor.BOLD + "✖ ยกเลิก"
				: ChatColor.RED + "" + ChatColor.BOLD + "✖ Cancel";
		String cancelSub = isThai
				? ChatColor.GRAY + "กลับสู่หน้าก่อนหน้า"
				: ChatColor.GRAY + "Return to previous page";
		ItemStack cancelItem = new ShopItem(Material.RED_STAINED_GLASS_PANE,
				cancelLabel, new String[]{cancelSub}, 1, false, true);
		for (int slot : CANCEL_SLOTS) {
			addButton(slot, cancelItem, (player, clickType) -> cancelClicked(player));
		}
	}

	private String[] buildPreviewLore(boolean isThai) {
		int cost = _salesItem.GetCost(getCurrencyType());
		String currencyName = getCurrencyType() != null ? getCurrencyType().toString() : "Coins";
		String costLine = isThai
				? C.cGray + "ราคา: " + C.cYellow + cost + " " + currencyName
				: C.cGray + "Cost: " + C.cYellow + cost + " " + currencyName;
		return new String[]{" ", costLine};
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
		String closeLore = isThai ? ChatColor.GRAY + "คลิกเพื่อปิด" : ChatColor.GRAY + "Click to close";
		ItemStack overlay = new ShopItem(Material.LIME_STAINED_GLASS_PANE,
				ChatColor.GREEN + "" + ChatColor.BOLD + successLabel,
				new String[]{ChatColor.WHITE + message, " ", closeLore}, 1, false, true);
		for (int i = 0; i < 27; i++) {
			addButton(i, overlay, (player, clickType) -> cancelClicked(player));
		}
		if (getPlayer() != null) {
			getPlayer().playSound(getPlayer().getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 0.9f);
		}
	}

	// ── Helpers ───────────────────────────────────────────────────────

	private static ItemStack buildPane(Material material, String name) {
		ItemStack item = new ItemStack(material);
		ItemMeta meta = item.getItemMeta();
		if (meta != null) {
			meta.setDisplayName(name);
			item.setItemMeta(meta);
		}
		return item;
	}

	// ── Action handlers ───────────────────────────────────────────────

	protected void okClicked(Player player) {
		processTransaction();
	}

	protected void cancelClicked(Player player) {
		getPlugin().getScheduler().cancelTask(_taskId);
		if (_returnPage != null) {
			getShop().openPageForPlayer(player, _returnPage);
		} else {
			player.closeInventory();
		}
	}

	// ── Transaction ───────────────────────────────────────────────────

	private void processTransaction() {
		// Lock buttons
		for (int slot : CONFIRM_SLOTS) clear(slot);
		for (int slot : CANCEL_SLOTS)  clear(slot);

		// Show processing state at preview slot
		boolean isThai = (getPlayer() != null) && LangManager.get().isThai(getPlayer());
		getInventory().setItem(PREVIEW_SLOT, buildPane(Material.YELLOW_STAINED_GLASS_PANE,
				ChatColor.YELLOW + "" + ChatColor.BOLD + (isThai ? "⏳ กำลังดำเนินการ..." : "⏳ Processing...")));

		_processing = true;

		if (_salesItem.IsKnown()) {
			getDonationManager().PurchaseKnownSalesPackage(new Callback<TransactionResponse>() {
				@Override
				public void run(TransactionResponse response) {
					showResultsPage(response);
				}
			}, getPlayer().getName(), getPlayer().getUniqueId(), _salesItem.GetCost(getCurrencyType()),
					_salesItem.GetSalesPackageId());
		} else {
			getDonationManager().PurchaseUnknownSalesPackage(new Callback<TransactionResponse>() {
				@Override
				public void run(TransactionResponse response) {
					showResultsPage(response);
				}
			}, getPlayer().getName(), getClientManager().Get(getPlayer()).getAccountId(), _salesItem.GetName(),
					getCurrencyType() == CurrencyType.Coins, _salesItem.GetCost(getCurrencyType()),
					_salesItem.OneTimePurchase());
		}

		_taskId = getPlugin().getScheduler().scheduleSyncRepeatingTask(getPlugin().getPlugin(), this, 2L, 2L);
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
				} catch (final Exception exception) {
					org.bukkit.Bukkit.getServer().getLogger().log(java.util.logging.Level.SEVERE,
							exception.getMessage(), exception);
				} finally {
					dispose();
				}
			}
		}
		_progressCount++;
	}

	private void showResultsPage(TransactionResponse response) {
		_processing = false;

		switch (response) {
		case Failed:
			buildErrorPage(LangManager.get().get(getPlayer(), "shop.error_processing"));
			getShop().addPlayerProcessError(getPlayer());
			break;
		case AlreadyOwns:
			buildErrorPage(LangManager.get().get(getPlayer(), "shop.error_already_owns"));
			getShop().addPlayerProcessError(getPlayer());
			break;
		case InsufficientFunds:
			buildErrorPage(LangManager.get().get(getPlayer(), "shop.error_insufficient_funds"));
			getShop().addPlayerProcessError(getPlayer());
			break;
		case Success:
			_salesItem.Sold(getPlayer(), getCurrencyType());
			buildSuccessPage(LangManager.get().get(getPlayer(), "shop.success"));

			// Post-success hook (refresh, grant items, etc.) — NOT a second purchase
			if (_runnable != null) {
				_runnable.run();
			}
			break;
		default:
			break;
		}

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
	public void dispose() {
		super.dispose();
		Bukkit.getScheduler().cancelTask(_taskId);
	}
}
