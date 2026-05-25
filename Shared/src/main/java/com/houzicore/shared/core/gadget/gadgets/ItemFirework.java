package com.houzicore.shared.core.gadget.gadgets;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilBlock;
import com.houzicore.shared.common.util.UtilFirework;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.core.gadget.GadgetManager;
import com.houzicore.shared.core.gadget.types.ItemGadget;

public class ItemFirework extends ItemGadget {
	public ItemFirework(GadgetManager manager) {
		super(manager, "Fireworks",
				new String[] { C.cWhite + "Need to celebrate?!", C.cWhite + "Use some fireworks!",
						C.cWhite + "Pew pew pew!", },
				-1, Material.FIREWORK_ROCKET, (byte) 0, 100, new Ammo("Fireworks", "50 Fireworks", Material.FIREWORK_ROCKET, (byte) 0,
						new String[] { C.cWhite + "50 Fireworks for you to launch!" }, 500, 50));
	}

	@Override
	public void ActivateCustom(Player player) {
		final Location loc = player.getEyeLocation().add(player.getLocation().getDirection());

		// Portal Disallow
		for (final Block block : UtilBlock.getSurrounding(loc.getBlock(), true)) {
			if (com.houzicore.shared.common.util.IdUtil.getTypeId(block) == 90) {
				boolean isThai = com.houzicore.shared.core.lang.LangManager.get().isThai(player);
				UtilPlayer.message(player, F.main("Skill", isThai ? "\u00A77\u0e04\u0e38\u0e13\u0e44\u0e21\u0e48\u0e2a\u0e32\u0e21\u0e32\u0e23\u0e16\u0e43\u0e0a\u0e49 " + F.skill(GetName()) + " \u0e43\u0e01\u0e25\u0e49\u0e1b\u0e23\u0e30\u0e15\u0e39\u0e21\u0e34\u0e15\u0e34\u0e44\u0e14\u0e49" : "§7You cannot use " + F.skill(GetName()) + " near the portal"));
				return;
			}
		}

		// Inform
		boolean isThaiInfo = com.houzicore.shared.core.lang.LangManager.get().isThai(player);
		UtilPlayer.message(player, F.main("Skill", isThaiInfo ? "\u00A77\u0e04\u0e38\u0e13\u0e43\u0e0a\u0e49 " + F.skill(GetName()) : "§7You used " + F.skill(GetName())));

		double r = Math.random();

		Color color = Color.FUCHSIA;
		if (r > 0.9) {
			color = Color.RED;
		} else if (r > 0.8) {
			color = Color.YELLOW;
		} else if (r > 0.7) {
			color = Color.GREEN;
		} else if (r > 0.6) {
			color = Color.BLUE;
		} else if (r > 0.5) {
			color = Color.AQUA;
		} else if (r > 0.4) {
			color = Color.LIME;
		} else if (r > 0.3) {
			color = Color.ORANGE;
		} else if (r > 0.2) {
			color = Color.TEAL;
		} else if (r > 0.1) {
			color = Color.WHITE;
		}

		r = Math.random();

		Type type = Type.BURST;
		if (r > 0.66) {
			type = Type.BALL;
		} else if (r > 0.33) {
			type = Type.BALL_LARGE;
		}

		UtilFirework.launchFirework(loc, FireworkEffect.builder().flicker(Math.random() > 0.5).withColor(color)
				.with(type).trail(Math.random() > 0.5).build(), new Vector(0, 0, 0), 0 + (int) (Math.random() * 3));
	}
}
