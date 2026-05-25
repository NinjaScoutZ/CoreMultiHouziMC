package com.houzicore.shared.core.gadget.types;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import com.houzicore.shared.common.util.NautHashMap;
import com.houzicore.shared.core.gadget.CosmeticRarity;
import com.houzicore.shared.core.gadget.GadgetManager;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;

/**
 * Base class for Banner gadgets.
 * Displays a floating banner/flag above the player.
 */
public abstract class BannerGadget extends Gadget {

    private final CosmeticRarity _rarity;
    private final NautHashMap<String, ArmorStand> _bannerStands = new NautHashMap<>();

    public BannerGadget(GadgetManager manager, String name, String[] desc,
            int cost, Material displayMaterial, byte displayData, CosmeticRarity rarity) {
        super(manager, GadgetType.Banner, name, desc, cost, displayMaterial, displayData);
        _rarity = rarity;
    }

    public CosmeticRarity getRarity() {
        return _rarity;
    }

    @Override
    public void EnableCustom(Player player) {
        _active.add(player);

        Location loc = player.getLocation().add(0, 2.5, 0);
        ArmorStand stand = player.getWorld().spawn(loc, ArmorStand.class, as -> {
            as.setInvisible(true);
            as.setGravity(false);
            as.setMarker(true);
            as.setSmall(true);
            as.setCustomNameVisible(true);
            as.setCustomName(getBannerDisplayName());
        });
        _bannerStands.put(player.getName(), stand);
    }

    @Override
    public void DisableCustom(Player player) {
        _active.remove(player);

        ArmorStand stand = _bannerStands.remove(player.getName());
        if (stand != null && !stand.isDead()) {
            stand.remove();
        }
    }

    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if (event.getType() != UpdateType.TICK) return;

        for (Player player : GetActive()) {
            ArmorStand stand = _bannerStands.get(player.getName());
            if (stand != null && player.isOnline()) {
                Location target = player.getLocation().add(0, 2.5, 0);
                stand.teleport(target);
            }
        }
    }

    protected abstract String getBannerDisplayName();
}
