package com.houzicore.shared.core.chat;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.UtilText;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

/**
 * Chat Tag / Chat Format Manager (#38)
 * Applies rank-based colored SmallCaps tags before player name in chat.
 * Tags use the HouziCore Rank enum (WARRIOR, SOVEREIGN, DIVINE, MODERATOR, ADMIN, OWNER).
 */
public class ChatTagManager implements Listener {

    private final CoreClientManager _clients;

    public ChatTagManager(JavaPlugin plugin, CoreClientManager clients) {
        _clients = clients;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        String tag = getTag(player);
        String nameColor = getNameColor(player);
        String name = player.getName();

        String rawMsg = LegacyComponentSerializer.legacySection()
            .serialize(event.message());

        String formatted = tag + " " + nameColor + name + C.cGray + " » " + C.cWhite + rawMsg;
        event.renderer((source, sourceDisplayName, message, viewer) ->
            Component.text(formatted)
        );
    }

    private String getTag(Player player) {
        if (_clients.Get(player) == null) return "§8[" + UtilText.toSmallCaps("player") + "]";
        Rank rank = _clients.Get(player).GetRank();

        if (rank.Has(Rank.OWNER))      return "§4§l[" + UtilText.toSmallCaps("owner")   + "]";
        if (rank.Has(Rank.ADMIN))      return "§c§l[" + UtilText.toSmallCaps("admin")   + "]";
        if (rank.Has(Rank.SNR_MODERATOR)) return "§6§l[" + UtilText.toSmallCaps("sr.mod") + "]";
        if (rank.Has(Rank.MODERATOR))  return "§6§l[" + UtilText.toSmallCaps("mod")     + "]";
        if (rank.Has(Rank.HELPER))     return "§b§l[" + UtilText.toSmallCaps("trainee") + "]";
        if (rank.Has(Rank.DIVINE))     return "§a§l[" + UtilText.toSmallCaps("เทพ")     + "]";
        if (rank.Has(Rank.SOVEREIGN))  return "§d§l[" + UtilText.toSmallCaps("ราชันย์")  + "]";
        if (rank.Has(Rank.WARRIOR))    return "§b§l[" + UtilText.toSmallCaps("จอมยุทธ")  + "]";
        return "§7[" + UtilText.toSmallCaps("player") + "]";
    }

    private String getNameColor(Player player) {
        if (_clients.Get(player) == null) return "§f";
        Rank rank = _clients.Get(player).GetRank();

        if (rank.Has(Rank.OWNER))      return "§4";
        if (rank.Has(Rank.ADMIN))      return "§c";
        if (rank.Has(Rank.SNR_MODERATOR) || rank.Has(Rank.MODERATOR)) return "§6";
        if (rank.Has(Rank.HELPER))     return "§b";
        if (rank.Has(Rank.DIVINE))     return "§a";
        if (rank.Has(Rank.SOVEREIGN))  return "§d";
        if (rank.Has(Rank.WARRIOR))    return "§b";
        return "§f";
    }
}
