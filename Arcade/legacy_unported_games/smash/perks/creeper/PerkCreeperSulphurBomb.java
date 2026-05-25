package nautilus.game.arcade.game.games.smash.perks.creeper;

import com.houzicore.shared.core.common.util.C;
import com.houzicore.shared.core.common.util.F;
import com.houzicore.shared.core.common.util.UtilAction;
import com.houzicore.shared.core.common.util.UtilBlock;
import com.houzicore.shared.core.common.util.UtilEnt;
import com.houzicore.shared.core.common.util.UtilEvent;
import com.houzicore.shared.core.common.util.UtilEvent.ActionType;
import com.houzicore.shared.core.common.util.UtilInv;
import com.houzicore.shared.core.common.util.UtilItem;
import com.houzicore.shared.core.common.util.UtilParticle;
import com.houzicore.shared.core.common.util.UtilParticle.ParticleType;
import com.houzicore.shared.core.common.util.UtilParticle.ViewDist;
import com.houzicore.shared.core.common.util.UtilPlayer;
import com.houzicore.shared.core.common.util.UtilServer;
import com.houzicore.shared.core.itemstack.ItemStackFactory;
import com.houzicore.shared.core.projectile.IThrown;
import com.houzicore.shared.core.projectile.ProjectileUser;
import com.houzicore.shared.core.recharge.Recharge;
import com.houzicore.shared.core.updater.UpdateType;
import com.houzicore.shared.minecraft.game.core.damage.CustomDamageEvent;
import com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.SmashPerk;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;

public class PerkCreeperSulphurBomb extends SmashPerk
{
	private int _cooldown;
	private float _damage;
	private float _knockbackMagnitude;
	private float _hitBox;

	private final IThrown _sulphurThrown = new IThrown()
	{
		@Override
		public void Collide(LivingEntity target, Block block, ProjectileUser data)
		{
			Explode(data);

			if (target == null)
			{
				return;
			}

			// Damage Event
			Manager.GetDamage().NewDamageEvent(target, data.getThrower(), null, DamageCause.PROJECTILE, _damage, true, true, false, UtilEnt.getName(data.getThrower()), GetName());
		}

		@Override
		public void Idle(ProjectileUser data)
		{
			Explode(data);
		}

		@Override
		public void Expire(ProjectileUser data)
		{
			Explode(data);
		}

		@Override
		public void ChunkUnload(ProjectileUser data)
		{
			data.getThrown().remove();
		}

		private void Explode(ProjectileUser data)
		{
			UtilParticle.PlayParticle(ParticleType.LARGE_EXPLODE, data.getThrown().getLocation(), 0, 0, 0, 0, 1, ViewDist.MAX, UtilServer.getPlayers());
			data.getThrown().getWorld().playSound(data.getThrown().getLocation(), Sound.EXPLODE, 1f, 1.5f);
			data.getThrown().remove();
		}
	};

	public PerkCreeperSulphurBomb()
	{
		super("Sulphur Bomb", new String[] { C.cYellow + "Right-Click" + C.cGray + " with Axe to use " + C.cGreen + "Sulphur Bomb" });
	}

	@Override
	public void setupValues()
	{
		_cooldown = getPerkTime("Cooldown");
		_damage = getPerkFloat("Damage");
		_knockbackMagnitude = getPerkFloat("Knockback Magnitude");
		_hitBox = getPerkFloat("Hit Box");
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

		if (!UtilItem.isAxe(player.getItemInHand()))
		{
			return;
		}

		if (!hasPerk(player))
		{
			return;
		}
		
		if (isSuperActive(player))
		{
			return;
		}

		if (!Recharge.Instance.use(player, GetName(), _cooldown, true, true))
		{
			return;
		}
		
		event.setCancelled(true);

		UtilInv.Update(player);

		Item ent = player.getWorld().dropItem(player.getEyeLocation(), ItemStackFactory.Instance.CreateStack(Material.COAL, (byte) 0));

		UtilAction.velocity(ent, player.getLocation().getDirection(), 1.2, false, 0, 0.2, 10, false);
		Manager.GetProjectile().AddThrow(ent, player, _sulphurThrown, -1, true, true, true, true, null, 1f, 1f, null, 1, UpdateType.SLOW, _hitBox);

		// Inform
		UtilPlayer.message(player, F.main("Game", "You used " + F.skill(GetName()) + "."));

		// Effect
		player.getWorld().playSound(player.getLocation(), Sound.CREEPER_DEATH, 2f, 1.5f);
	}

	@EventHandler
	public void Knockback(CustomDamageEvent event)
	{
		if (event.GetReason() == null || !event.GetReason().contains(GetName()))
		{
			return;
		}
		
		event.AddKnockback(GetName(), _knockbackMagnitude);
	}
}
