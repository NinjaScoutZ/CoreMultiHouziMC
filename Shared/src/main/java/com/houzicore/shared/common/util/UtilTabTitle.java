package com.houzicore.shared.common.util;

import org.bukkit.entity.Player;

public class UtilTabTitle {
    public static void setHeaderAndFooter(Player player, String header, String footer) {
        player.setPlayerListHeaderFooter(header, footer);
    }

    public static void broadcastHeaderAndFooter(String header, String footer) {
        for (Player player : UtilServer.getPlayers()) {
            setHeaderAndFooter(player, header, footer);
        }
    }
}
