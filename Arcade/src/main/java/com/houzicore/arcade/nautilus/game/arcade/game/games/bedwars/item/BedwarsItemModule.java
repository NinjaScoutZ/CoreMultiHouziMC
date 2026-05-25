package com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.item;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.houzicore.shared.common.util.UtilInv;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.recharge.Recharge;

import com.houzicore.arcade.nautilus.game.arcade.events.GameStateChangeEvent;
import com.houzicore.arcade.nautilus.game.arcade.game.Game.GameState;
import com.houzicore.arcade.nautilus.game.arcade.game.GameTeam;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.BedwarsModule;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.Bedwars;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.team.BedwarsTeam;

public class BedwarsItemModule extends BedwarsModule
{

	private final List<BedwarsSpecialItem> _items;

	public BedwarsItemModule(Bedwars game)
	{
		super(game);

		_items = game.generateSpecialItems();
	}

	@Override
	public void unregister()
	{
		super.unregister();
		_items.forEach(BedwarsSpecialItem::cleanup);
	}

	@EventHandler
	public void live(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Live)
		{
			return;
		}

		_items.forEach(BedwarsSpecialItem::setup);
	}

	@EventHandler
	public void playerInteract(PlayerInteractEvent event)
	{
		if (event.getAction() == Action.PHYSICAL)
		{
			return;
		}

		Player player = event.getPlayer();
		ItemStack itemStack = player.getItemInHand();

		if (UtilPlayer.isSpectator(player))
		{
			event.setCancelled(true);
			return;
		}

		if (itemStack == null)
		{
			return;
		}

		GameTeam team = _game.GetTeam(player);

		if (team == null)
		{
			return;
		}

		BedwarsTeam bedTeam = _game.getBedwarsTeamModule().getBedwarsTeam(team);

		if (bedTeam == null)
		{
			return;
		}

		for (BedwarsSpecialItem item : _items)
		{
			if (item.getItemStack().getType() != itemStack.getType())
			{
				continue;
			}

			event.setCancelled(true);
			boolean inform = item.getCooldown() >= 1000;

			if (!Recharge.Instance.usable(player, item.getName(), inform))
			{
				return;
			}

			if (item.onClick(event, bedTeam))
			{
				if (itemStack.getAmount() > 1)
				{
					itemStack.setAmount(itemStack.getAmount() - 1);
					player.setItemInHand(itemStack);
				}
				else
				{
					player.setItemInHand(null);
				}
				Recharge.Instance.useForce(player, item.getName(), item.getCooldown(), inform);
			}

			return;
		}
	}

}
