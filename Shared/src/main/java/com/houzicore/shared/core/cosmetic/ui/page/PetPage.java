package com.houzicore.shared.core.cosmetic.ui.page;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.common.CurrencyType;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.core.cosmetic.CosmeticManager;
import com.houzicore.shared.core.cosmetic.CosmeticProgression;
import com.houzicore.shared.core.cosmetic.ui.CosmeticShop;
import com.houzicore.shared.core.cosmetic.ui.GuiUtil;
import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.core.gadget.CosmeticRarity;
import com.houzicore.shared.core.pet.PetClient;
import com.houzicore.shared.core.shop.item.IButton;
import com.houzicore.shared.core.shop.item.ShopItem;
import com.houzicore.shared.core.shop.page.ShopPageBase;

public class PetPage extends ShopPageBase<CosmeticManager, CosmeticShop> {

    private static final PetInfo[] AVAILABLE_PETS = {
        new PetInfo(EntityType.WOLF, "Wolf", Material.BONE),
        new PetInfo(EntityType.CAT, "Cat", Material.COD),
        new PetInfo(EntityType.CHICKEN, "Chicken", Material.WHEAT_SEEDS),
        new PetInfo(EntityType.PIG, "Pig", Material.CARROT),
        new PetInfo(EntityType.COW, "Cow", Material.WHEAT),
        new PetInfo(EntityType.SHEEP, "Sheep", Material.WHITE_WOOL),
        new PetInfo(EntityType.VILLAGER, "Villager", Material.EMERALD),
        new PetInfo(EntityType.WITHER, "Widder", Material.WITHER_SKELETON_SKULL),
        new PetInfo(EntityType.FOX, "Fox", Material.SWEET_BERRIES),
        new PetInfo(EntityType.FROG, "Frog", Material.SLIME_BALL),
        new PetInfo(EntityType.AXOLOTL, "Axolotl", Material.TROPICAL_FISH),
        new PetInfo(EntityType.ALLAY, "Allay", Material.AMETHYST_SHARD),
        new PetInfo(EntityType.SNIFFER, "Sniffer", Material.TORCHFLOWER_SEEDS),
        new PetInfo(EntityType.CAMEL, "Camel", Material.CACTUS),
        new PetInfo(EntityType.PARROT, "Parrot", Material.COOKIE),
        new PetInfo(EntityType.PANDA, "Panda", Material.BAMBOO),
        new PetInfo(EntityType.ARMADILLO, "Armadillo", Material.BRUSH),
    };

    public PetPage(CosmeticManager plugin, CosmeticShop shop, CoreClientManager clientManager,
            DonationManager donationManager, String name, Player player) {
        super(plugin, shop, clientManager, donationManager, name, player);
        buildPage();
    }

    @Override
    protected void buildPage() {
        GuiUtil.fillBorders(getInventory());

        PetClient petClient = getPlugin().getPetManager().Get(getPlayer());
        EntityType activeType = null;
        if (getPlugin().getPetManager().hasActivePet(getPlayer().getName())) {
            activeType = getPlugin().getPetManager().getActivePet(getPlayer().getName()).getType();
        }

        int[][] rows = {
            {10, 11, 12, 13, 14, 15, 16},
            {19, 20, 21, 22, 23, 24, 25},
            {28, 29, 30, 31, 32, 33, 34},
            {37, 38, 39, 40, 41, 42, 43}
        };
        int maxPerRow = 7;

        List<PetInfo> pets = new ArrayList<>(Arrays.asList(AVAILABLE_PETS));
        pets.sort(Comparator
                .comparingInt((PetInfo pet) -> CosmeticProgression.getTierOrder(CosmeticProgression.getPetRarity(pet.name)))
                .thenComparingInt(pet -> CosmeticProgression.getPrice(CosmeticProgression.getPetRarity(pet.name)))
                .thenComparing(pet -> pet.name, String.CASE_INSENSITIVE_ORDER));

        int petCount = Math.min(pets.size(), maxPerRow * rows.length);
        int[] slotPositions = new int[petCount];
        int placed = 0;
        for (int r = 0; r < rows.length && placed < petCount; r++) {
            int remaining = petCount - placed;
            int rowsLeft = rows.length - r;
            int inThisRow = Math.min(maxPerRow, (remaining + rowsLeft - 1) / rowsLeft);
            if (inThisRow > remaining) {
                inThisRow = remaining;
            }

            int startOffset = (maxPerRow - inThisRow) / 2;
            for (int i = 0; i < inThisRow; i++) {
                slotPositions[placed++] = rows[r][startOffset + i];
            }
        }

        for (int idx = 0; idx < petCount; idx++) {
            int slot = slotPositions[idx];
            final PetInfo pet = pets.get(idx);
            final CosmeticRarity rarity = CosmeticProgression.getPetRarity(pet.name);
            final int cost = CosmeticProgression.getPrice(rarity);

            boolean owned = getDonationManager().Get(getPlayer().getName()).OwnsUnknownPackage(pet.name)
                    || (petClient != null && petClient.GetPets().containsKey(pet.entityType));
            boolean active = activeType == pet.entityType;

            String petName = petClient != null && petClient.GetPets().containsKey(pet.entityType)
                    && petClient.GetPets().get(pet.entityType) != null
                    && petClient.GetPets().get(pet.entityType).length() > 0
                    ? petClient.GetPets().get(pet.entityType) : pet.name;

            List<String> lore = new ArrayList<>();
            if (owned) {
                if (active) {
                    lore.add(C.cGreen + C.Bold + "▶ " + com.houzicore.shared.core.lang.LangManager.get().get(getPlayer(), "cosmetic.active"));
                } else {
                    lore.add(C.cGreen + "✔ " + com.houzicore.shared.core.lang.LangManager.get().get(getPlayer(), "cosmetic.unlocked"));
                }
            } else {
                lore.add(C.cRed + "✖ " + com.houzicore.shared.core.lang.LangManager.get().get(getPlayer(), "cosmetic.locked"));
            }

            lore.add(" ");
            lore.add(rarity.getDisplayName());
            lore.add(" ");
            lore.add(C.cGray + "Summon a " + petName + " pet");
            lore.add(C.cGray + "to follow you around.");
            lore.add(" ");

            if (owned) {
                lore.add(C.cDGray + com.houzicore.shared.core.lang.LangManager.get().get(getPlayer(), "cosmetic.cost")
                        + cost
                        + (com.houzicore.shared.core.lang.LangManager.get().isThai(getPlayer()) ? " เอสเซนส์" : " Essence"));
            } else {
                boolean canAfford = getDonationManager().Get(getPlayer().getName()).GetBalance(CurrencyType.Essence) >= cost;
                lore.add(C.cDGray + com.houzicore.shared.core.lang.LangManager.get().get(getPlayer(), "cosmetic.cost")
                        + (canAfford ? C.cGray : C.cDGray) + cost
                        + (com.houzicore.shared.core.lang.LangManager.get().isThai(getPlayer()) ? " เอสเซนส์" : " Essence"));
            }

            lore.add(" ");
            if (owned) {
                if (active) {
                    lore.add(C.cGray + com.houzicore.shared.core.lang.LangManager.get().get(getPlayer(), "cosmetic.put_away_pet"));
                } else {
                    lore.add(C.cYellow + com.houzicore.shared.core.lang.LangManager.get().get(getPlayer(), "cosmetic.summon_pet"));
                }
            } else if (getDonationManager().Get(getPlayer().getName()).GetBalance(CurrencyType.Essence) >= cost) {
                lore.add(C.cYellow + com.houzicore.shared.core.lang.LangManager.get().get(getPlayer(), "cosmetic.purchase"));
            } else {
                lore.add(C.cDGray + (com.houzicore.shared.core.lang.LangManager.get().isThai(getPlayer()) ? "เอสเซนส์ไม่พอ" : "Not enough Essence"));
            }

            final EntityType petType = pet.entityType;
            final boolean isActive = active;

            String title;
            if (owned) {
                title = active
                        ? (C.cGreen + C.Bold + petName)
                        : (rarity.getColor() + "" + org.bukkit.ChatColor.BOLD + petName);
            } else {
                title = C.cRed + petName;
            }

            ShopItem item = new ShopItem(pet.displayMaterial, (byte) 0, title, lore.toArray(new String[0]), 1, !owned, false);

            if (owned) {
                addButton(slot, item, new IButton() {
                    @Override
                    public void onClick(Player player, ClickType clickType) {
                        if (isActive) {
                            getPlugin().getPetManager().RemovePet(player, true);
                        } else {
                            getPlugin().getPetManager().addPetOwnerToQueue(player.getName(), petType);
                        }
                        refresh();
                    }
                });
                if (active) {
                    addGlow(slot);
                }
            } else if (getDonationManager().Get(getPlayer().getName()).GetBalance(CurrencyType.Essence) >= cost) {
                addButton(slot, item, new IButton() {
                    @Override
                    public void onClick(Player player, ClickType clickType) {
                        getShop().openPageForPlayer(getPlayer(), new com.houzicore.shared.core.shop.page.ConfirmationPage<>(
                                getPlugin(), getShop(), getClientManager(), getDonationManager(), new Runnable() {
                                    @Override
                                    public void run() {
                                        getDonationManager().PurchaseUnknownSalesPackage(
                                                new com.houzicore.shared.common.util.Callback<com.houzicore.shared.server.util.TransactionResponse>() {
                                                    @Override
                                                    public void run(com.houzicore.shared.server.util.TransactionResponse response) {
                                                        refresh();
                                                    }
                                                },
                                                getPlayer().getName(),
                                                getClientManager().Get(getPlayer()).getAccountId(),
                                                pet.name,
                                                true,
                                                cost,
                                                true);
                                    }
                                },
                                PetPage.this,
                                new com.houzicore.shared.core.shop.item.SalesPackageBase(pet.name, pet.displayMaterial, (byte) 0, new String[] {}, cost) {
                                    @Override
                                    public void Sold(Player p, CurrencyType type) {
                                    }
                                },
                                CurrencyType.Essence,
                                getPlayer()));
                    }
                });
            } else {
                setItem(slot, item);
            }
        }

        addButton(4, new ShopItem(Material.RED_BED, C.cGray + " \u21FD " + com.houzicore.shared.core.lang.LangManager.get().get(getPlayer(), "cosmetic.go_back"), new String[] {}, 1, false), new IButton() {
            @Override
            public void onClick(Player player, ClickType clickType) {
                getShop().openPageForPlayer(getPlayer(),
                        new Menu(getPlugin(), getShop(), getClientManager(), getDonationManager(), player));
            }
        });
    }

    private static class PetInfo {
        final EntityType entityType;
        final String name;
        final Material displayMaterial;

        PetInfo(EntityType entityType, String name, Material displayMaterial) {
            this.entityType = entityType;
            this.name = name;
            this.displayMaterial = displayMaterial;
        }
    }
}
