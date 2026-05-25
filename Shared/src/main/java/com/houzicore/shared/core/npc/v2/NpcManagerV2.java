package com.houzicore.shared.core.npc.v2;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.houzicore.shared.MiniPlugin;
import com.houzicore.shared.core.hologram.HologramManager;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;

public class NpcManagerV2 extends MiniPlugin {

    private final HologramManager hologramManager;
    private final List<HouziNPC> npcs = new ArrayList<>();

    public NpcManagerV2(JavaPlugin plugin, HologramManager hologramManager) {
        super("NpcManagerV2", plugin);
        this.hologramManager = hologramManager;
    }

    public void registerNpc(HouziNPC npc) {
        npcs.add(npc);
        if (npc.getConfig().getLocation().getChunk().isLoaded()) {
            npc.spawn(_plugin, hologramManager);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChunkLoad(ChunkLoadEvent event) {
        for (HouziNPC npc : npcs) {
            if (!npc.isSpawned() && npc.getConfig().getLocation().getChunk().equals(event.getChunk())) {
                npc.spawn(_plugin, hologramManager);
            }
        }
    }

    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if (event.getType() != UpdateType.SEC) return;
        
        for (HouziNPC npc : npcs) {
            if (!npc.isSpawned() && npc.getConfig().getLocation().getChunk().isLoaded()) {
                npc.spawn(_plugin, hologramManager);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof LivingEntity)) return;
        
        for (HouziNPC npc : npcs) {
            if (npc.isSpawned() && npc.getEntity().equals(event.getRightClicked())) {
                event.setCancelled(true);
                npc.onClick(event.getPlayer(), _plugin);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        for (HouziNPC npc : npcs) {
            if (npc.isSpawned() && npc.getEntity().equals(event.getEntity())) {
                event.setCancelled(true);
                return;
            }
        }
    }
}
