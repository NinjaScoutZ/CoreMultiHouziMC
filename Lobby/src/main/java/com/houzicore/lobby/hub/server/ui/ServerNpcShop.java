package com.houzicore.lobby.hub.server.ui;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.core.party.Party;
import com.houzicore.shared.core.shop.ShopBase;
import com.houzicore.shared.core.shop.page.ShopPageBase;
import com.houzicore.lobby.hub.server.ServerManager;

public class ServerNpcShop extends ShopBase<ServerManager>
{	
	public ServerNpcShop(ServerManager plugin, CoreClientManager clientManager, DonationManager donationManager, String name)
	{
		super(plugin, clientManager, donationManager, name);
	}

	@Override
	protected ShopPageBase<ServerManager, ? extends ShopBase<ServerManager>> buildPagesFor(Player player)
	{
		return new ServerNpcSelectionPage(getPlugin(), this, getClientManager(), getDonationManager(), getName(), player, getName());
	}

	@Override
	protected boolean canOpenShop(Player player)
	{
		Party party = getPlugin().getPartyManager().getPartyByPlayer(player);
		
		if (party != null && !player.getName().equalsIgnoreCase(party.getLeaderName()))
		{
			boolean isThai = com.houzicore.shared.core.lang.LangManager.get().isThai(player);
			player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1, .6f);
			player.sendMessage(F.main("Party", com.houzicore.shared.core.lang.LangManager.get().get(player, "hub.party.leader_only")));
			player.sendMessage(F.main("Party", com.houzicore.shared.core.lang.LangManager.get().get(player, "hub.party.leave_hint")));
			return false;
		}
		
		return true;
	}
	
	public void UpdatePages()
	{
		for (ShopPageBase<ServerManager, ? extends ShopBase<ServerManager>> page : getPlayerPageMap().values())
		{
			if (page instanceof ServerNpcPage)
			{
				((ServerNpcPage)page).Update();
			}
			else if (page instanceof ServerGameMenu)
			{
				((ServerGameMenu)page).Update();
			}
		}
	}
	
	protected void openShopForPlayer(Player player)
	{ 
		if (getPlugin().getHubManager().GetVisibility() != null)
			getPlugin().getHubManager().GetVisibility().addHiddenPlayer(player);
	}
	
	protected void closeShopForPlayer(Player player)
	{ 
		if (getPlugin().getHubManager().GetVisibility() != null)
			getPlugin().getHubManager().GetVisibility().removeHiddenPlayer(player);
	}
}
