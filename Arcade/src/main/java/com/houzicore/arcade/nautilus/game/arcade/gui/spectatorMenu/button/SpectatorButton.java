package com.houzicore.arcade.nautilus.game.arcade.gui.spectatorMenu.button;

//import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import com.houzicore.shared.common.util.F;
import com.houzicore.shared.core.shop.item.IButton;
import com.houzicore.arcade.ArcadeManager;

/**
 * Created by shaun on 14-09-26.
 */
public class SpectatorButton implements IButton
{
	private ArcadeManager _arcadeManager;
	private Player _player;
	private Player _target;

	public SpectatorButton(ArcadeManager arcadeManager, Player player, Player target)
	{
		_arcadeManager = arcadeManager;
		_player = player;
		_target = target;
	}

	@Override
	public void onClick(Player player, ClickType clickType)
	{
		// Make sure this player is still a spectator
		if (_arcadeManager.IsAlive(player))
			return;

		if (_arcadeManager.IsAlive(_target))
		{
			org.bukkit.Location targetLoc = _target.getLocation().add(0, 1, 0);
			_player.teleport(targetLoc);
			
			_player.playSound(targetLoc, org.bukkit.Sound.ENTITY_ENDERMAN_TELEPORT, 0.8f, 1.0f);
			org.bukkit.Bukkit.getScheduler().runTaskLater(_arcadeManager.getPlugin(), () -> {
				_player.playSound(targetLoc, org.bukkit.Sound.BLOCK_PORTAL_TRAVEL, 0.3f, 1.8f);
			}, 1L);
			org.bukkit.Bukkit.getScheduler().runTaskLater(_arcadeManager.getPlugin(), () -> {
				_player.playSound(targetLoc, org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.6f, 1.5f);
			}, 3L);
			
			com.houzicore.shared.common.util.UtilParticle.PlayParticle(com.houzicore.shared.common.util.UtilParticle.ParticleType.PORTAL, targetLoc, 0.4f, 1.0f, 0.4f, 0.1f, 30, com.houzicore.shared.common.util.UtilParticle.ViewDist.NORMAL, _player);
			com.houzicore.shared.common.util.UtilParticle.PlayParticle(com.houzicore.shared.common.util.UtilParticle.ParticleType.FIREWORKS_SPARK, targetLoc.clone().add(0,1,0), 0.3f, 0.3f, 0.3f, 0.05f, 8, com.houzicore.shared.common.util.UtilParticle.ViewDist.NORMAL, _player);
			
			com.houzicore.shared.common.util.UtilTextMiddle.display(
				"§d✦ " + com.houzicore.shared.common.util.UtilText.toSmallCaps("spectating"),
				"§7กำลังดู §e" + _target.getName(),
				5, 25, 10, _player
			);
		}
		else
		{
			_player.sendMessage(F.main("Spectate", F.name(_target.getName()) + " is no longer alive."));
		}
	}
}
