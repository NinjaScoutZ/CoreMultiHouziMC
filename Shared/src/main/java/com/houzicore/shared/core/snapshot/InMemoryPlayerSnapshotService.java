package com.houzicore.shared.core.snapshot;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import com.houzicore.shared.api.snapshot.PlayerSnapshotService;

public class InMemoryPlayerSnapshotService implements PlayerSnapshotService {

    private final Map<UUID, Map<String, PlayerSnapshot>> storage = new ConcurrentHashMap<>();

    @Override
    public void capture(Player player, String snapshotId) {
        if (player == null || snapshotId == null) return;

        PlayerSnapshot snapshot = new PlayerSnapshot(
                cloneArray(player.getInventory().getContents()),
                cloneArray(player.getInventory().getArmorContents()),
                cloneItem(player.getInventory().getItemInOffHand()),
                player.getHealth(),
                player.getFoodLevel(),
                player.getActivePotionEffects()
        );

        storage.computeIfAbsent(player.getUniqueId(), k -> new ConcurrentHashMap<>())
               .put(snapshotId, snapshot);
    }

    @Override
    public boolean hasSnapshot(Player player, String snapshotId) {
        if (player == null || snapshotId == null) return false;

        Map<String, PlayerSnapshot> userSnapshots = storage.get(player.getUniqueId());
        return userSnapshots != null && userSnapshots.containsKey(snapshotId);
    }

    @Override
    public void restore(Player player, String snapshotId) {
        if (player == null || snapshotId == null) return;

        Map<String, PlayerSnapshot> userSnapshots = storage.get(player.getUniqueId());
        if (userSnapshots == null) return;

        PlayerSnapshot snapshot = userSnapshots.remove(snapshotId); // Consume on restore
        if (snapshot == null) return;

        player.getInventory().setContents(cloneArray(snapshot.inventory()));
        player.getInventory().setArmorContents(cloneArray(snapshot.armor()));
        player.getInventory().setItemInOffHand(cloneItem(snapshot.offhand()));
        
        // Ensure valid health (handle max health changes between captures)
        double maxHealth = player.getMaxHealth();
        player.setHealth(Math.min(snapshot.health(), maxHealth));
        
        player.setFoodLevel(snapshot.foodLevel());

        // Clear existing effects then apply snapshot effects
        for (PotionEffect current : player.getActivePotionEffects()) {
            player.removePotionEffect(current.getType());
        }
        player.addPotionEffects(snapshot.effects());
    }

    @Override
    public void discard(Player player, String snapshotId) {
        if (player == null || snapshotId == null) return;
        
        Map<String, PlayerSnapshot> userSnapshots = storage.get(player.getUniqueId());
        if (userSnapshots != null) {
            userSnapshots.remove(snapshotId);
        }
    }

    @Override
    public void cleanup(UUID playerId) {
        storage.remove(playerId);
    }

    private ItemStack[] cloneArray(ItemStack[] source) {
        if (source == null) return new ItemStack[0];
        ItemStack[] dest = new ItemStack[source.length];
        for (int i = 0; i < source.length; i++) {
            dest[i] = cloneItem(source[i]);
        }
        return dest;
    }

    private ItemStack cloneItem(ItemStack source) {
        return (source == null) ? null : source.clone();
    }

    // Internal record to hold snapshot data safely
    private record PlayerSnapshot(
            ItemStack[] inventory,
            ItemStack[] armor,
            ItemStack offhand,
            double health,
            int foodLevel,
            Collection<PotionEffect> effects) {
    }
}
