package com.houzicore.shared.core.cosmetic.ui;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.messaging.PluginMessageListener;

import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.common.CurrencyType;
import com.houzicore.shared.core.cosmetic.CosmeticManager;
import com.houzicore.shared.core.cosmetic.ui.page.GadgetPage;
import com.houzicore.shared.core.cosmetic.ui.page.Menu;
import com.houzicore.shared.core.cosmetic.ui.page.PetTagPage;
import com.houzicore.shared.core.cosmetic.ui.page.TreasurePage;
import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.core.gadget.event.ItemGadgetOutOfAmmoEvent;
import com.houzicore.shared.core.shop.ShopBase;
import com.houzicore.shared.core.shop.page.ShopPageBase;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;

public class CosmeticShop extends ShopBase<CosmeticManager> implements PluginMessageListener {
	public CosmeticShop(CosmeticManager plugin, CoreClientManager clientManager, DonationManager donationManager,
			String name) {
		super(plugin, clientManager, donationManager, name, CurrencyType.Coins, CurrencyType.Essence);

		plugin.getPlugin().getServer().getMessenger().registerIncomingPluginChannel(plugin.getPlugin(), "minecraft:item_name",
				this);
	}

	@Override
	protected ShopPageBase<CosmeticManager, ? extends ShopBase<CosmeticManager>> buildPagesFor(Player player) {
		return new Menu(getPlugin(), this, getClientManager(), getDonationManager(), player);
	}

	@EventHandler
	public void itemGadgetEmptyAmmo(ItemGadgetOutOfAmmoEvent event) {
		new GadgetPage(getPlugin(), this, getClientManager(), getDonationManager(), "Gadgets", event.getPlayer())
				.purchaseGadget(event.getPlayer(), event.getGadget());
	}

	@Override
	public void onPluginMessageReceived(String channel, Player player, byte[] message) {
		if (!channel.equalsIgnoreCase("minecraft:item_name"))
			return;

		if (getPlayerPageMap().containsKey(player.getName())
				&& getPlayerPageMap().get(player.getName()) instanceof PetTagPage) {
			if (message != null && message.length >= 1) {
				final String tagName = new String(message);

				//((PetTagPage) getPlayerPageMap().get(player.getName())).SetTagName(tagName);
			}
		}
	}

	@EventHandler
	public void updateTreasure(UpdateEvent event) {
		if (event.getType() != UpdateType.TICK)
			return;

		for (final ShopPageBase<CosmeticManager, ? extends ShopBase<CosmeticManager>> shop : getPlayerPageMap()
				.values()) {
			if (shop instanceof TreasurePage) {
				((TreasurePage) shop).update();
			}
		}
	}
}
