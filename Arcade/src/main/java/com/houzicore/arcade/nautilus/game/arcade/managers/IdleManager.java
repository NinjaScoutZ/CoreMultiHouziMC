package com.houzicore.arcade.nautilus.game.arcade.managers;

import java.util.HashMap;
import java.util.Map;

import com.houzicore.shared.common.Rank;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.common.util.UtilTime;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.game.Game.GameState;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class IdleManager implements Listener
{
	private final ArcadeManager _arcadeManager;
	private final Map<Player, Float> _yaw = new HashMap<>();
	private final Map<Player, Double> _posX = new HashMap<>();
	private final Map<Player, Double> _posZ = new HashMap<>();
	private final Map<Player, Long> _idle = new HashMap<>();
	private final Map<Player, Integer> _beep = new HashMap<>();

	public IdleManager(ArcadeManager manager)
	{
		_arcadeManager = manager;

		Bukkit.getPluginManager().registerEvents(this, getArcadeManager().getPlugin());
	}
	
	@EventHandler
	public void ChatIdle(final AsyncPlayerChatEvent event)
	{
		if (!getArcadeManager().IsPlayerKickIdle())
			return;
		
		Bukkit.getServer().getScheduler().runTaskLater(getArcadeManager().getPlugin(), new Runnable()
		{
			@Override
			public void run()
			{
				_idle.put(event.getPlayer(), System.currentTimeMillis());
			}
		}, 1);
	}

	@EventHandler
	public void kickIdlePlayers(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;
		
		if (!getArcadeManager().IsPlayerKickIdle())
			return;
		
		if (getArcadeManager().GetGame() == null)
			return;

		for (Player player : UtilServer.getPlayers())
		{
			if (!_yaw.containsKey(player) || !_idle.containsKey(player))
			{
				_yaw.put(player, player.getLocation().getYaw());
				_posX.put(player, player.getLocation().getX());
				_posZ.put(player, player.getLocation().getZ());
				_idle.put(player, System.currentTimeMillis());
			}

			float currentYaw = player.getLocation().getYaw();
			double currentX = player.getLocation().getX();
			double currentZ = player.getLocation().getZ();

			boolean moved = currentYaw != _yaw.get(player)
				|| Math.abs(currentX - _posX.get(player)) > 0.1
				|| Math.abs(currentZ - _posZ.get(player)) > 0.1;

			if (!moved)
			{		
				if (UtilTime.elapsed(_idle.get(player), getArcadeManager().GetGame().IsLive() ? 240000 : 120000))
				{
					if (getArcadeManager().GetGame().GetState() != GameState.Recruit && !getArcadeManager().GetGame().IsAlive(player))
						continue;
					
					if (getArcadeManager().GetClients().Get(player).GetRank().Has(Rank.MODERATOR))
						continue;
					
					//Start Beeps
					if (!_beep.containsKey(player))
					{
						_beep.put(player, 20);
					}
					//Countdown
					else
					{ { }
						int count = _beep.get(player);
						
						if (count == 0)
						{
							player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 10f, 1f);
							getArcadeManager().GetPortal().sendPlayerToServer(player, "Lobby");
						}
						else
						{	
							float scale = (float) (0.8 + (((double)count/20d)*1.2));
							player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, scale, scale);
							
							if (count%2 == 0)
							{
								UtilPlayer.message(player, C.cGold + C.Bold + "You will be AFK removed in " + (count/2) + " seconds...");
							}
							
							count--;
							_beep.put(player, count);
						}
					}	
				}
					
				continue;
			}
				
			_yaw.put(player, currentYaw);
			_posX.put(player, currentX);
			_posZ.put(player, currentZ);
			_idle.put(player, System.currentTimeMillis());
			_beep.remove(player);
		}
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		_yaw.remove(event.getPlayer());
		_posX.remove(event.getPlayer());
		_posZ.remove(event.getPlayer());
		_idle.remove(event.getPlayer());
		_beep.remove(event.getPlayer());
	}

	public ArcadeManager getArcadeManager()
	{
		return _arcadeManager;
	}
}
