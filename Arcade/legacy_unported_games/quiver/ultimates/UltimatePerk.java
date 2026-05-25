package nautilus.game.arcade.game.games.quiver.ultimates;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import com.houzicore.shared.core.common.util.C;
import com.houzicore.shared.core.common.util.UtilPlayer;
import com.houzicore.shared.core.common.util.UtilServer;
import com.houzicore.shared.core.common.util.UtilTime;
import com.houzicore.shared.core.updater.UpdateType;
import com.houzicore.shared.core.updater.event.UpdateEvent;
import com.houzicore.shared.minecraft.game.core.combat.event.CombatDeathEvent;
import com.houzicore.arcade.nautilus.game.arcade.events.GameStateChangeEvent;
import com.houzicore.arcade.nautilus.game.arcade.game.Game.GameState;
import com.houzicore.arcade.nautilus.game.arcade.game.games.quiver.Quiver;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;

public abstract class UltimatePerk extends Perk
{

	private Map<UUID, Long> _lastUltimate = new HashMap<>();
	
	private long _length;
	
	private float _chargePassive;
	private float _chargePayload;
	private float _chargeKill;
	private float _chargeAssist;

	public UltimatePerk(String name, String[] perkDesc, long length, float chargePassive, float chargePayload, float chargeKill, float chargeAssist)
	{
		super(name, perkDesc);
		
		_length = length;
		_chargePassive = chargePassive;
		_chargePayload = chargePayload;
		_chargeKill = chargeKill;
		_chargeAssist = chargeAssist;
	}

	public void activate(Player player)
	{
		_lastUltimate.put(player.getUniqueId(), System.currentTimeMillis());

		player.getInventory().addItem(Quiver.SUPER_ARROW);

		ChatColor teamColor = Manager.GetGame().GetTeam(player).GetColor();
		
		UtilServer.broadcast(teamColor + C.Bold + player.getName() + C.cWhiteB + " activated their " + teamColor + C.Bold + GetName() + C.cWhiteB + ".");

		for (Player other : UtilServer.getPlayers())
		{
			other.playSound(other.getLocation(), Sound.ENDERDRAGON_GROWL, 1, 0);
		}
	}

	public void cancel(Player player)
	{		
		_lastUltimate.remove(player.getUniqueId());

		player.playSound(player.getLocation(), Sound.BLAZE_DEATH, 1, 0);
	}
	
	public boolean isUsable(Player player)
	{
		return true;
	}

	@EventHandler
	public void onUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST || _length == 0)
		{
			return;
		}
		
		for (UUID uuid : _lastUltimate.keySet())
		{
			Player player = UtilPlayer.searchExact(uuid);
			
			if (player == null)
			{
				continue;
			}
			
			if (UtilTime.elapsed(_lastUltimate.get(uuid), _length))
			{
				cancel(player);
			}
		}
	}

	@EventHandler
	public void onCombatDeath(CombatDeathEvent event)
	{
		if (!(event.GetEvent().getEntity() instanceof Player))
		{
			return;
		}

		Player player = (Player) event.GetEvent().getEntity();

		if (!isUsingUltimate(player))
		{
			return;
		}

		cancel(player);
	}
	
	@EventHandler
	public void onGameStateChange(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.End)
		{
			for (UUID uuid : _lastUltimate.keySet())
			{
				cancel(UtilPlayer.searchExact(uuid));
			}
		}
	}
	
	public boolean isUsingUltimate(Player player)
	{
		return isUsingUltimate(player.getUniqueId());
	}
	
	public boolean isUsingUltimate(UUID uuid)
	{
		return _lastUltimate.containsKey(uuid);
	}

	public Map<UUID, Long> getLastUltimate()
	{
		return _lastUltimate;
	}
	
	public long getLength()
	{
		return _length;
	}
	
	public float getChargePassive()
	{
		return _chargePassive;
	}
	
	public float getChargePayload()
	{
		return _chargePayload;
	}
	
	public float getChargeKill()
	{
		return _chargeKill;
	}
	
	public float getChargeAssist()
	{
		return _chargeAssist;
	}
}
