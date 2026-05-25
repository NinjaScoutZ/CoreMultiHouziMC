package com.houzicore.shared.core.combat;

import java.util.HashSet;
import java.util.Iterator;

// No NMS Imports
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.houzicore.shared.MiniPlugin;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.account.event.ClientUnloadEvent;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.NautHashMap;
import com.houzicore.shared.common.util.UtilEnt;
import com.houzicore.shared.common.util.UtilEvent;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.common.util.UtilTime;
import com.houzicore.shared.core.itemstack.ItemStackFactory;
import com.houzicore.shared.core.combat.event.ClearCombatEvent;
import com.houzicore.shared.core.combat.event.CombatDeathEvent;
import com.houzicore.shared.core.damage.CustomDamageEvent;

public class CombatManager extends MiniPlugin
{
	public enum AttackReason
	{
		Attack,
		CustomWeaponName,
		DefaultWeaponName
	}
	
	private static CombatManager _instance;

	public static CombatManager get()
	{
		return _instance;
	}

	private NautHashMap<Player, CombatLog> _active = new NautHashMap<Player, CombatLog>();
	private NautHashMap<String, ClientCombat> _combatClients = new NautHashMap<String, ClientCombat>();

	private HashSet<Player> _removeList = new HashSet<Player>();

	protected long ExpireTime = 15000;
	
	protected AttackReason _attackReason = AttackReason.CustomWeaponName;

	public CombatManager(JavaPlugin plugin)
	{
		super("Combat", plugin);
		_instance = this;
	}

	public void logKnockback(Player victim, LivingEntity attacker, String source)
	{
		if (victim == null || attacker == null || source == null)
			return;

		Get(victim).Attacked(UtilEnt.getName(attacker), 0.0, attacker, source, null);
	}

	@EventHandler
	public void UnloadDonor(ClientUnloadEvent event)
	{
		_combatClients.remove(event.GetName());
	}

	public ClientCombat Get(String name)
	{
		if (!_combatClients.containsKey(name))
			_combatClients.put(name, new ClientCombat());

		return _combatClients.get(name);
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void protectNPCs(EntityDamageEvent event)
	{
		if (event.getEntity().hasMetadata("NPC")) {
			event.setCancelled(true);
		}
	}
	
	//This is a backup, for when CustomDamageEvent is disabled (manually)
	@EventHandler(priority = EventPriority.MONITOR)
	public void AddAttack(EntityDamageEvent event)
	{
		if (event.isCancelled())
			return;
		
		if (event.getEntity() == null || !(event.getEntity() instanceof Player))
			return;
		
		Player damagee = (Player)event.getEntity();
		
		LivingEntity damagerEnt = UtilEvent.GetDamagerEntity(event, true);
		
		//Attacked by Entity
		if (damagerEnt != null)
		{
			if (damagerEnt instanceof Player)
				Get((Player)damagerEnt).SetLastCombat(System.currentTimeMillis());

			Get(damagee).Attacked(
					UtilEnt.getName(damagerEnt),
					event.getDamage(), damagerEnt,
					event.getCause() + "", null);
		}
		// Damager is WORLD
		else
		{
			DamageCause cause = event.getCause();

			String source = "?";
			String reason = "-";

			if (cause == DamageCause.BLOCK_EXPLOSION)
			{
				source = "Explosion";
				reason = "-";
			} else if (cause == DamageCause.CONTACT)
			{
				source = "Cactus";
				reason = "-";
			} else if (cause == DamageCause.CUSTOM)
			{
				source = "Custom";
				reason = "-";
			} else if (cause == DamageCause.DROWNING)
			{
				source = "Water";
				reason = "-";
			} else if (cause == DamageCause.ENTITY_ATTACK)
			{
				source = "Entity";
				reason = "Attack";
			} else if (cause == DamageCause.ENTITY_EXPLOSION)
			{
				source = "Explosion";
				reason = "-";
			} else if (cause == DamageCause.FALL)
			{
				source = "Fall";
				reason = "-";
			} else if (cause == DamageCause.FALLING_BLOCK)
			{
				source = "Falling Block";
				reason = "-";
			} else if (cause == DamageCause.FIRE)
			{
				source = "Fire";
				reason = "-";
			} else if (cause == DamageCause.FIRE_TICK)
			{
				source = "Fire";
				reason = "-";
			} else if (cause == DamageCause.LAVA)
			{
				source = "Lava";
				reason = "-";
			} else if (cause == DamageCause.LIGHTNING)
			{
				source = "Lightning";
				reason = "-";
			} else if (cause == DamageCause.MAGIC)
			{
				source = "Magic";
				reason = "-";
			} else if (cause == DamageCause.MELTING)
			{
				source = "Melting";
				reason = "-";
			} else if (cause == DamageCause.POISON)
			{
				source = "Poison";
				reason = "-";
			} else if (cause == DamageCause.PROJECTILE)
			{
				source = "Projectile";
				reason = "-";
			} else if (cause == DamageCause.STARVATION)
			{
				source = "Starvation";
				reason = "-";
			} else if (cause == DamageCause.SUFFOCATION)
			{
				source = "Suffocation";
				reason = "-";
			} else if (cause == DamageCause.SUICIDE)
			{
				source = "Suicide";
				reason = "-";
			} else if (cause == DamageCause.VOID)
			{
				source = "Void";
				reason = "-";
			} else if (cause == DamageCause.WITHER)
			{
				source = "Wither";
				reason = "-";
			}

			Get(damagee).Attacked(source,
					event.getDamage(), null, reason, null);
		}
		
	}

	public void AddAttack(CustomDamageEvent event)
	{
		// Not Player > No Log
		if (event.GetDamageePlayer() == null)
			return;

		// Damager is ENTITY
		if (event.GetDamagerEntity(true) != null)
		{
			String reason = event.GetReason();

			if (reason == null)
			{
				if (event.GetDamagerPlayer(false) != null)
				{
					Player damager = event.GetDamagerPlayer(false);

					
					reason = "Attack";
					
					if (_attackReason == AttackReason.DefaultWeaponName)
					{
						reason = "Fists";
						
						if (damager.getItemInHand() != null)
						{
							reason = ItemStackFactory.Instance.GetName(damager.getItemInHand().getType(), (byte)0, false);
						}
					}
					else if (_attackReason == AttackReason.CustomWeaponName)
					{
						reason = "Fists";

						if (damager.getItemInHand() != null && damager.getItemInHand().getType() != org.bukkit.Material.AIR)
						{
							if (damager.getItemInHand().hasItemMeta() && damager.getItemInHand().getItemMeta().hasDisplayName())
							{
								reason = damager.getItemInHand().getItemMeta().getDisplayName();
							}
							else
							{
								reason = ItemStackFactory.Instance.GetName(damager.getItemInHand().getType(), (byte)0, false);
							}
						}
					}
				} 
				else if (event.GetProjectile() != null)
				{
					if (event.GetProjectile() instanceof Arrow)
						reason = "Archery";
					else if (event.GetProjectile() instanceof Fireball)
						reason = "Fireball";
				}
			}

			if (event.GetDamagerEntity(true) instanceof Player)
				Get((Player)event.GetDamagerEntity(true)).SetLastCombat(System.currentTimeMillis());

			Get(event.GetDamageePlayer()).Attacked(
					UtilEnt.getName(event.GetDamagerEntity(true)),
					(int) event.GetDamage(), event.GetDamagerEntity(true),
					reason, event.GetDamageMod());
		}
		// Damager is WORLD
		else
		{
			DamageCause cause = event.GetCause();

			String source = "?";
			String reason = "-";
	
			if (cause == DamageCause.BLOCK_EXPLOSION)
			{
				source = "Explosion";
				reason = "-";
			} else if (cause == DamageCause.CONTACT)
			{
				source = "Cactus";
				reason = "-";
			} else if (cause == DamageCause.CUSTOM)
			{
				source = "Custom";
				reason = "-";
			} else if (cause == DamageCause.DROWNING)
			{
				source = "Water";
				reason = "-";
			} else if (cause == DamageCause.ENTITY_ATTACK)
			{
				source = "Entity";
				reason = "Attack";
			} else if (cause == DamageCause.ENTITY_EXPLOSION)
			{
				source = "Explosion";
				reason = "-";
			} else if (cause == DamageCause.FALL)
			{
				source = "Fall";
				reason = "-";
			} else if (cause == DamageCause.FALLING_BLOCK)
			{
				source = "Falling Block";
				reason = "-";
			} else if (cause == DamageCause.FIRE)
			{
				source = "Fire";
				reason = "-";
			} else if (cause == DamageCause.FIRE_TICK)
			{
				source = "Fire";
				reason = "-";
			} else if (cause == DamageCause.LAVA)
			{
				source = "Lava";
				reason = "-";
			} else if (cause == DamageCause.LIGHTNING)
			{
				source = "Lightning";
				reason = "-";
			} else if (cause == DamageCause.MAGIC)
			{
				source = "Magic";
				reason = "-";
			} else if (cause == DamageCause.MELTING)
			{
				source = "Melting";
				reason = "-";
			} else if (cause == DamageCause.POISON)
			{
				source = "Poison";
				reason = "-";
			} else if (cause == DamageCause.PROJECTILE)
			{
				source = "Projectile";
				reason = "-";
			} else if (cause == DamageCause.STARVATION)
			{
				source = "Starvation";
				reason = "-";
			} else if (cause == DamageCause.SUFFOCATION)
			{
				source = "Suffocation";
				reason = "-";
			} else if (cause == DamageCause.SUICIDE)
			{
				source = "Suicide";
				reason = "-";
			} else if (cause == DamageCause.VOID)
			{
				source = "Void";
				reason = "-";
			} else if (cause == DamageCause.WITHER)
			{
				source = "Wither";
				reason = "-";
			}
			
			if (event.GetReason() != null)
				reason = event.GetReason();
			
			Get(event.GetDamageePlayer()).Attacked(source,
					(int) event.GetDamage(), null, reason, event.GetDamageMod());
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void HandleDeath(PlayerDeathEvent event)
	{
		event.setDeathMessage(null);

		if (!_active.containsKey(event.getEntity()))
			return;

		CombatLog log = _active.remove(event.getEntity());
		log.SetDeathTime(System.currentTimeMillis());

		// Save Death
		Get(event.getEntity().getName()).GetDeaths().addFirst(log);

		// Add Kill/Assist
		int assists = 0;
		for (int i = 0; i < log.GetAttackers().size(); i++)
		{
			if (!log.GetAttackers().get(i).IsPlayer())
				continue;

			if (UtilTime.elapsed(log.GetAttackers().get(i).GetLastDamage(),
					ExpireTime))
				continue;

			if (log.GetKiller() == null)
			{
				log.SetKiller(log.GetAttackers().get(i));

				ClientCombat killerClient = Get(log.GetAttackers().get(i).GetName());

				if (killerClient != null)
					killerClient.GetKills().addFirst(log);
			}

			else
			{
				assists++;

				ClientCombat assistClient = Get(log.GetAttackers().get(i).GetName());

				if (assistClient != null)
					assistClient.GetAssists().addFirst(log);
			}
		}

		log.SetAssists(assists);

		// Event
		CombatDeathEvent deathEvent = new CombatDeathEvent(event, Get(event.getEntity().getName()), log);
		UtilServer.getServer().getPluginManager().callEvent(deathEvent);

		//XXX Death MSG
		if (deathEvent.GetBroadcastType() == DeathMessageType.Detailed || deathEvent.GetBroadcastType() == DeathMessageType.Absolute)
		{
			//Display Simple
			for (Player cur : event.getEntity().getWorld().getPlayers())
			{
				// Killed
				String killedColor = log.GetKilledColor();

				String deadPlayer = killedColor + event.getEntity().getName();

				// Killer
				if (log.GetKiller() != null)
				{
					String killerColor = log.GetKillerColor();

					String killPlayer = killerColor + log.GetKiller().GetName();

					if (log.GetAssists() > 0)
						killPlayer += " + " + log.GetAssists();

					String weapon = log.GetKiller().GetLastDamageSource();

					EntityDamageEvent lastEvent = event.getEntity().getLastDamageCause();
					DamageCause cause = lastEvent != null ? lastEvent.getCause() : null;
					String messageText;

					if (cause == DamageCause.VOID)
					{
						messageText = deadPlayer + C.cGray + " was knocked into the void by " + killPlayer + C.cGray + " with " + F.item(weapon) + ".";
					}
					else if (cause == DamageCause.LAVA)
					{
						messageText = deadPlayer + C.cGray + " was pushed into lava by " + killPlayer + C.cGray + " with " + F.item(weapon) + ".";
					}
					else if (cause == DamageCause.FALL)
					{
						messageText = deadPlayer + C.cGray + " fell to their death while fighting " + killPlayer + C.cGray + " with " + F.item(weapon) + ".";
					}
					else if (cause == DamageCause.FIRE || cause == DamageCause.FIRE_TICK)
					{
						messageText = deadPlayer + C.cGray + " was burned to a crisp while fighting " + killPlayer + C.cGray + " with " + F.item(weapon) + ".";
					}
					else
					{
						messageText = deadPlayer + C.cGray + " killed by " + killPlayer + C.cGray + " with " + F.item(weapon) + ".";
					}

					UtilPlayer.message(cur, F.main("Death", messageText));
				}
				// No Killer
				else
				{
					if (log.GetAttackers().isEmpty())
						UtilPlayer.message(cur, F.main("Death", deadPlayer
								+ C.cGray + " has died."));
					else
					{
						if (log.GetLastDamager() != null && log.GetLastDamager().GetReason() != null && log.GetLastDamager().GetReason().length() > 1)
						{
							UtilPlayer.message(
									cur,
									F.main("Death",
											deadPlayer
											+ C.cGray
											+ " killed by "
											+ F.name(log.GetLastDamager()
													.GetReason()))
													+ C.cGray + ".");
						}
						else
						{
							UtilPlayer.message(
									cur,
									F.main("Death",
											deadPlayer
											+ C.cGray
											+ " killed by "
											+ F.name(log.GetAttackers()
													.getFirst().GetName()))
													+ C.cGray + ".");
						}
					}
						
				}
			}
			
			//Self Detail
			if (deathEvent.GetBroadcastType() == DeathMessageType.Absolute)
				UtilPlayer.message(event.getEntity(), log.DisplayAbsolute());
			else
				UtilPlayer.message(event.getEntity(), log.Display());
		}	
		else if (deathEvent.GetBroadcastType() == DeathMessageType.Simple)
		{
			//Simple 
			if (log.GetKiller() != null)
			{
				//Killer
				String killerColor = log.GetKillerColor();
				String killPlayer = killerColor + log.GetKiller().GetName();
				

				// Killed
				String killedColor = log.GetKilledColor();
				String deadPlayer = killedColor + event.getEntity().getName();
				
				if (log.GetAssists() > 0)
					killPlayer += " + " + log.GetAssists();

				String weapon = log.GetKiller().GetLastDamageSource();

				Player killer = UtilPlayer.searchExact(log.GetKiller().GetName());

				EntityDamageEvent lastEvent = event.getEntity().getLastDamageCause();
				DamageCause cause = lastEvent != null ? lastEvent.getCause() : null;

				String killerMsg;
				String victimMsg;

				if (cause == DamageCause.VOID)
				{
					killerMsg = "You knocked " + F.elem(deadPlayer) + " into the void with " + F.item(weapon) + ".";
					victimMsg = killPlayer + C.cGray + " knocked you into the void with " + F.item(weapon) + ".";
				}
				else if (cause == DamageCause.LAVA)
				{
					killerMsg = "You pushed " + F.elem(deadPlayer) + " into lava with " + F.item(weapon) + ".";
					victimMsg = killPlayer + C.cGray + " pushed you into lava with " + F.item(weapon) + ".";
				}
				else if (cause == DamageCause.FALL)
				{
					killerMsg = F.elem(deadPlayer) + " fell to their death while fighting you with " + F.item(weapon) + ".";
					victimMsg = "You fell to your death while fighting " + killPlayer + C.cGray + " with " + F.item(weapon) + ".";
				}
				else if (cause == DamageCause.FIRE || cause == DamageCause.FIRE_TICK)
				{
					killerMsg = F.elem(deadPlayer) + " burned to a crisp while fighting you with " + F.item(weapon) + ".";
					victimMsg = "You burned to a crisp while fighting " + killPlayer + C.cGray + " with " + F.item(weapon) + ".";
				}
				else
				{
					killerMsg = "You killed " + F.elem(deadPlayer) + " with " + F.item(weapon) + ".";
					victimMsg = killPlayer + C.cGray + " killed you with " + F.item(weapon) + ".";
				}

				if (killer != null)
					UtilPlayer.message(killer, F.main("Death", killerMsg));
				
				UtilPlayer.message(event.getEntity(), F.main("Death", victimMsg));

			}
			else
			{
				if (log.GetAttackers().isEmpty())
				{
					UtilPlayer.message(event.getEntity(), F.main("Death", "You have died."));
				}

				else
				{
					UtilPlayer.message(event.getEntity(), F.main("Death", "You were killed by " + 
							F.name(log.GetAttackers().getFirst().GetName())) + C.cGray + ".");
				}
			}
		}			
	}

	@EventHandler
	public void ExpireOld(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;

		for (CombatLog log : _active.values())
			log.ExpireOld();
	}

	public void Add(Player player)
	{
		_active.put(player, new CombatLog(player, 15000));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void Clear(ClearCombatEvent event)
	{
		_active.remove(event.GetPlayer());
	}

	public CombatLog Get(Player player)
	{
		if (!_active.containsKey(player))
		{
			Add(player);
		}
			

		return _active.get(player);
	}

	public long GetExpireTime()
	{
		return ExpireTime;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void ClearInactives(UpdateEvent event)
	{
		if (event.getType() == UpdateType.MIN_02)
		{
			// Remove already marked inactives if still offline
			Iterator<Player> removeIterator = _removeList.iterator();

			while (removeIterator.hasNext())
			{
				Player player = removeIterator.next();

				if (!player.isOnline())
					_active.remove(player);

				removeIterator.remove();
			}

			// Mark inactives for cleanup next go around
			for (Player player : _active.keySet())
			{
				if (!player.isOnline())
					_removeList.add(player);
			}
		}
	}

	public void DebugInfo(Player player)
	{
		StringBuilder nameBuilder = new StringBuilder();

		for (Player combats : _active.keySet())
		{
			if (!combats.isOnline())
			{
				if (nameBuilder.length() != 0)
					nameBuilder.append(", ");

				nameBuilder.append(combats.getName());
			}
		}

		player.sendMessage(F.main(getName(), nameBuilder.toString()));
	}
	
	public void setUseWeaponName(AttackReason var)
	{
		_attackReason = var;
	}
	
	public AttackReason getUseWeapoName()
	{
		return _attackReason;
	}
}
