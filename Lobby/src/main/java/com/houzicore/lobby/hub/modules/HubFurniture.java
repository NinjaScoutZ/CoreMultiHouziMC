package com.houzicore.lobby.hub.modules;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import com.houzicore.shared.core.displayentity.DisplayEntityManager;
import com.houzicore.shared.core.displayentity.DisplayModel;
import com.houzicore.lobby.hub.HubManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Hub Furniture system.
 * 
 * Uses REAL blocks (stairs for chairs, slabs for tables) so players
 * can sit on them, cannot walk through them, and the world builder
 * can place/destroy them naturally.
 * 
 * The trophy showcase still uses a BDEngine DisplayModel for the
 * floating, spinning beacon effect.
 */
public class HubFurniture implements Listener {

    private final HubManager _manager;
    private final DisplayEntityManager _displayManager;
    private final List<ArmorStand> _seatEntities = new ArrayList<>();

    public HubFurniture(HubManager manager, DisplayEntityManager displayManager) {
        _manager = manager;
        _displayManager = displayManager;

        // Register seat-click listener
        Bukkit.getPluginManager().registerEvents(this, manager.getPlugin());

        // ── 1. Seat system initialized ──
    }

    // ── Seat Click Handler ──────────────────────────

    @EventHandler
    public void onSeatClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block block = event.getClickedBlock();
        if (block == null) return;

        // Only allow sitting on stair blocks (chairs)
        if (!(block.getBlockData() instanceof Stairs)) return;

        Player player = event.getPlayer();

        // Don't sit if sneaking (allows placing blocks on stairs)
        if (player.isSneaking()) return;

        // Check if someone is already sitting here
        Location seatLoc = block.getLocation().add(0.5, 0.0, 0.5);
        for (ArmorStand seat : _seatEntities) {
            if (seat.isValid() && seat.getLocation().distanceSquared(seatLoc) < 1.0) {
                if (!seat.getPassengers().isEmpty()) {
                    return; // Someone is already sitting
                }
            }
        }

        // Spawn invisible ArmorStand as seat
        ArmorStand seatStand = (ArmorStand) block.getWorld().spawnEntity(seatLoc, EntityType.ARMOR_STAND);
        seatStand.setVisible(false);
        seatStand.setGravity(false);
        seatStand.setSmall(true);
        seatStand.setMarker(true);
        seatStand.setInvulnerable(true);
        seatStand.setPersistent(false);
        seatStand.addPassenger(player);
        _seatEntities.add(seatStand);

        event.setCancelled(true);
    }

    @EventHandler
    public void onDismount(org.bukkit.event.entity.EntityDismountEvent event) {
        if (!(event.getDismounted() instanceof ArmorStand stand)) return;
        if (!_seatEntities.contains(stand)) return;

        // Clean up the seat ArmorStand
        _seatEntities.remove(stand);
        Bukkit.getScheduler().runTaskLater(_manager.getPlugin(), () -> {
            stand.remove();
        }, 1L);
    }

    /**
     * Cleanup all seat ArmorStands (called on disable).
     */
    public void cleanup() {
        for (ArmorStand seat : _seatEntities) {
            if (seat != null && seat.isValid()) {
                seat.eject();
                seat.remove();
            }
        }
        _seatEntities.clear();
    }
}
