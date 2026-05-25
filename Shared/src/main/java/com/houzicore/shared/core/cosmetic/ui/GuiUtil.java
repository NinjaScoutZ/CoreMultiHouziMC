package com.houzicore.shared.core.cosmetic.ui;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.houzicore.shared.core.gadget.CosmeticRarity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Shared GUI utilities for modern cosmetic shop pages.
 * Provides border filling, rarity indicators, progress bars, and text animations.
 */
public final class GuiUtil {

    private GuiUtil() {}

    // ── Border & Layout ──────────────────────────────────────────────

    private static final Material DEFAULT_BORDER = Material.BLACK_STAINED_GLASS_PANE;

    /**
     * Fill all border slots of a 54-slot (6-row) inventory with glass panes.
     */
    public static void fillBorders(Inventory inv, Material material) {
        ItemStack pane = createPane(material);
        int size = inv.getSize();
        int rows = size / 9;
        for (int i = 0; i < size; i++) {
            int row = i / 9;
            int col = i % 9;
            if (row == 0 || row == rows - 1 || col == 0 || col == 8) {
                if (inv.getItem(i) == null) {
                    inv.setItem(i, pane);
                }
            }
        }
    }

    public static void fillBorders(Inventory inv) {
        fillBorders(inv, DEFAULT_BORDER);
    }

    /**
     * Fill every empty slot with a filler pane (for clean look).
     */
    public static void fillEmpty(Inventory inv, Material material) {
        ItemStack pane = createPane(material);
        for (int i = 0; i < inv.getSize(); i++) {
            if (inv.getItem(i) == null) {
                inv.setItem(i, pane);
            }
        }
    }

    private static ItemStack createPane(Material material) {
        ItemStack pane = new ItemStack(material);
        ItemMeta meta = pane.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
            pane.setItemMeta(meta);
        }
        return pane;
    }

    // ── Rarity ───────────────────────────────────────────────────────

    /**
     * Create a glass pane border item matching the given rarity color.
     */
    public static ItemStack createRarityBorder(CosmeticRarity rarity) {
        return createPane(rarity.getBorderMaterial());
    }

    /**
     * Build a rarity lore line for display in item tooltips.
     */
    public static String rarityLoreLine(CosmeticRarity rarity) {
        return rarity.getDisplayName();
    }

    // ── Progress Bar ─────────────────────────────────────────────────

    /**
     * Create a text progress bar like: §a■■■■■§7■■■■■ 5/10
     */
    public static String createProgressBar(int owned, int total) {
        int bars = 10;
        int filled = total == 0 ? 0 : Math.min(bars, (int) ((double) owned / total * bars));
        StringBuilder sb = new StringBuilder();
        sb.append(ChatColor.GREEN);
        for (int i = 0; i < filled; i++) sb.append("■");
        sb.append(ChatColor.GRAY);
        for (int i = filled; i < bars; i++) sb.append("■");
        sb.append(" ").append(ChatColor.WHITE).append(owned).append("/").append(total);
        return sb.toString();
    }

    // ── Category Icon ────────────────────────────────────────────────

    /**
     * Create a standard category icon ItemStack with name and lore.
     */
    public static ItemStack createCategoryIcon(Material material, String name, String[] lore, boolean glow) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (lore != null && lore.length > 0) {
                meta.setLore(Arrays.asList(lore));
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    // ── Text Animations ──────────────────────────────────────────────

    private static final ChatColor[] RAINBOW = {
        ChatColor.RED, ChatColor.GOLD, ChatColor.YELLOW,
        ChatColor.GREEN, ChatColor.AQUA, ChatColor.LIGHT_PURPLE
    };

    private static final String[] SPARKLES = {"✦", "✧", "★", "☆", "✶", "✴"};

    /**
     * Rainbow cycle: each character gets a different rainbow color, offset shifts each tick.
     */
    public static String rainbowText(String text, int tick) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == ' ') {
                sb.append(' ');
                continue;
            }
            sb.append(RAINBOW[(i + tick) % RAINBOW.length]);
            sb.append(ChatColor.BOLD);
            sb.append(text.charAt(i));
        }
        return sb.toString();
    }

    /**
     * Shimmer: a bright "wave" (§f§l) slides across the text, rest is §7.
     */
    public static String shimmerText(String text, int tick, ChatColor baseColor) {
        int pos = tick % (text.length() + 4);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            if (i >= pos - 1 && i <= pos + 1) {
                sb.append(ChatColor.WHITE).append(ChatColor.BOLD);
            } else {
                sb.append(baseColor);
            }
            sb.append(text.charAt(i));
        }
        return sb.toString();
    }

    /**
     * Gradient pulse: alternates between two colors based on tick parity.
     */
    public static String gradientPulseText(String text, int tick, ChatColor color1, ChatColor color2) {
        ChatColor current = (tick % 6 < 3) ? color1 : color2;
        return current + "" + ChatColor.BOLD + text;
    }

    /**
     * Sparkle: decorative symbols rotate around the name.
     */
    public static String sparkleText(String text, int tick, ChatColor color) {
        String left = SPARKLES[tick % SPARKLES.length];
        String right = SPARKLES[(tick + 3) % SPARKLES.length];
        return color + left + " " + color + ChatColor.BOLD + text + " " + color + right;
    }

    /**
     * Typewriter: reveals text one character at a time, then resets.
     */
    public static String typewriterText(String text, int tick, ChatColor color) {
        int len = (tick % (text.length() + 5)) + 1; // +5 = pause at end
        if (len > text.length()) len = text.length();
        return color + "" + ChatColor.BOLD + text.substring(0, len) + ChatColor.GRAY + "█";
    }

    /**
     * Apply an animation to a given text based on AnimationType.
     */
    public static String animateText(String text, int tick, AnimationType type, ChatColor primaryColor) {
        switch (type) {
            case RAINBOW_CYCLE:
                return rainbowText(text, tick);
            case SHIMMER:
                return shimmerText(text, tick, primaryColor);
            case GRADIENT_PULSE:
                return gradientPulseText(text, tick, primaryColor, ChatColor.YELLOW);
            case SPARKLE:
                return sparkleText(text, tick, primaryColor);
            case TYPEWRITER:
                return typewriterText(text, tick, primaryColor);
            default:
                return primaryColor + text;
        }
    }

    // ── Lore Helpers ─────────────────────────────────────────────────

    /**
     * Build a standard cosmetic item lore with rarity, description, and status.
     */
    public static List<String> buildCosmeticLore(CosmeticRarity rarity, String[] description,
            boolean owned, boolean active) {
        List<String> lore = new ArrayList<>();
        lore.add(rarityLoreLine(rarity));
        lore.add("");
        for (String line : description) {
            lore.add(ChatColor.GRAY + line);
        }
        lore.add("");
        if (active) {
            lore.add(ChatColor.GREEN + "" + ChatColor.BOLD + "▶ ACTIVE");
        } else if (owned) {
            lore.add(ChatColor.YELLOW + "Click to activate!");
        } else {
            lore.add(ChatColor.RED + "✖ Not Unlocked");
            lore.add(ChatColor.GRAY + "Found in Treasure Chests");
        }
        return lore;
    }
}
