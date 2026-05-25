package com.houzicore.shared.core.gadget.gadgets;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.houzicore.shared.common.CurrencyType;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilAction;
import com.houzicore.shared.common.util.UtilEnt;
import com.houzicore.shared.common.util.UtilEvent;
import com.houzicore.shared.common.util.UtilFirework;
import com.houzicore.shared.common.util.UtilParticle;
import com.houzicore.shared.common.util.UtilParticle.ParticleType;
import com.houzicore.shared.common.util.UtilParticle.ViewDist;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilEvent.ActionType;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.core.disguise.disguises.DisguiseRabbit;
import com.houzicore.shared.core.gadget.GadgetManager;
import com.houzicore.shared.core.gadget.types.MorphGadget;
import com.houzicore.shared.core.itemstack.ItemStackFactory;
import com.houzicore.shared.recharge.Recharge;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;

public class MorphBunny extends MorphGadget {
	private final HashSet<Player> _jumpCharge = new HashSet<>();
	private final HashMap<Item, String> _eggs = new HashMap<>();

	public MorphBunny(GadgetManager manager) {
		super(manager, "Easter Bunny Morph",
				new String[] { C.cWhite + "Happy Easter!", " ",
						C.cYellow + "Charge Crouch" + C.cGray + " to use " + C.cGreen + "Super Jump",
						C.cYellow + "Left Click" + C.cGray + " to use " + C.cGreen + "Hide Easter Egg", " ",
						C.cRed + C.Bold + "WARNING: " + ChatColor.RESET + "Hide Easter Egg uses 500 Coins", " ",
						C.cPurple + "Special Limited Time Morph", C.cPurple + "Purchase at an administrator", },
				-1, Material.CREEPER_SPAWN_EGG, (byte) 98);
	}

	@Override
	public void DisableCustom(Player player) {
		_jumpCharge.remove(player);
		RemoveArmor(player);
		Manager.getDisguiseManager().undisguise(player);

		player.removePotionEffect(PotionEffectType.SPEED);
		player.removePotionEffect(PotionEffectType.JUMP_BOOST);

	}

	@EventHandler
	public void eggClean(UpdateEvent event) {
		if (event.getType() != UpdateType.FAST)
			return;

		final Iterator<Item> eggIter = _eggs.keySet().iterator();

		while (eggIter.hasNext()) {
			final Item egg = eggIter.next();

			if (!egg.isValid() || egg.getTicksLived() > 24000) {
				egg.remove();
				eggIter.remove();

				// Announce
				Bukkit.broadcastMessage(ChatColor.RESET + C.Bold + "No one found an " + C.cGold + C.Bold + "Easter Egg"
						+ ChatColor.RESET + C.Bold + "! " + _eggs.size() + " Eggs left!");
			} else {
				UtilParticle.PlayParticle(ParticleType.SPELL, egg.getLocation().add(0, 0.1, 0), 0.1f, 0.1f, 0.1f, 0, 1,
						ViewDist.NORMAL, UtilServer.getPlayers());
			}
		}
	}

	@EventHandler
	public void eggDespawnCancel(ItemDespawnEvent event) {
		if (_eggs.containsKey(event.getEntity())) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void eggHide(PlayerInteractEvent event) {
		final Player player = event.getPlayer();

		if (!IsActive(player))
			return;

		if (!UtilEvent.isAction(event, ActionType.L))
			return;

		if (Manager.getDonationManager().Get(player.getName()).GetBalance(CurrencyType.Coins) < 500) {
			boolean isThai = com.houzicore.shared.core.lang.LangManager.get().isThai(player);
			UtilPlayer.message(player, F.main("Gadget", isThai ? "\u00A77\u0e04\u0e38\u0e13\u0e21\u0e35\u0e40\u0e2b\u0e23\u0e35\u0e22\u0e0d\u0e44\u0e21\u0e48\u0e40\u0e1e\u0e35\u0e22\u0e07\u0e1e\u0e2d" : "§7You have insufficient Coins."));
			return;
		}

		if (!Recharge.Instance.use(player, "Hide Egg", 30000, true, false))
			return;

		// Color

		// Item
		final ItemStack eggStack = ItemStackFactory.Instance.CreateStack(Material.CREEPER_SPAWN_EGG, (byte) 0, 1,
				"Hidden Egg" + System.currentTimeMillis());
		eggStack.setDurability((short) 98);

		final Item egg = player.getWorld().dropItem(player.getEyeLocation().add(player.getLocation().getDirection()),
				eggStack);
		UtilAction.velocity(egg, player.getLocation().getDirection(), 0.2, false, 0, 0.2, 1, false);

		Manager.getDonationManager().RewardCoinsLater(GetName() + " Egg Hide", player, -500);

		egg.setPickupDelay(40);

		_eggs.put(egg, player.getName());

		// Announce
		Bukkit.broadcastMessage(
				C.cYellow + C.Bold + player.getName() + ChatColor.RESET + C.Bold + " hid an " + C.cYellow + C.Bold
						+ "Easter Egg" + ChatColor.RESET + C.Bold + " worth " + C.cYellow + C.Bold + "450 Coins");

		for (final Player other : UtilServer.getPlayers()) {
			other.playSound(other.getLocation(), Sound.ENTITY_CAT_HURT, 1.5f, 1.5f);
		}
	}

	@EventHandler
	public void eggPickup(EntityPickupItemEvent event) {
		if (_eggs.containsKey(event.getItem()) && event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			if (!_eggs.get(event.getItem()).equals(player.getName())) {
				_eggs.remove(event.getItem());

				event.setCancelled(true);
				event.getItem().remove();

				Manager.getDonationManager().RewardCoinsLater(GetName() + " Egg Pickup", player, 450);

				player.getWorld().playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.5f, 0.75f);
				player.getWorld().playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.5f, 1.25f);

				UtilFirework.playFirework(event.getItem().getLocation(), Type.BURST, Color.YELLOW, true, true);

				// Announce
				Bukkit.broadcastMessage(
						C.cGold + C.Bold + player.getName() + ChatColor.RESET + C.Bold + " found an " + C.cGold
								+ C.Bold + "Easter Egg" + ChatColor.RESET + C.Bold + "! " + _eggs.size() + " Eggs left!");
			}
		}
	}

	@Override
	public void EnableCustom(final Player player) {
		ApplyArmor(player);

		final DisguiseRabbit disguise = new DisguiseRabbit(player);
		//disguise.setName(player.getName(), Manager.getClientManager().Get(player).GetRank());
		//disguise.setCustomNameVisible(true);
		Manager.getDisguiseManager().disguise(disguise);

		player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 999999999, 1));
		player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 999999999, 1));
	}

	@EventHandler
	public void jumpBoost(UpdateEvent event) {
		if (event.getType() != UpdateType.TICK)
			return;

		final Iterator<Player> jumpIter = _jumpCharge.iterator();

		while (jumpIter.hasNext()) {
			final Player player = jumpIter.next();

			if (!player.isValid() || !player.isOnline() || !player.isSneaking()) {
				jumpIter.remove();
				continue;
			}

			player.setExp(Math.min(0.9999f, player.getExp() + 0.03f));

			player.playSound(player.getLocation(), Sound.ENTITY_CREEPER_PRIMED, 0.25f + player.getExp() * 0.5f, 0.5f + player.getExp());
		}
	}

	@EventHandler
	public void jumpTrigger(PlayerToggleSneakEvent event) {
		final Player player = event.getPlayer();

		if (!IsActive(player))
			return;

		// Start
		if (!event.getPlayer().isSneaking()) {
			if (UtilEnt.isGrounded(event.getPlayer())) {
				_jumpCharge.add(event.getPlayer());
			}
		}
		// Jump
		else if (_jumpCharge.remove(event.getPlayer())) {
			final float power = player.getExp();
			player.setExp(0f);

			UtilAction.velocity(player, power * 4, 0.4, 4, true);

			player.getWorld().playSound(player.getLocation(), Sound.ENTITY_CAT_HURT, 0.75f, 2f);
		}
	}
}
