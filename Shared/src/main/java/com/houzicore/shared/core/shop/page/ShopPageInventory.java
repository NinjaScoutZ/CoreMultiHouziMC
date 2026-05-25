package com.houzicore.shared.core.shop.page;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import com.houzicore.shared.MiniPlugin;
import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.core.shop.ShopBase;
import com.houzicore.shared.core.shop.item.IButton;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.core.itemstack.ItemBuilder;

public abstract class ShopPageInventory<PluginType extends MiniPlugin, ShopType extends ShopBase<PluginType>> extends
		ShopPageBase<PluginType, ShopType>
{
	private int _page;

	public ShopPageInventory(PluginType plugin, ShopType shop, CoreClientManager clientManager, DonationManager donationManager,
			String name, Player player)
	{
		super(plugin, shop, clientManager, donationManager, name, player, 54);
	}

	public ShopPageInventory(PluginType plugin, ShopType shop, CoreClientManager clientManager, DonationManager donationManager,
			String name, Player player, int slots)
	{
		super(plugin, shop, clientManager, donationManager, name, player, slots);
	}

	protected abstract IButton[] getButtons();

	protected abstract ItemStack[] getItems();

	protected abstract void buildItems();

	@Override
	protected final void buildPage()
	{
		clearPage();
		buildItems();

		IButton[] buttons = getButtons();
		ItemStack[] items = getItems();

		int maxLen = Math.max(items.length, buttons.length);
		boolean pages = maxLen > getSize();

		_page = Math.max(0, Math.min(_page, pages ? (int) Math.ceil(maxLen / (double) (getSize() - 9)) - 1 : 0));

		int start = pages ? _page * (getSize() - 9) : 0;

		for (int slot = 0; slot < (pages ? getSize() - 9 : getSize()); slot++)
		{
			IButton button = null;
			ItemStack item = null;

			if (slot + start < buttons.length)
			{
				button = buttons[slot + start];
			}

			if (slot + start < items.length)
			{
				item = items[slot + start];
			}

			if (button != null)
			{
				super.addButton(slot, item, button);
			}
			else
			{
				setItem(slot, item);
			}
		}

		if (pages)
		{
			for (int slot = 0; slot < 9; slot++)
			{
				int realSlot = getSize() - (9 - slot);

				if ((slot == 0 && _page > 0) || (slot == 8 && maxLen > (_page + 1) * (getSize() - 9)))
				{
					final int nextPage = slot == 0 ? _page - 1 : _page + 1;

					ItemStack item = new ItemBuilder(Material.OAK_SIGN)
						.setTitle(C.cGreen + (slot == 0 ? "Previous Page" : "Next Page"))
						.setAmount(nextPage + 1)
						.build();

					super.addButton(realSlot, item, new IButton()
					{
						@Override
						public void onClick(Player player, ClickType clickType)
						{
							setPage(nextPage);
						}
					});
				}
				else if (slot == 4)
				{
					ItemStack item = new ItemBuilder(Material.PAPER)
						.setTitle(C.cYellow + "Page " + (_page + 1))
						.setAmount(_page + 1)
						.build();

					setItem(realSlot, item);
				}
			}
		}
	}

	public void setPage(int newPage)
	{
		_page = newPage;
		buildPage();
	}
}
