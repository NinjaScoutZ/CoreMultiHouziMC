package com.houzicore.shared.core.gadget.gadgets;

import java.util.HashSet;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.FireworkEffect.Type;
////import org.bukkit.craftbukkit.v1_21_R1.entity.org.bukkit.entity.Player;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilFirework;
import com.houzicore.shared.common.util.UtilInv;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.core.gadget.GadgetManager;
import com.houzicore.shared.core.gadget.types.ItemGadget;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;

public class ItemEtherealPearl extends ItemGadget {
	private final HashSet<String> _riding = new HashSet<>();

	public ItemEtherealPearl(GadgetManager manager) {
		super(manager, "Ethereal Pearl",
				new String[] { C.cWhite + "Take a ride through the skies",
						C.cWhite + "on your very own Ethereal Pearl!", },
				-1, Material.ENDER_PEARL, (byte) 0, 500, new Ammo("Ethereal Pearl", "50 Pearls", Material.ENDER_PEARL,
						(byte) 0, new String[] { C.cWhite + "50 Pearls to get around with!" }, 500, 50));
	}

	@Override
	public void ActivateCustom(Player player) {
		player.eject();
		player.leaveVehicle();

		final EnderPearl pearl = player.launchProjectile(EnderPearl.class);
		pearl.addPassenger(player);

		// Inform
		boolean isThai = com.houzicore.shared.core.lang.LangManager.get().isThai(player);
		UtilPlayer.message(player, F.main("Skill", isThai ? "\u00A77\u0e04\u0e38\u0e13\u0e02\u0e27\u0e49\u0e32\u0e07 " + F.skill(GetName()) : "§7You threw " + F.skill(GetName())));

		// Dont Collide
		//player.spectating = true;

		UtilInv.Update(player);

		_riding.add(player.getName());
	}

	@EventHandler
	public void clean(PlayerQuitEvent event) {
		_riding.remove(event.getPlayer().getName());
	}

	@Override
	public void DisableCustom(Player player) {
		super.DisableCustom(player);
	}

	@EventHandler
	public void disableNoCollide(UpdateEvent event) {
		if (event.getType() != UpdateType.SEC)
			return;

		for (final Player player : UtilServer.getPlayers())
			if (_riding.contains(player.getName()))
				if (player.getVehicle() == null) {
					//player.spectating = false;
					_riding.remove(player.getName());
				}
	}

	@EventHandler
	public void teleportCancel(PlayerTeleportEvent event) {
		if (!IsActive(event.getPlayer()))
			return;

		if (event.getCause() == TeleportCause.ENDER_PEARL) {
			// Firework
			final FireworkEffect effect = FireworkEffect.builder().flicker(false).withColor(Color.PURPLE)
					.with(Type.BALL).trail(true).build();

			try {
				UtilFirework.playFirework(event.getTo(), effect);
			} catch (final Exception e) {
				org.bukkit.Bukkit.getServer().getLogger().log(java.util.logging.Level.SEVERE, e.getMessage(), e);
			}

			event.setCancelled(true);
		}
	}
}
