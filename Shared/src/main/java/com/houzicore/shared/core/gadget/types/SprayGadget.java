package com.houzicore.shared.core.gadget.types;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;

import com.houzicore.shared.core.gadget.CosmeticRarity;
import com.houzicore.shared.core.gadget.GadgetManager;

/**
 * Base class for Spray gadgets.
 * Player right-clicks a block to spray a particle pattern on the surface.
 * The spray lasts for 5 seconds then fades.
 */
public abstract class SprayGadget extends Gadget {

    private final CosmeticRarity _rarity;

    public SprayGadget(GadgetManager manager, String name, String[] desc,
            int cost, Material displayMaterial, byte displayData, CosmeticRarity rarity) {
        super(manager, GadgetType.Spray, name, desc, cost, displayMaterial, displayData);
        _rarity = rarity;
    }

    public CosmeticRarity getRarity() {
        return _rarity;
    }

    @Override
    public void EnableCustom(Player player) {
        _active.add(player);
    }

    @Override
    public void DisableCustom(Player player) {
        _active.remove(player);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!IsActive(player)) return;
        if (event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null) return;

        event.setCancelled(true);

        Block block = event.getClickedBlock();
        BlockFace face = event.getBlockFace();

        double cx = block.getX() + 0.5 + face.getModX() * 0.55;
        double cy = block.getY() + 0.5 + face.getModY() * 0.55;
        double cz = block.getZ() + 0.5 + face.getModZ() * 0.55;

        org.bukkit.Location center = new org.bukkit.Location(block.getWorld(), cx, cy, cz);

        player.getWorld().playSound(center, org.bukkit.Sound.ENTITY_ITEM_PICKUP, 0.5f, 1.5f);

        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks >= 100) {
                    cancel();
                    return;
                }
                renderSpray(center, face, ticks);
                ticks += 2;
            }
        }.runTaskTimer(Manager.getPlugin(), 0L, 2L);
    }

    /**
     * Subclasses override to render their unique spray pattern.
     */
    protected abstract void renderSpray(org.bukkit.Location center, BlockFace face, int tick);
}
