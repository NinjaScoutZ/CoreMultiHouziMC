package com.houzicore.shared.core.mount;

import java.util.HashMap;
import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.houzicore.shared.common.CurrencyType;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.core.mount.event.MountActivateEvent;
import com.houzicore.shared.core.shop.item.SalesPackageBase;

public abstract class Mount<T> extends SalesPackageBase implements Listener {
	protected HashSet<Player> _owners = new HashSet<>();
	protected HashMap<Player, T> _active = new HashMap<>();

	public MountManager Manager;

	public Mount(MountManager manager, String name, Material material, byte displayData, String[] description,
			int coins) {
		super(name, material, displayData, description, coins);

		KnownPackage = false;
		Manager = manager;

		Manager.getPlugin().getServer().getPluginManager().registerEvents(this, Manager.getPlugin());
	}

	public abstract void Disable(Player player);

	public void DisableForAll() {
		for (final Player player : UtilServer.getPlayers()) {
			Disable(player);
		}
	}

	public final void Enable(Player player) {
		final MountActivateEvent gadgetEvent = new MountActivateEvent(player, this);
		Bukkit.getServer().getPluginManager().callEvent(gadgetEvent);

		if (gadgetEvent.isCancelled()) {
			UtilPlayer.message(player, F.main("Inventory", com.houzicore.shared.core.lang.LangManager.get().get(player, "mount.not_enabled").replace("{0}", GetName())));
			return;
		}

		Manager.setActive(player, this);
		EnableCustom(player);
	}

	public abstract void EnableCustom(Player player);

	public HashMap<Player, T> GetActive() {
		return _active;
	}

	public HashSet<Player> GetOwners() {
		return _owners;
	}

	public boolean HasMount(Player player) {
		return _owners.contains(player);
	}

	public boolean IsActive(Player player) {
		return _active.containsKey(player);
	}

	@EventHandler
	public void PlayerJoin(PlayerJoinEvent event) {
		// Ownership is determined by DonationManager.OwnsUnknownPackage / InventoryManager
		// at UI render time (MountPage). No eager population needed here.
	}

	@EventHandler
	public void PlayerQuit(PlayerQuitEvent event) {
		_owners.remove(event.getPlayer());
		Disable(event.getPlayer());
	}

	@Override
	public void Sold(Player player, CurrencyType currencyType) {
		if (com.houzicore.shared.core.inventory.InventoryManager.Instance != null) {
			com.houzicore.shared.core.inventory.InventoryManager.Instance.addItemToInventory(player, "Mount", GetName(), 1);
		}
	}
}
