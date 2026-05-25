package nautilus.game.arcade.game.games.smash.perks.enderman;

import org.bukkit.Color;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;

import com.houzicore.shared.core.common.util.C;
import com.houzicore.shared.core.common.util.F;
import com.houzicore.shared.core.common.util.UtilBlock;
import com.houzicore.shared.core.common.util.UtilEvent;
import com.houzicore.shared.core.common.util.UtilEvent.ActionType;
import com.houzicore.shared.core.common.util.UtilFirework;
import com.houzicore.shared.core.common.util.UtilItem;
import com.houzicore.shared.core.common.util.UtilParticle.ParticleType;
import com.houzicore.shared.core.common.util.UtilPlayer;
import com.houzicore.shared.core.common.util.UtilServer;
import com.houzicore.shared.core.common.util.particles.effects.LineParticle;
import com.houzicore.shared.core.recharge.Recharge;
import com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.SmashPerk;

public class PerkBlink extends SmashPerk
{

	private static final float INCREMENTATION = 0.2F;
	
	private String _name;
	private double _range;
	private int _recharge;

	public PerkBlink(String name)
	{
		this(name, 0, 0);
	}

	public PerkBlink(String name, double range, int recharge)
	{
		super(name, new String[] { C.cYellow + "Right-Click" + C.cGray + " with Axe to " + C.cGreen + name });

		_name = name;
		_range = range;
		_recharge = recharge;
	}

	@Override
	public void setupValues()
	{
		_range = getPerkDouble("Range");
		_recharge = getPerkTime("Cooldown");
	}

	@EventHandler
	public void Blink(PlayerInteractEvent event)
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

		if (!Recharge.Instance.use(player, _name, _recharge, true, true))
		{
			return;
		}

		LineParticle lineParticle = new LineParticle(player.getEyeLocation(), player.getLocation().getDirection(), INCREMENTATION, _range, ParticleType.SMOKE, UtilServer.getPlayers());

		while (!lineParticle.update())
		{
		}

		// Firework
		UtilFirework.playFirework(player.getEyeLocation(), Type.BALL, Color.BLACK, false, false);

		player.playSound(player.getLocation(), Sound.ENDERMAN_TELEPORT, 1f, 1f);
		player.teleport(lineParticle.getDestination());
		player.setFallDistance(0);
		player.playSound(player.getLocation(), Sound.ENDERMAN_TELEPORT, 1f, 1f);

		// Firework
		UtilFirework.playFirework(player.getEyeLocation(), Type.BALL, Color.BLACK, false, false);

		// Inform
		UtilPlayer.message(player, F.main("Game", "You used " + F.skill(_name) + "."));
	}
}
