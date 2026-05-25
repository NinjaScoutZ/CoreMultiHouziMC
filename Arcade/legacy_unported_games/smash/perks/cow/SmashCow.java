package nautilus.game.arcade.game.games.smash.perks.cow;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import com.houzicore.shared.core.disguise.disguises.DisguiseCow;
import com.houzicore.shared.core.disguise.disguises.DisguiseMooshroom;
import com.houzicore.shared.core.recharge.Recharge;
import com.houzicore.shared.minecraft.game.core.damage.CustomDamageEvent;
import com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.SmashKit;
import com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.SmashUltimate;

public class SmashCow extends SmashUltimate
{

	private int _damageBuff;
	private int _health;
	
	public SmashCow()
	{
		super("Mooshroom Madness", new String[] {}, Sound.COW_IDLE, 0);
	}

	@Override
	public void setupValues()
	{
		super.setupValues();

		_damageBuff = getPerkInt("Damage Buff");
		_health = getPerkInt("Health");
	}

	@Override
	public void activate(Player player)
	{
		super.activate(player);
		
		SmashKit kit = (SmashKit) Kit;
		
		kit.disguise(player, DisguiseMooshroom.class);

		// Health
		player.setMaxHealth(_health);
		player.setHealth(_health);

		// Sound
		player.getWorld().playSound(player.getLocation(), Sound.COW_HURT, 5f, 0.25f);

		// Recharges
		Recharge.Instance.recharge(player, "Angry Herd");
		Recharge.Instance.recharge(player, "Milk Spiral");
	}
	
	@Override
	public void cancel(Player player)
	{
		super.cancel(player);
		
		SmashKit kit = (SmashKit) Kit;
		kit.disguise(player, DisguiseCow.class);
		
		player.setMaxHealth(20);
	}

	@EventHandler
	public void damageBuff(CustomDamageEvent event)
	{
		Player player = event.GetDamagerPlayer(true);
		
		if (player == null)
		{
			return;
		}
		
		if (isUsingUltimate(player))
		{
			event.AddMod(player.getName(), GetName(), _damageBuff, false);
		}
	}
}
