package com.houzicore.shared.core.title;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.houzicore.shared.MiniPlugin;
import com.houzicore.shared.account.CoreClient;
import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.HouziColorParser;
import com.houzicore.shared.core.inventory.InventoryManager;
import com.houzicore.shared.core.level.LvlManager;
import com.houzicore.shared.core.preferences.PreferencesManager;
import com.houzicore.shared.core.preferences.UserPreferences;
import com.houzicore.shared.core.stats.event.StatChangeEvent;

public class TitleManager extends MiniPlugin {

    public static TitleManager Instance;

    private final CoreClientManager _clientManager;
    private final LvlManager _lvlManager;
    private final PreferencesManager _preferencesManager;
    private final TitleShop _shop;

    public TitleManager(JavaPlugin plugin, CoreClientManager clientManager, LvlManager lvlManager, PreferencesManager preferencesManager, com.houzicore.shared.core.donation.DonationManager donationManager) {
        super("Title Manager", plugin);
        Instance = this;

        _clientManager = clientManager;
        _lvlManager = lvlManager;
        _preferencesManager = preferencesManager;
        _shop = new TitleShop(this, clientManager, donationManager);
    }

    public void openShop(Player caller) {
        if (_shop != null) {
            _shop.attemptShopOpen(caller);
        }
    }

    public TitleType getEquippedTitle(Player player) {
        if (_preferencesManager == null) return null;
        UserPreferences prefs = _preferencesManager.Get(player);
        if (prefs == null || prefs.ActiveTitle == null || prefs.ActiveTitle.isEmpty()) {
            return null;
        }
        return TitleType.getByKey(prefs.ActiveTitle);
    }

    public String getFormattedTitle(Player player, String language) {
        TitleType title = getEquippedTitle(player);
        if (title == null) return "";
        
        String rawText = title.getIcon() + " " + title.getDisplayName(language);
        String format = "<GRADIENT:" + title.getGradient() + ">" + rawText + "</GRADIENT>";
        return HouziColorParser.parse(format);
    }

    public void equipTitle(Player player, TitleType title) {
        if (_preferencesManager == null || player == null) return;
        UserPreferences prefs = _preferencesManager.Get(player);
        if (prefs == null) return;

        if (title == null) {
            prefs.ActiveTitle = null;
        } else {
            if (!hasTitle(player, title)) {
                return;
            }
            prefs.ActiveTitle = title.getKey();
        }

        _preferencesManager.savePreferences(player);
    }

    public void unequipTitle(Player player) {
        equipTitle(player, null);
    }

    public boolean hasTitle(Player player, TitleType title) {
        if (player == null || title == null) return false;

        // 1. Check FREE unlock
        if (title.getUnlockType() == TitleType.UnlockType.FREE) {
            return true;
        }

        // 2. Check LEVEL unlock
        if (title.getUnlockType() == TitleType.UnlockType.LEVEL) {
            if (_lvlManager != null) {
                int level = _lvlManager.getLevel(player);
                if (level >= title.getRequiredLevel()) {
                    return true;
                }
            }
        }

        // 3. Check RANK unlock
        if (title.getUnlockType() == TitleType.UnlockType.RANK) {
            if (_clientManager != null) {
                CoreClient client = _clientManager.Get(player);
                if (client != null && client.GetRank() != null) {
                    // Check if player rank >= required rank (equal to or smaller compareTo value)
                    if (client.GetRank().compareTo(title.getRequiredRank()) <= 0) {
                        return true;
                    }
                }
            }
        }

        // 4. Check Inventory fallback
        if (InventoryManager.Instance != null) {
            com.houzicore.shared.core.inventory.ClientInventory inv = InventoryManager.Instance.Get(player);
            if (inv != null && inv.getItemCount(title.getKey()) > 0) {
                return true;
            }
        }

        return false;
    }

    public List<TitleType> getOwnedTitles(Player player) {
        List<TitleType> owned = new ArrayList<>();
        for (TitleType title : TitleType.values()) {
            if (hasTitle(player, title)) {
                owned.add(title);
            }
        }
        return owned;
    }

    public void checkAndUnlockTitles(Player player) {
        if (player == null || InventoryManager.Instance == null) return;

        for (TitleType title : TitleType.values()) {
            if (title.getUnlockType() == TitleType.UnlockType.FREE) {
                continue; // Free titles don't need inventory entry
            }

            // If player qualifies for the title but doesn't have it in inventory
            boolean qualifies = false;
            if (title.getUnlockType() == TitleType.UnlockType.LEVEL && _lvlManager != null) {
                qualifies = _lvlManager.getLevel(player) >= title.getRequiredLevel();
            } else if (title.getUnlockType() == TitleType.UnlockType.RANK && _clientManager != null) {
                CoreClient client = _clientManager.Get(player);
                if (client != null && client.GetRank() != null) {
                    qualifies = client.GetRank().compareTo(title.getRequiredRank()) <= 0;
                }
            }

            if (qualifies) {
                com.houzicore.shared.core.inventory.ClientInventory inv = InventoryManager.Instance.Get(player);
                if (inv != null && inv.getItemCount(title.getKey()) == 0) {
                    InventoryManager.Instance.addItemToInventory(player, "Title", title.getKey(), 1);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Delay checking to ensure inventory is loaded
        runSyncLater(() -> {
            if (event.getPlayer().isOnline()) {
                checkAndUnlockTitles(event.getPlayer());
            }
        }, 60L); // 3 seconds delay
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onXpStatChange(StatChangeEvent event) {
        if (!LvlManager.XP_STAT.equalsIgnoreCase(event.getStatName())) {
            return;
        }

        Player player = Bukkit.getPlayerExact(event.getPlayerName());
        if (player == null || !player.isOnline()) {
            return;
        }

        int oldLevel = LvlManager.levelFromXp(event.getValueBefore());
        int newLevel = LvlManager.levelFromXp(event.getValueAfter());

        if (newLevel > oldLevel) {
            // Player leveled up, check unlocks immediately
            checkAndUnlockTitles(player);
        }
    }
}
