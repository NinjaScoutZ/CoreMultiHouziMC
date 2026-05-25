package nautilus.game.arcade.game.games.smash.perks.skeleton;

import java.util.Map;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;

import com.houzicore.shared.core.common.util.C;
import com.houzicore.shared.core.common.util.F;
import com.houzicore.shared.core.common.util.UtilBlock;
import com.houzicore.shared.core.common.util.UtilEvent;
import com.houzicore.shared.core.common.util.UtilEvent.ActionType;
import com.houzicore.shared.core.common.util.UtilItem;
import com.houzicore.shared.core.common.util.UtilPlayer;
import com.houzicore.shared.core.recharge.Recharge;
import com.houzicore.shared.minecraft.game.core.damage.CustomDamageEvent;
import com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.SmashPerk;

public class PerkBoneExplosion extends SmashPerk
{
	
	private int _cooldown;
	private int _damageRadius;
	private int _damage;
	private float _knockbackMagnitude;
	
	public PerkBoneExplosion()
	{
		super("Bone Explosion", new String[] { C.cYellow + "Right-Click" + C.cGray + " with Axe to " + C.cGreen + "Bone Explosion" });
	}

	@Override
	public void setupValues()
	{
		_cooldown = getPerkTime("Cooldown");
		_damageRadius = getPerkInt("Damage Radius");
		_damage = getPerkInt("Damage");
		_knockbackMagnitude = getPerkFloat("Knockback Magnitude");
	}

	@EventHandler
	public void Skill(PlayerInteractEvent event)
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
		
		Map<Player, Double> nearby = UtilPlayer.getInRadius(player.getLocation(), _damageRadius);
		
		for (Player other : nearby.keySet())
		{
			if (player.equals(other))
			{
				continue;
			}
			
			// Inform
			UtilPlayer.message(other, F.main("Game", F.elem(Manager.GetColor(player) + player.getName()) + " used " + F.skill(GetName()) + "."));

			// Damage Event
			Manager.GetDamage().NewDamageEvent(other, player, null, DamageCause.CUSTOM, _damage * nearby.get(other), true, true, false, player.getName(), GetName());
		}

		// Inform
		UtilPlayer.message(player, F.main("Game", "You used " + F.skill(GetName()) + "."));

		// Effect
		Manager.GetBlood().Effects(null, player.getLocation().add(0, 0.5, 0), 48, 0.8, Sound.SKELETON_HURT, 2f, 1.2f, Material.BONE, (byte) 0, 40, false);
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
