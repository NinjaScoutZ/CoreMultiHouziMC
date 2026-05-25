package com.houzicore.shared.core.chat.command;

import org.bukkit.entity.Player;

import com.houzicore.shared.common.Rank;
import com.houzicore.shared.core.chat.Chat;
import com.houzicore.shared.core.command.CommandBase;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.object.ObjectContents;
import net.kyori.adventure.text.object.SpriteObjectContents;

/**
 * /sprite <atlas:name> — Display a Minecraft atlas sprite in chat.
 * Examples:
 *   /sprite block/oak_log         -> shows oak log from blocks atlas
 *   /sprite gui:container/slot    -> shows container slot from gui atlas
 *   /sprite mob_effect/speed      -> shows speed effect sprite
 */
public class SpriteCommand extends CommandBase<Chat> {

    public SpriteCommand(Chat plugin) {
        super(plugin, Rank.ALL, "sprite");
    }

    @Override
    public void Execute(Player caller, String[] args) {
        if (args.length == 0) {
            caller.sendMessage(Component.text("§3§l| §bSprite Viewer", NamedTextColor.AQUA));
            caller.sendMessage(Component.text("§7Usage: §f/sprite <name>", NamedTextColor.GRAY));
            caller.sendMessage(Component.text("§7Usage: §f/sprite <atlas>:<name>", NamedTextColor.GRAY));
            caller.sendMessage(Component.empty());
            caller.sendMessage(Component.text("§7Examples:", NamedTextColor.GRAY));

            // Show some example sprites
            String[][] examples = {
                {"block/oak_log", "blocks"},
                {"block/diamond_block", "blocks"},
                {"block/gold_block", "blocks"},
                {"block/emerald_block", "blocks"},
                {"block/redstone_block", "blocks"},
                {"block/lapis_block", "blocks"},
                {"item/diamond_sword", "blocks"},
                {"item/golden_apple", "blocks"},
                {"mob_effect/speed", "mob_effects"},
                {"mob_effect/strength", "mob_effects"},
            };

            Component exampleLine = Component.empty();
            for (String[] ex : examples) {
                try {
                    Key spriteKey = Key.key(ex[0]);
                    SpriteObjectContents sprite = ObjectContents.sprite(spriteKey);
                    Component spriteComp = Component.object().contents(sprite).build();
                    exampleLine = exampleLine.append(
                        spriteComp
                            .hoverEvent(HoverEvent.showText(Component.text("§e" + ex[0] + "\n§7Click to use")))
                            .clickEvent(ClickEvent.suggestCommand("/sprite " + ex[0]))
                    ).append(Component.text(" "));
                } catch (Exception ignored) {}
            }
            caller.sendMessage(exampleLine);
            caller.sendMessage(Component.empty());
            caller.sendMessage(Component.text("§7Use §fF3 + S §7in-game to dump atlas files for reference.", NamedTextColor.GRAY));
            return;
        }

        String input = args[0];
        
        try {
            Component spriteComp;
            String displayName;
            
            if (input.contains(":") && !input.startsWith("minecraft:")) {
                // atlas:name format (e.g. gui:container/slot)
                String[] parts = input.split(":", 2);
                Key atlasKey = Key.key(parts[0]);
                Key spriteKey = Key.key(parts[1]);
                SpriteObjectContents sprite = ObjectContents.sprite(spriteKey, atlasKey);
                spriteComp = Component.object().contents(sprite).build();
                displayName = parts[0] + ":" + parts[1];
            } else {
                // Just name (default blocks atlas)
                String cleanName = input.startsWith("minecraft:") ? input.substring(10) : input;
                Key spriteKey = Key.key(cleanName);
                SpriteObjectContents sprite = ObjectContents.sprite(spriteKey);
                spriteComp = Component.object().contents(sprite).build();
                displayName = cleanName;
            }

            Component result = Component.text("§3§l| §bSprite: §f" + displayName + " ")
                    .append(spriteComp);
            caller.sendMessage(result);

            // Show usage hint
            Component hint = Component.text("§7Tag: §f<sprite:" + displayName + ">", NamedTextColor.GRAY)
                    .clickEvent(ClickEvent.copyToClipboard("<sprite:" + displayName + ">"))
                    .hoverEvent(HoverEvent.showText(Component.text("§eClick to copy tag")));
            caller.sendMessage(hint);

        } catch (Exception e) {
            caller.sendMessage(Component.text("§cInvalid sprite: " + input + " — " + e.getMessage()));
        }
    }
}
