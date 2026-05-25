package com.houzicore.shared.core.gadget.gadgets;

import org.bukkit.ChatColor;
import org.bukkit.Material;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.core.gadget.CosmeticRarity;
import com.houzicore.shared.core.gadget.GadgetManager;
import com.houzicore.shared.core.gadget.types.BannerGadget;

public class BannerChampion extends BannerGadget {

    public BannerChampion(GadgetManager manager) {
        super(manager, "Champion Banner",
                new String[] { C.cWhite + "A golden banner floats", C.cWhite + "above your head!" },
                -2, Material.ORANGE_BANNER, (byte) 0, CosmeticRarity.LEGENDARY);
    }

    @Override
    protected String getBannerDisplayName() {
        return ChatColor.GOLD + "" + ChatColor.BOLD + "⚜ " +
               ChatColor.YELLOW + "" + ChatColor.BOLD + "CHAMPION" +
               ChatColor.GOLD + "" + ChatColor.BOLD + " ⚜";
    }
}
