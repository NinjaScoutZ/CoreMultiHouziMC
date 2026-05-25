package com.houzicore.shared.core.gadget.gadgets;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import com.houzicore.shared.core.gadget.GadgetManager;
import com.houzicore.shared.core.gadget.types.MorphGadget;
import com.houzicore.shared.core.disguise.disguises.DisguiseParrot;

public class MorphParrot extends MorphGadget {
    public MorphParrot(GadgetManager manager) {
        super(manager, "Parrot Morph", new String[] { "Squawk squawk!" }, 20000, Material.PLAYER_HEAD, (byte) 0);
    }
    @Override
    public void EnableCustom(Player player) {
        ApplyArmor(player);
        DisguiseParrot disguise = new DisguiseParrot(player);
        Manager.getDisguiseManager().disguise(disguise);
    }
    @Override
    public void DisableCustom(Player player) {
        RemoveArmor(player);
        Manager.getDisguiseManager().undisguise(player);
    }
}
