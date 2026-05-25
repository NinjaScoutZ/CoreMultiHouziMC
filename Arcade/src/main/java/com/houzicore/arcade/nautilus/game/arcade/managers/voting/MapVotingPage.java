package com.houzicore.arcade.nautilus.game.arcade.managers.voting;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.core.shop.page.ShopPageBase;
import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.managers.MapVotingManager;

public class MapVotingPage extends ShopPageBase<ArcadeManager, MapVotingShop> {

    private final MapVotingManager _votingManager;

    public MapVotingPage(ArcadeManager plugin, MapVotingShop shop, CoreClientManager clientManager, DonationManager donationManager, String name, Player player, MapVotingManager votingManager) {
        super(plugin, shop, clientManager, donationManager, name, player, 54);
        _votingManager = votingManager;
        buildPage();
    }

    @Override
    protected void buildPage() {
        if (getInventory() == null) return;
        
        getInventory().clear();
        getButtonMap().clear();

        java.util.List<String> options = _votingManager.getMapOptions();
        
        for (int i = 0; i < Math.min(5, options.size()); i++) {
            final String mapName = options.get(i);
            int mapVotes = _votingManager.getVotesForMap(mapName);
            boolean voted = _votingManager.hasVotedFor(getPlayer(), mapName);
            
            int baseSlot = i * 9;
            
            // Action handler
            com.houzicore.shared.core.shop.item.IButton action = (clicker, clickType) -> {
                playAcceptSound(clicker);
                _votingManager.voteFor(clicker, mapName);
                buildPage();
            };

            // Col 0: Sign (Map Name)
            String author = com.houzicore.arcade.nautilus.game.arcade.managers.MapVotingManager.getMapAuthor(_votingManager.getGameType(), mapName);
            String authorLabel = com.houzicore.shared.core.lang.LangManager.get().get(getPlayer(), "arcade.map_built_by", "Built by: ");
            com.houzicore.shared.core.itemstack.ItemBuilder signBuilder = new com.houzicore.shared.core.itemstack.ItemBuilder(Material.OAK_SIGN)
                .setTitle((voted ? C.cGreen : C.cYellow) + mapName)
                .addLore(
                    "", 
                    C.cGray + "Votes: " + C.cGreen + mapVotes,
                    C.cGray + authorLabel + C.cAqua + author
                );
            
            addButton(baseSlot, signBuilder.build(), action);

            // Col 1: Iron Bars
            addButton(baseSlot + 1, new com.houzicore.shared.core.itemstack.ItemBuilder(Material.IRON_BARS).setTitle(" ").build(), action);

            // Col 2-6: Glass panes representing votes
            int glassesToGreen = Math.min(5, mapVotes);
            for (int g = 0; g < 5; g++) {
                Material glassMat = (g < glassesToGreen) ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE;
                String glassTitle = (g < glassesToGreen) ? C.cGreen + "Vote Cast" : C.cRed + "Empty Vote";
                addButton(baseSlot + 2 + g, new com.houzicore.shared.core.itemstack.ItemBuilder(glassMat).setTitle(glassTitle).build(), action);
            }

            // Col 8: Paper (Super Vote!)
            com.houzicore.shared.core.itemstack.ItemBuilder paperBuilder = new com.houzicore.shared.core.itemstack.ItemBuilder(Material.PAPER)
                .setTitle(C.cAqua + "Super Vote!")
                .addLore(
                    C.cGray + "You have " + C.cAqua + "0" + C.cGray + " Super Votes.",
                    "",
                    C.cGray + "Super votes can be purchased from",
                    C.cGray + "the shop in the lobby.",
                    "",
                    C.cYellow + "Click to Vote " + C.cAqua + mapName + C.cYellow + "!"
                );
            
            ItemStack paper = paperBuilder.build();
            if (voted) {
                ItemMeta meta = paper.getItemMeta();
                if (meta != null) {
                    meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
                    meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
                    paper.setItemMeta(meta);
                }
            }
            
            addButton(baseSlot + 8, paper, action);
        }
    }
}
