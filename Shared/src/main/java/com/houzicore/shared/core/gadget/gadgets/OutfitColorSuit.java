package com.houzicore.shared.core.gadget.gadgets;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilGear;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.core.gadget.GadgetManager;
import com.houzicore.shared.core.gadget.types.OutfitGadget;
import com.houzicore.shared.core.itemstack.ItemStackFactory;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;

public class OutfitColorSuit extends OutfitGadget {
	private final Color _color;
	private final Particle _accentParticle;

	public OutfitColorSuit(GadgetManager manager, String name, String[] desc, int cost, ArmorSlot slot, Material mat,
			byte data, Color color, Particle accentParticle) {
		super(manager, name, desc, cost, slot, mat, data);

		_color = color;
		_accentParticle = accentParticle;
	}

	@Override
	public void DisableCustom(Player player) {
		RemoveArmor(player);
	}

	@Override
	public void EnableCustom(Player player) {
		Manager.RemoveMorph(player);
		Manager.RemoveOutfit(player, GetSlot());
		_active.add(player);

		UtilPlayer.message(player, F.main("Gadget",
				com.houzicore.shared.core.lang.LangManager.get().get(player, "gadget.equipped")
						.replace("{0}", F.elem(GetName()))));

		ItemStack stack = ItemStackFactory.Instance.CreateStack(GetDisplayMaterial(), GetDisplayData(), 1, GetName());
		if (stack.getItemMeta() instanceof LeatherArmorMeta meta) {
			meta.setColor(_color);
			stack.setItemMeta(meta);
		}

		if (GetSlot() == ArmorSlot.Helmet) {
			player.getInventory().setHelmet(stack);
		} else if (GetSlot() == ArmorSlot.Chest) {
			player.getInventory().setChestplate(stack);
		} else if (GetSlot() == ArmorSlot.Legs) {
			player.getInventory().setLeggings(stack);
		} else if (GetSlot() == ArmorSlot.Boots) {
			player.getInventory().setBoots(stack);
		}
	}

	@EventHandler
	public void onUpdate(UpdateEvent event) {
		if (event.getType() != UpdateType.FAST) {
			return;
		}

		for (Player player : UtilServer.getPlayers()) {
			if (!IsActive(player)) {
				continue;
			}

			ItemStack stack = getArmorPiece(player);
			if (!UtilGear.isMat(stack, GetDisplayMaterial())) {
				Disable(player);
				continue;
			}

			if (stack.getItemMeta() instanceof LeatherArmorMeta meta && !meta.getColor().equals(_color)) {
				meta.setColor(_color);
				stack.setItemMeta(meta);
			}

			if (GetSlot() == ArmorSlot.Helmet && Manager.isMoving(player)) {
				player.getWorld().spawnParticle(_accentParticle, player.getLocation().add(0, 1.2, 0), 2, 0.25, 0.2,
						0.25, 0.01);
			}
		}
	}

	private ItemStack getArmorPiece(Player player) {
		if (GetSlot() == ArmorSlot.Helmet) {
			return player.getInventory().getHelmet();
		}
		if (GetSlot() == ArmorSlot.Chest) {
			return player.getInventory().getChestplate();
		}
		if (GetSlot() == ArmorSlot.Legs) {
			return player.getInventory().getLeggings();
		}
		return player.getInventory().getBoots();
	}
}
