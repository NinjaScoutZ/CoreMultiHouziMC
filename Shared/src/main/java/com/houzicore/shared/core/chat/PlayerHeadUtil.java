package com.houzicore.shared.core.chat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.object.ObjectContents;
import net.kyori.adventure.text.object.PlayerHeadObjectContents;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

/**
 * PlayerHeadUtil — Provides player-head components for tab/chat messages.
 *
 * ── FEATURE FLAG ────────────────────────────────────────────────────────────
 *   Set ENABLED = false to completely disable. No other code changes needed.
 *   This is safe to toggle after server updates or plugin conflicts.
 *
 * ── WHAT THIS DOES ──────────────────────────────────────────────────────────
 *   Produces a native Adventure object component for the inline Minecraft
 *   player head, plus a legacy hover fallback for chat contexts.
 *
 * ── INTEGRATION POINTS ──────────────────────────────────────────────────────
 *   Currently wired at:
 *     1. Chat.java#filterChat()  — the 'prefixComp' hover (name in chat)
 *
 *   This replaces the old HouziExtension/FlectonePulse <player_head> tag path
 *   for our own tab list rendering.
 */
public class PlayerHeadUtil {
    private static final String STEVE_HEAD_NAME = "MHF_Steve";
    private static final String STEVE_HEAD_TEXTURE = "ewogICJ0aW1lc3RhbXAiIDogMTc3NjQ4MTAxNzEzOCwKICAicHJvZmlsZUlkIiA6ICJjMDZmODkwNjRjOGE0OTExOWMyOWVhMWRiZDFhYWI4MiIsCiAgInByb2ZpbGVOYW1lIiA6ICJNSEZfU3RldmUiLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDVjNGVlNWNlMjBhZWQ5ZTMzZTg2NmM2NmNhYTM3MTc4NjA2MjM0YjM3MjEwODRiZjAxZDEzMzIwZmIyZWIzZiIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9";

    // ── Toggles for Player Head features ─────────────────────────────────────
    // Toggle 1: Enables the 3D skull hover effect (runs strictly on HouziCore)
    public static boolean HOVER_HEAD_ENABLED = true;

    // Toggle 2: Enables native inline player head components in tab/chat text.
    public static boolean INLINE_HEAD_ENABLED = true;

    public static Component buildInlineHead(Player player) {
        return buildInlineHead(player, true);
    }

    public static Component buildInlineHead(Player player, boolean trailingSpace) {
        if (!INLINE_HEAD_ENABLED || player == null) return Component.empty();

        try {
            Component head = Component.object().contents(buildPlayerHeadContents(player)).build();

            return trailingSpace ? head.append(Component.space()) : head;
        } catch (Throwable ignored) {
            return Component.empty();
        }
    }

    public static Component buildInlineHead(String playerName) {
        return buildInlineHead(playerName, true);
    }

    public static Component buildInlineHead(String playerName, boolean trailingSpace) {
        if (!INLINE_HEAD_ENABLED || playerName == null || playerName.isEmpty()) return Component.empty();

        try {
            Player onlineTarget = Bukkit.getPlayerExact(playerName);
            if (onlineTarget != null && onlineTarget.isOnline()) {
                return buildInlineHead(onlineTarget, trailingSpace);
            }

            Component head = Component.object().contents(
                    ObjectContents.playerHead()
                            .name(playerName)
                            .hat(true)
                            .build()
            ).build();

            return trailingSpace ? head.append(Component.space()) : head;
        } catch (Throwable ignored) {
            return Component.empty();
        }
    }

    public static Component buildSteveHead(boolean trailingSpace) {
        Component head = buildInlineHeadWithTexture(STEVE_HEAD_NAME, STEVE_HEAD_TEXTURE, trailingSpace);
        if (!head.equals(Component.empty())) {
            return head;
        }

        return buildInlineHead(STEVE_HEAD_NAME, trailingSpace);
    }

    private static Component buildInlineHeadWithTexture(String playerName, String textureValue, boolean trailingSpace) {
        if (!INLINE_HEAD_ENABLED || playerName == null || playerName.isEmpty() || textureValue == null || textureValue.isEmpty()) {
            return Component.empty();
        }

        try {
            Component head = Component.object().contents(
                    ObjectContents.playerHead()
                            .name(playerName)
                            .profileProperty(PlayerHeadObjectContents.property("textures", textureValue))
                            .hat(true)
                            .build()
            ).build();

            return trailingSpace ? head.append(Component.space()) : head;
        } catch (Throwable ignored) {
            return Component.empty();
        }
    }

    private static PlayerHeadObjectContents buildPlayerHeadContents(Player player) {
        PlayerHeadObjectContents.Builder builder = ObjectContents.playerHead()
                .id(player.getUniqueId())
                .name(player.getName())
                .hat(true);

        Object profile = player.getPlayerProfile();
        Iterable<?> properties = readProfileProperties(profile);
        if (properties != null) {
            for (Object property : properties) {
                String name = readProfileProperty(property, "getName");
                if (!"textures".equalsIgnoreCase(name)) {
                    continue;
                }

                String value = readProfileProperty(property, "getValue");
                if (value != null && !value.isEmpty()) {
                    builder.profileProperty(PlayerHeadObjectContents.property("textures", value));
                    break;
                }
            }
        }

        return builder.build();
    }

    private static Iterable<?> readProfileProperties(Object profile) {
        if (profile == null) {
            return null;
        }

        try {
            Object properties = profile.getClass().getMethod("getProperties").invoke(profile);
            return properties instanceof Iterable ? (Iterable<?>) properties : null;
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static String readProfileProperty(Object property, String methodName) {
        if (property == null) {
            return null;
        }

        try {
            Object value = property.getClass().getMethod(methodName).invoke(property);
            return value instanceof String ? (String) value : null;
        } catch (Throwable ignored) {
            return null;
        }
    }

    /**
     * Legacy tag string for old HouziExtension/FlectonePulse pipelines.
     * Prefer {@link #buildInlineHead(Player)} inside HouziCore modules.
     *
     * @param player The player whose face to show
     * @return The tag string or an empty string if disabled
     */
    public static String getHeadTag(Player player) {
        if (!INLINE_HEAD_ENABLED || player == null) return "";
        return "<player_head:" + player.getName() + "> ";
    }


    /**
     * Builds a hover event that shows the player's skull item with their skin.
     * When the viewer hovers over the sender's name in chat, a tooltip
     * showing a preview of their Minecraft head + name will appear.
     *
     * @param player The chat message sender
     * @return A HoverEvent wrapping the player's head, or null if disabled
     */
    public static HoverEvent<?> buildHeadHover(Player player) {
        if (!HOVER_HEAD_ENABLED) return null;

        try {
            // Build a PlayerHead ItemStack with the sender's skin
            ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) skull.getItemMeta();
            if (meta != null) {
                meta.setOwningPlayer(player);
                skull.setItemMeta(meta);
            }

            // Adventure-compatible hover: show the skull item (displays the player's face)
            return skull.asHoverEvent();

        } catch (Exception e) {
            // Graceful degrade: if anything fails, return plain text hover
            return HoverEvent.showText(Component.text(player.getName(), NamedTextColor.YELLOW));
        }
    }

    /**
     * Wraps any Component with the head hover if enabled,
     * otherwise returns the component unchanged.
     */
    public static Component withHeadHover(Component component, Player player) {
        if (!HOVER_HEAD_ENABLED) return component;
        HoverEvent<?> hover = buildHeadHover(player);
        return hover != null ? component.hoverEvent(hover) : component;
    }
}
