package com.houzicore.shared.core.gadget.gadgets;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilGear;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.core.gadget.GadgetManager;
import com.houzicore.shared.core.gadget.types.ItemGadget;

public class ItemDuelingSword extends ItemGadget {
	public ItemDuelingSword(GadgetManager manager) {
		super(manager, "Dueling Sword",
				new String[] { C.cWhite + "While active, you are able to fight",
						C.cWhite + "against other people who are also", C.cWhite + "wielding a dueling sword.", },
				-1, Material.WOODEN_SWORD, (byte) 3, 1000, new Ammo("Dueling Sword", "10 Swords", Material.WOODEN_SWORD,
						(byte) 0, new String[] { C.cWhite + "10 Swords to duel with" }, 1000, 10));
	}

	@Override
	public void ActivateCustom(Player player) {
		final ItemStack stack = new ItemStack(Material.GOLDEN_SWORD);
		final ItemMeta meta = stack.getItemMeta();
		meta.setDisplayName("Dueling Sword");
		stack.setItemMeta(meta);

		player.getInventory().setItem(Manager.getActiveItemSlot(), stack);

		// Inform
		boolean isThai = com.houzicore.shared.core.lang.LangManager.get().isThai(player);
		UtilPlayer.message(player, F.main("Skill", isThai ? "\u00A77\u0e04\u0e38\u0e13\u0e43\u0e0a\u0e49 " + F.skill(GetName()) : "§7You used " + F.skill(GetName())));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void damage(EntityDamageByEntityEvent event) {
		if (!(event.getEntity() instanceof Player) || !(event.getDamager() instanceof Player))
			return;

		final Player damager = (Player) event.getDamager();
		final Player damagee = (Player) event.getEntity();

		if (!UtilGear.isMat(damager.getItemInHand(), Material.GOLDEN_SWORD)
				|| !UtilGear.isMat(damagee.getItemInHand(), Material.GOLDEN_SWORD))
			return;

		event.setCancelled(false);

		event.setDamage(4);
	}
}
