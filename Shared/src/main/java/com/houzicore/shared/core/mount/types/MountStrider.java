package com.houzicore.shared.core.mount.types;

import org.bukkit.Material;
import org.bukkit.entity.Strider;
import org.bukkit.entity.Player;

import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.core.mount.Mount;
import com.houzicore.shared.core.mount.MountManager;

public class MountStrider extends Mount<Strider> {

    public MountStrider(MountManager manager) {
        super(manager, "Strider Mount", Material.WARPED_FUNGUS_ON_A_STICK, (byte)0, new String[] {
            "Walk on lava!"
        }, 15000);
    }

    @Override
    public void Disable(Player player) {
        Strider mount = _active.remove(player);
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
        
        Strider mount = player.getWorld().spawn(player.getLocation(), Strider.class);
        mount.setAdult();
        mount.setCustomName(player.getName() + "'s " + GetName());
        
        UtilPlayer.message(player, F.main("Mount", "You spawned " + F.elem(GetName()) + "."));
        
        _active.put(player, mount);
        mount.addPassenger(player);
    }
}
