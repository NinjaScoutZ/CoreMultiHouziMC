package com.houzicore.shared.core.mount.types;

import org.bukkit.Material;
import org.bukkit.entity.Bee;
import org.bukkit.entity.Player;

import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.core.mount.Mount;
import com.houzicore.shared.core.mount.MountManager;

public class MountBee extends Mount<Bee> {

    public MountBee(MountManager manager) {
        super(manager, "Bee Mount", Material.HONEYCOMB, (byte)0, new String[] {
            "Bzz bzz bzz!"
        }, 15000);
    }

    @Override
    public void Disable(Player player) {
        Bee mount = _active.remove(player);
        if (mount != null) {
            mount.remove();
            UtilPlayer.message(player, F.main("Mount", "You despawned " + F.elem(GetName()) + "."));
            Manager.removeActive(player);
        }
    }

    @Override
    public void EnableCustom(Player player) {
        player.leaveVehicle();
        player.eject();

        Manager.DeregisterAll(player);
        
        Bee mount = player.getWorld().spawn(player.getLocation(), Bee.class);
        mount.setAdult();
        mount.setCustomName(player.getName() + "'s " + GetName());
        
        UtilPlayer.message(player, F.main("Mount", "You spawned " + F.elem(GetName()) + "."));
        
        _active.put(player, mount);
        mount.addPassenger(player);
    }
}
