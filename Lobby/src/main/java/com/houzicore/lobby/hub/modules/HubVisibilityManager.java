package com.houzicore.lobby.hub.modules;

import java.util.HashMap;
import java.util.HashSet;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.houzicore.shared.MiniPlugin;
import com.houzicore.shared.common.util.UtilEvent;
import com.houzicore.shared.common.util.UtilGear;
import com.houzicore.shared.common.util.UtilMath;
import com.houzicore.shared.common.util.UtilParticle;
import com.houzicore.shared.common.util.UtilEvent.ActionType;
import com.houzicore.shared.common.util.UtilParticle.ParticleType;
import com.houzicore.shared.common.util.UtilParticle.ViewDist;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.shared.core.visibility.VisibilityManager;
import com.houzicore.lobby.hub.HubManager;

public class HubVisibilityManager extends MiniPlugin
{
	public HubManager Manager;

	private HashMap<Player, Integer> _particle = new HashMap<Player, Integer>();
	private HashSet<Player> _hiddenPlayers = new HashSet<Player>();

	public HubVisibilityManager(HubManager manager)
	{
		super("Visibility Manager", manager.getPlugin());

		Manager = manager;
	}

	public void addHiddenPlayer(Player player)
	{
		_hiddenPlayers.add(player);

	}

	public void removeHiddenPlayer(Player player)
	{
		_hiddenPlayers.remove(player);
	}

	@EventHandler
	public void removeHiddenPlayerOnQuit(PlayerQuitEvent event)
	{
		_hiddenPlayers.remove(event.getPlayer());
	}

	@EventHandler
	public void updateVisibility(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
			return;

		for (Player player : UtilServer.getPlayers())
		{
			boolean hideMe = Manager.GetTutorial().InTutorial(player) || 
					UtilMath.offset2d(player.getLocation(), Manager.GetSpawn()) == 0 || 
					Manager.getPreferences().Get(player).Invisibility || 
					_hiddenPlayers.contains(player);

			for (Player other : UtilServer.getPlayers())
			{
				if (player.equals(other))
					continue;

				if (hideMe || 
						!Manager.getPreferences().Get(other).ShowPlayers || 
						Manager.GetTutorial().InTutorial(other))
				{
					VisibilityManager.Instance.setVisibility(player, false, other);
				}
				else
				{
					VisibilityManager.Instance.setVisibility(player, true, other);
				}
			}
		}
	}	

	@EventHandler
	public void ParticleSwap(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();

		if (!player.isOp())
			return;

		if (!UtilGear.isMat(player.getItemInHand(), Material.GOLD_NUGGET))
			return;

		int past = 0;
		if (_particle.containsKey(player))
			past = _particle.get(player);

		if (UtilEvent.isAction(event, ActionType.R))
		{
			past = (past+1)%ParticleType.values().length;
		}
		else if (UtilEvent.isAction(event, ActionType.L))
		{
			past = past - 1;
			if (past < 0)
				past = ParticleType.values().length - 1;
		}

		_particle.put(player, past);

		player.sendMessage("Particle: " + ParticleType.values()[past]);
	}

	@EventHandler
	public void Particles(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;

		for (Player player : _particle.keySet())
		{

			UtilParticle.PlayParticle(ParticleType.values()[_particle.get(player)], player.getLocation().add(1, 1, 0), 0f, 0f, 0f, 0, 1,
					ViewDist.NORMAL, UtilServer.getPlayers());

		}
	}
}
