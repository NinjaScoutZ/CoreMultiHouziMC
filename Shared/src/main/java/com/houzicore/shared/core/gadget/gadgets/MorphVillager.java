package com.houzicore.shared.core.gadget.gadgets;

import java.util.HashSet;
import java.util.Iterator;

import org.bukkit.ChatColor;
import org.bukkit.EntityEffect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import com.houzicore.shared.common.CurrencyType;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilAction;
import com.houzicore.shared.common.util.UtilAlg;
import com.houzicore.shared.common.util.UtilEvent;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilEvent.ActionType;
import com.houzicore.shared.core.disguise.disguises.DisguiseVillager;
import com.houzicore.shared.core.projectile.IThrown;
import com.houzicore.shared.core.projectile.ProjectileUser;
import com.houzicore.shared.recharge.Recharge;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.shared.core.gadget.GadgetManager;
import com.houzicore.shared.core.gadget.types.MorphGadget;

public class MorphVillager extends MorphGadget implements IThrown {
	private final HashSet<Item> _gems = new HashSet<>();

	public MorphVillager(GadgetManager manager) {
		super(manager, "Villager Morph",
				new String[] { C.cWhite + "HURRRR! MURR HURRR!", " ",
						C.cYellow + "Left Click" + C.cGray + " to use " + C.cGreen + "Essence Throw", " ",
						C.cRed + C.Bold + "WARNING: " + ChatColor.RESET + "Essence Throw uses 20 Essence" },
				12000, Material.EMERALD, (byte) 0);
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

	@Override
	public void Collide(LivingEntity target, Block block, ProjectileUser data) {
		if (target == null)
			return;

		if (target instanceof Player)
			if (Manager.collideEvent(this, (Player) target))
				return;

		// Pull
		UtilAction.velocity(target, UtilAlg.getTrajectory(data.GetThrown().getLocation(), target.getEyeLocation()), 1,
				false, 0, 0.2, 0.8, true);

		UtilAction.velocity(data.GetThrown(), UtilAlg.getTrajectory(target, data.GetThrown()), 0.5, false, 0, 0, 0.8,
				true);

		// Effect
		target.playEffect(EntityEffect.HURT);
	}

	@Override
	public void DisableCustom(Player player) {
		RemoveArmor(player);
		Manager.getDisguiseManager().undisguise(player);
	}

	@Override
	public void EnableCustom(final Player player) {
		ApplyArmor(player);

		final DisguiseVillager disguise = new DisguiseVillager(player);
		//disguise.setName(player.getName(), Manager.getClientManager().Get(player).GetRank());
		//disguise.setCustomNameVisible(true);
		Manager.getDisguiseManager().disguise(disguise);
	}

	@Override
	public void Expire(ProjectileUser data) {

	}

	@Override
	public void Idle(ProjectileUser data) {

	}

	@EventHandler
	public void Pickup(EntityPickupItemEvent event) {
		if (_gems.contains(event.getItem())) {
			event.setCancelled(true);
			event.getItem().remove();

			if (event.getEntity() instanceof Player) {
				Player player = (Player) event.getEntity();
				Manager.getDonationManager().RewardEssenceLater("Emerald Trading", player, 16);
				player.getWorld().playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 2f);
			}
		}
	}

	@EventHandler
	public void skill(PlayerInteractEvent event) {
		final Player player = event.getPlayer();

		if (!IsActive(player))
			return;

		if (!UtilEvent.isAction(event, ActionType.L))
			return;

		if (Manager.getDonationManager().Get(player.getName()).GetBalance(CurrencyType.Essence) >= 500) {
			boolean isThai = com.houzicore.shared.core.lang.LangManager.get().isThai(player);
			UtilPlayer.message(player, F.main("Gadget", isThai ? "\u00A77\u0e04\u0e38\u0e13\u0e21\u0e35 Essence \u0e44\u0e21\u0e48\u0e40\u0e1e\u0e35\u0e22\u0e07\u0e1e\u0e2d" : "§7You have insufficient Essence."));
			return;
		}

		if (!Recharge.Instance.use(player, GetName(), 800, false, false))
			return;

		player.getWorld().playSound(player.getLocation(), Sound.ENTITY_VILLAGER_AMBIENT, 1f, 1f);

		// Item
		final Item gem = player.getWorld().dropItem(player.getEyeLocation().add(player.getLocation().getDirection()),
				new ItemStack(Material.EMERALD));
		UtilAction.velocity(gem, player.getLocation().getDirection(), 1, false, 0, 0.2, 1, false);

		// Throw
		Manager.getProjectileManager().AddThrow(gem, player, this, -1, true, true, true, null, 1.4f, 0.8f, null, null,
				0, UpdateType.TICK, 0.5f);

		Manager.getDonationManager().RewardEssence(null, "Emeny Drops", player.getName(), player.getUniqueId(), -500);

		gem.setPickupDelay(40);

		_gems.add(gem);
	}
}
