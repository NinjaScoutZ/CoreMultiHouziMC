package nautilus.game.arcade.game.games.paintball.kits.perks;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import com.houzicore.shared.core.common.util.C;
import com.houzicore.shared.core.common.util.UtilBlock;
import com.houzicore.shared.core.common.util.UtilServer;
import com.houzicore.shared.core.recharge.Recharge;
import com.houzicore.shared.core.updater.UpdateType;
import com.houzicore.shared.core.updater.event.UpdateEvent;
import com.houzicore.arcade.nautilus.game.arcade.game.GameTeam;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;

public class PerkPaintballMachineGun extends Perk 
{
	public PerkPaintballMachineGun() 
	{
		super("Machine Gun", new String[] 
				{
				C.cYellow + "Right-Click" + C.cGray + " to use " + C.cGreen + "Machine Gun",
				"Experience Bar represents weapon overheating."
				});
	}
	
	@EventHandler
	public void WeaponCooldown(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;
		
		for (Player player : UtilServer.getPlayers())
		{
			if (!Kit.HasKit(player))
				continue;
			
			if (Recharge.Instance.usable(player, "Cool"))
				player.setExp((float)Math.max(0f, player.getExp() - 0.020f));
		}
	}

	@EventHandler 
	public void Shoot(PlayerInteractEvent event)
	{
		if (!Manager.GetGame().IsLive())
			return;
		
		if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;

		if (UtilBlock.usable(event.getClickedBlock()))
			return;

		if (event.getPlayer().getItemInHand() == null)
			return;

		if (event.getPlayer().getItemInHand().getType() != Material.DIAMOND_BARDING)
			return;

		final Player player = event.getPlayer();

		if (!Kit.HasKit(player))
			return;
		
		final GameTeam team = Manager.GetGame().GetTeam(player);
		if (team == null)
			return;
		
		event.setCancelled(true);
		
		if (!Recharge.Instance.use(player, GetName(), 80, false, false))
			return;
		
		ShootPaintball(player, team);
		
		/*
		UtilServer.getServer().getScheduler().scheduleSyncDelayedTask(Manager.getPlugin(), new Runnable()
		{
			public void run()
			{
				ShootPaintball(player, team);
			}
		}, 2);
		*/
	}
	
	public void ShootPaintball(Player player, GameTeam team)
	{
		//Energy
		if (player.getExp() >= 0.97)
			return;	
		
		player.setExp((float) Math.min(.999, player.getExp() + 0.025));
		
		//Shoot
		Vector rand = new Vector(Math.random() - 0.5, Math.random() - 0.5, Math.random() - 0.5);
		rand.multiply(0.25);

		if (team.GetColor() == ChatColor.AQUA)
		{
			//Projectile proj = player.getWorld().spawn(player.getEyeLocation().add(player.getLocation().getDirection()), Snowball.class);
			//proj.setVelocity(player.getLocation().getDirection().multiply(1.6).add(rand));
			//proj.setShooter(player);
			
			Projectile proj = player.launchProjectile(Snowball.class);
			proj.setVelocity(proj.getVelocity().multiply(1.6).add(rand));

			//Sound
			player.getWorld().playSound(player.getLocation(), Sound.CHICKEN_EGG_POP, 1.5f, 2f);
		}
		else
		{
			//Projectile proj = player.getWorld().spawn(player.getEyeLocation().add(player.getLocation().getDirection()), EnderPearl.class);
			//proj.setVelocity(player.getLocation().getDirection().multiply(1.6).add(rand));
			//proj.setShooter(player);
			
			Projectile proj = player.launchProjectile(EnderPearl.class);
			proj.setVelocity(proj.getVelocity().multiply(1.6).add(rand));

			//Sound
			player.getWorld().playSound(player.getLocation(), Sound.CHICKEN_EGG_POP, 1.5f, 1.75f);
		}
		
		Recharge.Instance.useForce(player, "Cool", 250);
	}
}
