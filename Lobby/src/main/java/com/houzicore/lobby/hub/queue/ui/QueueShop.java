package com.houzicore.lobby.hub.queue.ui;

import java.util.Iterator;

import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.core.party.Party;
import com.houzicore.shared.core.shop.ShopBase;
import com.houzicore.shared.core.shop.page.ShopPageBase;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.lobby.hub.queue.QueueManager;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

public class QueueShop extends ShopBase<QueueManager>
{
	public QueueShop(QueueManager plugin, CoreClientManager clientManager,	com.houzicore.shared.core.donation.DonationManager donationManager,	String name)
	{
		super(plugin, clientManager, donationManager, name);
	}

	@Override
	protected ShopPageBase<QueueManager, ? extends ShopBase<QueueManager>> buildPagesFor(Player player)
	{
		return new QueuePage(getPlugin(), this, getClientManager(), getDonationManager(), "          " + ChatColor.UNDERLINE + "Queuer 9001", player);
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
	
	@EventHandler
	public void UpdatePages(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTER)
			return;
		
		for (Iterator<ShopPageBase<QueueManager, ? extends ShopBase<QueueManager>>> iterator = getPlayerPageMap().values().iterator(); iterator.hasNext();)
		{
			ShopPageBase<QueueManager, ? extends ShopBase<QueueManager>> page = iterator.next();
			
			if (page instanceof QueuePage)
			{
				((QueuePage)page).Update();
			}
		}
	}
}
