package com.houzicore.lobby.hub.ui.radio;

import com.houzicore.lobby.hub.HubManager;
import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.core.itemstack.ItemStackFactory;
import com.houzicore.shared.core.music.RadioManager;
import com.houzicore.shared.core.music.NBSParser.Song;
import com.houzicore.shared.core.shop.page.ShopPageBase;
import com.houzicore.shared.core.lang.LangManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class RadioPage extends ShopPageBase<HubManager, RadioShop> {
    private final RadioManager _radioManager;

    public RadioPage(HubManager plugin, RadioShop shop, CoreClientManager clientManager,
                      DonationManager donationManager, Player player, RadioManager radioManager) {
        super(plugin, shop, clientManager, donationManager, "Music Radio", player, 54);
        _radioManager = radioManager;
        buildPage();
    }

    @Override
    protected void buildPage() {
        boolean isThai = LangManager.get().isThai(getPlayer());
        
        // 1. Blue glass border
        ItemStack glass = ItemStackFactory.Instance.CreateStack(
            Material.CYAN_STAINED_GLASS_PANE, (byte) 0, 1,
            isThai ? "§bเครื่องเล่นเพลง" : "§bMusic Player"
        );
        for (int i = 0; i < getSize(); i++) {
            getInventory().setItem(i, glass);
        }

        // Available slots inside border: 10-16, 19-25, 28-34, 37-43 (28 slots)
        int[] slots = new int[] {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
        };

        List<Song> playlist = _radioManager.getPlaylist();
        for (int i = 0; i < playlist.size() && i < slots.length; i++) {
            Song song = playlist.get(i);
            boolean isPlaying = (_radioManager.getActiveSong() == song);
            
            Material mat = isPlaying ? Material.MUSIC_DISC_PIGSTEP : Material.MUSIC_DISC_13;
            String name = (isPlaying ? "§a§l" : "§e") + song.name;
            String[] lore = new String[] {
                "§8───────────",
                "§7" + (isThai ? "ผู้แต่ง: " : "Author: ") + (song.author.isEmpty() ? (isThai ? "ไม่ทราบ" : "Unknown") : song.author),
                "",
                isPlaying ? (isThai ? "§aกำลังเล่นอยู่!" : "§aCurrently Playing!") : (isThai ? "§fคลิกเพื่อเล่นเพลงนี้!" : "§fClick to Play!")
            };

            final int index = i;
            addButton(slots[i], ItemStackFactory.Instance.CreateStack(mat, (byte)0, 1, name, lore),
                (player, clickType) -> {
                    playAcceptSound(player);
                    _radioManager.playSongIndex(index);
                    refresh(); // Refreshes the GUI to show the new active song
                }
            );
        }

        // Control buttons
        addButton(48, ItemStackFactory.Instance.CreateStack(Material.NOTE_BLOCK, (byte)0, 1, isThai ? "§c§lหยุดเพลง" : "§c§lStop Music", new String[]{isThai ? "§7คลิกเพื่อหยุดเพลงในวิทยุ" : "§7Click to stop the radio."}),
            (player, clickType) -> {
                playAcceptSound(player);
                _radioManager.stop();
                refresh();
            }
        );

        addButton(49, ItemStackFactory.Instance.CreateStack(Material.BARRIER, (byte)0, 1, isThai ? "§c§lปิดเมนู" : "§c§lClose", new String[]{isThai ? "§7ปิดเมนูนี้" : "§7Close the menu."}),
            (player, clickType) -> {
                player.closeInventory();
            }
        );
        
        String shuffleState = _radioManager.isShuffle() ? (isThai ? "§aเปิด" : "§aON") : (isThai ? "§cปิด" : "§cOFF");
        addButton(50, ItemStackFactory.Instance.CreateStack(Material.REDSTONE_TORCH, (byte)0, 1, (isThai ? "§e§lสุ่มเพลง: " : "§e§lShuffle: ") + shuffleState, new String[] { isThai ? "§7คลิกเพื่อเปิด/ปิดระบบสุ่มเพลง" : "§7Click to toggle shuffle mode." }),
            (player, clickType) -> {
                playAcceptSound(player);
                _radioManager.toggleShuffle();
                refresh();
            }
        );
    }
}
