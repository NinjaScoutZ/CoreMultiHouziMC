package nautilus.game.arcade.game.games.smash.perks.guardian;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.houzicore.shared.core.common.util.C;
import com.houzicore.shared.core.common.util.UtilAction;
import com.houzicore.shared.core.common.util.UtilAlg;
import com.houzicore.shared.core.common.util.UtilBlock;
import com.houzicore.shared.core.common.util.UtilEvent;
import com.houzicore.shared.core.common.util.UtilEvent.ActionType;
import com.houzicore.shared.core.common.util.UtilParticle.ParticleType;
import com.houzicore.shared.core.common.util.UtilParticle.ViewDist;
import com.houzicore.shared.core.common.util.UtilItem;
import com.houzicore.shared.core.common.util.UtilParticle;
import com.houzicore.shared.core.projectile.IThrown;
import com.houzicore.shared.core.projectile.ProjectileUser;
import com.houzicore.shared.core.recharge.Recharge;
import com.houzicore.shared.core.updater.UpdateType;
import com.houzicore.shared.core.updater.event.UpdateEvent;
import com.houzicore.shared.minecraft.game.core.damage.CustomDamageEvent;

import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;

public class PerkWhirlpoolBlade extends Perk implements IThrown
{

	private int _cooldown;
	private int _expireTime;
	private float _velocity;
	private float _hitBox;
	private int _damage;
	
	private Set<Item> _items = new HashSet<>();
	
	public PerkWhirlpoolBlade()
	{
		super("Whirlpool Axe", new String[] { C.cYellow + "Right-Click" + C.cGray + " with Axe to use " + C.cGreen + "Whirlpool Axe" });
	}

	@Override
	public void setupValues()
	{
		_cooldown = getPerkTime("Cooldown");
		_expireTime = getPerkTime("Expire Time");
		_velocity = getPerkFloat("Velocity");
		_hitBox = getPerkFloat("Hit Box");
		_damage = getPerkInt("Damage");
	}

	@EventHandler
	public void activate(PlayerInteractEvent event)
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

		if (!UtilItem.isAxe(player.getItemInHand()))
		{
			return;
		}

		if (!hasPerk(player))
		{
			return;
		}
		
		if (!Recharge.Instance.use(player, GetName(), _cooldown, true, true))
		{
			return;
		}
		
		player.playSound(player.getLocation(), Sound.DIG_SNOW, 1, 1);
		
		Item item = player.getWorld().dropItem(player.getEyeLocation(), new ItemStack(Material.PRISMARINE_SHARD));
		
		item.setVelocity(player.getLocation().getDirection().multiply(_velocity));
		Manager.GetProjectile().AddThrow(item, player, this, _expireTime, true, true, true, false, false, _hitBox);
		_items.add(item);
	}
	
	@EventHandler
	public void update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}
		
		Iterator<Item> iterator = _items.iterator();
		
		while (iterator.hasNext())
		{
			Item item = iterator.next();
			
			if (!item.isValid())
			{
				iterator.remove();
				continue;
			}
			
			UtilParticle.PlayParticleToAll(ParticleType.DRIP_WATER, item.getLocation(), 0, 0, 0, 0.01F, 1, ViewDist.LONG);
		}
	}

	@Override
	public void Collide(LivingEntity target, Block block, ProjectileUser data)
	{
		if (!UtilBlock.airFoliage(block))
		{
			data.getThrown().remove();
		}
		
		if (target == null)
		{
			return;
		}
		
		CustomDamageEvent event = Manager.GetDamage().NewDamageEvent(target, data.getThrower(), null, DamageCause.CUSTOM, _damage, false, true, true, data.getThrower().getName(), GetName());
		if(event.IsCancelled())
		{
			return;
		}
		UtilAction.velocity(target, UtilAlg.getTrajectory(target, data.getThrower()).setY(0.5));
	}

	@Override
	public void Idle(ProjectileUser data)
	{
		data.getThrown().remove();
	}

	@Override
	public void Expire(ProjectileUser data)
	{
		data.getThrown().remove();
	}
	
	@Override
	public void ChunkUnload(ProjectileUser data)
	{
		data.getThrown().remove();
	}
}
