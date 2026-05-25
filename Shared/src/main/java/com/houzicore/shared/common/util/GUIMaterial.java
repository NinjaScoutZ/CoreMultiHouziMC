package com.houzicore.shared.common.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

/**
 * Wraps either a Material or a Skull texture into a single type.
 * Makes GUI icon definitions clean — one class handles both regular items and custom heads.
 *
 * <pre>
 * new GUIMaterial(Material.DIAMOND_SWORD)              // normal item
 * new GUIMaterial("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp...")   // skull texture
 * </pre>
 *
 * Ported from: net.swofty.type.generic.gui.inventory.item.GUIMaterial
 */
public class GUIMaterial {

    private final Material material;
    private final String skullTexture;

    /** Create from a standard Material. */
    public GUIMaterial(Material material) {
        this.material = material;
        this.skullTexture = null;
    }

    /** Create from a base64 skull texture string. */
    public GUIMaterial(String skullTexture) {
        this.material = Material.PLAYER_HEAD;
        this.skullTexture = skullTexture;
    }

    public boolean isSkull() {
        return skullTexture != null;
    }

    /** Create a plain ItemStack (no display name or lore). */
    public ItemStack toItemStack() {
        if (isSkull()) {
            return UtilSkull.getCustomSkull(skullTexture);
        }
        return new ItemStack(material);
    }

    /** Create an ItemStack with display name and optional lore lines. */
    public ItemStack toItemStack(String displayName, String... lore) {
        ItemStack item = toItemStack();
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (displayName != null) meta.setDisplayName(displayName);
            if (lore != null && lore.length > 0) meta.setLore(Arrays.asList(lore));
            item.setItemMeta(meta);
        }
        return item;
    }

    public Material getMaterial() { return material; }
    public String getSkullTexture() { return skullTexture; }
}
