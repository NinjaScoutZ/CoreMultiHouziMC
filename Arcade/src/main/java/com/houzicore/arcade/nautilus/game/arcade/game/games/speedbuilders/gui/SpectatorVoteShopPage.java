package com.houzicore.arcade.nautilus.game.arcade.game.games.speedbuilders.gui;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.core.itemstack.ItemStackFactory;
import com.houzicore.shared.core.shop.page.ShopPageBase;
import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.game.games.speedbuilders.SpeedBuilders;
import com.houzicore.arcade.nautilus.game.arcade.game.games.speedbuilders.lang.SpeedBuildersLang;

public class SpectatorVoteShopPage extends ShopPageBase<ArcadeManager, SpectatorVoteShop>
{
    private final SpeedBuilders _game;

    public SpectatorVoteShopPage(SpeedBuilders game, ArcadeManager plugin, SpectatorVoteShop shop, CoreClientManager clientManager,
                                 DonationManager donationManager, String title, Player player)
    {
        super(plugin, shop, clientManager, donationManager, title, player, 54);
        _game = game;
        buildPage();
    }

    @Override
    protected void buildPage()
    {
        // 1. Light Blue glass border (byte 3 is light blue stained glass pane)
        ItemStack glass = ItemStackFactory.Instance.CreateStack(
            Material.LIGHT_BLUE_STAINED_GLASS_PANE, (byte) 3, 1,
            "§bEssence: " + getDonationManager().Get(getPlayer()).GetEssence()
        );
        for (int i = 0; i < getSize(); i++)
        {
            getInventory().setItem(i, glass);
        }

        // 2. Add players as interactive buttons
        List<Player> aliveBuilders = new ArrayList<>(_game.getBuildRecreations().keySet());

        int[] slots = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
        };

        int index = 0;
        for (Player builder : aliveBuilders)
        {
            if (index >= slots.length) break;
            int slot = slots[index++];

            Integer v = _game.getVotesReceived().get(builder);
            int votes = (v != null) ? v : 0;

            // Build the head item
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            if (meta != null)
            {
                meta.setOwningPlayer(builder);
                
                boolean isSelected = builder.equals(_game.getSpectatorVotes().get(getPlayer()));
                String displayName = SpeedBuildersLang.get().get(getPlayer(), "speedbuilders.gui.vote.item_name", builder.getName());
                if (isSelected)
                {
                    displayName = displayName + " §a✔";
                }
                meta.setDisplayName(displayName);

                String[] loreTemplate = SpeedBuildersLang.get().list(getPlayer(), "speedbuilders.gui.vote.item_lore", new String[] {
                    "&7Click to vote for their creativity!",
                    "&8───────────",
                    "&fVotes: &b{0}"
                });

                List<String> lore = new ArrayList<>();
                for (String line : loreTemplate)
                {
                    lore.add(line.replace("{0}", String.valueOf(votes)));
                }

                if (isSelected)
                {
                    lore.add("");
                    lore.add(SpeedBuildersLang.get().get(getPlayer(), "speedbuilders.gui.vote.currently_selected"));
                }

                meta.setLore(lore);

                if (isSelected)
                {
                    meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
                    meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
                }

                head.setItemMeta(meta);
            }

            addButton(slot, head, (clicker, clickType) -> {
                // Prevent voting for self
                if (clicker.equals(builder))
                {
                    playDenySound(clicker);
                    return;
                }

                // Check if they already voted for this same player
                if (_game.getSpectatorVotes().containsKey(clicker))
                {
                    Player currentVote = _game.getSpectatorVotes().get(clicker);
                    if (currentVote.equals(builder))
                    {
                        clicker.sendMessage(SpeedBuildersLang.get().get(clicker, "speedbuilders.gui.vote.already_voted", builder.getName()));
                        playDenySound(clicker);
                        return;
                    }
                    // Subtract previous vote from the old target
                    Integer vOld = _game.getVotesReceived().get(currentVote);
                    int oldVal = (vOld != null) ? vOld : 0;
                    _game.getVotesReceived().put(currentVote, Math.max(0, oldVal - 1));
                }

                // Apply new vote
                Integer vCur = _game.getVotesReceived().get(builder);
                int curVal = (vCur != null) ? vCur : 0;
                _game.getVotesReceived().put(builder, curVal + 1);
                _game.getSpectatorVotes().put(clicker, builder);

                clicker.sendMessage(SpeedBuildersLang.get().get(clicker, "speedbuilders.gui.vote.success", builder.getName()));
                playAcceptSound(clicker);

                // Update spectator hotbar Nether Star glow and title
                _game.updateSpectatorHotbarItem(clicker);

                // Re-build/refresh page to reflect updated vote counts immediately
                buildPage();
            });
        }
    }
}
