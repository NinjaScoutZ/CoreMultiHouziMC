package com.houzicore.mapbuilder.bootstrap;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import com.houzicore.shared.api.PlayerStateApplier;
import com.houzicore.shared.api.context.PlayerContextId;
import com.houzicore.shared.api.context.PlayerContextService;
import com.houzicore.shared.common.util.UtilInv;

/**
 * Applies the MAP_EDIT and MAP_PREVIEW player states.
 *
 * IMPORTANT — Inventory ownership:
 *   Entry: cleanState() is called AFTER snapshotService.capture() in startSession().
 *          The player's real inventory is safe in the snapshot before we wipe anything.
 *   Exit:  snapshotService.restore() is called BEFORE applyContextState() in endSession().
 *          This applier never destroys permanent player data on exit.
 */
public class MapBuilderPlayerStateApplier implements PlayerStateApplier {

    private final PlayerContextService contextService;

    public MapBuilderPlayerStateApplier(PlayerContextService contextService) {
        this.contextService = contextService;
    }

    @Override
    public void applyContextState(Player player, PlayerContextId contextId) {
        if (contextId == PlayerContextId.MAP_EDIT) {
            // cleanState() was already called by startSession() before snapshot capture → entry wipe
            // Here we only give the editor-specific loadout on top of a clean slate.
            applyMapEditorLoadout(player);
        }
        // MAP_PREVIEW: flight only — inventory is whatever was restored from the snapshot.
        // No explicit item assignment needed; cleanState on entry is sufficient.
    }

    @Override
    public void refreshState(Player player) {
        PlayerContextId contextId = contextService.getCurrentContextId(player);
        applyContextState(player, contextId);
    }

    /**
     * Called during MAP_EDIT entry, AFTER snapshotService.capture() and BEFORE applyContextState().
     * Wipes the session-editor slate — the player's real inventory is already safely captured.
     */
    @Override
    public void cleanState(Player player) {
        UtilInv.Clear(player);
        player.setGameMode(GameMode.CREATIVE);
        player.setAllowFlight(true);
        player.setFlying(false);
        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        player.setSaturation(20f);
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
    }

    // -------------------------------------------------------------------------

    private void applyMapEditorLoadout(Player player) {
        player.setGameMode(GameMode.CREATIVE);
        player.setAllowFlight(true);

        org.bukkit.inventory.Inventory inv = player.getInventory();

        // Slot 0: Dashboard
        inv.setItem(0, createWand(Material.NETHER_STAR,
                com.houzicore.mapbuilder.tool.DashboardToolHandler.WAND_NAME,
                "§7Right-Click: Map completeness & export"));

        // Slot 1: Point Tool
        inv.setItem(1, createWand(Material.BLAZE_ROD,
                com.houzicore.mapbuilder.tool.PointToolHandler.WAND_NAME,
                "§7Right-Click: Place selected point",
                "§7Shift+Right: Change selection",
                "§7Q (Drop): Deselect"));

        // Slot 2: Boundary Tool
        inv.setItem(2, createWand(Material.WOODEN_AXE,
                com.houzicore.mapbuilder.tool.BoundaryToolHandler.WAND_NAME,
                "§7Left-Click: Set Min corner",
                "§7Right-Click: Set Max corner"));

        // Slot 3: Display Tool
        inv.setItem(3, createWand(Material.BREEZE_ROD,
                com.houzicore.mapbuilder.tool.DisplayToolHandler.WAND_NAME,
                "§7Shift+Right: Browse block materials",
                "§7Right-Click: Place Display entity"));

        // Slot 4: Eraser
        inv.setItem(4, createWand(Material.IRON_HOE,
                com.houzicore.mapbuilder.tool.EraserToolHandler.WAND_NAME,
                "§7Left-Click: Delete nearest point (4m)",
                "§7Right-Click: Inspect nearest point"));

        // Slot 5: Model Editor (kept from legacy)
        inv.setItem(5, createWand(Material.GOLDEN_HOE,
                org.bukkit.ChatColor.LIGHT_PURPLE + "Model Editor",
                "§7Right/Left-Click: Edit nearest model"));

        // Slot 7: Undo / Redo
        inv.setItem(7, createWand(Material.CLOCK,
                org.bukkit.ChatColor.YELLOW + "Undo / Redo",
                "§7Left-Click: Undo last action",
                "§7Right-Click: Redo"));

        // Slot 8: Finish
        inv.setItem(8, createWand(Material.EMERALD,
                com.houzicore.mapbuilder.tool.FinishToolHandler.WAND_NAME,
                "§7Right-Click: Validate + Export",
                "§7Shift+Right: Force Export (bypass gate)",
                "§7Left-Click: Cancel session"));
    }

    private org.bukkit.inventory.ItemStack createWand(Material mat, String name, String... lore) {
        org.bukkit.inventory.ItemStack item = new org.bukkit.inventory.ItemStack(mat);
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(java.util.Arrays.asList(lore));
        item.setItemMeta(meta);
        return item;
    }
}
