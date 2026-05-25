package com.houzicore.mapparser.listener;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import com.houzicore.shared.common.util.C;

/**
 * Provides instant visual and audio feedback when builders place or break
 * MapParser marker blocks (Pressure Plates on Wool, Signs on Sponge).
 * <p>
 * This removes the guesswork — builders immediately know whether their
 * placement registered correctly.
 */
public class MarkerFeedbackListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Block placed = event.getBlock();
        Material type = placed.getType();
        Player player = event.getPlayer();

        // ── Gold Pressure Plate → Team Spawn or Corner ──
        if (type == Material.LIGHT_WEIGHTED_PRESSURE_PLATE) {
            Block below = placed.getRelative(BlockFace.DOWN);
            if (below.getType().name().endsWith("_WOOL")) {
                String color = below.getType().name().replace("_WOOL", "");

                if (color.equals("WHITE")) {
                    notifySuccess(player, placed, "📍 Corner Marker", "§eMap Boundary Corner");
                } else {
                    String teamName = getTeamDisplayName(color);
                    notifySuccess(player, placed, "👥 Team Spawn", "§b" + teamName + " §7team spawn");
                }
                return;
            }
            // Gold plate on non-wool
            notifyWarning(player, "⚠ Gold Pressure Plate placed on non-wool block! Not a valid marker.");
        }

        // ── Iron Pressure Plate → Data Loc ──
        if (type == Material.HEAVY_WEIGHTED_PRESSURE_PLATE) {
            Block below = placed.getRelative(BlockFace.DOWN);
            if (below.getType().name().endsWith("_WOOL")) {
                String color = below.getType().name().replace("_WOOL", "");
                notifySuccess(player, placed, "📌 Data Loc", "§d" + color + " §7data point");
                return;
            }
            notifyWarning(player, "⚠ Iron Pressure Plate placed on non-wool block! Not a valid marker.");
        }

        // ── Sign on Sponge → Custom Loc ──
        if (type.name().endsWith("_SIGN") && !type.name().contains("WALL")) {
            Block below = placed.getRelative(BlockFace.DOWN);
            if (below.getType() == Material.SPONGE) {
                notifySuccess(player, placed, "🏷️ Custom Loc", "§aSign on Sponge §7(write name on sign)");
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block broken = event.getBlock();
        Player player = event.getPlayer();

        // Warn when breaking a marker component
        if (broken.getType() == Material.LIGHT_WEIGHTED_PRESSURE_PLATE) {
            Block below = broken.getRelative(BlockFace.DOWN);
            if (below.getType().name().endsWith("_WOOL")) {
                player.sendMessage(C.cRed + "⚠ Removed a Team Spawn / Corner marker!");
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f);
            }
        }
        if (broken.getType() == Material.HEAVY_WEIGHTED_PRESSURE_PLATE) {
            Block below = broken.getRelative(BlockFace.DOWN);
            if (below.getType().name().endsWith("_WOOL")) {
                player.sendMessage(C.cRed + "⚠ Removed a Data Loc marker!");
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f);
            }
        }
    }

    // ─── Helpers ─────────────────────────────────────────────────────

    private void notifySuccess(Player player, Block block, String markerType, String detail) {
        // ActionBar message
        player.sendActionBar(net.kyori.adventure.text.Component.text(
                "§a✓ " + markerType + " registered §7— " + detail
        ));

        // Particle burst at marker
        block.getWorld().spawnParticle(
                Particle.HAPPY_VILLAGER,
                block.getLocation().add(0.5, 0.8, 0.5),
                8, 0.3, 0.2, 0.3, 0
        );

        // Sound
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1f, 1.5f);
    }

    private void notifyWarning(Player player, String message) {
        player.sendMessage(C.cRed + message);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f);
    }

    private String getTeamDisplayName(String color) {
        return switch (color) {
            case "RED" -> "Red";
            case "BLUE" -> "Blue";
            case "LIGHT_BLUE" -> "Sky";
            case "YELLOW" -> "Yellow";
            case "GREEN" -> "Green";
            case "LIME" -> "Lime";
            case "ORANGE" -> "Orange";
            case "PINK" -> "Pink";
            case "PURPLE" -> "Purple";
            case "CYAN" -> "Cyan";
            case "BROWN" -> "Brown";
            case "BLACK" -> "Black";
            case "GRAY" -> "Gray";
            case "LIGHT_GRAY" -> "LightGray";
            case "MAGENTA" -> "Magenta";
            default -> color;
        };
    }
}
