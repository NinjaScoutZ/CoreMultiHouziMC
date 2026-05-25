package com.houzicore.shared.core.cosmetic;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.java.JavaPlugin;

import com.houzicore.shared.MiniPlugin;
import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.HouziColorParser;
import com.houzicore.shared.common.util.UtilGear;
import com.houzicore.shared.common.util.UtilInv;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.core.cosmetic.ui.CosmeticShop;
import com.houzicore.shared.core.cosmetic.data.CosmeticTransferState;
import com.houzicore.shared.core.cosmetic.data.CosmeticTransferStateRepository;
import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.core.gadget.GadgetManager;
import com.houzicore.shared.core.gadget.types.Gadget;
import com.houzicore.shared.core.gadget.types.GadgetType;
import com.houzicore.shared.core.inventory.InventoryManager;
import com.houzicore.shared.core.itemstack.ItemStackFactory;
import com.houzicore.shared.core.mount.MountManager;
import com.houzicore.shared.core.mount.Mount;
import com.houzicore.shared.core.pet.PetManager;
import com.houzicore.shared.core.treasure.TreasureManager;

public class CosmeticManager extends MiniPlugin {
	private static final int TRANSFER_STATE_TIMEOUT_SECONDS = 60 * 10;

	private final InventoryManager _inventoryManager;
	private final GadgetManager _gadgetManager;
	private final MountManager _mountManager;
	private final PetManager _petManager;
	private final TreasureManager _treasureManager;
	private final CosmeticTransferStateRepository _transferStateRepository;

	private final CosmeticShop _shop;

	private boolean _showInterface = true;
	private int _interfaceSlot = 4;

	public CosmeticManager(JavaPlugin plugin, CoreClientManager clientManager, DonationManager donationManager,
			InventoryManager inventoryManager, GadgetManager gadgetManager, MountManager mountManager,
			PetManager petManager, TreasureManager treasureManager) {
		super("Cosmetic Manager", plugin);

		_inventoryManager = inventoryManager;
		_gadgetManager = gadgetManager;
		_mountManager = mountManager;
		_petManager = petManager;
		_treasureManager = treasureManager;
		_transferStateRepository = new CosmeticTransferStateRepository();

		_shop = new CosmeticShop(this, clientManager, donationManager, _moduleName);
	}

	public void disableItemsForGame() {
		_gadgetManager.DisableAll();
		_mountManager.DisableAll();
		_petManager.DisableAll();
	}

	public GadgetManager getGadgetManager() {
		return _gadgetManager;
	}

	public InventoryManager getInventoryManager() {
		return _inventoryManager;
	}

	public MountManager getMountManager() {
		return _mountManager;
	}

	public PetManager getPetManager() {
		return _petManager;
	}

	public TreasureManager getTreasureManager() {
		return _treasureManager;
	}

	public void giveInterfaceItem(Player player) {
		if (!UtilGear.isMat(player.getInventory().getItem(_interfaceSlot), Material.CHEST)) {
			player.getInventory().setItem(_interfaceSlot, createInterfaceItem(player));

			_gadgetManager.redisplayActiveItem(player);

			UtilInv.Update(player);
		}
	}

	public boolean isShowingInterface() {
		return _showInterface;
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Bukkit.getScheduler().runTaskLater(getPlugin(), () -> restoreTransferState(event.getPlayer()), 40L);

		if (!_showInterface)
			return;

		giveInterfaceItem(event.getPlayer());
	}

	@EventHandler
	public void openShop(PlayerInteractEvent event) {
		if (!_showInterface)
			return;

		if (event.getHand() != EquipmentSlot.HAND)
			return;

		if (event.hasItem() && event.getItem().getType() == Material.CHEST) {
			event.setCancelled(true);

			_shop.attemptShopOpen(event.getPlayer());
		}
	}

	@EventHandler
	public void orderThatChest(final PlayerDropItemEvent event) {
		if (!_showInterface)
			return;

		if (event.getItemDrop().getItemStack().getType() == Material.CHEST) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(getPlugin(), new Runnable() {
				@Override
				public void run() {
					if (event.getPlayer().isOnline()) {
						event.getPlayer().getInventory().remove(Material.CHEST);
						event.getPlayer().getInventory().setItem(_interfaceSlot, createInterfaceItem(event.getPlayer()));
						event.getPlayer().updateInventory();
					}
				}
			});
		}
	}

	public void setActive(boolean showInterface) {
		_showInterface = showInterface;

		if (!showInterface) {
			for (final Player player : UtilServer.getPlayers()) {
				if (player.getOpenInventory().getTopInventory().getHolder() != player) {
					player.closeInventory();
				}
			}
		}
	}

	public void setHideParticles(boolean b) {
		_gadgetManager.setHideParticles(b);
	}

	public void setInterfaceSlot(int i) {
		_interfaceSlot = i;

		_gadgetManager.setActiveItemSlot(i - 1);
	}

	public void showInterface(boolean showInterface) {
		final boolean changed = _showInterface == showInterface;

		_showInterface = showInterface;

		if (changed) {
			for (final Player player : Bukkit.getOnlinePlayers()) {
				if (_showInterface) {
					player.getInventory().setItem(_interfaceSlot, createInterfaceItem(player));
				} else {
					player.getInventory().setItem(_interfaceSlot, null);
				}
			}
		}
	}

	@EventHandler(priority = org.bukkit.event.EventPriority.LOWEST)
	public void onPlayerQuit(org.bukkit.event.player.PlayerQuitEvent event) {
		saveTransferState(event.getPlayer());
	}

	private void saveTransferState(Player player) {
		if (player == null) {
			return;
		}

		List<String> activeGadgets = _gadgetManager.getActiveSnapshot(player);
		String activeMount = _mountManager.getActiveMountName(player);
		org.bukkit.entity.EntityType activePetType = _petManager.getActivePetType(player);

		if (activeGadgets.isEmpty() && activeMount == null && activePetType == null) {
			_transferStateRepository.removeElement(player.getName());
			return;
		}

		_transferStateRepository.addElement(new CosmeticTransferState(
				player.getName(),
				activeGadgets,
				activeMount,
				activePetType == null ? null : activePetType.name()), TRANSFER_STATE_TIMEOUT_SECONDS);
	}

	private void restoreTransferState(Player player) {
		if (player == null || !player.isOnline()) {
			return;
		}

		CosmeticTransferState state = _transferStateRepository.getElement(player.getName());
		if (state == null) {
			return;
		}

		for (String entry : state.getActiveGadgets()) {
			if (entry == null || entry.isEmpty() || !entry.contains("|")) {
				continue;
			}

			String[] parts = entry.split("\\|", 2);
			if (parts.length != 2) {
				continue;
			}

			try {
				GadgetType type = GadgetType.valueOf(parts[0]);
				Gadget gadget = _gadgetManager.findGadget(type, parts[1]);
				Gadget activeGadget = _gadgetManager.getActive(player, type);
				if (gadget != null && activeGadget != gadget) {
					gadget.Enable(player);
				}
			} catch (IllegalArgumentException ignored) {
			}
		}

		if (state.getActiveMount() != null) {
			Mount<?> mount = _mountManager.findMount(state.getActiveMount());
			if (mount != null && _mountManager.getActive(player) == null) {
				mount.Enable(player);
			}
		}

		if (state.getActivePetType() != null && !_petManager.hasActivePet(player.getName())) {
			try {
				_petManager.AddPetOwner(player, org.bukkit.entity.EntityType.valueOf(state.getActivePetType()), player.getLocation());
			} catch (IllegalArgumentException ignored) {
			}
		}

		_transferStateRepository.removeElement(player.getName());
	}

	public org.bukkit.inventory.ItemStack createInterfaceItem(Player player) {
		return ItemStackFactory.Instance.CreateStack(
				Material.CHEST,
				(byte) 0,
				1,
				HouziColorParser.parse(com.houzicore.shared.core.lang.LangManager.get().get(player, "hub.item.cosmetic")),
				new String[] {
						HouziColorParser.parse(com.houzicore.shared.core.lang.LangManager.get().get(player, "hub.item.cosmetic.lore1")),
						HouziColorParser.parse(com.houzicore.shared.core.lang.LangManager.get().get(player, "hub.item.cosmetic.lore2"))
				});
	}
}
