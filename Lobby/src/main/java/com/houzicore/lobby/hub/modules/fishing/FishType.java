package com.houzicore.lobby.hub.modules.fishing;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import java.util.HashMap;
import java.util.Map;

public enum FishType {

    JUNK("ของเน่าเสีย", "Trash", Material.KELP, 250,  0,   null,                    ChatColor.DARK_GRAY),
    COMMON("ปลาธรรมดา", "Common Fish", Material.COD, 500,  5,   null,                    ChatColor.GRAY),
    UNCOMMON("ปลาลาย", "Striped Fish", Material.SALMON, 150,  15,  null,                    ChatColor.GREEN),
    RARE("ปลาสีทอง", "Golden Fish", Material.PUFFERFISH, 60,   40,  null,                    ChatColor.GOLD),
    KEY("กุญแจหีบสมบัติ", "Treasure Key", Material.TRIPWIRE_HOOK,  30,   20,  "Treasure Key",          ChatColor.AQUA),
    EPIC("กล่องสุ่มระดับสูง", "Rare Treasure", Material.CHEST, 5,   100, "Rare Treasure Key",     ChatColor.DARK_PURPLE),
    LEGENDARY("ยศรายเดือน/แจ็คพ็อต", "Jackpot/Rank", Material.ENDER_CHEST,   1,   0,   "Titan_Rank_30D",        ChatColor.GOLD);

    public final String displayName;
    public final String displayNameEN;
    public final Material icon;
    public final int weight;
    public final int essence;
    public final String salesPackage; // null = essence only
    public final ChatColor color;

    FishType(String displayName, String displayNameEN, Material icon, int weight, int essence, String salesPackage, ChatColor color) {
        this.displayName   = displayName;
        this.displayNameEN = displayNameEN;
        this.icon          = icon;
        this.weight        = weight;
        this.essence       = essence;
        this.salesPackage  = salesPackage;
        this.color         = color;
    }

    public String getName(boolean isThai) {
        return isThai ? displayName : displayNameEN;
    }

    public enum CatchGrade {
        PERFECT, GOOD, SLOW
    }

    public static Map<FishType, Integer> getAdjustedWeights(CatchGrade grade, int combo, boolean frenzy) {
        Map<FishType, Integer> weights = new HashMap<>();
        
        // Multiplier: +2% chance per combo, +10% if perfect. Max double original chance.
        double rankMultiplier = 1.0 + (combo * 0.02) + (grade == CatchGrade.PERFECT ? 0.10 : 0);
        if (rankMultiplier > 2.0) rankMultiplier = 2.0;
        
        for (FishType t : values()) {
            int w = t.weight;
            
            if (frenzy) {
                if (t == JUNK || t == COMMON) w = 0;
                else if (t == UNCOMMON) w += 300; 
                else w = (int) Math.ceil(w * 2.5); 
            } else {
                if (t.ordinal() >= RARE.ordinal()) {
                    w = (int) Math.ceil(w * rankMultiplier);
                }
            }
            
            weights.put(t, Math.max(0, w));
        }
        
        return weights;
    }

    public static Map<FishType, Double> getDropChances(int combo, boolean frenzy) {
        Map<FishType, Integer> w = getAdjustedWeights(CatchGrade.GOOD, combo, frenzy);
        double total = 0;
        for (int val : w.values()) total += val;
        
        Map<FishType, Double> chances = new HashMap<>();
        if (total == 0) return chances;
        
        for (FishType t : values()) {
            chances.put(t, (w.get(t) / total) * 100.0);
        }
        return chances;
    }

    public static FishType draw(CatchGrade grade, int combo, boolean frenzy) {
        Map<FishType, Integer> w = getAdjustedWeights(grade, combo, frenzy);
        int total = 0;
        for (int val : w.values()) total += val;
        
        if (total <= 0) return COMMON;
        
        int roll = (int) (Math.random() * total);
        for (FishType t : values()) {
            roll -= w.get(t);
            if (roll < 0) return t;
        }
        return COMMON;
    }

    public boolean isSpecial() {
        return salesPackage != null;
    }
}
