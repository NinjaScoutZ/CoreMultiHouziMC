package com.houzicore.shared.core.mount;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilPlayer;

public class DragonMount extends Mount<DragonData> {
	public DragonMount(MountManager manager, String name, String[] desc, Material displayMaterial, byte displayData,
			int cost) {
		super(manager, name, displayMaterial, displayData, desc, cost);

		KnownPackage = false;
	}

	@Override
	public void Disable(Player player) {
		final DragonData data = _active.remove(player);
		if (data != null) {
			data.DragonBase.remove();

			// Inform
			UtilPlayer.message(player, F.main("Mount", com.houzicore.shared.core.lang.LangManager.get().get(player, "mount.despawned").replace("{0}", F.elem(GetName()))));

			Manager.removeActive(player);
		}
	}

	@Override
	public void EnableCustom(final Player player) {
		player.leaveVehicle();
		player.eject();

		// Remove other mounts
		Manager.DeregisterAll(player);

		// Inform
		UtilPlayer.message(player, F.main("Mount", com.houzicore.shared.core.lang.LangManager.get().get(player, "mount.spawned").replace("{0}", F.elem(GetName()))));

		// Store
		final DragonData dragonData = new DragonData(this, player);
		// Set max health to 1 so player doesn't see a bunch of mount hearts flashing
		// when NewsManager changes the health
		dragonData.DragonBase.setMaxHealth(1.0);
		dragonData.DragonBase.setHealth(1.0);
		_active.put(player, dragonData);
	}
}
