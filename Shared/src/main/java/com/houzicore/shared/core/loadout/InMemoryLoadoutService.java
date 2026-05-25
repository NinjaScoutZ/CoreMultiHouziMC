package com.houzicore.shared.core.loadout;

import org.bukkit.entity.Player;

import com.houzicore.shared.api.loadout.LoadoutItemRegistry;
import com.houzicore.shared.api.loadout.LoadoutProfile;
import com.houzicore.shared.api.loadout.LoadoutService;
import com.houzicore.shared.common.util.UtilInv;

public class InMemoryLoadoutService implements LoadoutService {
    
    private final LoadoutItemRegistry registry;

    public InMemoryLoadoutService(LoadoutItemRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void apply(Player player, LoadoutProfile profile) {
        registry.getFactory(profile).ifPresent(factory -> {
            UtilInv.Clear(player);
            java.util.List<org.bukkit.inventory.ItemStack> items = factory.create(player);
            for (int slot = 0; slot < items.size(); slot++) {
                org.bukkit.inventory.ItemStack item = items.get(slot);
                if (item == null) {
                    continue;
                }
                String mat = item.getType().name();
                if (mat.endsWith("_HELMET")) player.getInventory().setHelmet(item);
                else if (mat.endsWith("_CHESTPLATE")) player.getInventory().setChestplate(item);
                else if (mat.endsWith("_LEGGINGS")) player.getInventory().setLeggings(item);
                else if (mat.endsWith("_BOOTS")) player.getInventory().setBoots(item);
                else player.getInventory().setItem(slot, item);
            }
        });
    }

    @Override
    public void clear(Player player) {
        UtilInv.Clear(player);
    }
}
