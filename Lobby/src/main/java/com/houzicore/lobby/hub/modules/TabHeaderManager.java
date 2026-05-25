package com.houzicore.lobby.hub.modules;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.houzicore.shared.MiniPlugin;
import com.houzicore.shared.account.CoreClient;
import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.core.common.BrandConfig;
import com.houzicore.shared.core.chat.PlayerHeadUtil;
import com.houzicore.shared.core.lang.LangManager;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

/**
 * TabHeaderManager — Arcade-style tab header & footer for Lobby.
 *
 * Header: separator → small caps brand → bilingual tagline
 * Footer: online count + lobby name → server address → separator
 */
public class TabHeaderManager extends MiniPlugin {

    private final CoreClientManager _clientManager;
    private final LegacyComponentSerializer _leg = LegacyComponentSerializer.legacySection();
    private String _serverName;

    public TabHeaderManager(JavaPlugin plugin, CoreClientManager clientManager) {
        super("Tab Header", plugin);
        _clientManager = clientManager;
    }

    private String getServerName() {
        if (_serverName == null) {
            _serverName = getPlugin().getConfig().getString("serverstatus.name", "Lobby");
        }
        return _serverName;
    }

    // ── Send tab on join ────────────────────────────────────────────────────

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        sendTab(event.getPlayer());
    }

    // ── Periodic refresh (online count changes) ──────────────────────────

    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if (event.getType() != UpdateType.SEC) return;

        for (Player player : UtilServer.getPlayers()) {
            sendTab(player);
        }
    }

    // ── Build & send ────────────────────────────────────────────────────────

    private void sendTab(Player player) {
        boolean isThai = LangManager.get().isThai(player);
        int online = UtilServer.getPlayers().length;
        CoreClient client = _clientManager.Get(player);
        Rank rank = (client != null && client.GetRank() != null) ? client.GetRank() : Rank.ALL;

        // ── Header ──
        String header =
            "§8§m──────────────────────────\n" +
            BrandConfig.tabHeader() + "\n" +
            (isThai ? "§7เซิร์ฟเวอร์ Minecraft สุดพรีเมียม" : "§7The Premium Minecraft Server");

        // ── Footer ──
        String rankTag = (rank == Rank.ALL) ? "§7Player" : rank.GetTag(false, false);

        String footer =
            "\n§7🌐 " + (isThai ? "ออนไลน์" : "Online") + ": §a" + online +
            "  §8|  " +
            "§7👑 " + rankTag +
            "  §8|  " +
            "§7🎮 §e" + getServerName() +
            "\n§fplay." + BrandConfig.website() + "\n" +
            "§8§m──────────────────────────";

        Component headerComp = _leg.deserialize(header);
        Component footerComp = _leg.deserialize(footer);
        player.sendPlayerListHeaderAndFooter(headerComp, footerComp);

        Component headComp = PlayerHeadUtil.buildInlineHead(player);
        Component wideTag = com.houzicore.shared.core.chat.Chat.buildWideTagComponent(rank.name());
        
        if (wideTag != null) {
            player.playerListName(headComp.append(wideTag).append(Component.space()).append(_leg.deserialize("§f" + player.getName())));
        } else {
            player.playerListName(headComp.append(_leg.deserialize(rankTag + " §f" + player.getName())));
        }
    }
}
