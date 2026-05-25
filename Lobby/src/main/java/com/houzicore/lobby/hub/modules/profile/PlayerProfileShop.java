package com.houzicore.lobby.hub.modules.profile;

import org.bukkit.entity.Player;

import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.common.util.NautHashMap;
import com.houzicore.shared.common.util.UtilText;
import com.houzicore.shared.core.shop.ShopBase;
import com.houzicore.shared.core.shop.page.ShopPageBase;
import com.houzicore.shared.core.stats.StatsManager;
import com.houzicore.lobby.hub.HubManager;

public class PlayerProfileShop extends ShopBase<HubManager> {

    private final StatsManager _stats;
    private final com.houzicore.shared.core.level.LvlManager _levels;
    private final NautHashMap<Player, Player> _targetMap = new NautHashMap<>();

    public PlayerProfileShop(HubManager plugin, CoreClientManager clientManager, StatsManager stats, com.houzicore.shared.core.level.LvlManager levels) {
        super(plugin, clientManager, null, "Profile");
        _stats = stats;
        _levels = levels;
    }

    public void setTarget(Player viewer, Player target) {
        _targetMap.put(viewer, target);
        // Clear cached page so it rebuilds with the new target
        if (getPageMap().containsKey(viewer.getName())) {
            getPageMap().remove(viewer.getName());
        }
    }

    @Override
    protected ShopPageBase<HubManager, ? extends ShopBase<HubManager>> buildPagesFor(Player player) {
        Player target = _targetMap.get(player);
        String title = "§8" + UtilText.toSmallCaps("profile") + " » " + (target != null ? target.getName() : "?");
        return new PlayerProfilePage(getPlugin(), this, getClientManager(), title, player, target, _stats, _levels);
    }
}
