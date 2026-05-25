package com.houzicore.shared.core.preferences.ui;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.common.util.GuiUtil;
import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.core.lang.LangManager;
import com.houzicore.shared.core.preferences.PreferencesManager;
import com.houzicore.shared.core.preferences.UserPreferences;
import com.houzicore.shared.core.shop.item.IButton;
import com.houzicore.shared.core.shop.item.ShopItem;
import com.houzicore.shared.core.shop.page.ShopPageBase;

/**
 * Preferences GUI — reworked clean layout
 *
 * 54-slot (6 rows):
 *   Row 0  (0–8)   — black glass border (top)
 *   Row 1  (9–17)  — title pane (slot 13), close button (slot 16)
 *   Row 2  (18–26) — "GENERAL" section row (filled with blue panes, header at 22)
 *   Row 3  (27–35) — General pref icons at slots 27–31
 *   Row 4  (36–44) — Indicator glasses at 36–40 (below each general pref)
 *   Row 5  (45–53) — Staff prefs (conditional) — black glass border (bottom)
 */
public class PreferencesPage extends ShopPageBase<PreferencesManager, PreferencesShop> {

    // ── Dirty flags ────────────────────────────────────────────────────────────
    private boolean _hubGamesToggled;
    private boolean _hubPlayersToggled;
    private boolean _hubChatToggled;
    private boolean _hubPrivateChatToggled;
    private boolean _hubPartyRequestsToggled;
    private boolean _pendingFriendRequestsToggled;
    private boolean _hubInvisibilityToggled;
    private boolean _hubForcefieldToggled;
    private boolean _macReportsToggled;
    private boolean _hubIgnoreVelocityToggled;
    private boolean _languageToggled;
    private boolean _radioToggled;

    // ── Buttons ────────────────────────────────────────────────────────────────
    private IButton _toggleHubGames;
    private IButton _toggleHubPlayers;
    private IButton _toggleChat;
    private IButton _togglePrivateChat;
    private IButton _toggleHubPartyRequests;
    private IButton _togglePendingFriendRequests;
    private IButton _toggleHubInvisibility;
    private IButton _toggleHubForcefield;
    private IButton _toggleHubIgnoreVelocity;
    private IButton _toggleMacReports;
    private IButton _toggleLanguage;
    private IButton _toggleRadio;

    private static final IButton NO_OP = new IButton() {
        @Override public void onClick(Player player, ClickType clickType) {}
    };

    public PreferencesPage(PreferencesManager plugin, PreferencesShop shop, CoreClientManager clientManager,
            DonationManager donationManager, String name, Player player) {
        super(plugin, shop, clientManager, donationManager, name, player, 54);
        createButtons();
        buildPage();
    }

    // ── Layout ─────────────────────────────────────────────────────────────────

    @Override
    protected void buildPage() {
        clear();
        GuiUtil.fillBorders(getInventory());

        final UserPreferences prefs = getPlugin().Get(getPlayer());
        final LangManager lm = LangManager.get();
        final Player p = getPlayer();

        final String on     = lm.get(p, "prefs.status.enabled");
        final String off    = lm.get(p, "prefs.status.disabled");
        final String toOn   = lm.get(p, "prefs.click.enable");
        final String toOff  = lm.get(p, "prefs.click.disable");

        // ── Row 1 — Title label (slot 13) and Close button (slot 16) ─────────
        addButton(13, new ShopItem(Material.ENCHANTING_TABLE, (byte) 0,
                ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + lm.get(p, "prefs.title"),
                new String[]{ChatColor.GRAY + lm.get(p, "prefs.subtitle_1")}, 1, false, false), NO_OP);

        addButton(16, new ShopItem(Material.BARRIER, ChatColor.RED + "" + ChatColor.BOLD
                + lm.get(p, "prefs.close"), new String[]{ChatColor.GRAY + lm.get(p, "prefs.close_desc")}, 1, false),
                new IButton() {
                    @Override public void onClick(Player player, ClickType clickType) { player.closeInventory(); }
                });

        // ── Row 2 — "GENERAL" section header (blue panes fill, label at 22) ──
        GuiUtil.fillRow(getInventory(), 18, Material.BLUE_STAINED_GLASS_PANE, " ");
        addButton(22, new ShopItem(Material.BLUE_STAINED_GLASS_PANE, (byte) 0,
                ChatColor.AQUA + "" + ChatColor.BOLD + lm.get(p, "prefs.section.general"),
                new String[]{}, 1, false, false), NO_OP);

        // ── Row 3 — General pref icons (slots 27–31) ─────────────────────────
        // Language
        boolean isEng = "ENG".equalsIgnoreCase(prefs.Language);
        String displayLang = isEng ? "English" : "Thai";
        String nextLang    = isEng ? "Thai" : "English";
        addButton(27, new ShopItem(Material.BOOK, (byte) 0,
                ChatColor.AQUA + lm.get(p, "prefs.language.name"),
                new String[]{
                        ChatColor.YELLOW + lm.get(p, "prefs.language.desc.1").replace("{0}", ChatColor.WHITE + displayLang),
                        "",
                        ChatColor.GREEN + lm.get(p, "prefs.language.desc.2").replace("{0}", nextLang)
                }, 1, false, false), _toggleLanguage);

        // Visibility
        addPrefButton(28, Material.ENDER_EYE, lm.get(p, "prefs.visibility.name"),
                prefs.ShowPlayers, _toggleHubPlayers, on, off, toOn, toOff);

        // Chat
        addPrefButton(29, Material.PAPER, lm.get(p, "prefs.chat.name"),
                prefs.ShowChat, _toggleChat, on, off, toOn, toOff);

        // Party / Friend requests
        addPrefButton(30, Material.PLAYER_HEAD, lm.get(p, "prefs.party.name"),
                prefs.PartyRequests, _toggleHubPartyRequests, on, off, toOn, toOff);

        // Stacker / Hub games
        addPrefButton(31, Material.FIRE_CHARGE, lm.get(p, "prefs.stacker.name"),
                prefs.HubGames, _toggleHubGames, on, off, toOn, toOff);

        // Radio
        addPrefButton(32, Material.JUKEBOX, lm.get(p, "prefs.radio.name"),
                prefs.PlayRadio, _toggleRadio, on, off, toOn, toOff);

        // ── Row 4 — Indicator glasses (slots 36–40) ───────────────────────────
        addIndicator(36, isEng,              lm.get(p, "prefs.language.name"));
        addIndicator(37, prefs.ShowPlayers,  lm.get(p, "prefs.visibility.name"));
        addIndicator(38, prefs.ShowChat,     lm.get(p, "prefs.chat.name"));
        addIndicator(39, prefs.PartyRequests,lm.get(p, "prefs.party.name"));
        addIndicator(40, prefs.HubGames,     lm.get(p, "prefs.stacker.name"));
        addIndicator(41, prefs.PlayRadio,    lm.get(p, "prefs.radio.name"));

        // ── Row 5 — Staff prefs (conditional) ────────────────────────────────
        Rank rank     = getClientManager().Get(p).GetRank();
        boolean isAdmin = rank.Has(Rank.ADMIN) || rank == Rank.JNR_DEV;
        boolean isMod   = rank.Has(Rank.MODERATOR) && !isAdmin;
        boolean isMedia = rank == Rank.YOUTUBE || rank == Rank.TWITCH;

        if (isAdmin || isMod || isMedia) {
            GuiUtil.fillRow(getInventory(), 45, Material.RED_STAINED_GLASS_PANE, " ");
            addButton(45, new ShopItem(Material.RED_STAINED_GLASS_PANE, (byte) 0,
                    ChatColor.RED + "" + ChatColor.BOLD + lm.get(p, "prefs.section.staff"),
                    new String[]{}, 1, false, false), NO_OP);

            int col = 46;
            if (isAdmin || isMedia) {
                addPrefButton(col, Material.NETHER_STAR, "Hub Invisibility",
                        prefs.Invisibility, _toggleHubInvisibility, on, off, toOn, toOff);
                col++;
                addPrefButton(col, Material.SLIME_BALL, "Hub Forcefield",
                        prefs.HubForcefield, _toggleHubForcefield, on, off, toOn, toOff);
                col++;
            }
            if (isAdmin || isMod || isMedia) {
                addPrefButton(col, Material.SADDLE, "Hub Ignore Velocity",
                        prefs.IgnoreVelocity, _toggleHubIgnoreVelocity, on, off, toOn, toOff);
                col++;
            }
            if (isAdmin || isMod) {
                addPrefButton(col, Material.WRITABLE_BOOK, "Mac Reports",
                        prefs.ShowMacReports, _toggleMacReports, on, off, toOn, toOff);
            }
        }
    }

    // ── Private helpers ────────────────────────────────────────────────────────

    private void addPrefButton(int slot, Material mat, String name, boolean state, IButton btn,
                               String on, String off, String toOn, String toOff) {
        String color   = state ? ChatColor.GREEN.toString() : ChatColor.RED.toString();
        String status  = state ? on : off;
        String hint    = state ? toOff : toOn;
        addButton(slot, new ShopItem(mat, (byte) 0, color + name,
                new String[]{color + status, "", ChatColor.GRAY + hint}, 1, false, false), btn);
    }

    private void addIndicator(int slot, boolean state, String label) {
        Material mat = state ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE;
        String color = state ? ChatColor.GREEN.toString() : ChatColor.RED.toString();
        addButton(slot, new ShopItem(mat, (byte) 0, color + label, new String[]{}, 1, false, false), NO_OP);
    }

    // ── Button wiring ──────────────────────────────────────────────────────────

    private void createButtons() {
        _toggleHubGames = new IButton() {
            @Override public void onClick(Player player, ClickType ct) { toggleHubGames(player); }
        };
        _toggleHubPlayers = new IButton() {
            @Override public void onClick(Player player, ClickType ct) { toggleHubPlayers(player); }
        };
        _toggleChat = new IButton() {
            @Override public void onClick(Player player, ClickType ct) { toggleChat(player); }
        };
        _togglePrivateChat = new IButton() {
            @Override public void onClick(Player player, ClickType ct) { togglePrivateChat(player); }
        };
        _toggleHubPartyRequests = new IButton() {
            @Override public void onClick(Player player, ClickType ct) { toggleHubPartyRequests(player); }
        };
        _togglePendingFriendRequests = new IButton() {
            @Override public void onClick(Player player, ClickType ct) { togglePendingFriendRequests(player); }
        };
        _toggleHubInvisibility = new IButton() {
            @Override public void onClick(Player player, ClickType ct) { toggleHubInvisibility(player); }
        };
        _toggleHubForcefield = new IButton() {
            @Override public void onClick(Player player, ClickType ct) { toggleHubForcefield(player); }
        };
        _toggleMacReports = new IButton() {
            @Override public void onClick(Player player, ClickType ct) { toggleMacReports(player); }
        };
        _toggleHubIgnoreVelocity = new IButton() {
            @Override public void onClick(Player player, ClickType ct) { toggleHubIgnoreVelocity(player); }
        };
        _toggleLanguage = new IButton() {
            @Override public void onClick(Player player, ClickType ct) { toggleLanguage(player); }
        };
        _toggleRadio = new IButton() {
            @Override public void onClick(Player player, ClickType ct) { toggleRadio(player); }
        };
    }

    // ── Lifecycle ──────────────────────────────────────────────────────────────

    @Override
    public void playerClosed() {
        super.playerClosed();
        if (preferencesChanged()) {
            getPlugin().savePreferences(getPlayer());
        }
    }

    public boolean preferencesChanged() {
        return _hubGamesToggled || _hubPlayersToggled || _hubChatToggled || _hubPrivateChatToggled
                || _hubPartyRequestsToggled || _hubInvisibilityToggled || _hubForcefieldToggled
                || _pendingFriendRequestsToggled || _languageToggled || _radioToggled;
    }

    // ── Toggles ────────────────────────────────────────────────────────────────

    protected void toggleChat(Player player) {
        playAcceptSound(player);
        boolean ns = !getPlugin().Get(player).ShowChat;
        getPlugin().Get(player).ShowChat = ns;
        getPlugin().Get(player).PrivateMessaging = ns;
        _hubChatToggled = !_hubChatToggled;
        buildPage();
    }

    protected void toggleHubForcefield(Player player) {
        playAcceptSound(player);
        getPlugin().Get(player).HubForcefield = !getPlugin().Get(player).HubForcefield;
        _hubForcefieldToggled = !_hubForcefieldToggled;
        buildPage();
    }

    protected void toggleHubGames(Player player) {
        playAcceptSound(player);
        getPlugin().Get(player).HubGames = !getPlugin().Get(player).HubGames;
        _hubGamesToggled = !_hubGamesToggled;
        buildPage();
    }

    protected void toggleHubIgnoreVelocity(Player player) {
        playAcceptSound(player);
        getPlugin().Get(player).IgnoreVelocity = !getPlugin().Get(player).IgnoreVelocity;
        _hubIgnoreVelocityToggled = !_hubIgnoreVelocityToggled;
        buildPage();
    }

    protected void toggleHubInvisibility(Player player) {
        playAcceptSound(player);
        getPlugin().Get(player).Invisibility = !getPlugin().Get(player).Invisibility;
        _hubInvisibilityToggled = !_hubInvisibilityToggled;
        buildPage();
    }

    protected void toggleHubPartyRequests(Player player) {
        playAcceptSound(player);
        boolean ns = !getPlugin().Get(player).PartyRequests;
        getPlugin().Get(player).PartyRequests = ns;
        getPlugin().Get(player).PendingFriendRequests = ns;
        _hubPartyRequestsToggled = !_hubPartyRequestsToggled;
        buildPage();
    }

    protected void toggleHubPlayers(Player player) {
        playAcceptSound(player);
        getPlugin().Get(player).ShowPlayers = !getPlugin().Get(player).ShowPlayers;
        _hubPlayersToggled = !_hubPlayersToggled;
        buildPage();
    }

    protected void toggleMacReports(Player player) {
        playAcceptSound(player);
        getPlugin().Get(player).ShowMacReports = !getPlugin().Get(player).ShowMacReports;
        _macReportsToggled = !_macReportsToggled;
        buildPage();
    }

    protected void togglePendingFriendRequests(Player player) {
        playAcceptSound(player);
        getPlugin().Get(player).PendingFriendRequests = !getPlugin().Get(player).PendingFriendRequests;
        _pendingFriendRequestsToggled = !_pendingFriendRequestsToggled;
        buildPage();
    }

    protected void togglePrivateChat(Player player) {
        playAcceptSound(player);
        getPlugin().Get(player).PrivateMessaging = !getPlugin().Get(player).PrivateMessaging;
        _hubPrivateChatToggled = !_hubPrivateChatToggled;
        buildPage();
    }

    protected void toggleLanguage(Player player) {
        playAcceptSound(player);
        UserPreferences p = getPlugin().Get(player);
        if (p.Language == null) p.Language = "THA";
        p.Language = p.Language.equalsIgnoreCase("ENG") ? "THA" : "ENG";
        _languageToggled = !_languageToggled;
        buildPage();
    }

    protected void toggleRadio(Player player) {
        playAcceptSound(player);
        getPlugin().Get(player).PlayRadio = !getPlugin().Get(player).PlayRadio;
        _radioToggled = !_radioToggled;
        buildPage();
    }
}
