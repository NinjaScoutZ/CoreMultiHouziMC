package com.houzicore.arcade.nautilus.game.arcade.game.games.sneakyassassins;

import com.houzicore.arcade.nautilus.game.arcade.game.modules.CompassModule;

import com.houzicore.shared.common.util.*;
import com.houzicore.shared.common.util.UtilParticle.ParticleType;
import com.houzicore.shared.common.util.UtilParticle.ViewDist;
import com.houzicore.shared.core.disguise.*;
import com.houzicore.shared.recharge.Recharge;
import com.houzicore.shared.updater.*;
import com.houzicore.shared.updater.event.*;
//import com.houzicore.shared.combat.event.*;
//import com.houzicore.shared.damage.*;
import com.houzicore.arcade.*;
import com.houzicore.arcade.nautilus.game.arcade.events.*;
import com.houzicore.arcade.nautilus.game.arcade.game.*;
import com.houzicore.arcade.nautilus.game.arcade.game.games.sneakyassassins.kits.*;
import com.houzicore.arcade.nautilus.game.arcade.game.games.sneakyassassins.npc.*;
import com.houzicore.arcade.nautilus.game.arcade.game.games.sneakyassassins.powerups.*;
import com.houzicore.arcade.nautilus.game.arcade.kit.*;
import com.houzicore.arcade.nautilus.game.arcade.stats.KillEntityStatTracker;
import com.houzicore.arcade.nautilus.game.arcade.stats.MasterAssassinStatTracker;
import com.houzicore.arcade.nautilus.game.arcade.stats.RevealStatTracker;
import com.houzicore.arcade.nautilus.game.arcade.stats.TheMastersMasterStatTracker;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.potion.*;

import java.util.*;

public class SneakyAssassins extends SoloGame
{
	private static final Map<Material, Double> SWORD_DAMAGE = new EnumMap<>(Material.class);

	static
	{
		SWORD_DAMAGE.put(Material.WOODEN_SWORD, 4.0);
		SWORD_DAMAGE.put(Material.STONE_SWORD, 4.5);
		SWORD_DAMAGE.put(Material.GOLDEN_SWORD, 5.0);
		SWORD_DAMAGE.put(Material.IRON_SWORD, 6.0);
		SWORD_DAMAGE.put(Material.DIAMOND_SWORD, 7.0);
	}

	private NpcManager _npcManager;
	private PowerUpManager _powerUpManager;
	private EntityType _disguiseType = EntityType.VILLAGER;
	private int _revealCountdown = 60;
	private KillEntityStatTracker _killEntityStatTracker;

	public SneakyAssassins(ArcadeManager manager)
	{
		super(
				manager,
				GameType.SneakyAssassins,
				new Kit[]{
						new KitEscapeArtist(manager, EntityType.VILLAGER),
						new KitAssassin(manager, EntityType.VILLAGER),
						new KitRevealer(manager, EntityType.VILLAGER),
						new KitBriber(manager, EntityType.VILLAGER),
				},
				// EN
				new String[]
				{
						"Each kill grants you +1 Heart",
						"Powerups upgrade Armor/Weapon",
						"Get 5 Powerups to become a Master!",
						"Master has a Compass to find others",
						"Last player alive wins!"
				}, 
				// TH
				new String[]
				{
						"[TH] Each kill grants you +1 Heart",
						"[TH] Powerups upgrade Armor/Weapon",
						"[TH] Get 5 Powerups to become a Master!",
						"[TH] Master has a Compass to find others",
						"[TH] Last player alive wins!"
				});

		this._npcManager = new NpcManager(this, UtilMath.random);

		this.StrictAntiHack = true;
		
		this.DamageTeamSelf = true;
		this.PrepareFreeze = false;
		
		this.HungerSet = 20;
		
		registerModule(new CompassModule(this).setGiveItem(false));
		


		_killEntityStatTracker = new KillEntityStatTracker(this, "Incompetence", _npcManager.getDisguiseType());

		registerStatTrackers(
				new MasterAssassinStatTracker(this),
				new TheMastersMasterStatTracker(this),
				new RevealStatTracker(this, "ISeeYou"),
				_killEntityStatTracker
		);
	}

	@Override
	public void ParseData()
	{
		Collections.shuffle(GetTeamList().get(0).GetSpawns());

		_powerUpManager = new PowerUpManager(this, UtilMath.random, WorldData.GetDataLocs("RED"));

		String disguiseTypeName = WorldData.get("DISGUISE_TYPE");
		if (disguiseTypeName != null)
			_disguiseType = EntityType.valueOf(disguiseTypeName.toUpperCase());

		_npcManager.setDisguiseType(_disguiseType);
		_killEntityStatTracker.setEntityType(_disguiseType);

		for (Kit kit : GetKits())
			kit.setEntityType(_disguiseType);
	}

	public PowerUpManager getPowerUpManager()
	{
		return _powerUpManager;
	}

	@EventHandler
	public void onSpawnNpcs(GamePrepareCountdownCommence event)
	{
		if (event.GetGame() == this)
		{
			for (int i = 0; i < 200; i++)				
				getNpcManager().spawnNpc();
		} 
	}

	/*
	@EventHandler(ignoreCancelled = true)
	public void onPlayerAttackWithSword(EntityDamageByEntityEvent event)
	{
		if (!(event.getDamager() instanceof Player))
			return;

		Player damager = (Player) event.getDamager();

		if (GetTeam(damager) == null)
			return;

		if (damager.getItemInHand() == null)
			return;

		Double damage = SWORD_DAMAGE.get(damager.getItemInHand().getType());

		if (damage == null)
			return;

		event.setDamage(damage);
	}
	*/

	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerAttackWithSword(EntityDamageByEntityEvent event)
	{
		if (!(event.getDamager() instanceof Player)) return;
		Player damager = ((Player) event.getDamager());
		if (damager == null)
			return;

		if (!IsAlive(damager))
			return;

		if (damager.getItemInHand() == null)
			return;

		Double damage = SWORD_DAMAGE.get(damager.getItemInHand().getType());

		if (damage == null)
			return;

  // /* event.AddMod(...) */, "Sword Damage", damage - event.getDamage(), false);
	}

	@EventHandler
	public void onCombatDeath(PlayerDeathEvent event)
	{
		if (event.getEntity().getKiller() == null)
			return;

		// IsPlayer check not needed as getKiller returns Player natively in Bukkit

		Player player = UtilPlayer.searchExact(event.getEntity().getKiller().getName());
		if (player == null)
			return;

		player.setMaxHealth(player.getMaxHealth() + 2);
		UtilPlayer.health(player, 2);
		
		player.getInventory().addItem(SneakyAssassinKit.SMOKE_BOMB.clone());
	}

	@Override
	@EventHandler
	public void ScoreboardUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;

		if (GetTeamList().isEmpty())
			return;

		Scoreboard.Reset();

		Scoreboard.WriteBlank();
		
		GameTeam team = GetTeamList().get(0);

		if (team.GetPlayers(false).size() < 13)
		{
			for (Player player : team.GetPlayers(false))
			{
				if (team.IsAlive(player))
				{
					Scoreboard.WriteOrdered("Powerups", C.cGreen + player.getName(), getPowerUpManager().getPowerUpCount(player), true);
				}
				else
				{
					Scoreboard.WriteOrdered("Powerups", C.cGray + player.getName(), getPowerUpManager().getPowerUpCount(player), true);
				}
			}
		}
		else if (team.GetPlayers(true).size() < 13)
		{
			for (Player player : team.GetPlayers(true))
			{
				Scoreboard.WriteOrdered("Powerups", C.cGreen + player.getName(), getPowerUpManager().getPowerUpCount(player), true);
			}
		}
		else
		{
			Scoreboard.Write(C.cGreen + "Players Alive");
			Scoreboard.Write("" + team.GetPlayers(true).size());

			Scoreboard.WriteBlank();
			Scoreboard.Write(C.cRed + "Players Dead");
			Scoreboard.Write("" + (team.GetPlayers(false).size() - team.GetPlayers(true).size()));
		}

		GetScoreboard().WriteBlank();
		GetScoreboard().Write(C.cYellow + "Player Reveal");
		GetScoreboard().Write(String.valueOf(Math.max(0, _revealCountdown)) + (_revealCountdown == 1 ? " Second" : " Seconds"));
		GetScoreboard().Draw();
	}

	@EventHandler
	public void onRevealCountdown(UpdateEvent event)
	{
		if (GetState() == GameState.Live && event.getType() == UpdateType.SEC)
		{
			_revealCountdown--;

			if (_revealCountdown <= 3 && _revealCountdown > 0)
			{
				this.Announce(F.main("Game", C.cYellow + C.Bold + "Players Revealed in " + _revealCountdown + " Seconds"));
			}
			else if (_revealCountdown == 0)
			{
				for (Player player : Bukkit.getOnlinePlayers())
				{
					if (IsAlive(player))
					{
						Manager.GetDisguise().getService().clear(player);
						player.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 80, 0));
					}
				}
				
				this.Announce(F.main("Game", C.cRed + C.Bold + "Players Revealed!"));
			}
			else if (_revealCountdown == -4)
			{
				for (Player player : Bukkit.getOnlinePlayers())
				{
					if (IsAlive(player))
					{
						com.houzicore.shared.api.disguise.DisguiseRequest request = new com.houzicore.shared.api.disguise.DisguiseRequest(
							player.getUniqueId(),
							com.houzicore.shared.api.disguise.DisguiseArchetype.MOB,
							_disguiseType.name(),
							true,
							false,
							false
						);
						Manager.GetDisguise().getService().apply(player, request);
						player.removePotionEffect(PotionEffectType.WITHER);
					}
				}

				_revealCountdown = 60;
				
				this.Announce(F.main("Game", C.cGreen + C.Bold + "Players Disguised!"));
			}
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerInteractEntity(PlayerInteractEntityEvent event)
	{
		if (event.getRightClicked() instanceof Villager)
		{
			event.setCancelled(true);
			
			if (UtilGear.isMat(event.getPlayer().getItemInHand(), Material.EMERALD))
			{
				if (!Recharge.Instance.use(event.getPlayer(), "Bribe Villager", 8000, true, true))
					return;
				
				_npcManager.setBribed(event.getRightClicked(), event.getPlayer());
				
				UtilInv.remove(event.getPlayer(), Material.EMERALD, (byte)0, 1);
				
				UtilPlayer.message(event.getPlayer(), F.main("Game", "You used " + F.elem("Bribe Villager") + "."));
				
				event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.ENTITY_VILLAGER_YES, 1f, 1f);
				
				UtilParticle.PlayParticle(ParticleType.HAPPY_VILLAGER, event.getRightClicked().getLocation().add(0, 1, 0), 0.3f, 0.5f, 0.3f, 0, 10,
						ViewDist.NORMAL, UtilServer.getPlayers());
			}
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onEntityCombust(EntityCombustEvent event)
	{
		event.setCancelled(true);
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerInteractHanging(PlayerInteractEntityEvent event)
	{
		if (event.getRightClicked() instanceof Hanging)
			event.setCancelled(true);
	}

	@EventHandler(ignoreCancelled = true)
	public void onWitherEffectDamager(EntityDamageByEntityEvent event)
	{
		if (event.getCause() == EntityDamageEvent.DamageCause.WITHER)
			event.setCancelled(true);
	}

	public NpcManager getNpcManager()
	{
		return _npcManager;
	}
	
	@EventHandler
	public void unregisterListeners(GameStateChangeEvent event)
	{
		if (event.GetState() == GameState.End || event.GetState() == GameState.Dead)
		{
			HandlerList.unregisterAll(_npcManager);
			HandlerList.unregisterAll(_powerUpManager);
			_npcManager = null;
			_powerUpManager = null;
		}
	}
}
