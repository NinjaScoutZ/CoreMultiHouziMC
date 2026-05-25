package com.houzicore.arcade.nautilus.game.arcade.game.games.hideseek.runtime;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.houzicore.arcade.nautilus.game.arcade.game.games.hideseek.lang.PropRushLang;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.core.itemstack.ItemStackFactory;

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

public enum PropRushAbilityDefinition
{
    FAKE_SOUND_PING(CatalogGroup.ABILITY, "fake_sound_ping", 2, Material.NOTE_BLOCK, 28000),
    PROP_SWAP(CatalogGroup.ABILITY, "prop_swap", 8, Material.SLIME_BALL, 6000),
    CAT_TAUNT(CatalogGroup.ABILITY, "cat_taunt", 6, Material.JUKEBOX, 30000),
    SIGNAL_FLARE(CatalogGroup.ABILITY, "signal_flare", 7, Material.FIREWORK_ROCKET, 20000),
    HIDER_SNOWBALL(CatalogGroup.ABILITY, "hider_snowball", 0, Material.SNOWBALL, 0),
    SIXTH_SENSE(CatalogGroup.ABILITY, "sixth_sense", 1, Material.ENDER_EYE, 0),
    DASH(CatalogGroup.ABILITY, "dash", 3, Material.FEATHER, 15000),
    TRACKER_COMPASS(CatalogGroup.ABILITY, "tracker_compass", 8, Material.COMPASS, 30000),

    DECOY(CatalogGroup.PERK, "decoy", 5, Material.ARMOR_STAND, 20000),
    PHASE_SHIFT(CatalogGroup.PERK, "phase_shift", 5, Material.ENDER_PEARL, 15000),
    BOMB_SHELL(CatalogGroup.PERK, "bomb_shell", 5, Material.FIREWORK_STAR, 22000),
    SECRET_PASSAGE(CatalogGroup.PERK, "secret_passage", 5, Material.TRIPWIRE_HOOK, 25000),
    MIRROR_IMAGE(CatalogGroup.PERK, "mirror_image", 5, Material.ECHO_SHARD, 18000),
    SCANNER_PULSE(CatalogGroup.PERK, "scanner_pulse", 2, Material.COMPASS, 12000),
    FLARE(CatalogGroup.PERK, "flare", 2, Material.FIRE_CHARGE, 20000),
    BLOODHOUND_SENSE(CatalogGroup.PERK, "bloodhound_sense", 2, Material.BONE, 15000),
    SMOKE_BOMB(CatalogGroup.PERK, "smoke_bomb", 2, Material.GUNPOWDER, 20000),
    BOUNTY_DASH(CatalogGroup.PERK, "bounty_dash", 2, Material.CROSSBOW, 12000),
    PURGE_PULSE(CatalogGroup.PERK, "purge_pulse", 2, Material.AMETHYST_SHARD, 18000),
    SKY_SWEEP(CatalogGroup.PERK, "sky_sweep", 2, Material.FEATHER, 18000),
    ECHO_SENTRY(CatalogGroup.PERK, "echo_sentry", 2, Material.SCULK_SENSOR, 20000);

    private final CatalogGroup catalogGroup;
    private final String key;
    private final int slot;
    private final Material material;
    private final int baseCooldownMs;

    PropRushAbilityDefinition(CatalogGroup catalogGroup, String key, int slot, Material material, int baseCooldownMs)
    {
        this.catalogGroup = catalogGroup;
        this.key = key;
        this.slot = slot;
        this.material = material;
        this.baseCooldownMs = baseCooldownMs;
    }

    public String getKey()
    {
        return key;
    }

    public int getSlot()
    {
        return slot;
    }

    public Material getMaterial()
    {
        return material;
    }

    public int getBaseCooldownMs()
    {
        return baseCooldownMs;
    }

    public boolean isPerkBacked()
    {
        return catalogGroup == CatalogGroup.PERK;
    }

    public String getNameKey()
    {
        return catalogGroup == CatalogGroup.ABILITY
                ? "prop_rush.ability." + key + ".name"
                : "prop_rush.perk." + key + ".name";
    }

    public String getLoreKey()
    {
        return catalogGroup == CatalogGroup.ABILITY
                ? "prop_rush.ability." + key + ".lore"
                : "prop_rush.perk." + key + ".desc";
    }

    public ItemStack createItem(Player player)
    {
        PropRushLang lang = PropRushLang.get();
        String displayName = lang.getFallback(player, getNameKey());
        String suffix = lang.getFallback(player, "prop_rush.item.action_suffix");

        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(C.cYellow + C.Bold + displayName + C.cWhite + suffix);
        if (material.getMaxDurability() > 1) {
            meta.setUnbreakable(true);
        }
        List<String> lore = new ArrayList<String>();

        for (String line : lang.list(player, getLoreKey(), new String[0]))
        {
            lore.add(line == null || line.isEmpty() ? "" : C.cGray + line);
        }

        if (baseCooldownMs > 0)
        {
            lore.add("");
            lore.add(lang.getFallback(player, "prop_rush.item.cooldown_label",
                    Placeholder.unparsed("seconds", String.valueOf(baseCooldownMs / 1000))));
            lore.add(lang.getFallback(player, "prop_rush.item.cooldown_note"));
        }

        meta.setLore(lore);
        
        // Tag morph tool specifically
        if (this == PROP_SWAP) {
            meta.getPersistentDataContainer().set(
                new org.bukkit.NamespacedKey("houzicore", "hideseek_morph_tool"),
                org.bukkit.persistence.PersistentDataType.BYTE,
                (byte) 1
            );
        }
        
        item.setItemMeta(meta);
        return item;
    }

    private enum CatalogGroup
    {
        ABILITY,
        PERK
    }
}
