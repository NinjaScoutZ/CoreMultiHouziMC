package com.houzicore.shared.core.cosmetic.ui.page;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Creature;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.common.CurrencyType;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.core.cosmetic.CosmeticManager;
import com.houzicore.shared.core.cosmetic.ui.AnimationType;
import com.houzicore.shared.core.cosmetic.ui.CosmeticShop;
import com.houzicore.shared.core.cosmetic.ui.GuiUtil;
import com.houzicore.shared.core.cosmetic.ui.button.OpenAuras;
import com.houzicore.shared.core.cosmetic.ui.button.OpenBanners;
import com.houzicore.shared.core.cosmetic.ui.button.OpenCostumes;
import com.houzicore.shared.core.cosmetic.ui.button.OpenGadgets;
import com.houzicore.shared.core.cosmetic.ui.button.OpenMorphs;
import com.houzicore.shared.core.cosmetic.ui.button.OpenMounts;
import com.houzicore.shared.core.cosmetic.ui.button.OpenMusic;
import com.houzicore.shared.core.cosmetic.ui.button.OpenParticles;
import com.houzicore.shared.core.cosmetic.ui.button.OpenPets;
import com.houzicore.shared.core.cosmetic.ui.button.OpenKillEffects;
import com.houzicore.shared.core.cosmetic.ui.button.OpenCollections;
import com.houzicore.shared.core.cosmetic.ui.button.OpenSprays;
import com.houzicore.shared.core.cosmetic.ui.button.OpenWinEffects;
import com.houzicore.shared.core.cosmetic.ui.button.OpenBaits;
import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.core.gadget.types.Gadget;
import com.houzicore.shared.core.gadget.types.GadgetType;
import com.houzicore.shared.core.mount.Mount;
import com.houzicore.shared.core.shop.item.IButton;
import com.houzicore.shared.core.shop.item.ShopItem;
import com.houzicore.shared.core.shop.page.ConfirmationPage;
import com.houzicore.shared.core.treasure.TreasureKey;

/**
 * Modernized Cosmetic Menu with animated category icons,
 * glass-pane borders, new categories (Sprays, Auras, Win Effects, Banners),
 * and active cosmetic preview row.
 *
 * Layout (54 slots, 6 rows):
 * Row 1: [border] [coins] [border] [⚡ COSMETICS ⚡] [border] [essence] [border]
 * Row 2: [border] [Particles] [Gadgets] [Morphs] [Mounts] [Pets] [Costumes] [Music] [border]
 * Row 3: [border] [Sprays] [Auras] [WinEffects] [Banners] [border x3] [border]
 * Row 4: [border] [─── Active Cosmetics ───] [border]
 * Row 5: [border] [active items preview row] [border]
 * Row 6: [border x4] [close] [border x4]
 */
public class Menu extends AnimatedMenuPage {

    public Menu(CosmeticManager plugin, CosmeticShop shop, CoreClientManager clientManager,
            DonationManager donationManager, Player player) {
        super(plugin, shop, clientManager, donationManager, com.houzicore.shared.core.lang.LangManager.get().get(player, "cosmetic.title"), player);

        buildPage();
        startAnimations();
    }

    public void attemptPurchaseKey(Player player) {
        getShop().openPageForPlayer(player, new ConfirmationPage<>(getPlugin(), getShop(), getClientManager(),
                getDonationManager(), new Runnable() {
                    @Override
                    public void run() {
                        getPlugin().getInventoryManager().addItemToInventory(getPlayer(), "Treasure", "Treasure Key",
                                1);
                        refresh();
                    }
                }, this, new TreasureKey(), CurrencyType.Essence, getPlayer()));
    }

    @Override
    protected void buildPage() {
        // ── Fill borders ──
        GuiUtil.fillBorders(getInventory());
        com.houzicore.shared.core.lang.LangManager lm = com.houzicore.shared.core.lang.LangManager.get();

        int coins = getDonationManager().Get(getPlayer().getName()).getCoins();
        int essence = getDonationManager().Get(getPlayer().getName()).GetBalance(CurrencyType.Essence);

        addItem(2, new ShopItem(Material.SUNFLOWER,
                ChatColor.GOLD + "" + ChatColor.BOLD + coins + " Coins",
                new String[] {
                    "",
                    ChatColor.GRAY + "Earned from playing games,",
                    ChatColor.GRAY + "completing quests, and events."
                }, 1, false));

        addItem(4, new ShopItem(Material.NETHER_STAR,
                ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "⚡ COSMETICS ⚡",
                new String[] {
                    "",
                    ChatColor.GRAY + "Choose your cosmetics",
                    ChatColor.GRAY + "and stand out from the crowd!"
                }, 1, false));

        addItem(6, new ShopItem(Material.DIAMOND,
                ChatColor.AQUA + "" + ChatColor.BOLD + essence + " Essence",
                new String[] {
                    "",
                    ChatColor.GRAY + "Purchase Essence from",
                    ChatColor.GRAY + "store." + com.houzicore.shared.core.common.BrandConfig.website(),
                    "",
                    ChatColor.AQUA + "Ultra Rank" + ChatColor.GRAY + " → 7,500/mo",
                    ChatColor.DARK_PURPLE + "Hero Rank" + ChatColor.GRAY + " → 15,000/mo",
                    ChatColor.GREEN + "Legend Rank" + ChatColor.GRAY + " → 30,000/mo",
                }, 1, false));

        // ── Row 2: Main Categories (slots 10-16) ──
        String tParticles = lm.get(getPlayer(), "cosmetic.particles", "Particle Effects");
        addButton(10, new ShopItem(Material.BLAZE_POWDER,
                ChatColor.AQUA + "" + ChatColor.BOLD + tParticles,
                new String[] { "", ChatColor.GRAY + "Flashy particle trails!" }, 1, false),
                new OpenParticles(this));
        registerAnimatedSlot(10, tParticles, AnimationType.RAINBOW_CYCLE, ChatColor.AQUA);

        String tGadgets = lm.get(getPlayer(), "cosmetic.gadgets", "Gadgets");
        addButton(11, new ShopItem(Material.BOW,
                ChatColor.GREEN + "" + ChatColor.BOLD + tGadgets,
                new String[] { "", ChatColor.GRAY + "Party bombs, footballs, duels and wild toys!" }, 1, false),
                new OpenGadgets(this));
        registerAnimatedSlot(11, tGadgets, AnimationType.RAINBOW_CYCLE, ChatColor.GREEN);

        String tMorphs = lm.get(getPlayer(), "cosmetic.morphs", "Morphs");
        addButton(12, new ShopItem(Material.LEATHER,
                ChatColor.YELLOW + "" + ChatColor.BOLD + tMorphs,
                new String[] { "", ChatColor.GRAY + "Transform into creatures!" }, 1, false),
                new OpenMorphs(this));
        registerAnimatedSlot(12, tMorphs, AnimationType.RAINBOW_CYCLE, ChatColor.YELLOW);

        String tMounts = lm.get(getPlayer(), "cosmetic.mounts", "Mounts");
        addButton(13, new ShopItem(Material.IRON_HORSE_ARMOR,
                ChatColor.GOLD + "" + ChatColor.BOLD + tMounts,
                new String[] { "", ChatColor.GRAY + "Ride in style!" }, 1, false),
                new OpenMounts(this));
        registerAnimatedSlot(13, tMounts, AnimationType.RAINBOW_CYCLE, ChatColor.GOLD);

        String tPets = lm.get(getPlayer(), "cosmetic.pets", "Pets");
        addButton(14, new ShopItem(Material.BONE,
                ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + tPets,
                new String[] { "", ChatColor.GRAY + "Adorable companions!" }, 1, false),
                new OpenPets(this));
        registerAnimatedSlot(14, tPets, AnimationType.RAINBOW_CYCLE, ChatColor.LIGHT_PURPLE);

        String tCostumes = lm.get(getPlayer(), "cosmetic.costumes", "Costumes");
        addButton(15, new ShopItem(Material.GOLDEN_CHESTPLATE,
                ChatColor.RED + "" + ChatColor.BOLD + tCostumes,
                new String[] { "", ChatColor.GRAY + "Dress to impress!" }, 1, false),
                new OpenCostumes(this));
        registerAnimatedSlot(15, tCostumes, AnimationType.RAINBOW_CYCLE, ChatColor.RED);

        String tMusic = lm.get(getPlayer(), "cosmetic.music", "Music");
        addButton(16, new ShopItem(Material.MUSIC_DISC_CAT,
                ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + tMusic,
                new String[] { "", ChatColor.GRAY + "Play your favorite tunes!" }, 1, false),
                new OpenMusic(this));
        registerAnimatedSlot(16, tMusic, AnimationType.RAINBOW_CYCLE, ChatColor.DARK_PURPLE);

        // ── Row 3: New Categories (slots 19-22) ──
        String tSprays = lm.get(getPlayer(), "cosmetic.sprays", "Sprays");
        addButton(19, new ShopItem(Material.PAINTING,
                ChatColor.AQUA + "" + ChatColor.BOLD + "✎ " + tSprays,
                new String[] { "", ChatColor.GRAY + "Spray art on surfaces!" }, 1, false),
                new OpenSprays(this));
        registerAnimatedSlot(19, tSprays, AnimationType.SHIMMER, ChatColor.AQUA);

        String tAuras = lm.get(getPlayer(), "cosmetic.auras", "Auras");
        addButton(20, new ShopItem(Material.END_CRYSTAL,
                ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "✨ " + tAuras,
                new String[] { "", ChatColor.GRAY + "A glowing aura around you!" }, 1, false),
                new OpenAuras(this));
        registerAnimatedSlot(20, tAuras, AnimationType.SHIMMER, ChatColor.LIGHT_PURPLE);

        String tWinEffects = lm.get(getPlayer(), "cosmetic.wineffects", "Win Effects");
        addButton(21, new ShopItem(Material.FIREWORK_ROCKET,
                ChatColor.GOLD + "" + ChatColor.BOLD + "🏆 " + tWinEffects,
                new String[] { "", ChatColor.GRAY + "Celebrate your victories!" }, 1, false),
                new OpenWinEffects(this));
        registerAnimatedSlot(21, tWinEffects, AnimationType.SHIMMER, ChatColor.GOLD);

        String tBanners = lm.get(getPlayer(), "cosmetic.banners", "Banners");
        addButton(22, new ShopItem(Material.ORANGE_BANNER,
                ChatColor.YELLOW + "" + ChatColor.BOLD + "⚑ " + tBanners,
                new String[] { "", ChatColor.GRAY + "Floating banners above you!" }, 1, false),
                new OpenBanners(this));
        registerAnimatedSlot(22, tBanners, AnimationType.SHIMMER, ChatColor.YELLOW);

        String tBaits = lm.get(getPlayer(), "cosmetic.baits", "Baits");
        addButton(23, new ShopItem(Material.TROPICAL_FISH,
                ChatColor.AQUA + "" + ChatColor.BOLD + "🎣 " + tBaits,
                new String[] { "", ChatColor.GRAY + "Get more bites!" }, 1, false),
                new OpenBaits(this));
        registerAnimatedSlot(23, tBaits, AnimationType.SHIMMER, ChatColor.AQUA);

        String tCollections = lm.get(getPlayer(), "cosmetic.collections", "Collections");
        addButton(24, new ShopItem(Material.DIAMOND,
                ChatColor.GREEN + "" + ChatColor.BOLD + "✦ " + tCollections,
                new String[] { "", ChatColor.GRAY + "View your cosmetic sets", ChatColor.GRAY + "and unlock bonuses!" }, 1, false),
                new OpenCollections(this));
        registerAnimatedSlot(24, tCollections, AnimationType.SHIMMER, ChatColor.GREEN);

        String tKillEffects = lm.get(getPlayer(), "cosmetic.killeffects", "Kill Effects");
        if (tKillEffects == null || tKillEffects.isEmpty()) tKillEffects = "Kill Effects";
        addButton(25, new ShopItem(Material.WITHER_ROSE,
                ChatColor.DARK_RED + "" + ChatColor.BOLD + "☠ " + tKillEffects,
                new String[] { "", ChatColor.GRAY + "Explosions upon your victims!" }, 1, false),
                new OpenKillEffects(this));
        registerAnimatedSlot(25, tKillEffects, AnimationType.SHIMMER, ChatColor.DARK_RED);

        // ── Row 4: Separator ──
        String tActive = lm.get(getPlayer(), "cosmetic.active_preview", "Active Cosmetics");
        for (int i = 28; i <= 34; i++) {
            setItem(i, GuiUtil.createCategoryIcon(Material.GRAY_STAINED_GLASS_PANE,
                    ChatColor.DARK_GRAY + "" + ChatColor.BOLD + "━━━ " + tActive + " ━━━",
                    new String[] {}, false));
        }

        // ── Row 5: Active Cosmetics Preview (slots 37-43) ──
        buildActivePreview();

        // ── Row 6: Close button ──
        addButton(49, new ShopItem(Material.BARRIER,
                ChatColor.RED + "" + ChatColor.BOLD + lm.get(getPlayer(), "prefs.close"),
                new String[] {}, 1, false), new IButton() {
            @Override
            public void onClick(Player player, ClickType clickType) {
                player.closeInventory();
            }
        });
    }

    private void buildActivePreview() {
        int slot = 37;

        // Active Particle
        if (getPlugin().getGadgetManager().getActive(getPlayer(), GadgetType.Particle) != null) {
            Gadget g = getPlugin().getGadgetManager().getActive(getPlayer(), GadgetType.Particle);
            addDeactivateButton(slot, g, GadgetType.Particle);
        }
        slot++;

        // Active Gadget
        if (getPlugin().getGadgetManager().getActive(getPlayer(), GadgetType.Item) != null) {
            Gadget g = getPlugin().getGadgetManager().getActive(getPlayer(), GadgetType.Item);
            addDeactivateButton(slot, g, GadgetType.Item);
        }
        slot++;

        // Active Morph
        if (getPlugin().getGadgetManager().getActive(getPlayer(), GadgetType.Morph) != null) {
            Gadget g = getPlugin().getGadgetManager().getActive(getPlayer(), GadgetType.Morph);
            addDeactivateButton(slot, g, GadgetType.Morph);
        }
        slot++;

        // Active Mount
        if (getPlugin().getMountManager().getActive(getPlayer()) != null) {
            Mount<?> mount = getPlugin().getMountManager().getActive(getPlayer());
            addButton(slot, new ShopItem(mount.GetDisplayMaterial(), mount.GetDisplayData(),
                    ChatColor.GREEN + "" + ChatColor.BOLD + "▶ " + mount.GetName(),
                    new String[] { "", ChatColor.GRAY + com.houzicore.shared.core.lang.LangManager.get().get(getPlayer(), "cosmetic.click_deactivate") }, 1, false, false),
                    new IButton() {
                        @Override
                        public void onClick(Player player, ClickType clickType) {
                            playAcceptSound(player);
                            mount.Disable(player);
                            refresh();
                        }
                    });
        }
        slot++;

        // Active Pet
        if (getPlugin().getPetManager().hasActivePet(getPlayer().getName())) {
            Creature activePet = getPlugin().getPetManager().getActivePet(getPlayer().getName());
            String petName = activePet.getType() == EntityType.WITHER ? "Widder"
                    : (activePet.getCustomName() != null ? activePet.getCustomName() : activePet.getType().name());
            addButton(slot, new ShopItem(Material.SHEEP_SPAWN_EGG, (byte) 0,
                    ChatColor.GREEN + "" + ChatColor.BOLD + "▶ " + petName,
                    new String[] { "", ChatColor.GRAY + com.houzicore.shared.core.lang.LangManager.get().get(getPlayer(), "cosmetic.click_deactivate") }, 1, false, false),
                    new IButton() {
                        @Override
                        public void onClick(Player player, ClickType clickType) {
                            playAcceptSound(player);
                            getPlugin().getPetManager().RemovePet(player, true);
                            refresh();
                        }
                    });
        }
        slot++;

        // Active Aura
        if (getPlugin().getGadgetManager().getActive(getPlayer(), GadgetType.Aura) != null) {
            Gadget g = getPlugin().getGadgetManager().getActive(getPlayer(), GadgetType.Aura);
            addDeactivateButton(slot, g, GadgetType.Aura);
        }
        slot++;

        // Active Spray
        if (getPlugin().getGadgetManager().getActive(getPlayer(), GadgetType.Spray) != null) {
            Gadget g = getPlugin().getGadgetManager().getActive(getPlayer(), GadgetType.Spray);
            addDeactivateButton(slot, g, GadgetType.Spray);
        }
		slot++;

        // Active Bait
        if (getPlugin().getGadgetManager().getActive(getPlayer(), GadgetType.Bait) != null) {
            Gadget g = getPlugin().getGadgetManager().getActive(getPlayer(), GadgetType.Bait);
            addDeactivateButton(slot, g, GadgetType.Bait);
        }
    }

    private void addDeactivateButton(int slot, Gadget gadget, GadgetType type) {
        addButton(slot, new ShopItem(gadget.GetDisplayMaterial(), gadget.GetDisplayData(),
                ChatColor.GREEN + "" + ChatColor.BOLD + "▶ " + gadget.GetName(),
                new String[] { "", ChatColor.GRAY + com.houzicore.shared.core.lang.LangManager.get().get(getPlayer(), "cosmetic.click_deactivate") }, 1, false, false),
                new IButton() {
                    @Override
                    public void onClick(Player player, ClickType clickType) {
                        playAcceptSound(player);
                        gadget.Disable(player);
                        refresh();
                    }
                });
        registerAnimatedSlot(slot, gadget.GetName(), AnimationType.SHIMMER, ChatColor.GREEN);
    }

    // ── Kept for backwards compat ──
    public void openCostumes(Player player) {
        getShop().openPageForPlayer(player,
                new CostumePage(getPlugin(), getShop(), getClientManager(), getDonationManager(), "Costumes", player));
    }

    public void openMusic(Player player) {
        getShop().openPageForPlayer(player,
                new MusicPage(getPlugin(), getShop(), getClientManager(), getDonationManager(), "Music", player));
    }
}
