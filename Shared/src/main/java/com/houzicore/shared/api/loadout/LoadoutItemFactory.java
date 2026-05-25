package com.houzicore.shared.api.loadout;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.util.List;

@FunctionalInterface
public interface LoadoutItemFactory {
    /**
     * Creates the list of item stacks for a loadout profile.
     * The implementation can assign particular slots if it uses specific methods, 
     * but standard returning gives sequential ordered items.
     * Given that we might want exact slot placement, maybe it returns a map or handles the inventory directly?
     * The planned design said: `List<ItemStack> create(Player player);`
     * Let's stick to the plan.
     */
    List<ItemStack> create(Player player);
}
