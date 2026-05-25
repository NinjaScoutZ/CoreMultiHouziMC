package com.houzicore.arcade.nautilus.game.arcade.kit.perks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilBlock;
import com.houzicore.shared.common.util.UtilEnt;
import com.houzicore.shared.common.util.UtilEvent;
import com.houzicore.shared.common.util.UtilEvent.ActionType;
import com.houzicore.shared.common.util.UtilGear;
import com.houzicore.shared.common.util.UtilParticle;
import com.houzicore.shared.common.util.UtilParticle.ParticleType;
import com.houzicore.shared.common.util.UtilParticle.ViewDist;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.recharge.Recharge;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.data.IBlockRestorer;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.WitherSkull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class PerkWitherAttack extends Perk
{
	private ArrayList<WitherSkull> _active = new ArrayList<WitherSkull>();
	
	public PerkWitherAttack() 
	{
		super("Wither Skull", new String[] 
				{ 
				C.cYellow + "Left-Click" + C.cGray + " with Gold Sword to use " + C.cGreen + "Wither Skull"
				});
	}
	
	
	@EventHandler
	public void Activate(PlayerInteractEvent event)
	{
		if (event.isCancelled())
			return;
		
		if (!UtilEvent.isAction(event, ActionType.L))
			return;
		
		Player player = event.getPlayer();
		
		if (!Kit.HasKit(player))
			return;
		
		if (!UtilGear.isMat(event.getPlayer().getItemInHand(), Material.GOLDEN_SWORD))
			return;
		
		if (!Recharge.Instance.use(player, GetName(), 2000, true, true))
			return;
		
		//Fire
		_active.add(player.launchProjectile(WitherSkull.class));
		
		//Sound
		player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WITHER_SHOOT, 1f, 1f);
		
		//Inform
		UtilPlayer.message(player, F.main("Game", "You used " + F.skill(GetName()) + "."));
	}
	
	@EventHandler
	public void explode(EntityExplodeEvent event)
	{
		if (!_active.contains(event.getEntity()))
			return;
		
		event.setCancelled(true);
		
		WitherSkull skull = (WitherSkull)event.getEntity();
		
		UtilParticle.PlayParticle(ParticleType.HUGE_EXPLOSION, skull.getLocation(), 0, 0, 0, 0, 1,
				ViewDist.MAX, UtilServer.getPlayers());
		
		explode(skull);
	}
	
	@EventHandler
	public void clean(UpdateEvent event)  
	{
		if (event.getType() != UpdateType.FAST)
			return;

		Iterator<WitherSkull> skullIterator = _active.iterator();
		
		while (skullIterator.hasNext())
		{
			WitherSkull skull = skullIterator.next();
			
			if (!skull.isValid())
			{
				skullIterator.remove();
				skull.remove();
				continue;
			}
		}
	}
		
	@EventHandler(priority = EventPriority.LOWEST)
	public void ExplodeDamage(EntityDamageByEntityEvent event)
	{
		if (event.isCancelled())
			return;
		
		if (event.getDamager() != null && event.getDamager() instanceof WitherSkull)
			event.setCancelled(true);
	}
	
	private void explode(WitherSkull skull) 
	{	
		double scale = 0.4 + 0.6 * Math.min(1, skull.getTicksLived()/20d);
		
		//Players
		HashMap<Player, Double> players = UtilPlayer.getInRadius(skull.getLocation(), 7);
		for (Player player : players.keySet())
		{
			if (!Manager.GetGame().IsAlive(player))
				continue;

			//Damage Event
			Manager.GetDamage().NewDamageEvent(player, (LivingEntity)skull.getShooter(), null,
					DamageCause.CUSTOM, 2 + 10 * players.get(player) * scale, true, true, false,
					UtilEnt.getName((LivingEntity)skull.getShooter()), GetName());
		}
		
		//Blocks
		Set<Block> blocks = UtilBlock.getInRadius(skull.getLocation(), 4d).keySet();
		
		Iterator<Block> blockIterator = blocks.iterator();
		while (blockIterator.hasNext())
		{
			Block block = blockIterator.next();
			
			if (block.isLiquid())
				blockIterator.remove();
			
			else if (block.getRelative(BlockFace.UP).isLiquid())
				blockIterator.remove();
		}

		if (Manager.GetGame() != null && Manager.GetGame() instanceof IBlockRestorer)
		{
			((IBlockRestorer)Manager.GetGame()).addBlocks(blocks);
		}
		
		
		Manager.GetExplosion().BlockExplosion(blocks, skull.getLocation(), false);
	}
}
