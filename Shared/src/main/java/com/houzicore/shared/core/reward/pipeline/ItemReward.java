package com.houzicore.shared.core.reward.pipeline;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.UtilParticle;

/**
 * Concrete implementation of RewardBase for distributing physical inventory items.
 * Does not support essence/currency multipliers.
 */
public class ItemReward extends RewardBase {

    private final ItemStack _item;

    public ItemReward(String reason, ItemStack item) {
        super(reason, item.getAmount());
        _item = item.clone();
    }

    @Override
    public boolean supportsMultiplier() {
        return false; // Physical items cannot be multiplied by game essence multipliers
    }

    @Override
    public void giveReward(Player player, double multiplier) {
        player.getInventory().addItem(_item);
    }

    @Override
    public void playAnimation(Player player, Location location) {
        if (location == null) location = player.getLocation().add(0, 1, 0);
        player.playSound(location, Sound.ENTITY_ITEM_PICKUP, 1f, 1.5f);
        UtilParticle.PlayParticle(UtilParticle.ParticleType.FIREWORKS_SPARK, location, 0.5f, 0.5f, 0.5f, 0.1f, 20, UtilParticle.ViewDist.NORMAL, player);
    }

    @Override
    public String getSummaryString(double multiplier) {
        String itemName = "";
        if (_item.getItemMeta() != null && _item.getItemMeta().hasDisplayName()) {
            itemName = _item.getItemMeta().getDisplayName();
        } else {
            // Very simple fallback for display naming
            itemName = _item.getType().name().replace("_", " ").toLowerCase();
            itemName = Character.toUpperCase(itemName.charAt(0)) + itemName.substring(1);
        }
        
        return "  " + C.cGreen + "▸ " + C.cWhite + (int)getAmount() + "x " + itemName + "  \u00A78• \u00A7e" + getReason();
    }
}
