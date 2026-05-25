package com.houzicore.shared.core.gadget.gadgets;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import com.houzicore.shared.core.gadget.GadgetManager;
import com.houzicore.shared.core.gadget.types.MorphGadget;
import com.houzicore.shared.core.disguise.disguises.DisguiseWarden;

public class MorphWarden extends MorphGadget {
    public MorphWarden(GadgetManager manager) {
        super(manager, "Warden Morph", new String[] { "Listen closely..." }, 50000, Material.SCULK_SENSOR, (byte) 0);
    }
    @Override
    public void EnableCustom(Player player) {
        ApplyArmor(player);
        DisguiseWarden disguise = new DisguiseWarden(player);
        Manager.getDisguiseManager().disguise(disguise);
    }
    @Override
    public void DisableCustom(Player player) {
        RemoveArmor(player);
        Manager.getDisguiseManager().undisguise(player);
    }
}
