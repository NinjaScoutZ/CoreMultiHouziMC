package com.houzicore.shared.core.chat.command;

import org.bukkit.entity.Player;

import com.houzicore.shared.common.Rank;
import com.houzicore.shared.core.chat.Chat;
import com.houzicore.shared.core.command.CommandBase;
import com.houzicore.shared.core.icon.CustomIconManager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * /symbol [page] — Browse all loaded texture icons with pagination.
 * Similar to FlectonePulse's /sprite command for showing available symbols.
 */
public class SymbolCommand extends CommandBase<Chat> {

    private static final int ITEMS_PER_PAGE = 15;

    public SymbolCommand(Chat plugin) {
        super(plugin, Rank.ALL, "symbol");
    }

    @Override
    public void Execute(Player caller, String[] args) {
        CustomIconManager mgr = CustomIconManager.getInstance();
        if (mgr == null) {
            caller.sendMessage(Component.text("§cIcon system is not loaded."));
            return;
        }

        // Gather all unique base names (strip _1, _2, etc.)
        Map<String, Integer> baseNames = new TreeMap<>();
        for (String key : mgr.getAllKeys()) {
            String upper = key.toUpperCase();
            // Match pattern NAME_N
            if (upper.matches(".*_\\d+$")) {
                String base = upper.replaceAll("_\\d+$", "");
                baseNames.merge(base, 1, Integer::sum);
            } else {
                baseNames.put(upper, 1);
            }
        }

        List<String> sorted = new ArrayList<>(baseNames.keySet());
        int totalItems = sorted.size();
        int totalPages = Math.max(1, (int) Math.ceil((double) totalItems / ITEMS_PER_PAGE));

        int page = 1;
        if (args.length > 0) {
            try {
                page = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {}
        }
        page = Math.max(1, Math.min(page, totalPages));

        int start = (page - 1) * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, totalItems);

        // Header
        caller.sendMessage(Component.text("§3§l| §bAvailable symbols, total " + totalItems));
        caller.sendMessage(Component.empty());

        // Show icons
        Component line = Component.empty();
        for (int i = start; i < end; i++) {
            String name = sorted.get(i);
            Component wideTag = Chat.buildWideTagComponent(name);
            if (wideTag != null) {
                Component iconWithHover = wideTag
                        .hoverEvent(HoverEvent.showText(
                                Component.text("§e" + name.toLowerCase() + "\n§7Click to type: §f<texture:" + name.toLowerCase() + ">")))
                        .clickEvent(ClickEvent.suggestCommand("<texture:" + name.toLowerCase() + ">"));
                line = line.append(iconWithHover).append(Component.text(" "));
            }
        }
        caller.sendMessage(line);
        caller.sendMessage(Component.empty());

        // Pagination
        Component prev = page > 1
                ? Component.text("←", NamedTextColor.GREEN, TextDecoration.BOLD)
                    .clickEvent(ClickEvent.runCommand("/symbol " + (page - 1)))
                    .hoverEvent(HoverEvent.showText(Component.text("Previous page")))
                : Component.text("←", NamedTextColor.DARK_GRAY);

        Component next = page < totalPages
                ? Component.text("→", NamedTextColor.GREEN, TextDecoration.BOLD)
                    .clickEvent(ClickEvent.runCommand("/symbol " + (page + 1)))
                    .hoverEvent(HoverEvent.showText(Component.text("Next page")))
                : Component.text("→", NamedTextColor.DARK_GRAY);

        Component pagination = Component.text("§3§l| ")
                .append(prev)
                .append(Component.text(" Page: " + page + "/" + totalPages + " ", NamedTextColor.AQUA))
                .append(next);

        caller.sendMessage(pagination);
    }
}
