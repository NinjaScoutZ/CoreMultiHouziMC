package com.houzicore.arcade.nautilus.game.arcade.game.modules;

import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Player;
import com.houzicore.arcade.nautilus.game.arcade.game.Game;
import java.util.HashMap;

public class ItemGiverModule extends GameModule<Game> {
    private final HashMap<String, ItemStack[]> _kitItems = new HashMap<>();

    public ItemGiverModule(Game game) {
        super(game);
    }

    public void addKit(String kitName, ItemStack... items) {
        _kitItems.put(kitName, items);
    }

    public void give(Player player, String kitName) {
        if (!isActive()) return;
        ItemStack[] items = _kitItems.get(kitName);
        if (items != null) {
            for (ItemStack item : items) {
                if (item != null) {
                    player.getInventory().addItem(item.clone());
                }
            }
        }
    }
}
