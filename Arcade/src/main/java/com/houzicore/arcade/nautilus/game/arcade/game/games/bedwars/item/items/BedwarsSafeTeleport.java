package com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.item.items;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.UtilEnt;
import com.houzicore.shared.common.util.UtilParticle;
import com.houzicore.shared.common.util.UtilParticle.ParticleType;
import com.houzicore.shared.common.util.UtilParticle.ViewDist;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.core.itemstack.ItemBuilder;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;

import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.Bedwars;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.item.BedwarsSpecialItem;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.team.BedwarsTeam;

public class BedwarsSafeTeleport extends BedwarsSpecialItem implements Listener
{

	public static final ItemStack ITEM_STACK = new ItemBuilder(Material.ENDER_EYE)
			.setTitle(C.cYellow + C.Bold + "Safe Teleport")
			.addLore("", "Teleports you have to a safe location", "if you fall into the void.", "Warning! Safe Teleport has a", C.cRed + "20 second" + C.cGray + " cooldown between uses.", "Uses: " + C.cRed + "1")
			.build();

	private final Map<Player, Location> _safeLocations;

	public BedwarsSafeTeleport(Bedwars game)
	{
		super(game, ITEM_STACK, "Safe Teleport", TimeUnit.SECONDS.toMillis(20));

		_safeLocations = new HashMap<>();
	}

	@Override
	protected void setup()
	{
		org.bukkit.Bukkit.getPluginManager().registerEvents(this, _game.getArcadeManager().getPlugin());
	}

	@Override
	protected void cleanup()
	{
		_safeLocations.clear();
		org.bukkit.event.HandlerList.unregisterAll(this);
	}

	@Override
	protected boolean onClick(PlayerInteractEvent event, BedwarsTeam bedTeam)
	{
		event.setCancelled(true);

		Player player = event.getPlayer();
		Location location = _safeLocations.get(player);

		if (location == null)
		{
			return false;
		}

		player.teleport(location.add(0, 1.5, 0));
		player.setFallDistance(0);
		player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 0);
		UtilParticle.PlayParticle(ParticleType.WITCH_MAGIC, player.getEyeLocation(), 0.5F, 0.5F, 0.5F, 0.5F, 10, ViewDist.NORMAL, UtilServer.getPlayers());
		return true;
	}

	@EventHandler
	public void updateSafeLocation(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
		{
			return;
		}

		for (Player player : _game.GetPlayers(true))
		{
			if (!player.isOnline() || UtilPlayer.isSpectator(player) || !UtilEnt.isGrounded(player))
			{
				continue;
			}

			_safeLocations.put(player, player.getLocation());
		}
	}

	@EventHandler
	public void playerQuit(PlayerQuitEvent event)
	{
		_safeLocations.remove(event.getPlayer());
	}
}
