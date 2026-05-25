package com.houzicore.shared.core.gadget.types;

import java.util.HashSet;

import com.houzicore.shared.common.CurrencyType;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.core.gadget.GadgetManager;
import com.houzicore.shared.core.gadget.event.GadgetActivateEvent;
import com.houzicore.shared.core.shop.item.SalesPackageBase;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public abstract class Gadget extends SalesPackageBase implements Listener {
	public GadgetManager Manager;

	private final GadgetType _gadgetType;

	protected HashSet<Player> _active = new HashSet<>();

	public com.houzicore.shared.core.gadget.CosmeticRarity getRarity() {
		return com.houzicore.shared.core.gadget.CosmeticRarity.COMMON;
	}

	/**
	 * Whether this gadget should persist when entering Arcade minigames.
	 * Default: false (suspended during games). Override to true for premium gadgets.
	 */
	public boolean isGameCompatible() {
		return false;
	}

	public Gadget(GadgetManager manager, GadgetType gadgetType, String name, String[] desc, int cost, Material mat,
			byte data) {
		this(manager, gadgetType, name, desc, cost, mat, data, 1);
	}

	public Gadget(GadgetManager manager, GadgetType gadgetType, String name, String[] desc, int cost, Material mat,
			byte data, int quantity) {
		super(name, mat, data, desc, cost, quantity);

		_gadgetType = gadgetType;
		KnownPackage = false;

		Manager = manager;

		Manager.getPlugin().getServer().getPluginManager().registerEvents(this, Manager.getPlugin());
	}

	public void Disable(Player player) {
		if (IsActive(player)) {
			Manager.removeActive(player, this);
			DisableCustom(player);
		}
	}

	public abstract void DisableCustom(Player player);

	public void DisableForAll() {
		for (final Player player : UtilServer.getPlayers()) {
			Disable(player);
		}
	}

	public void Enable(Player player) {
		final GadgetActivateEvent gadgetEvent = new GadgetActivateEvent(player, this);
		Bukkit.getServer().getPluginManager().callEvent(gadgetEvent);

		if (gadgetEvent.isCancelled()) {
			UtilPlayer.message(player, F.main("Inventory", com.houzicore.shared.core.lang.LangManager.get().get(player, "gadget.not_activated").replace("{0}", F.elem(GetName()))));
			return;
		}

		EnableCustom(player);
		Manager.setActive(player, this);
	}

	public abstract void EnableCustom(Player player);

	public HashSet<Player> GetActive() {
		return _active;
	}

	public GadgetType getGadgetType() {
		return _gadgetType;
	}

	public boolean IsActive(Player player) {
		return _active.contains(player);
	}

	@EventHandler
	public void PlayerQuit(PlayerQuitEvent event) {
		Disable(event.getPlayer());
	}

	@Override
	public void Sold(Player player, CurrencyType currencyType) {
		if (com.houzicore.shared.core.inventory.InventoryManager.Instance != null) {
			com.houzicore.shared.core.inventory.InventoryManager.Instance.addItemToInventory(player, getGadgetType().name(), GetName(), getQuantity());
		}
	}

}
