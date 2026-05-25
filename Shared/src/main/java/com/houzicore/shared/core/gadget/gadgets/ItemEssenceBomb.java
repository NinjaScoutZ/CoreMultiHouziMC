package com.houzicore.shared.core.gadget.gadgets;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.houzicore.shared.common.CurrencyType;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilAction;
import com.houzicore.shared.common.util.UtilBlock;
import com.houzicore.shared.common.util.UtilFirework;
import com.houzicore.shared.common.util.UtilGear;
import com.houzicore.shared.common.util.UtilInv;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.common.util.UtilTime;
import com.houzicore.shared.core.gadget.GadgetManager;
import com.houzicore.shared.core.gadget.event.ItemGadgetOutOfAmmoEvent;
import com.houzicore.shared.core.gadget.types.ItemGadget;
import com.houzicore.shared.core.itemstack.ItemStackFactory;
import com.houzicore.shared.recharge.Recharge;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;

public class ItemEssenceBomb extends ItemGadget {
	private final HashMap<Item, Long> _activeBombs = new HashMap<>();
	private final HashSet<Item> _gems = new HashSet<>();

	public ItemEssenceBomb(GadgetManager manager) {
		super(manager, "Essence Party Bomb",
				new String[] { C.cWhite + "It's party time! You will be", C.cWhite + "everyones favourite player",
						C.cWhite + "when you use one of these!", " ",
						C.cRed + C.Bold + "WARNING: " + ChatColor.RESET + "This uses 2000 Essence" },
				-1, Material.EMERALD, (byte) 0, 30000, new Ammo("Essence Party Bomb", "10 Essence Party Bomb", Material.EMERALD,
						(byte) 0, new String[] { C.cWhite + "10 Coin Party Bomb to PARTY!" }, 10, 10));
	}

	@Override
	@EventHandler
	public void Activate(PlayerInteractEvent event) {
		if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;

		if (UtilBlock.usable(event.getClickedBlock()))
			return;

		if (!UtilGear.isMat(event.getPlayer().getItemInHand(), GetDisplayMaterial()))
			return;

		final Player player = event.getPlayer();

		if (!IsActive(player))
			return;

		event.setCancelled(true);

		// Stock
		if (Manager.getInventoryManager().Get(player).getItemCount(GetName()) <= 0) {

			boolean isThai = com.houzicore.shared.core.lang.LangManager.get().isThai(player);
			UtilPlayer.message(player, F.main("Gadget", isThai ? "\u00A77\u0e04\u0e38\u0e13\u0e44\u0e21\u0e48\u0e21\u0e35 " + F.elem(GetName()) + " \u00A77\u0e40\u0e2b\u0e25\u0e37\u0e2d\u0e2d\u0e22\u0e39\u0e48\u0e40\u0e25\u0e22" : "§7You don't have any " + F.elem(GetName()) + " §7left."));

			final ItemGadgetOutOfAmmoEvent ammoEvent = new ItemGadgetOutOfAmmoEvent(event.getPlayer(), this);
			Bukkit.getServer().getPluginManager().callEvent(ammoEvent);

			return;
		}

		// Gems
		if (Manager.getDonationManager().Get(player.getName()).GetBalance(CurrencyType.Essence) < 2000) {
			UtilPlayer.message(player,
					F.main("Inventory", "You do not have the required " + C.cGreen + "2000 Essence") + ".");
			return;
		}

		// Already In Use
		if (!_activeBombs.isEmpty()) {
			UtilPlayer.message(player,
					F.main("Inventory", "There is already a " + F.elem(C.cGreen + "Essence Bomb")) + " being used.");
			return;
		}

		// Recharge
		if (!Recharge.Instance.use(player, GetName(), _recharge, _recharge > 1000, false)) {
			UtilInv.Update(player);
			return;
		}

		// Use Stock/Gems
		Manager.getInventoryManager().addItemToInventory(player, getGadgetType().name(), GetName(), -1);
		Manager.getDonationManager().RewardEssence(null, GetName(), event.getPlayer().getName(),
				event.getPlayer().getUniqueId(), -2000);

		player.getInventory().setItem(Manager.getActiveItemSlot(),
				ItemStackFactory.Instance.CreateStack(GetDisplayMaterial(), GetDisplayData(), 1, F.item(GetName())));

		ActivateCustom(event.getPlayer());
	}

	@Override
	public void ActivateCustom(Player player) {
		final Item item = player.getWorld().dropItem(player.getEyeLocation().add(player.getLocation().getDirection()),
				new ItemStack(Material.EMERALD_BLOCK));
		UtilAction.velocity(item, player.getLocation().getDirection(), 1, false, 0, 0.2, 1, false);
		_activeBombs.put(item, System.currentTimeMillis());

		// Inform
		for (final Player other : UtilServer.getPlayers()) {
			boolean isThai = com.houzicore.shared.core.lang.LangManager.get().isThai(other);
			UtilPlayer.message(other, "\u00A7a\u00A7l" + player.getName() + (isThai ? " \u00A7f\u00A7l\u0e02\u0e27\u0e49\u0e32\u0e07 " : " §f§lthrew ")
					+ C.cGreen + C.Bold + "Essence Party Bomb" + C.cWhite + C.Bold + "!");
		}
	}

	@EventHandler
	public void Clean(UpdateEvent event) {
		if (event.getType() != UpdateType.FAST)
			return;

		final Iterator<Item> gemIterator = _gems.iterator();

		while (gemIterator.hasNext()) {
			final Item gem = gemIterator.next();

			if (!gem.isValid() || gem.getTicksLived() > 1200) {
				gem.remove();
				gemIterator.remove();
			}
		}
	}

	@EventHandler
	public void Pickup(EntityPickupItemEvent event) {
		if (_activeBombs.keySet().contains(event.getItem())) {
			event.setCancelled(true);
		} else if (_gems.contains(event.getItem())) {
			event.setCancelled(true);
			event.getItem().remove();

			if (event.getEntity() instanceof Player) {
				Player player = (Player) event.getEntity();
				Manager.getDonationManager().RewardEssenceLater(GetName() + " Pickup", player, 4);
				player.getWorld().playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 2f);
			}
		}
	}

	@EventHandler
	public void Update(UpdateEvent event) {
		if (event.getType() != UpdateType.TICK)
			return;

		final Iterator<Item> itemIterator = _activeBombs.keySet().iterator();

		while (itemIterator.hasNext()) {
			final Item item = itemIterator.next();
			final long time = _activeBombs.get(item);

			if (UtilTime.elapsed(time, 3000)) {
				if (Math.random() > 0.80) {
					UtilFirework.playFirework(item.getLocation(), FireworkEffect.builder().flicker(false)
							.withColor(Color.GREEN).with(Type.BURST).trail(false).build());
				} else {
					item.getWorld().playSound(item.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1f, 1f);
				}

				final Item gem = item.getWorld().dropItem(item.getLocation().add(0, 1, 0),
						new ItemStack(Material.EMERALD));

				// Velocity
				final long passed = System.currentTimeMillis() - time;
				final Vector vel = new Vector(Math.sin(passed / 300d), 0, Math.cos(passed / 300d));

				UtilAction.velocity(gem, vel, Math.abs(Math.sin(passed / 3000d)), false, 0,
						0.2 + Math.abs(Math.cos(passed / 3000d)) * 0.8, 1, false);

				gem.setPickupDelay(40);

				_gems.add(gem);
			}

			if (UtilTime.elapsed(time, 23000)) {
				item.remove();
				itemIterator.remove();
			}
		}
	}
}
