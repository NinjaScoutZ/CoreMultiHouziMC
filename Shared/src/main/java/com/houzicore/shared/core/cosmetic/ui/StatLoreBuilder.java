package com.houzicore.shared.core.cosmetic.ui;

import com.houzicore.shared.core.gadget.CosmeticRarity;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Structured lore builder inspired by Swofty HypixelSkyBlock's ItemLore pattern.
 * Renders stat lines with icons, color-coded values, and modifier breakdowns.
 * <p>
 * Example output:
 * <pre>
 *   §9§l✦ Rare
 *
 *   §7Coin Bonus: §a+15%
 *   §7XP Boost: §b+10%
 *
 *   §7A cute cat pet that follows
 *   §7you around the lobby.
 *
 *   §a§l▶ ACTIVE
 * </pre>
 *
 * Usage:
 * <pre>
 *   List&lt;String&gt; lore = new StatLoreBuilder()
 *       .rarity(CosmeticRarity.RARE)
 *       .stat("Coin Bonus", 15, "%", StatLoreBuilder.StatColor.GREEN)
 *       .stat("XP Boost", 10, "%", StatLoreBuilder.StatColor.AQUA)
 *       .description("A cute cat pet that follows", "you around the lobby.")
 *       .status(true, true) // owned, active
 *       .build();
 * </pre>
 */
public class StatLoreBuilder {

    /** Predefined stat color schemes matching Swofty/Hypixel conventions. */
    public enum StatColor {
        RED(ChatColor.RED, "❁"),
        GREEN(ChatColor.GREEN, "✦"),
        AQUA(ChatColor.AQUA, "✎"),
        BLUE(ChatColor.BLUE, "☣"),
        GOLD(ChatColor.GOLD, "☘"),
        LIGHT_PURPLE(ChatColor.LIGHT_PURPLE, "♣"),
        WHITE(ChatColor.WHITE, "✯"),
        YELLOW(ChatColor.YELLOW, "⚔");

        private final ChatColor color;
        private final String symbol;

        StatColor(ChatColor color, String symbol) {
            this.color = color;
            this.symbol = symbol;
        }

        public ChatColor getColor() { return color; }
        public String getSymbol() { return symbol; }
    }

    private CosmeticRarity rarity;
    private final List<StatEntry> stats = new ArrayList<>();
    private final List<String> descriptionLines = new ArrayList<>();
    private Boolean owned;
    private Boolean active;
    private String activeText = ChatColor.GREEN + "" + ChatColor.BOLD + "▶ ACTIVE";
    private String ownedText = ChatColor.YELLOW + "Click to activate!";
    private String lockedText = ChatColor.RED + "✖ Not Unlocked";
    private final List<String> extraFooter = new ArrayList<>();

    // ── Builder API ─────────────────────────────────────────────

    public StatLoreBuilder rarity(CosmeticRarity rarity) {
        this.rarity = rarity;
        return this;
    }

    /**
     * Add a stat line.
     * @param name  Stat display name (e.g. "Coin Bonus")
     * @param value Numeric value
     * @param suffix Value suffix (e.g. "%" or "")
     * @param color Color scheme for the value
     */
    public StatLoreBuilder stat(String name, double value, String suffix, StatColor color) {
        stats.add(new StatEntry(name, value, 0, suffix, color));
        return this;
    }

    /**
     * Add a stat line with a bonus breakdown.
     * Renders as: §7Name: §a+{base} §e(+{bonus})
     */
    public StatLoreBuilder stat(String name, double baseValue, double bonusValue, String suffix, StatColor color) {
        stats.add(new StatEntry(name, baseValue, bonusValue, suffix, color));
        return this;
    }

    public StatLoreBuilder description(String... lines) {
        for (String line : lines) {
            descriptionLines.add(line);
        }
        return this;
    }

    public StatLoreBuilder status(boolean owned, boolean active) {
        this.owned = owned;
        this.active = active;
        return this;
    }

    public StatLoreBuilder statusText(String activeText, String ownedText, String lockedText) {
        this.activeText = activeText;
        this.ownedText = ownedText;
        this.lockedText = lockedText;
        return this;
    }

    public StatLoreBuilder footer(String... lines) {
        for (String line : lines) {
            extraFooter.add(line);
        }
        return this;
    }

    // ── Build ───────────────────────────────────────────────────

    public List<String> build() {
        List<String> lore = new ArrayList<>();

        // Rarity header
        if (rarity != null) {
            lore.add(rarity.getDisplayName());
            lore.add("");
        }

        // Stat lines
        if (!stats.isEmpty()) {
            for (StatEntry entry : stats) {
                lore.add(formatStatLine(entry));
            }
            lore.add("");
        }

        // Description
        if (!descriptionLines.isEmpty()) {
            for (String line : descriptionLines) {
                lore.add(ChatColor.GRAY + line);
            }
            lore.add("");
        }

        // Status
        if (owned != null) {
            if (active != null && active) {
                lore.add(activeText);
            } else if (owned) {
                lore.add(ownedText);
            } else {
                lore.add(lockedText);
            }
        }

        // Extra footer
        for (String line : extraFooter) {
            lore.add(line);
        }

        return lore;
    }

    // ── Internal ────────────────────────────────────────────────

    private String formatStatLine(StatEntry entry) {
        double total = entry.baseValue + entry.bonusValue;
        String prefix = total >= 0 ? "+" : "";
        String valueStr;

        if (total == (long) total) {
            valueStr = prefix + (long) total + entry.suffix;
        } else {
            valueStr = prefix + String.format("%.1f", total) + entry.suffix;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(ChatColor.GRAY).append(entry.name).append(": ");
        sb.append(entry.color.getColor()).append(valueStr);

        // Bonus breakdown (Swofty pattern: yellow parenthetical bonus)
        if (entry.bonusValue != 0) {
            String bonusPrefix = entry.bonusValue >= 0 ? "+" : "";
            String bonusStr;
            if (entry.bonusValue == (long) entry.bonusValue) {
                bonusStr = bonusPrefix + (long) entry.bonusValue;
            } else {
                bonusStr = bonusPrefix + String.format("%.1f", entry.bonusValue);
            }
            sb.append(" ").append(ChatColor.YELLOW).append("(").append(bonusStr).append(")");
        }

        return sb.toString();
    }

    private record StatEntry(String name, double baseValue, double bonusValue, String suffix, StatColor color) {}
}
