package com.houzicore.shared.core.npc.v2;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.houzicore.shared.common.util.UtilEnt;
import com.houzicore.shared.core.hologram.Hologram;
import com.houzicore.shared.core.hologram.HologramManager;

public abstract class HouziNPC {

    private final HouziNPCConfig config;
    private final Map<String, DialogueSet> dialogueMap = new HashMap<>();
    
    private LivingEntity entity;
    private Hologram hologram;
    
    private final Map<UUID, Boolean> playersInDialogue = new HashMap<>();

    public HouziNPC(HouziNPCConfig config) {
        this.config = config;
        for (DialogueSet ds : dialogues()) {
            dialogueMap.put(ds.getId(), ds);
        }
    }

    public HouziNPCConfig getConfig() {
        return config;
    }

    /**
     * Spawn the entity and hologram. Called by NpcManagerV2.
     */
    public void spawn(Plugin plugin, HologramManager hologramManager) {
        if (entity != null && entity.isValid()) {
            return;
        }

        Location loc = config.getLocation();
        if (loc.getWorld() == null || !loc.getChunk().isLoaded()) {
            return;
        }

        // Standard Spawning
        entity = (LivingEntity) loc.getWorld().spawnEntity(loc, config.getEntityType());
        
        UtilEnt.Vegetate(entity);
        UtilEnt.silence(entity, true);
        UtilEnt.ghost(entity, true, false);
        entity.setCustomNameVisible(false);
        entity.setCustomName(null);
        entity.setCanPickupItems(false);
        entity.setRemoveWhenFarAway(false);
        entity.setPersistent(false);
        
        // Disable AI for static NPC
        entity.setAI(false);
        entity.setCollidable(false);
        
        // Spawn Hologram
        String[] lines = config.getHolograms();
        if (lines != null && lines.length > 0) {
            hologram = new Hologram(hologramManager, entity.getLocation().add(0, entity.getEyeHeight() + 1.2, 0), lines);
            hologram.setFollowEntity(entity);
            hologram.start();
        }
    }

    public void remove() {
        if (hologram != null) {
            hologram.stop();
            hologram = null;
        }
        if (entity != null) {
            entity.remove();
            entity = null;
        }
    }

    public LivingEntity getEntity() {
        return entity;
    }

    public boolean isSpawned() {
        return entity != null && entity.isValid();
    }

    public boolean isInDialogue(Player player) {
        return playersInDialogue.getOrDefault(player.getUniqueId(), false);
    }

    /**
     * Plays a predefined dialogue set asynchronously.
     */
    public CompletableFuture<Void> setDialogue(Plugin plugin, Player player, String dialogueId) {
        DialogueSet ds = dialogueMap.get(dialogueId);
        if (ds == null) {
            CompletableFuture<Void> failed = new CompletableFuture<>();
            failed.completeExceptionally(new IllegalArgumentException("Dialogue ID not found: " + dialogueId));
            return failed;
        }

        playersInDialogue.put(player.getUniqueId(), true);
        
        String npcName = "NPC";
        if (config.getHolograms() != null && config.getHolograms().length > 0) {
            npcName = org.bukkit.ChatColor.stripColor(config.getHolograms()[0]);
        }

        return ds.play(plugin, player, npcName).whenComplete((v, ex) -> {
            playersInDialogue.put(player.getUniqueId(), false);
        });
    }

    // Abstract Methods for Subclasses
    
    /**
     * Called when a player clicks this NPC.
     */
    public abstract void onClick(Player player, Plugin plugin);

    /**
     * Register dialogues here.
     */
    public abstract DialogueSet[] dialogues();

}
