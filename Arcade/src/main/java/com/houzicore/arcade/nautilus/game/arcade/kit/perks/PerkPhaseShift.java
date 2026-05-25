package com.houzicore.arcade.nautilus.game.arcade.kit.perks;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;
import com.houzicore.shared.common.util.UtilBlock;
import com.houzicore.shared.common.util.UtilParticle;
import com.houzicore.shared.common.util.UtilParticle.ParticleType;
import com.houzicore.shared.common.util.UtilParticle.ViewDist;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.recharge.Recharge;

public class PerkPhaseShift extends Perk
{
	public PerkPhaseShift()
	{
		super("Phase Shift", new String[] {
			"Right-click Ender Pearl to teleport 4 blocks through pressure.",
			"Best used to break line-of-sight after Hunters fully commit to the chase."
		});
	}

	@EventHandler
	public void onInteract(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();

		if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;

		if (player.getInventory().getItemInMainHand().getType() != Material.ENDER_PEARL)
			return;

		if (!Kit.HasKit(player))
			return;

		// Cancel event to prevent pearl throw
		event.setCancelled(true);

		if (!Recharge.Instance.use(player, GetName(), 5000, true, true))
			return;

		Location loc = player.getLocation();
		Vector dir = loc.getDirection().normalize();
		Location target = loc.clone().add(dir.clone().multiply(4));

		// Find a valid destination by raytracing backwards if target is solid
		while ((UtilBlock.solid(target.getBlock()) || UtilBlock.solid(target.getBlock().getRelative(BlockFace.UP))) && target.distanceSquared(loc) > 1)
		{
			target.subtract(dir.clone().normalize());
		}

		// Keep original pitch and yaw
		target.setPitch(loc.getPitch());
		target.setYaw(loc.getYaw());

		// Pre-Teleport Effects
		UtilParticle.PlayParticle(ParticleType.PORTAL, player.getLocation().add(0, 1, 0), 0.5f, 1.0f, 0.5f, 0.1f, 20, ViewDist.NORMAL, UtilServer.getPlayers());
		UtilParticle.PlayParticle(ParticleType.FIREWORKS_SPARK, player.getLocation().add(0, 1, 0), 0.5f, 1.0f, 0.5f, 0.1f, 20, ViewDist.NORMAL, UtilServer.getPlayers());

		player.teleport(target);

		// Post-Teleport Effects
		UtilParticle.PlayParticle(ParticleType.PORTAL, player.getLocation().add(0, 1, 0), 0.5f, 1.0f, 0.5f, 0.1f, 20, ViewDist.NORMAL, UtilServer.getPlayers());
		UtilParticle.PlayParticle(ParticleType.FIREWORKS_SPARK, player.getLocation().add(0, 1, 0), 0.5f, 1.0f, 0.5f, 0.1f, 20, ViewDist.NORMAL, UtilServer.getPlayers());

		// Layer 1 Sound
		player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);

		// Layer 2 Sound (1 tick delay based on superrule.md)
		Manager.getPlugin().getServer().getScheduler().runTaskLater(Manager.getPlugin(), () -> 
		{
			player.playSound(player.getLocation(), Sound.BLOCK_END_PORTAL_SPAWN, 0.5f, 1.0f);
		}, 1L);
	}
}
