package com.houzicore.shared.core.gadget.types;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import com.houzicore.shared.core.gadget.CosmeticRarity;
import com.houzicore.shared.core.gadget.GadgetManager;

/**
 * Gadget type for furniture cosmetic rewards.
 * Each furniture item is backed by a BDEngine model that can be spawned
 * in the player's lobby island or personal space.
 */
public class FurnitureGadget extends Gadget {

    private final String _modelId;
    private final Material _previewMaterial;
    private final CosmeticRarity _rarity;

    /**
     * @param manager         The owning GadgetManager
     * @param name            Display name shown in the cosmetic menu
     * @param modelId         BDEngine model name used for spawning the furniture
     * @param rarity          Cosmetic rarity tier
     * @param previewMaterial Material used as the preview icon in shop GUIs
     */
    public FurnitureGadget(GadgetManager manager, String name, String modelId,
            CosmeticRarity rarity, Material previewMaterial) {
        super(manager, GadgetType.Item, name,
                new String[] {
                        org.bukkit.ChatColor.GRAY + "A decorative furniture piece.",
                        org.bukkit.ChatColor.GRAY + "Place it in your personal space!"
                },
                -2, previewMaterial, (byte) 0);

        _modelId = modelId;
        _rarity = rarity;
        _previewMaterial = previewMaterial;
    }

    // ── Accessors ──────────────────────────────────────────────────

    /**
     * Returns the BDEngine model ID used to spawn this furniture in-world.
     */
    public String getModelId() {
        return _modelId;
    }

    /**
     * Returns the Material used as a preview icon in shop menus.
     */
    public Material getPreviewMaterial() {
        return _previewMaterial;
    }

    @Override
    public CosmeticRarity getRarity() {
        return _rarity;
    }

    // ── Gadget Lifecycle ───────────────────────────────────────────

    @Override
    public void EnableCustom(Player player) {
        _active.add(player);
    }

    @Override
    public void DisableCustom(Player player) {
        _active.remove(player);
    }
}
