package nautilus.game.arcade.game.games.smash.perks.golem;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Effect;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import com.houzicore.shared.core.common.util.C;
import com.houzicore.shared.core.common.util.F;
import com.houzicore.shared.core.common.util.UtilAction;
import com.houzicore.shared.core.common.util.UtilBlock;
import com.houzicore.shared.core.common.util.UtilEnt;
import com.houzicore.shared.core.common.util.UtilEvent;
import com.houzicore.shared.core.common.util.UtilEvent.ActionType;
import com.houzicore.shared.core.common.util.UtilItem;
import com.houzicore.shared.core.common.util.UtilItem.ItemCategory;
import com.houzicore.shared.core.common.util.UtilPlayer;
import com.houzicore.shared.core.common.util.UtilTime;
import com.houzicore.shared.core.recharge.Recharge;
import com.houzicore.shared.core.updater.UpdateType;
import com.houzicore.shared.core.updater.event.UpdateEvent;
import com.houzicore.shared.minecraft.game.core.damage.CustomDamageEvent;

import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;

public class PerkSeismicSlam extends Perk
{

	private long _cooldown;
	private long _time;
	private int _damage;
	private int _radius;
	private float _knockbackMagnitude;

	private final ItemCategory _itemCategory;
	private final Map<LivingEntity, Long> _live = new HashMap<>();

	public PerkSeismicSlam()
	{
		this("Seismic Slam", 0, 0, 0, 0, 0, ItemCategory.SHOVEL);
	}

	public PerkSeismicSlam(String name, long cooldown, long time, int damage, int radius, float knockbackMagnitude, ItemCategory itemCategory)
	{
		super(name, new String[]
				{
						C.cYellow + "Right-Click" + C.cGray + " with Spade to " + C.cGreen + "Seismic Slam"
				});

		_cooldown = cooldown;
		_time = time;
		_damage = damage;
		_radius = radius;
		_knockbackMagnitude = knockbackMagnitude;
		_itemCategory = itemCategory;
	}

	@Override
	public void setupValues()
	{
		_cooldown = getPerkTime("Cooldown");
		_time = getPerkTime("Time");
		_damage = getPerkInt("Damage");
		_radius = getPerkInt("Radius");
		_knockbackMagnitude = getPerkFloat("Knockback Magnitude");
	}

	@EventHandler
	public void leap(PlayerInteractEvent event)
	{
		if (!UtilEvent.isAction(event, ActionType.R) || UtilBlock.usable(event.getClickedBlock()))
		{
			return;
		}

		Player player = event.getPlayer();

		if (!UtilItem.is(player.getItemInHand(), _itemCategory) || !hasPerk(player) || !Recharge.Instance.use(player, GetName(), _cooldown, true, true))
		{
			return;
		}

		// Action
		Vector vec = player.getLocation().getDirection();
		if (vec.getY() < 0)
		{
			vec.setY(vec.getY() * -1);
		}

		UtilAction.velocity(player, vec, 1, true, 1, 0, 1, true);

		// Record
		_live.put(player, System.currentTimeMillis());

		// Inform
		UtilPlayer.message(player, F.main("Game", "You used " + F.skill(GetName()) + "."));
	}

	@EventHandler
	public void slam(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		for (Player player : Manager.GetGame().GetPlayers(true))
		{
			if (!UtilEnt.onBlock(player) || !_live.containsKey(player) || !UtilTime.elapsed(_live.get(player), _time))
			{
				continue;
			}

			_live.remove(player);

			// Action

			Map<LivingEntity, Double> targets = UtilEnt.getInRadius(player.getLocation(), _radius);

			for (LivingEntity cur : targets.keySet())
			{
				if (cur.equals(player))
				{
					continue;
				}

				// Inform
				if (cur instanceof Player)
				{
					if (!Manager.canHurt(player, (Player) cur))
					{
						return;
					}

					UtilPlayer.message(cur, F.main(Manager.getName(), F.name(player.getName()) + " hit you with " + F.skill(GetName()) + "."));
				}

				// Damage Event
				Manager.GetDamage().NewDamageEvent(cur, player, null, DamageCause.CUSTOM, _damage * targets.get(cur) + 0.5, true, true, false, player.getName(), GetName());

				// Condition
				Manager.GetCondition().Factory().Falling(GetName(), cur, player, 10, false, true);
			}

			// Effect
			player.getWorld().playSound(player.getLocation(), Sound.ZOMBIE_WOOD, 2f, 0.2f);

			for (Block cur : UtilBlock.getInRadius(player.getLocation(), 4).keySet())
			{
				if (Math.random() < 0.9 || UtilBlock.airFoliage(cur) || !UtilBlock.airFoliage(cur.getRelative(BlockFace.UP)))
				{
					continue;
				}

				cur.getWorld().playEffect(cur.getLocation(), Effect.STEP_SOUND, cur.getType());
			}
		}
	}

	@EventHandler
	public void knockback(CustomDamageEvent event)
	{
		if (event.GetReason() == null || !event.GetReason().contains(GetName()))
		{
			return;
		}

		event.AddKnockback(GetName(), _knockbackMagnitude);
	}

	@EventHandler
	public void deactivateDeath(PlayerDeathEvent event)
	{
		_live.remove(event.getEntity());
	}
}
