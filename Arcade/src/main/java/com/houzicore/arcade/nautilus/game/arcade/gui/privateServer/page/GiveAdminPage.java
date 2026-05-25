package com.houzicore.arcade.nautilus.game.arcade.gui.privateServer.page;

import org.bukkit.entity.Player;

import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.gui.privateServer.PrivateServerShop;

public class GiveAdminPage extends PlayerPage
{
	public GiveAdminPage(ArcadeManager plugin, PrivateServerShop shop, Player player)
	{
		super(plugin, shop, "Give Admin", player);
		buildPage();
	}

	@Override
	public boolean showPlayer(Player player)
	{
		return !_manager.isAdmin(player, false);
	}

	@Override
	public void clicked(int slot, Player player)
	{
		removeButton(slot);
		_manager.giveAdmin(player);
		UtilPlayer.message(getPlayer(), F.main("Server", "You gave " + F.name(player.getName()) + " admin power."));
	}

	@Override
	public String getDisplayString(Player player)
	{
		return "Click to Make Admin";
	}
}
