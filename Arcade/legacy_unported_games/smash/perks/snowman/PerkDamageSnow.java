package nautilus.game.arcade.game.games.smash.perks.snowman;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import com.houzicore.shared.minecraft.game.core.damage.CustomDamageEvent;
import com.houzicore.shared.core.common.util.C;
import com.houzicore.shared.core.common.util.UtilPlayer;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;

public class PerkDamageSnow extends Perk
{
	private int _damage;
	private double _knockback;

	public PerkDamageSnow(int damage, double knockback)
	{
		super("Snow Attack", new String[] { C.cGray + "+" + damage + " Damage and " + (int) ((knockback - 1) * 100) + "% Knockback to enemies on snow.", });

		_damage = damage;
		_knockback = knockback;
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void Knockback(CustomDamageEvent event)
	{
		if (event.GetDamageeEntity().getLocation().getBlock().getType() != Material.SNOW)
		{
			return;
		}
		
		Player damager = event.GetDamagerPlayer(true);
		
		if (damager == null)
		{
			return;
		}
		
		if (!hasPerk(damager) || UtilPlayer.isSpectator(damager))
		{
			return;
		}
		
		event.AddMod(damager.getName(), GetName(), _damage, false);
		event.AddKnockback("Knockback Snow", _knockback);
	}
}
