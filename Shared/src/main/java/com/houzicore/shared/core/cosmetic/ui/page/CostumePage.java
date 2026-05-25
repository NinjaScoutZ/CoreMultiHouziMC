package com.houzicore.shared.core.cosmetic.ui.page;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.core.cosmetic.CosmeticManager;
import com.houzicore.shared.core.cosmetic.ui.CosmeticShop;
import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.core.gadget.types.Gadget;
import com.houzicore.shared.core.gadget.types.GadgetType;
import com.houzicore.shared.core.gadget.types.ItemGadget;
import com.houzicore.shared.core.gadget.types.OutfitGadget;
import com.houzicore.shared.core.shop.item.IButton;
import com.houzicore.shared.core.shop.item.ShopItem;
import com.houzicore.shared.core.cosmetic.ui.GuiUtil;

/**
 * Created by shaun on 14-09-15.
 */
public class CostumePage extends GadgetPage {
	public CostumePage(CosmeticManager plugin, CosmeticShop shop, CoreClientManager clientManager,
			DonationManager donationManager, String name, Player player) {
		super(plugin, shop, clientManager, donationManager, name, player);

		buildPage();
	}

	@Override
	public void activateGadget(Player player, Gadget gadget) {
		if (gadget instanceof ItemGadget) {
			if (getPlugin().getInventoryManager().Get(player).getItemCount(gadget.GetName()) <= 0) {
				purchaseGadget(player, gadget);
				return;
			}
		}

		playAcceptSound(player);
		gadget.Enable(player);

		buildPage();
	}

	@Override
	protected void buildPage() {
		GuiUtil.fillBorders(getInventory());

		final List<Class> costumeClasses = new ArrayList<>();

		for (final Gadget gadget : getPlugin().getGadgetManager().getGadgets(GadgetType.Costume)) {
			final OutfitGadget outfitGadget = (OutfitGadget) gadget;
			final Class clazz = gadget.getClass();

			if (!costumeClasses.contains(clazz)) {
				costumeClasses.add(clazz);
			}

			// Safe layout: each costume set gets a column, starting from row 2 inner slots.
			// Column positions: 2, 4 (i.e. slots col 2 and 4 of a 9-wide row)
			// Row offsets: Helmet=row 2(+9), Chest=row 3(+18), Legs=row 4(+27), Boots=row 5(+36)
			// This keeps all slots within the safe inner grid (max slot = 43).
			int col = costumeClasses.indexOf(clazz) * 2 + 2; // columns: 2, 4, 6...
			int slot = col; // base in row 1

			if (outfitGadget.GetSlot() == OutfitGadget.ArmorSlot.Helmet) {
				slot += 9;   // row 2: slots 11, 13
			} else if (outfitGadget.GetSlot() == OutfitGadget.ArmorSlot.Chest) {
				slot += 18;  // row 3: slots 20, 22
			} else if (outfitGadget.GetSlot() == OutfitGadget.ArmorSlot.Legs) {
				slot += 27;  // row 4: slots 29, 31
			} else if (outfitGadget.GetSlot() == OutfitGadget.ArmorSlot.Boots) {
				slot += 36;  // row 5: slots 38, 40
			}

			addGadget(gadget, slot);

			if (gadget.IsActive(getPlayer())) {
				addGlow(slot);
			}
		}

		addButton(8, new ShopItem(Material.TNT, C.cRed + C.Bold + "Remove all Clothing", new String[] {}, 1, false),
				new IButton() {
					@Override
					public void onClick(Player player, ClickType clickType) {
						boolean gadgetDisabled = false;
						for (final Gadget gadget : getPlugin().getGadgetManager().getGadgets(GadgetType.Costume)) {
							if (gadget.IsActive(player)) {
								gadgetDisabled = true;
								gadget.Disable(player);
							}
						}

						if (gadgetDisabled) {
							buildPage();
							player.playSound(player.getEyeLocation(), Sound.ENTITY_GENERIC_SPLASH, 1, 1);
						}
					}
				});

		addButton(4, new ShopItem(Material.RED_BED, C.cGray + " \u21FD Go Back", new String[] {}, 1, false), new IButton() {
			@Override
			public void onClick(Player player, ClickType clickType) {
				getShop().openPageForPlayer(getPlayer(),
						new Menu(getPlugin(), getShop(), getClientManager(), getDonationManager(), player));
			}
		});
	}
}
