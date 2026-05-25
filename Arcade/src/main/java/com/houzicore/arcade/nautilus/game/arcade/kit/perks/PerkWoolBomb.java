package com.houzicore.arcade.nautilus.game.arcade.kit.perks;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;

import com.houzicore.shared.api.disguise.DisguiseRequest;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilAction;
import com.houzicore.shared.common.util.UtilAlg;
import com.houzicore.shared.common.util.UtilBlock;
import com.houzicore.shared.common.util.UtilEnt;
import com.houzicore.shared.common.util.UtilInv;
import com.houzicore.shared.common.util.UtilParticle;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.common.util.UtilTime;
import com.houzicore.shared.common.util.UtilParticle.ParticleType;
import com.houzicore.shared.common.util.UtilParticle.ViewDist;
import com.houzicore.shared.core.itemstack.ItemStackFactory;
import com.houzicore.shared.core.projectile.IThrown;
import com.houzicore.shared.core.projectile.ProjectileUser;
import com.houzicore.shared.recharge.Recharge;
import com.houzicore.shared.recharge.RechargedEvent;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.data.WoolBombData;

public class PerkWoolBomb extends Perk implements IThrown
{
	private HashMap<Player, Item> _thrown = new HashMap<Player, Item>();
	private HashMap<Player, WoolBombData> _active = new HashMap<Player, WoolBombData>();
	
	public PerkWoolBomb() 
	{
		super("Wool Mine", new String[] 
				{
				C.cYellow + "Right-Click" + C.cGray + " with Axe to " + C.cGreen + "Wool Mine"
				});
	}

	@EventHandler
	public void skill(PlayerInteractEvent event)
	{
		if (event.isCancelled())
			return;

		if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;

		if (UtilBlock.usable(event.getClickedBlock()))
			return;

		if (event.getPlayer().getItemInHand() == null)
			return;

		if (!event.getPlayer().getItemInHand().getType().toString().contains("_AXE"))
			return;

		Player player = event.getPlayer();

		if (!Kit.HasKit(player))
			return;
		
		if (!Recharge.Instance.usable(player, GetName() + " Rate"))
			return;
		
		if (_active.containsKey(player))
		{
			if (detonate(player, true))
				return;
		}
		
		if (_thrown.containsKey(player))
		{
			if (solidify(player, true))
				return;
		}
		
		launch(player);

		event.setCancelled(true);
	}

	private void launch(Player player)
	{
		if (!Recharge.Instance.use(player, GetName(), 8000, true, true))
			return;

		org.bukkit.entity.Item ent = player.getWorld().dropItem(player.getEyeLocation(), ItemStackFactory.Instance.CreateStack(Material.WHITE_WOOL, (byte)0));

		UtilAction.velocity(ent, player.getLocation().getDirection(), 1, false, 0, 0.2, 10, false);	

// Manager.getDamager().AddThrow(ent, player, this, -1, true, true, true,
// null, 1f, 1f,
// null, 1, UpdateType.SLOW,
// 0.5f);
		
		_thrown.put(player, ent);

		//Inform
		UtilPlayer.message(player, F.main("Game", "You launched " + F.skill(GetName()) + "."));

		//Effect
		player.getWorld().playSound(player.getLocation(), Sound.ENTITY_SHEEP_AMBIENT, 2f, 1.5f);
		
		//Rate
		Recharge.Instance.useForce(player, GetName() + " Rate", 800);
		
		//Disguise
		updateSheepSheared(player, true);
	}
	
	@EventHandler
	public void rechargeWool(RechargedEvent event)
	{
		if (event.GetAbility().equals(GetName()))
		{
			updateSheepSheared(event.GetPlayer(), false);
		}
	}

	private void updateSheepSheared(Player player, boolean sheared)
	{
		Manager.GetDisguise().getService().getActiveSession(player.getUniqueId())
				.map(session -> session.request())
				.filter(request -> "SHEEP".equalsIgnoreCase(request.variantKey()))
				.map(request -> request.withAttribute("sheared", String.valueOf(sheared)))
				.ifPresent(request -> applyUpdatedDisguise(player, request));
	}

	private void applyUpdatedDisguise(Player player, DisguiseRequest request)
	{
		Manager.GetDisguise().getService().apply(player, request);
	}
	
	private boolean solidify(LivingEntity ent, boolean inform)
	{
		if (!(ent instanceof Player))
			return false;
		
		Player player = (Player)ent;
		
		Item thrown = _thrown.remove(player);
		if (thrown == null)
			return false;
		
		//Make Block
		Block block = thrown.getLocation().getBlock();
		
		Manager.GetBlockRestore().Restore(block);
		
		_active.put(player, new WoolBombData(block));
		
		block.setType(Material.WHITE_WOOL);
		block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, block.getType());
		
		//Clean
		thrown.remove();
		
		//Rate
		Recharge.Instance.useForce(player, GetName() + " Rate", 1000);
		
		//Inform
		if (inform)
		{
			player.getWorld().playSound(player.getLocation(), Sound.ENTITY_SHEEP_AMBIENT, 2f, 1.5f);
			
			UtilPlayer.message(player, F.main("Game", "You armed " + F.skill(GetName()) + "."));
		}
		
		return true;
	}

	private boolean detonate(Player player, boolean inform)
	{
		WoolBombData data = _active.remove(player);
		
		if (data == null)
			return false;
		
		//Restore
		data.restore();
		
		//Explode
		UtilParticle.PlayParticle(ParticleType.HUGE_EXPLOSION, data.Block.getLocation().add(0.5, 0.5, 0.5), 0, 0, 0, 0, 1,
				ViewDist.MAX, UtilServer.getPlayers());
		data.Block.getWorld().playSound(data.Block.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 3f, 0.8f);
		
		//Damage
		HashMap<LivingEntity, Double> targets = UtilEnt.getInRadius(data.Block.getLocation().add(0.5, 0.5, 0.5), 9);
		for (LivingEntity cur : targets.keySet())
		{
			//Damage Event
			Manager.GetDamage().NewDamageEvent(cur, player, null,
					DamageCause.CUSTOM, 14 * targets.get(cur) + 0.5, false, true, false,
					player.getName(), GetName());
			
			//Condition
			Manager.GetCondition().Factory().Falling(GetName(), cur, player, 10, false, true);
			
			//Knockback
			UtilAction.velocity(cur, UtilAlg.getTrajectory2d(data.Block.getLocation().add(0.5, 0.5, 0.5), cur.getEyeLocation()), 0.5 + 2.5 * targets.get(cur), true, 0.8, 0, 10, true);

			//Inform
			if (cur instanceof Player && !player.equals(cur))
				UtilPlayer.message((Player)cur, F.main("Game", F.name(player.getName()) +" hit you with " + F.skill(GetName()) + "."));	
		}
		
		//Rate
		Recharge.Instance.useForce(player, GetName() + " Rate", 800);
		
		//Inform
		if (inform)
		{
			player.getWorld().playSound(player.getLocation(), Sound.ENTITY_SHEEP_AMBIENT, 2f, 1.5f);
			
			UtilPlayer.message(player, F.main("Game", "You detonated " + F.skill(GetName()) + "."));
		}

		return true;
	}

	@Override
	public void Collide(LivingEntity target, Block block, ProjectileUser data) 
	{
		solidify(data.GetThrower(), false);

		if (target == null)
			return;

		//Damage Event
		Manager.GetDamage().NewDamageEvent(target, data.GetThrower(), null,
				DamageCause.PROJECTILE, 4, true, true, false,
				UtilEnt.getName(data.GetThrower()), GetName());
	}

	@Override
	public void Idle(ProjectileUser data) 
	{
		solidify(data.GetThrower(), false);
	}

	@Override
	public void Expire(ProjectileUser data) 
	{
		solidify(data.GetThrower(), false);
	}
	
	@EventHandler
	public void colorExpireUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTER)
			return;
		
		HashSet<Player> detonate = new HashSet<Player>();
		
		Iterator<Player> playerIterator = _active.keySet().iterator();
		
		while (playerIterator.hasNext())
		{
			Player player = playerIterator.next();
			WoolBombData data = _active.get(player);
			
			if (UtilTime.elapsed(data.Time, 8000))
			{
				detonate.add(player);
				continue;
			}
			
			if (Recharge.Instance.usable(player, GetName() + " Rate"))
			{
				if (com.houzicore.shared.common.util.IdUtil.getData(data.Block) == 14)
				{
					data.Block.setType(com.houzicore.shared.common.util.IdUtil.getMaterial(35, (byte) 0));
				}
				else
				{
					data.Block.setType(com.houzicore.shared.common.util.IdUtil.getMaterial(35, (byte) 14));
				}
			}
		}
		
		for (Player player : detonate)
		{
			detonate(player, false);
		}
	}

	@EventHandler
	public void knockback(EntityDamageByEntityEvent event)
	{
		if (event.getCause().name() == null || !event.getCause().name().contains(GetName()))
			return;

  // /* event.AddKnockback(...) */, 2);
	}
}
