package com.houzicore.arcade.nautilus.game.arcade.game.games.primalgames.kit;

import java.util.HashSet;
import java.util.Iterator;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.UtilAction;
import com.houzicore.shared.common.util.UtilMath;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.recharge.Recharge;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;
import org.bukkit.event.block.Action;

public class PerkLeapSlam extends Perk
{
	private HashSet<Player> _leaping = new HashSet<Player>();

	public PerkLeapSlam() 
	{
		super("Leap Slam", new String[] 
				{ 
				C.cGray + "Right-Click with Axe to leap.",
				C.cGray + "Creates an explosion upon landing.",
				C.cGray + "Deals 4 damage to nearby enemies."
				});
	}
		
	@EventHandler
	public void onInteract(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();
		if (!Kit.HasKit(player)) return;
		if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		
		if (player.getInventory().getItemInMainHand() == null) return;
		if (!player.getInventory().getItemInMainHand().getType().name().contains("AXE")) return;

		if (!Recharge.Instance.use(player, GetName(), 12000, true, true)) return;

		// Leap
		UtilAction.velocity(player, player.getLocation().getDirection(), 1.5, false, 0, 0.2, 1.2, true);
		player.playSound(player.getLocation(), Sound.ENTITY_BAT_TAKEOFF, 1f, 1f);
		
		_leaping.add(player);
	}

	@EventHandler
	public void onUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK) return;

		Iterator<Player> hit = _leaping.iterator();
		while (hit.hasNext())
		{
			Player player = hit.next();
			if (!player.isOnline() || player.isDead() || !Manager.GetGame().IsAlive(player))
			{
				hit.remove();
				continue;
			}

			// Has to have travelled up first before landing counts, but simple check is enough since tick delay
			if (player.getVelocity().getY() < -0.1 && player.isOnGround())
			{
				hit.remove();
				slam(player);
			}
		}
	}

	private void slam(Player player)
	{
		player.getWorld().spawnParticle(Particle.EXPLOSION, player.getLocation(), 1, 0, 0, 0, 0);
		player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1f, 1f);

		for (Player other : Manager.GetGame().GetPlayers(true))
		{
			if (other.equals(player)) continue;
			if (UtilMath.offset(other, player) > 4.0) continue;

			// Manager.GetDamage().NewDamageEvent(other, player, null, org.bukkit.event.entity.EntityDamageEvent.DamageCause.CUSTOM, 4.0, false, true, false, "Leap Slam", "Leap Slam");
			other.damage(4.0, player);
			org.bukkit.util.Vector traj = other.getLocation().toVector().subtract(player.getLocation().toVector());
			traj.setY(0).normalize();
			UtilAction.velocity(other, traj, 1.0, true, 0.4, 0, 1.0, true);
		}
	}
}
