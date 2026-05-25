package com.houzicore.arcade.nautilus.game.arcade.game.games.searchanddestroy;

import com.houzicore.shared.core.shop.item.IButton;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;

//import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

public class EvolveButton implements IButton
{
    private SearchAndDestroy _search;
    private Kit _kit;

    public EvolveButton(SearchAndDestroy arcadeManager, Kit kit)
    {
        _search = arcadeManager;
        _kit = kit;
    }

    @Override
    public void onClick(Player player, ClickType clickType)
    {
        // Make sure this player isn't a spectator
        if (!_search.IsAlive(player))
            return;

        _search.SetKit(player, _kit, true);
        _search.onEvolve(player);
        player.closeInventory();
    }
}
