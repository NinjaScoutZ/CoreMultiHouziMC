package com.houzicore.shared.core.mount.types;

import org.bukkit.Material;
import org.bukkit.entity.Phantom;
import org.bukkit.entity.Player;

import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.core.mount.Mount;
import com.houzicore.shared.core.mount.MountManager;

public class MountPhantom extends Mount<Phantom> {

    public MountPhantom(MountManager manager) {
        super(manager, "Phantom Mount", Material.PHANTOM_MEMBRANE, (byte)0, new String[] {
            "Swoop from the skies!"
        }, 30000);
    }

    @Override
    public void Disable(Player player) {
        Phantom mount = _active.remove(player);
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
        
        Phantom mount = player.getWorld().spawn(player.getLocation(), Phantom.class);
        mount.setSize(1);
        mount.setCustomName(player.getName() + "'s " + GetName());
        
        UtilPlayer.message(player, F.main("Mount", "You spawned " + F.elem(GetName()) + "."));
        
        _active.put(player, mount);
        mount.addPassenger(player);
    }
}
