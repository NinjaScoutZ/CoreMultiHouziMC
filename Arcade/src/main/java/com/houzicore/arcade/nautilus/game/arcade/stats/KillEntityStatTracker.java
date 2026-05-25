package com.houzicore.arcade.nautilus.game.arcade.stats;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;

import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import com.houzicore.arcade.nautilus.game.arcade.game.Game;

public class KillEntityStatTracker extends StatTracker<Game>
{
	private final String _statName;
	private EntityType _entityType;

	public KillEntityStatTracker(Game game, String statName, EntityType entityType)
	{
		super(game);

		_statName = statName;
		setEntityType(entityType);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onKillingBlow(EntityDamageByEntityEvent event)
	{
		if (event.getEntity().getType() != getEntityType())
			return;

		if (!event.getEntity().isDead())
			return;
		
		if (!(event.getDamager() instanceof Player)) return;
		Player player = ((Player) event.getDamager());
		if (player == null)
			return;
		
		addStat(player, getStatName(), 1, false, false);
	}

	public String getStatName()
	{
		return _statName;
	}

	public EntityType getEntityType()
	{
		return _entityType;
	}

	public void setEntityType(EntityType entityType)
	{
		_entityType = entityType;
	}
}
