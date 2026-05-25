package com.houzicore.bungeecord.command;

import com.houzicore.bungeecord.playerTracker.PlayerTracker;
import com.houzicore.shared.serverdata.data.PlayerStatus;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;

public class FindCommand extends Command {

    private final PlayerTracker _playerTracker;
    private final Plugin _plugin;

    public FindCommand(Plugin plugin, PlayerTracker playerTracker) {
        // "bfind" is command, permission "houzicore.admin", alias "find"
        super("bfind", "houzicore.admin", "find");
        _plugin = plugin;
        _playerTracker = playerTracker;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(new TextComponent(ChatColor.RED + "Usage: /bfind <player>"));
            return;
        }

        String target = args[0];
        
        // Fetch from Redis async to prevent blocking the BungeeCord event loop
        _plugin.getProxy().getScheduler().runAsync(_plugin, () -> {
            PlayerStatus status = _playerTracker.getPlayerStatus(target);
            if (status != null && status.getServer() != null) {
                sender.sendMessage(new TextComponent(ChatColor.GREEN + target + " is currently on: " + ChatColor.YELLOW + status.getServer()));
            } else {
                sender.sendMessage(new TextComponent(ChatColor.RED + target + " is not online on the network."));
            }
        });
    }
}
