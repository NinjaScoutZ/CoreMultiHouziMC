package com.houzicore.shared.core.gadget.gadgets;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import com.houzicore.shared.core.gadget.GadgetManager;
import com.houzicore.shared.core.gadget.types.MorphGadget;
import com.houzicore.shared.core.disguise.disguises.DisguiseFox;

public class MorphFox extends MorphGadget {
    public MorphFox(GadgetManager manager) {
        super(manager, "Fox Morph", new String[] { "Be a sneaky fox!" }, 20000, Material.PLAYER_HEAD, (byte) 0);
    }
    @Override
    public void EnableCustom(Player player) {
        ApplyArmor(player);
        DisguiseFox disguise = new DisguiseFox(player);
        Manager.getDisguiseManager().disguise(disguise);
    }
    @Override
    public void DisableCustom(Player player) {
        RemoveArmor(player);
        Manager.getDisguiseManager().undisguise(player);
    }
}
