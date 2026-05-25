package com.houzicore.kittester;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class TestKitCommand implements CommandExecutor {

    private final KitTester plugin;

    public TestKitCommand(KitTester plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (!player.hasPermission("houzicore.kittester")) {
            player.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }

        // Example: Give a test item to trigger an ability
        ItemStack testItem = new ItemStack(Material.BLAZE_ROD);
        ItemMeta meta = testItem.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GOLD + "Skill Tester Wand");
            testItem.setItemMeta(meta);
        }
        
        player.getInventory().addItem(testItem);
        player.sendMessage(ChatColor.GREEN + "You have received the Skill Tester Wand! Use this to test your abilities.");

        // TODO: Register or trigger your test Perk/Kit logic here

        return true;
    }
}
