package com.houzicore.shared.core.gadget.types;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilBlock;
import com.houzicore.shared.common.util.UtilGear;
import com.houzicore.shared.common.util.UtilInv;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.core.gadget.GadgetManager;
import com.houzicore.shared.core.gadget.event.ItemGadgetOutOfAmmoEvent;
import com.houzicore.shared.core.gadget.gadgets.Ammo;
import com.houzicore.shared.core.itemstack.ItemStackFactory;
import com.houzicore.shared.recharge.Recharge;

public abstract class ItemGadget extends Gadget {
	private final Ammo _ammo;
	protected long _recharge;

	public ItemGadget(GadgetManager manager, String name, String[] desc, int cost, Material mat, byte data,
			long recharge, Ammo ammo) {
		super(manager, GadgetType.Item, name, desc, cost, mat, data);

		_ammo = ammo;
		_recharge = recharge;
		Free = true;
	}

	@EventHandler
	public void Activate(PlayerInteractEvent event) {
		if (event.getHand() != EquipmentSlot.HAND)
			return;

		if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;

		if (UtilBlock.usable(event.getClickedBlock()))
			return;

		if (!UtilGear.isMat(event.getPlayer().getInventory().getItemInMainHand(), GetDisplayMaterial()))
			return;

		final Player player = event.getPlayer();

		if (!IsActive(player))
			return;

		event.setCancelled(true);

		// Stock
		if (Manager.getInventoryManager().Get(player).getItemCount(GetName()) <= 0) {
			UtilPlayer.message(player, F.main("Gadget", com.houzicore.shared.core.lang.LangManager.get().get(player, "gadget.none_left").replace("{0}", F.elem(GetName()))));

			final ItemGadgetOutOfAmmoEvent ammoEvent = new ItemGadgetOutOfAmmoEvent(event.getPlayer(), this);
			Bukkit.getServer().getPluginManager().callEvent(ammoEvent);

			return;
		}

		// Recharge
		if (!Recharge.Instance.use(player, GetName(), _recharge, _recharge > 1000, false)) {
			UtilInv.Update(player);
			return;
		}

		Manager.getInventoryManager().addItemToInventory(player, getGadgetType().name(), GetName(), -1);

		player.getInventory()
				.setItem(Manager.getActiveItemSlot(), ItemStackFactory.Instance.CreateStack(GetDisplayMaterial(),
						GetDisplayData(), 1,
						F.item(Manager.getInventoryManager().Get(player).getItemCount(GetName()) + " " + GetName())));

		ActivateCustom(event.getPlayer());
	}

	public abstract void ActivateCustom(Player player);

	public void ApplyItem(Player player, boolean inform) {
		Manager.RemoveItem(player);

		_active.add(player);

		final List<String> itemLore = new ArrayList<>();
		itemLore.addAll(Arrays.asList(GetDescription()));
		itemLore.add(org.bukkit.ChatColor.BLACK + "");
		itemLore.add((com.houzicore.shared.core.lang.LangManager.get().get(player, "gadget.amount")) + Manager.getInventoryManager().Get(player).getItemCount(GetName()));

		player.getInventory()
				.setItem(Manager.getActiveItemSlot(), ItemStackFactory.Instance.CreateStack(GetDisplayMaterial(),
						GetDisplayData(), 1,
						F.item(Manager.getInventoryManager().Get(player).getItemCount(GetName()) + " " + GetName())));

		if (inform) {
			UtilPlayer.message(player, F.main("Gadget", com.houzicore.shared.core.lang.LangManager.get().get(player, "gadget.equipped").replace("{0}", F.elem(GetName()))));
		}
	}

	@Override
	public void DisableCustom(Player player) {
		RemoveItem(player);
	}

	@Override
	public void EnableCustom(Player player) {
		ApplyItem(player, true);
	}

	@Override
	public HashSet<Player> GetActive() {
		return _active;
	}

	public Ammo getAmmo() {
		return _ammo;
	}

	@Override
	public boolean IsActive(Player player) {
		return _active.contains(player);
	}

	public boolean IsItem(Player player) {
		return UtilInv.IsItem(player.getInventory().getItemInMainHand(), GetDisplayMaterial(), GetDisplayData());
	}

	@EventHandler
	public void orderThatChest(PlayerDropItemEvent event) {
		if (IsActive(event.getPlayer()) && event.getItemDrop().getItemStack().getType() == GetDisplayMaterial()) {
			final Player player = event.getPlayer();

			Bukkit.getScheduler().scheduleSyncDelayedTask(Manager.getPlugin(), new Runnable() {
				@Override
				public void run() {
					if (player.isOnline()) {
						player.getInventory().remove(GetDisplayMaterial());
						player.getInventory().setItem(Manager.getActiveItemSlot(),
								ItemStackFactory.Instance.CreateStack(GetDisplayMaterial(), GetDisplayData(), 1,
										F.item(Manager.getInventoryManager().Get(player).getItemCount(GetName()) + " "
												+ GetName())));
						UtilInv.Update(player);
					}
				}
			});
		}
	}

	public void RemoveItem(Player player) {
		if (_active.remove(player)) {
			player.getInventory().setItem(Manager.getActiveItemSlot(), null);

			UtilPlayer.message(player, F.main("Gadget", com.houzicore.shared.core.lang.LangManager.get().get(player, "gadget.unequipped").replace("{0}", F.elem(GetName()))));
		}
	}
}
