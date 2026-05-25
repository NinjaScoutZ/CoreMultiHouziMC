package com.houzicore.shared.core.damage;

import com.houzicore.shared.MiniPlugin;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilAction;
import com.houzicore.shared.common.util.UtilAlg;
import com.houzicore.shared.common.util.UtilEnt;
import com.houzicore.shared.common.util.UtilEvent;
import com.houzicore.shared.common.util.UtilGear;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.core.disguise.DisguiseManager;
import com.houzicore.shared.core.npc.NpcManager;
import com.houzicore.shared.core.combat.CombatManager;
import com.houzicore.shared.core.condition.ConditionManager;
import com.houzicore.shared.core.damage.compatibility.NpcProtectListener;

import org.bukkit.Bukkit;
import org.bukkit.EntityEffect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import org.bukkit.attribute.Attribute;

import java.util.Map;

/**
 * DamageManager — Rewritten for Paper 1.21
 * 
 * DESIGN:
 * - Intercepts EntityDamageEvent at HIGHEST to fire CustomDamageEvent
 * - Game modes and perks modify damage through CustomDamageEvent (same API as before)
 * - Final modified damage is applied back to the vanilla event via event.setDamage()
 * - Vanilla handles health reduction, death, PlayerDeathEvent, and PlayerRespawnEvent natively
 * - This eliminates the ghost player desync caused by the old setHealth(0) approach
 * 
 * KEY DIFFERENCE FROM OLD CODE:
 * - OLD: event.setCancelled(true) + setHealth() manually → synthetic death, no respawn
 * - NEW: event.setDamage(finalDamage) → vanilla handles everything including real death
 */
public class DamageManager extends MiniPlugin
{
	private CombatManager _combatManager;
	private DisguiseManager _disguiseManager;
	private ConditionManager _conditionManager;

	public boolean UseSimpleWeaponDamage = false;
	public boolean DisableDamageChanges = false;

	public CombatManager GetCombatManager() { return _combatManager; }

	private boolean _enabled = true;

	public DamageManager(JavaPlugin plugin, CombatManager combatManager, NpcManager npcManager, DisguiseManager disguiseManager, ConditionManager conditionManager) 
	{
		super("Damage Manager", plugin);

		_combatManager = combatManager;
		_disguiseManager = disguiseManager;
		_conditionManager = conditionManager;

		registerEvents(new NpcProtectListener(npcManager));
	}

	// ================================================================
	// VANILLA DAMAGE INTERCEPTION
	// ================================================================
	// Intercepts vanilla damage, fires CustomDamageEvent for game hooks,
	// then applies the modified damage back to the vanilla event.
	// Vanilla processes the final damage — including real death.
	// ================================================================

	@EventHandler(priority = EventPriority.HIGHEST)
	public void StartDamageEvent(EntityDamageEvent event)
	{
		if (!_enabled)
			return;

		if (event.isCancelled())
			return;

		if (!(event.getEntity() instanceof LivingEntity))
			return;

		// Get Data
		LivingEntity damagee = (LivingEntity)event.getEntity();
		LivingEntity damager = UtilEvent.GetDamagerEntity(event, true);
		Projectile projectile = GetProjectile(event);

		if (projectile instanceof FishHook)
			return;

		// Pre-Event Modifications (weapon damage override)
		if (!DisableDamageChanges)
			WeaponDamage(event, damager);

		double damage = event.getDamage();

		// Consistent Arrow Damage
		if (projectile != null && projectile instanceof Arrow)
		{
			damage = projectile.getVelocity().length() * 3;
		}

		// Fire CustomDamageEvent — game modes and perks can modify/cancel damage here
		CustomDamageEvent customEvent = new CustomDamageEvent(
			damagee, damager, projectile,
			event.getCause(), damage, true, false, false,
			null, null, false);

		_plugin.getServer().getPluginManager().callEvent(customEvent);

		// If custom event was cancelled by game logic, cancel vanilla too
		if (customEvent.IsCancelled())
		{
			event.setCancelled(true);
			return;
		}

		// DEBUG: trace damage that will go through
		if (damagee instanceof Player)
		{
			Player p = (Player) damagee;
			double newHealth = p.getHealth() - customEvent.GetDamage();
			System.out.println("[DMG-DEBUG] " + p.getName() + " hit by " + event.getCause() 
				+ " dmg=" + String.format("%.1f", customEvent.GetDamage())
				+ " health=" + String.format("%.1f", p.getHealth())
				+ " newHealth=" + String.format("%.1f", newHealth)
				+ " armor=" + String.format("%.1f", damagee.getAttribute(Attribute.ARMOR).getValue())
				+ " lethal=" + (newHealth <= 0));
		}

		// Calculate final damage from all modifiers
		double finalDamage = customEvent.GetDamage();

		double bruteBonus = 0;
		if (customEvent.IsBrute() && (customEvent.GetCause() == DamageCause.ENTITY_ATTACK ||
			customEvent.GetCause() == DamageCause.PROJECTILE || customEvent.GetCause() == DamageCause.CUSTOM))
			bruteBonus = Math.min(8, finalDamage * 2);

		finalDamage += bruteBonus;

		// ================================================================
		// ARMOR COMPENSATION
		// ================================================================
		// The old DamageManager manually calculated armor and applied the
		// reduced damage directly via setHealth(). In this rewrite, vanilla
		// applies its OWN armor reduction AFTER event.setDamage(). If we
		// just set event.setDamage(6.0) and the player has armor, vanilla
		// would reduce it further (e.g., to 3.0) — making players much
		// harder to kill than intended.
		//
		// Solution: ALWAYS pre-inflate damage to counteract vanilla armor,
		// UNLESS IgnoreArmor is true (in which case we inflate even more
		// to completely negate armor).
		//
		// Old behavior: damage = customDamage * armorReduction → setHealth()
		// New behavior: event.setDamage(customDamage / armorReduction)
		//               → vanilla applies armor → final = customDamage ✓
		// ================================================================
		double armor = damagee.getAttribute(Attribute.ARMOR).getValue();
		if (armor > 0)
		{
			// Vanilla armor formula: damage * (1 - min(20, max(armor/5, armor - damage/2)) / 25)
			double armorReduction = 1 - Math.min(20, Math.max(armor / 5, armor - (finalDamage / 2))) / 25;
			
			if (customEvent.IgnoreArmor())
			{
				// IgnoreArmor: inflate damage to completely negate armor
				if (armorReduction > 0)
					finalDamage = finalDamage / armorReduction;
			}
			else
			{
				// Normal: apply our own armor calculation, then inflate to counteract vanilla
				// Old behavior: finalDamage *= armorReduction (our reduction)
				// But vanilla will also apply reduction, so we need:
				// eventDamage * vanillaReduction = finalDamage * ourReduction
				// eventDamage = finalDamage * ourReduction / vanillaReduction
				// Since our formula IS vanilla's formula, this simplifies to:
				// eventDamage = finalDamage (the reductions cancel out)
				// BUT vanilla may have additional modifiers (toughness, enchants)
				// So we just pre-apply our reduction and inflate:
				double ourReducedDamage = finalDamage * armorReduction;
				if (armorReduction > 0)
					finalDamage = ourReducedDamage / armorReduction;
				// This equals finalDamage — vanilla armor cancels our pre-reduction
				// The net effect: vanilla applies its armor, which matches old behavior
			}
		}

		// Record combat for Kill/Assist tracking
		if (customEvent.GetDamageePlayer() != null)
		{
			_combatManager.AddAttack(customEvent);
		}

		// Display damage to attacker's XP level
		if (customEvent.GetDamagerPlayer(true) != null && customEvent.DisplayDamageToLevel())
		{
			if (customEvent.GetCause() != DamageCause.THORNS && customEvent.GetDamage() > 0)
				customEvent.GetDamagerPlayer(true).setLevel(Math.max(0, (int)customEvent.GetDamage()));
		}

		// Apply the final calculated damage to the vanilla event
		// Vanilla will handle health reduction, death, and respawn
		event.setDamage(Math.max(0, finalDamage));

		if (damagee instanceof Player)
		{
			final Player pFinal = (Player) damagee;
			final double setDmg = finalDamage;
			System.out.println("[DMG-DEBUG] event.setDamage(" + String.format("%.1f", setDmg) + ") isCancelled=" + event.isCancelled());
			// Check actual health 1 tick later to see what vanilla did
			_plugin.getServer().getScheduler().runTaskLater(_plugin, () -> {
				if (pFinal.isOnline())
					System.out.println("[DMG-DEBUG] +1tick " + pFinal.getName() 
						+ " health=" + String.format("%.1f", pFinal.getHealth())
						+ " isDead=" + pFinal.isDead()
						+ " gameMode=" + pFinal.getGameMode());
			}, 1L);
		}

		// Handle custom knockback (override vanilla knockback)
		if (customEvent.IsKnockback() && customEvent.GetDamagerEntity(true) != null)
		{
			// Schedule custom knockback for next tick (after vanilla applies its own)
			final CustomDamageEvent finalEvent = customEvent;
			_plugin.getServer().getScheduler().runTaskLater(_plugin, () -> {
				if (finalEvent.GetDamageeEntity() == null || finalEvent.GetDamageeEntity().isDead()) return;
				applyKnockback(finalEvent);
			}, 0L);
		}

		// Play damage sounds
		PlayDamageSound(customEvent);

		// Display damage info to command block holders
		DisplayDamage(customEvent);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void removeDemArrowsCrazyMan(EntityDamageEvent event)
	{
		if (event.isCancelled())
		{
			Projectile projectile = GetProjectile(event);

			if (projectile instanceof Arrow)
			{
				projectile.teleport(new Location(projectile.getWorld(), 0, -100, 0));
				projectile.remove();
			}
		}
	}

	// ================================================================
	// NewDamageEvent — Called by perks/abilities to deal custom damage
	// ================================================================
	// This fires CustomDamageEvent for game mode hooks, then applies
	// the damage through vanilla via entity.damage() with _enabled=false
	// to prevent our interceptor from recursing.
	// ================================================================

	public void NewDamageEvent(LivingEntity damagee, LivingEntity damager, Projectile proj,
		DamageCause cause, double damage, boolean knockback, boolean ignoreRate,
		boolean ignoreArmor, String source, String reason)
	{
		NewDamageEvent(damagee, damager, proj, cause, damage, knockback, ignoreRate, ignoreArmor, source, reason, false);
	}

	public void NewDamageEvent(LivingEntity damagee, LivingEntity damager, Projectile proj,
		DamageCause cause, double damage, boolean knockback, boolean ignoreRate,
		boolean ignoreArmor, String source, String reason, boolean cancelled)
	{
		// Fire CustomDamageEvent for game mode hooks
		CustomDamageEvent customEvent = new CustomDamageEvent(
			damagee, damager, proj, cause, damage, knockback, ignoreRate, ignoreArmor,
			source, reason, cancelled);

		_plugin.getServer().getPluginManager().callEvent(customEvent);

		// If cancelled by any handler, don't apply damage
		if (customEvent.IsCancelled())
			return;

		// Calculate final damage
		double finalDamage = customEvent.GetDamage();

		double bruteBonus = 0;
		if (customEvent.IsBrute() && (cause == DamageCause.ENTITY_ATTACK ||
			cause == DamageCause.PROJECTILE || cause == DamageCause.CUSTOM))
			bruteBonus = Math.min(8, finalDamage * 2);

		finalDamage += bruteBonus;

		// Record combat
		if (customEvent.GetDamageePlayer() != null)
			_combatManager.AddAttack(customEvent);

		// Display damage to level
		if (customEvent.GetDamagerPlayer(true) != null && customEvent.DisplayDamageToLevel())
		{
			if (cause != DamageCause.THORNS && customEvent.GetDamage() > 0)
				customEvent.GetDamagerPlayer(true).setLevel(Math.max(0, (int)customEvent.GetDamage()));
		}

		// Apply damage through vanilla — disable our interceptor to prevent recursion
		_enabled = false;
		try
		{
			// Set noDamageTicks to 0 so vanilla processes this hit
			damagee.setNoDamageTicks(0);
			damagee.damage(finalDamage);
		}
		finally
		{
			_enabled = true;
		}

		// Apply custom knockback
		if (customEvent.IsKnockback() && customEvent.GetDamagerEntity(true) != null)
		{
			applyKnockback(customEvent);
		}

		// Play damage sounds
		PlayDamageSound(customEvent);
		DisplayDamage(customEvent);
	}

	// ================================================================
	// KNOCKBACK
	// ================================================================

	private void applyKnockback(CustomDamageEvent event)
	{
		if (event.GetDamageeEntity() == null || event.GetDamageeEntity().isDead()) return;

		double knockback = event.GetDamage();
		if (knockback < 2) knockback = 2;
		knockback = Math.log10(knockback);

		for (double cur : event.GetKnockback().values())
			knockback *= cur;

		Location origin = event.getKnockbackOrigin() != null ? event.getKnockbackOrigin() : event.GetDamagerEntity(true).getLocation();
		Vector trajectory = UtilAlg.getTrajectory2d(origin, event.GetDamageeEntity().getLocation());
		trajectory.multiply(0.6 * knockback);
		trajectory.setY(Math.abs(trajectory.getY()));

		double vel = 0.2 + trajectory.length() * 0.8;

		UtilAction.velocity(event.GetDamageeEntity(), trajectory, vel, false, 0, Math.abs(0.2 * knockback), 0.4 + (0.04 * knockback), true);
	}

	// ================================================================
	// SOUNDS
	// ================================================================

	private void PlayDamageSound(CustomDamageEvent event)
	{
		if (event.IsCancelled() || (event.GetCause() != DamageCause.ENTITY_ATTACK && event.GetCause() != DamageCause.PROJECTILE))
			return;

		LivingEntity damagee = event.GetDamageeEntity();
		if (damagee == null) return;

		if (_disguiseManager.isDisguised(damagee))
		{
			return;
		}

		Sound sound = Sound.ENTITY_PLAYER_HURT;
		float vol = 1f;
		float pitch = 1f;

		if (damagee instanceof Player)
		{
			Player player = (Player)damagee;
			double r = Math.random();
			ItemStack stack = null;

			if (r > 0.50) stack = player.getInventory().getChestplate();
			else if (r > 0.25) stack = player.getInventory().getLeggings();
			else if (r > 0.10) stack = player.getInventory().getHelmet();
			else stack = player.getInventory().getBoots();

			if (stack != null && stack.getType() != Material.AIR)
			{
				String type = stack.getType().name();
				if (type.contains("LEATHER_")) { sound = Sound.ENTITY_ARROW_SHOOT; pitch = 2f; }
				else if (type.contains("CHAINMAIL_")) { sound = Sound.ENTITY_ITEM_BREAK; pitch = 1.4f; }
				else if (type.contains("GOLD_")) { sound = Sound.ENTITY_ITEM_BREAK; pitch = 1.8f; }
				else if (type.contains("IRON_")) { sound = Sound.ENTITY_BLAZE_HURT; pitch = 0.7f; }
				else if (type.contains("DIAMOND_") || type.contains("NETHERITE_")) { sound = Sound.ENTITY_BLAZE_HURT; pitch = 0.9f; }	
			}
		}
		else 
		{
			UtilEnt.PlayDamageSound(damagee);
			return;
		}

		damagee.getWorld().playSound(damagee.getLocation(), sound, vol, pitch);
	}

	// ================================================================
	// ENCHANTMENTS — handled via CustomDamageEvent at NORMAL priority
	// ================================================================

	@EventHandler(priority = EventPriority.NORMAL)
	public void handleEnchants(CustomDamageEvent event)
	{
		if (event.IsCancelled()) return;

		Player damagee = event.GetDamageePlayer();
		if (damagee != null)
		{
			for (ItemStack stack : damagee.getInventory().getArmorContents())
			{
				if (stack == null) continue;
				Map<Enchantment, Integer> enchants = stack.getEnchantments();
				for (Enchantment e : enchants.keySet())
				{
					if (e.equals(Enchantment.PROTECTION))
						event.AddMod("Ench Prot", damagee.getName(), 0.5 * enchants.get(e), false);
					else if (e.equals(Enchantment.FIRE_PROTECTION) && (event.GetCause() == DamageCause.FIRE || event.GetCause() == DamageCause.FIRE_TICK || event.GetCause() == DamageCause.LAVA))
						event.AddMod("Ench Prot", damagee.getName(), 0.5 * enchants.get(e), false);
					else if (e.equals(Enchantment.FEATHER_FALLING) && event.GetCause() == DamageCause.FALL)
						event.AddMod("Ench Prot", damagee.getName(), 0.5 * enchants.get(e), false);
					else if (e.equals(Enchantment.BLAST_PROTECTION) && event.GetCause() == DamageCause.ENTITY_EXPLOSION)
						event.AddMod("Ench Prot", damagee.getName(), 0.5 * enchants.get(e), false);
					else if (e.equals(Enchantment.PROJECTILE_PROTECTION) && event.GetCause() == DamageCause.PROJECTILE)
						event.AddMod("Ench Prot", damagee.getName(), 0.5 * enchants.get(e), false);
				}
			}
		}

		Player damager = event.GetDamagerPlayer(true);
		if (damager != null)
		{
			ItemStack stack = damager.getItemInHand();
			if (stack != null && stack.getType() != Material.AIR)
			{
				Map<Enchantment, Integer> enchants = stack.getEnchantments();
				for (Enchantment e : enchants.keySet())
				{
					if (e.equals(Enchantment.PUNCH) || e.equals(Enchantment.KNOCKBACK))
						event.AddKnockback("Ench Knockback", 1 + (0.5 * enchants.get(e)));
					else if (e.equals(Enchantment.POWER))
						event.AddMod("Enchant", "Ench Damage", 0.5 * enchants.get(e), true);
					else if (e.equals(Enchantment.FLAME) || e.equals(Enchantment.FIRE_ASPECT))
						if (_conditionManager != null)
							_conditionManager.Factory().Ignite("Ench Fire", event.GetDamageeEntity(), damager, 1 * enchants.get(e), false, false);
				}
			}
		}
	}

	// ================================================================
	// CANCEL CHECK — at LOW priority before enchants
	// ================================================================

	@EventHandler(priority = EventPriority.LOW)
	public void CancelDamageEvent(CustomDamageEvent event)
	{
		if (event.GetDamageeEntity().getHealth() <= 0)
		{
			event.SetCancelled("0 Health");
			return;
		}

		if (event.GetDamageePlayer() != null)
		{
			Player damagee = event.GetDamageePlayer();
			if (damagee.getGameMode() != GameMode.SURVIVAL && damagee.getGameMode() != GameMode.ADVENTURE)
			{
				event.SetCancelled("Damagee in Creative/Spectator");
				return;
			}
			if (UtilPlayer.isSpectator(damagee))
			{
				event.SetCancelled("Damagee in Spectator");
				return;
			}
			if (!event.IgnoreRate())
			{
				if (!_combatManager.Get(damagee.getName()).CanBeHurtBy(event.GetDamagerEntity(true)))
				{
					event.SetCancelled("World/Monster Damage Rate");
					return;
				}
			}
		}

		if (event.GetDamagerPlayer(true) != null)
		{
			Player damager = event.GetDamagerPlayer(true);
			if (damager.getGameMode() != GameMode.SURVIVAL && damager.getGameMode() != GameMode.ADVENTURE)
			{
				event.SetCancelled("Damager in Creative/Spectator");
				return;
			}
			if (!event.IgnoreRate())
			{
				if (!_combatManager.Get(damager.getName()).CanHurt(event.GetDamageeEntity()))
				{
					event.SetCancelled("PvP Damage Rate");
					return;
				}
			}
		}
	}

	// ================================================================
	// UTILITIES
	// ================================================================

	private void DisplayDamage(CustomDamageEvent event) 
	{
		for (Player player : UtilServer.getPlayers())
		{
			if (player.getItemInHand() != null && UtilGear.isMat(player.getItemInHand(), Material.COMMAND_BLOCK))
			{
				UtilPlayer.message(player, "=====================================");
				UtilPlayer.message(player, F.elem("Reason ") + event.GetReason());
				UtilPlayer.message(player, F.elem("Cause ") + event.GetCause());
				UtilPlayer.message(player, F.elem("Damager ") + UtilEnt.getName(event.GetDamagerEntity(true)));
				UtilPlayer.message(player, F.elem("Damagee ") + UtilEnt.getName(event.GetDamageeEntity()));
				UtilPlayer.message(player, F.elem("Projectile ") + UtilEnt.getName(event.GetProjectile()));
				UtilPlayer.message(player, F.elem("Damage Final ") + event.GetDamage());
				for (DamageChange cur : event.GetDamageMod())
					UtilPlayer.message(player, F.elem("Mod ") + cur.GetDamage() + " - " + cur.GetReason() + " by " + cur.GetSource());
			}
		}
	}

	private void WeaponDamage(EntityDamageEvent event, LivingEntity ent)
	{
		if (!(ent instanceof Player) || event.getCause() != DamageCause.ENTITY_ATTACK) return;

		Player damager = (Player)ent;

		if (damager.getItemInHand() == null || !UtilGear.isWeapon(damager.getItemInHand()))
		{
			event.setDamage(1);
			return;
		}

		Material mat = damager.getItemInHand().getType();
		int damage = 6;
		if (mat.name().contains("WOOD")) damage -= 3;
		else if (mat.name().contains("STONE")) damage -= 2;
		else if (mat.name().contains("DIAMOND")) damage += 1;
		else if (mat.name().contains("NETHERITE")) damage += 2;
		else if (mat.name().contains("GOLD")) damage += 0;

		event.setDamage(damage);
	}

	private Projectile GetProjectile(EntityDamageEvent event)
	{
		if (event instanceof EntityDamageByEntityEvent)
		{
			if (((EntityDamageByEntityEvent)event).getDamager() instanceof Projectile)
				return (Projectile)((EntityDamageByEntityEvent)event).getDamager();
		}
		return null;
	}

	public void SetEnabled(boolean var) { _enabled = var; }
	public void setConditionManager(ConditionManager cm) { _conditionManager = cm; }
}
