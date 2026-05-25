package com.houzicore.shared.core.gadget.types;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ProjectileLaunchEvent;

import com.houzicore.shared.core.gadget.GadgetManager;

public abstract class TracerGadget extends Gadget {

	public TracerGadget(GadgetManager manager, String name, String[] desc, int cost, Material mat, byte data) {
		super(manager, GadgetType.Tracer, name, desc, cost, mat, data);
	}

	@Override
	public void DisableCustom(Player player) {
	}

	@Override
	public void EnableCustom(Player player) {
	}

	@EventHandler
	public void onProjectileLaunch(ProjectileLaunchEvent event) {
		if (event.getEntity().getShooter() instanceof Player) {
			Player shooter = (Player) event.getEntity().getShooter();
			if (IsActive(shooter)) {
				playTracer(event.getEntity());
			}
		}
	}

	public abstract void playTracer(Projectile projectile);
}
