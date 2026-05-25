package com.houzicore.shared.core.gadget.gadgets;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.UtilAction;
import com.houzicore.shared.common.util.UtilFirework;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.common.util.UtilTime;
import com.houzicore.shared.core.gadget.GadgetManager;
import com.houzicore.shared.core.gadget.types.ItemGadget;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;

public class ItemCoinBomb extends ItemGadget {
	private final HashMap<Item, Long> _active = new HashMap<>();
	private final HashSet<Item> _coins = new HashSet<>();

	public ItemCoinBomb(GadgetManager manager) {
		super(manager, "Coin Party Bomb",
				new String[] { C.cWhite + "It's party time! You will be", C.cWhite + "everyones favourite player",
						C.cWhite + "when you use one of these!", },
				-1, Material.SUNFLOWER, (byte) 0, 30000,
				new Ammo("Coin Party Bomb", "1 Coin Party Bomb", Material.SUNFLOWER, (byte) 0,
						new String[] { C.cWhite + "1 Coin Party Bomb to PARTY!" }, 2000, 1));
	}

	@Override
	public void ActivateCustom(Player player) {
		final Item item = player.getWorld().dropItem(player.getEyeLocation().add(player.getLocation().getDirection()),
				new ItemStack(Material.GOLD_BLOCK));
		UtilAction.velocity(item, player.getLocation().getDirection(), 1, false, 0, 0.2, 1, false);
		_active.put(item, System.currentTimeMillis());

		// Inform
		for (final Player other : UtilServer.getPlayers()) {
			boolean isThai = com.houzicore.shared.core.lang.LangManager.get().isThai(other);
			UtilPlayer.message(other, "\u00A7e\u00A7l" + player.getName() + (isThai ? " \u00A7f\u00A7l\u0e02\u0e27\u0e49\u0e32\u0e07 " : " §f§lthrew ")
					+ C.cYellow + C.Bold + "Coin Party Bomb" + C.cWhite + C.Bold + "!");
		}
	}

	@EventHandler
	public void Clean(UpdateEvent event) {
		if (event.getType() != UpdateType.FAST)
			return;

		final Iterator<Item> coinIterator = _coins.iterator();

		while (coinIterator.hasNext()) {
			final Item coin = coinIterator.next();

			if (!coin.isValid() || coin.getTicksLived() > 1200) {
				coin.remove();
				coinIterator.remove();
			}
		}
	}

	@EventHandler
	public void Pickup(EntityPickupItemEvent event) {
		if (_active.keySet().contains(event.getItem())) {
			event.setCancelled(true);
		} else if (_coins.contains(event.getItem())) {
			event.setCancelled(true);
			event.getItem().remove();
			
			if (event.getEntity() instanceof Player) {
				Player player = (Player) event.getEntity();
				Manager.getDonationManager().RewardCoinsLater(GetName() + " Pickup", player, 4);
				player.getWorld().playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 2f);
			}
		}
	}

	@EventHandler
	public void Update(UpdateEvent event) {
		if (event.getType() != UpdateType.TICK)
			return;

		final Iterator<Item> itemIterator = _active.keySet().iterator();

		while (itemIterator.hasNext()) {
			final Item item = itemIterator.next();
			final long time = _active.get(item);

			if (UtilTime.elapsed(time, 3000)) {
				if (Math.random() > 0.80) {
					UtilFirework.playFirework(item.getLocation(), FireworkEffect.builder().flicker(false)
							.withColor(Color.YELLOW).with(Type.BURST).trail(false).build());
				} else {
					item.getWorld().playSound(item.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1f, 1f);
				}

				final Item coin = item.getWorld().dropItem(item.getLocation().add(0, 1, 0),
						new ItemStack(Material.SUNFLOWER));

				// Velocity
				final long passed = System.currentTimeMillis() - time;
				final Vector vel = new Vector(Math.sin(passed / 300d), 0, Math.cos(passed / 300d));

				UtilAction.velocity(coin, vel, Math.abs(Math.sin(passed / 3000d)), false, 0,
						0.2 + Math.abs(Math.cos(passed / 3000d)) * 0.8, 1, false);

				coin.setPickupDelay(40);

				_coins.add(coin);
			}

			if (UtilTime.elapsed(time, 23000)) {
				item.remove();
				itemIterator.remove();
			}
		}
	}
}
