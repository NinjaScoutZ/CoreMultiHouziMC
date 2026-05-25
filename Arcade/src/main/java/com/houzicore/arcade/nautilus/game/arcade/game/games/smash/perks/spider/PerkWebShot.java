package com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.spider;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilAction;
import com.houzicore.shared.common.util.UtilBlock;
import com.houzicore.shared.common.util.UtilEnt;
import com.houzicore.shared.common.util.UtilEvent;
import com.houzicore.shared.common.util.UtilEvent.ActionType;
import com.houzicore.shared.common.util.UtilItem;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.core.itemstack.ItemStackFactory;
import com.houzicore.shared.core.projectile.IThrown;
import com.houzicore.shared.core.projectile.ProjectileUser;
import com.houzicore.shared.recharge.Recharge;
import com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.SmashPerk;

public class PerkWebShot extends SmashPerk implements IThrown
{

	private int _cooldownNormal = 10000;
	private int _cooldownSmash = 1000;
	private int _webs = 20;
	
	public PerkWebShot()
	{
		super("Spin Web", new String[] { C.cYellow + "Right-Click" + C.cGray + " with Axe to use " + C.cGreen + "Spin Web" });
	}

	@Override
	public void setupValues()
	{
		_cooldownNormal = getPerkTime("Cooldown Normal");
		_cooldownSmash = getPerkTime("Cooldown Smash");
		_webs = getPerkInt("Webs");
	}

	@EventHandler
	public void ShootWeb(PlayerInteractEvent event)
	{
		if (event.isCancelled())
		{
			return;
		}

		if (!UtilEvent.isAction(event, ActionType.R))
		{
			return;
		}

		if (UtilBlock.usable(event.getClickedBlock()))
		{
			return;
		}

		Player player = event.getPlayer();

		if (!player.getInventory().getItemInMainHand().getType().name().endsWith("_AXE"))
		{
			return;
		}

		if (!hasPerk(player))
		{
			return;
		}

		if (!Recharge.Instance.use(player, GetName(), isSuperActive(player) ? _cooldownSmash : _cooldownNormal, !isSuperActive(player), !isSuperActive(player)))
		{
			return;
		}

		event.setCancelled(true);

		// Boost
		UtilAction.velocity(player, 1.2, 0.2, 1.2, true);

		for (int i = 0; i < _webs; i++)
		{
			Item ent = player.getWorld().dropItem(player.getLocation().add(0, 0.5, 0), ItemStackFactory.Instance.CreateStack(Material.COBWEB, (byte) 0, 1, "Web " + player.getName() + " " + i));

			Vector random = new Vector(Math.random() - 0.5, Math.random() - 0.5, Math.random() - 0.5);
			random.normalize();
			random.multiply(0.2);

			UtilAction.velocity(ent, player.getLocation().getDirection().multiply(-1).add(random), 1 + Math.random() * 0.4, false, 0, 0.2, 10, false);
			Manager.GetProjectile().AddThrow(ent, player, this, -1, true, true, true, true, 0.5f);
		}

		// Inform
		UtilPlayer.message(player, F.main("Game", "You used " + F.skill(GetName()) + "."));

		// Effect
		player.getWorld().playSound(player.getLocation(), Sound.ENTITY_SPIDER_AMBIENT, 2f, 0.6f);
	}

	@Override
	public void Collide(LivingEntity target, Block block, ProjectileUser data)
	{
		if (target != null)
		{
			data.GetThrown().remove();

			Manager.GetBlockRestore().Add(target.getLocation().getBlock(), 30, (byte) 0, 3000);

			// Damage Event
			Manager.GetDamage().NewDamageEvent(target, data.GetThrower(), null, DamageCause.PROJECTILE, 6, false, false, false, UtilEnt.getName(data.GetThrower()), GetName());

			target.setVelocity(new Vector(0, 0, 0));

			return;
		}

		Web(data);
	}

	@Override
	public void Idle(ProjectileUser data)
	{
		Web(data);
	}

	@Override
	public void Expire(ProjectileUser data)
	{
		Web(data);
	}
	


	public void Web(ProjectileUser data)
	{
		Location loc = data.GetThrown().getLocation();
		data.GetThrown().remove();

		if (data.GetThrown().getTicksLived() > 3)
		{
			Manager.GetBlockRestore().Add(loc.getBlock(), 30, (byte) 0, 2000);
		}
	}
}