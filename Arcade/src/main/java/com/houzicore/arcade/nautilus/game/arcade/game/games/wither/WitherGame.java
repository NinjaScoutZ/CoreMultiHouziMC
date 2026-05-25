package com.houzicore.arcade.nautilus.game.arcade.game.games.wither;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilAction;
import com.houzicore.shared.common.util.UtilAlg;
import com.houzicore.shared.common.util.UtilBlock;
import com.houzicore.shared.common.util.UtilParticle;
import com.houzicore.shared.common.util.UtilEvent.ActionType;
import com.houzicore.shared.common.util.UtilParticle.ParticleType;
import com.houzicore.shared.common.util.UtilParticle.ViewDist;
import com.houzicore.shared.common.util.UtilMath;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.common.util.UtilTextMiddle;
import com.houzicore.shared.common.util.UtilTime;
import com.houzicore.shared.core.data.BlockData;
import com.houzicore.shared.core.itemstack.ItemBuilder;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
//import com.houzicore.shared.condition.Condition.ConditionType;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.entity.Player;
import org.bukkit.GameMode;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.GameType;
import com.houzicore.arcade.nautilus.game.arcade.events.GameStateChangeEvent;
import com.houzicore.arcade.nautilus.game.arcade.game.GameTeam;
import com.houzicore.arcade.nautilus.game.arcade.game.GameTeam.PlayerState;
import com.houzicore.arcade.nautilus.game.arcade.game.TeamGame;
import com.houzicore.arcade.nautilus.game.arcade.game.games.paintball.PlayerCopy;
import com.houzicore.arcade.nautilus.game.arcade.game.games.wither.events.HumanReviveEvent;
import com.houzicore.arcade.nautilus.game.arcade.game.games.wither.kit.KitHumanArcher;
import com.houzicore.arcade.nautilus.game.arcade.game.games.wither.kit.KitHumanMedic;
import com.houzicore.arcade.nautilus.game.arcade.game.games.wither.kit.KitHumanEditor;
import com.houzicore.arcade.nautilus.game.arcade.game.games.wither.kit.KitWitherMinion;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;
import com.houzicore.arcade.nautilus.game.arcade.kit.NullKit;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.data.IBlockRestorer;
import com.houzicore.arcade.nautilus.game.arcade.stats.WitherAssaultReviveTracker;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
//import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.entity.Wither;
import org.bukkit.entity.WitherSkull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class WitherGame extends TeamGame implements IBlockRestorer
{
	private GameTeam _runners;
	private GameTeam _withers;

	private double _witherFactor = 2.5;

	private int _yLimit = 0;
	private int _maxY;

	private long _gameTime = 300000;

	private HashMap<Player, PlayerCopy> _doubles = new HashMap<Player, PlayerCopy>();

	// private int _livesPerPlayer = 3;
	// private HashMap<Player, Integer> _lives = new HashMap<Player, Integer>();

	private HashSet<BlockData> _blocks = new HashSet<BlockData>();
	
	private ArrayList<Location> _locationsOfBlocks = new ArrayList<Location>();

	public WitherGame(ArcadeManager manager)
	{
		super(manager, GameType.WitherAssault,

		new Kit[]
		{
				new KitHumanArcher(manager),
				new KitHumanMedic(manager),
				new KitHumanEditor(manager),
				new NullKit(manager),
				new KitWitherMinion(manager),
		},

		// EN
				new String[]
		{

				C.cGreen + "Humans" + C.cWhite + "  Run and hide from the Withers",
				C.cGreen + "Humans" + C.cWhite + "  Revive your dead allies!",
				C.cGreen + "Humans" + C.cWhite + "  Win by surviving for 5 minutes",
				" ",
				C.cRed + "Withers" + C.cWhite + "  Moves very slowly when near ground",
				C.cRed + "Withers" + C.cWhite + "  Kill all the Humans within 5 Minutes",
		}, 
				// TH
				new String[]
		{

				C.cGreen + "[TH] Humans" + C.cWhite + "[TH]   Run and hide from the Withers",
				C.cGreen + "[TH] Humans" + C.cWhite + "[TH]   Revive your dead allies!",
				C.cGreen + "[TH] Humans" + C.cWhite + "[TH]   Win by surviving for 5 minutes",
				"[TH]  ",
				C.cRed + "[TH] Withers" + C.cWhite + "[TH]   Moves very slowly when near ground",
				C.cRed + "[TH] Withers" + C.cWhite + "[TH]   Kill all the Humans within 5 Minutes",
		});

		this.DeathOut = true;
		this.DamageTeamSelf = false;
		this.DamageSelf = false;
		this.DeathSpectateSecs = 4;
		this.HungerSet = 20;
		this.WorldBoundaryKill = false;
		this.BlockBreak = true;
		this.BlockPlace = true;
		this.ItemPickup = true;

		this.KitRegisterState = GameState.Prepare;

		this.TeamArmor = true;
		this.TeamArmorHotbar = false;

		_help = new String[]
		{
			"Blocks placed by an Editor can be passed by other humans by " + C.cDGreen + "Right-clicking the block",
			"Withers are too powerful to be killed. Hiding is the only option!",
			"Medics are a valuable asset. Stick with them and keep them safe!",

		};
		
	}

	@Override
	public void ParseData()
	{
		java.util.ArrayList<org.bukkit.Location> locs = WorldData.GetDataLocs("RED");
		if (!locs.isEmpty()) {
			_yLimit = locs.get(0).getBlockY();
		}
	}

	@EventHandler
	public void teamSetup(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Recruit)
			return;

		for (Kit kit : GetKits())
		{
			for (GameTeam team : GetTeamList())
			{
				if (team.GetColor() == ChatColor.RED)
				{
					_withers = team;
					_withers.SetName("Withers");
					_withers.SetColor(ChatColor.RED);

					if (!kit.GetName().contains("Wither"))
						team.GetRestrictedKits().add(kit);
				}
				else
				{
					_runners = team;
					_runners.SetName("Humans");
					_runners.SetColor(ChatColor.GREEN);

					if (kit.GetName().contains("Wither"))
						team.GetRestrictedKits().add(kit);
				}
			}
		}
	}

	@EventHandler
	public void teamBalance(UpdateEvent event)
	{
		if (!InProgress())
			return;

		if (event.getType() != UpdateType.FAST)
			return;

		// Not Enough Players
		if (_runners.GetPlayers(true).size() < 2)
			return;

		// Enough Withers
		if (_withers.GetPlayers(true).size() * _witherFactor >= _runners
				.GetPlayers(true).size())
			return;

		Player player = UtilAlg.Random(_runners.GetPlayers(true));
		setWither(player, true);
	}

//	@EventHandler
//	public void assignCompassOnStart(GameStateChangeEvent event)
//	{
//		if (event.GetState() != GameState.Live)
//			return;
//
//		for (Player players : _withers.GetPlayers(true))
//		{
//			ItemStack compass = new ItemBuilder(Material.COMPASS, 1).build();
//
//			ItemMeta im = compass.getItemMeta();
//			im.setLore(new ArrayList<String>(Arrays.asList(
//					F.item("A compass that trigger"),
//					F.item("your ability to track humans!"))));
//			im.setDisplayName(F.name("Scent Trigger"));
//
//			compass.setItemMeta(im);
//
//			players.getInventory().setItem(7, compass);
//		}
//	}

	// Cancel wither shooting in waiting lobby
	@EventHandler
	public void onWitherSkullFire(ProjectileLaunchEvent event)
	{
		if (GetState() == GameState.Recruit || GetState() == GameState.Prepare)
		{
			Projectile proj = event.getEntity();
			WitherSkull ws = (WitherSkull) proj;

			if (ws.getShooter() instanceof Wither)
			{
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void handleCustomBlockPlace(BlockPlaceEvent event)
	{
		if(!IsLive())
		{
			return;
		}
		com.houzicore.arcade.nautilus.game.arcade.kit.Kit kit = GetKit(event.getPlayer());
		if(kit == null || !kit.GetName().contentEquals("Human Editor"))
		{
			return;
		}
		if(GetTeamList().size() < 2) return;
		_maxY = GetTeamList().get(1).GetSpawn().getBlockY() - 4;
		if(event.getBlock().getLocation().getBlockY() < _maxY)
		{
			event.getPlayer().sendMessage(F.main("BlockChecker", "You may not build under this height!"));
			event.setCancelled(true);
			return;
		}
		
		_locationsOfBlocks.add(event.getBlock().getLocation());
	}
	
	@EventHandler
	public void handleCustomBlockbreak(BlockBreakEvent event)
	{
		if(!IsLive())
		{
			return;
		}
		com.houzicore.arcade.nautilus.game.arcade.kit.Kit kit = GetKit(event.getPlayer());
		if(kit == null || !kit.GetName().contentEquals("Human Editor"))
		{
			return;
		}
		if(GetTeamList().size() < 2) return;
		Location blockLocation = event.getBlock().getLocation();
		_maxY = GetTeamList().get(1).GetSpawn().getBlockY() - 3;
		if(blockLocation.add(0,1,0).getBlock().getType() == Material.AIR)
		{
			for(Player humans: _runners.GetPlayers(true))
			{
				if(IsAlive(humans))
				{
					if(humans.getLocation().add(0,-1,0).getBlock().equals(event.getBlock()))
					{
						if(humans.getName() != event.getPlayer().getName())
						{
							event.setCancelled(true);
							return;
						}
					}
				}
			}
		}

		
		if(blockLocation.getBlockY() < _maxY)
		{
			event.getPlayer().sendMessage(F.main("BlockChecker", "You may not build under this height!"));
			event.setCancelled(true);
			return;
		}
	}
	
	@EventHandler
	public void handleCustomItemPickup(PlayerPickupItemEvent event)
	{
		if(!IsLive())
		{
			return;
		}
		com.houzicore.arcade.nautilus.game.arcade.kit.Kit kit = GetKit(event.getPlayer());
		if(kit == null || !kit.GetName().contentEquals("Human Editor"))
		{
			return;
		}
	}
	
	//On Player interact with a placed block by Editor
	@EventHandler
	public void onPlayerInteract(final PlayerInteractEvent event)
	{
		if(!IsLive())
		{
			return;
		}
		if(!IsAlive(event.getPlayer()))
		{
			return;
		}
		if(GetTeam(event.getPlayer()).GetColor() == ChatColor.RED)
		{
			return;
		}
		if(event.getAction() == Action.RIGHT_CLICK_BLOCK)
		{
			Block clickedBlock = event.getClickedBlock();
			
			if(_locationsOfBlocks.contains(clickedBlock.getLocation()))
			{
				new BukkitRunnable()
				{
					
					@Override
					public void run()
					{
						if(!(event.getPlayer().getItemInHand().getType().isBlock()) || event.getPlayer().getItemInHand().getType() == Material.AIR)
						{
							UtilParticle.PlayParticle(ParticleType.FLAME, event.getClickedBlock().getLocation().add(0.5, 0.5, 0.5), 0, 0, 0, 0, 1, ViewDist.LONG, UtilServer.getPlayers());
							event.getClickedBlock().getWorld().playSound(event.getClickedBlock().getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 2f, 1f);
							Manager.GetBlockRestore().Add(event.getClickedBlock(), 0,
									com.houzicore.shared.common.util.IdUtil.getData(event.getClickedBlock()), 2000);
						}
					}
				}.runTaskLater(Manager.getPlugin(), 5);
			}
		}
	}

	public void setWither(Player player, boolean forced)
	{
		// _lives.remove(player);

		SetPlayerTeam(player, _withers, true);

		// Kit
		Kit newKit = GetKits()[GetKits().length-1];

		SetKit(player, newKit, false);
		newKit.ApplyKit(player);

		player.teleport(_withers.GetSpawn());

		if (forced)
		{
			AddGems(player, 10, "Forced Wither", false, false);

			Announce(F.main(
					"Game",
					F.elem(_withers.GetColor() + player.getName())
							+ " has become a "
							+ F.elem(_withers.GetColor() + newKit.GetName())
							+ "."));

			player.getWorld().strikeLightningEffect(player.getLocation());
		}
	}

	// @EventHandler
	// public void playerLoseLife(final PlayerDeathOutEvent event)
	// {
	// Player player = event.GetPlayer();
	//
	// if (_lives.containsKey(player))
	// {
	// int lives = _lives.get(player);
	//
	// if (lives <= 1)
	// return;
	//
	// _lives.put(player, lives - 1);
	//
	// UtilPlayer.message(player, F.main("Game", "You have " + F.elem(C.cGreen +
	// C.Bold + lives + " Lives Remaining") + "."));
	//
	// event.setCancelled(true);
	// }
	// }

	@EventHandler
	public void gameStart(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Live)
			return;

		UtilTextMiddle.display(C.cGreen + "Humans Hiding",
				"15 Seconds until Assault", 10, 80, 10);

		for (Player player : _withers.GetPlayers(true))
		{
//			//Manager.GetCondition()
//					//.Factory()
//					//.Blind("Game Start", player, null, 15, 0, false, false,
//							//false);
		}
	}
	
	@EventHandler
	public void removeUselessPlayerCopies(UpdateEvent event)
	{
		if(event.getType() != UpdateType.TWOSEC)
		return;
		
		for(Player players: _doubles.keySet())
		{
			if(!players.isOnline())
			{
				PlayerCopy pc = _doubles.get(players);
				pc.GetEntity().remove();
				_doubles.remove(players);
			}
		}
		
	}

	// @EventHandler
	// public void playerLivesDisplay(PlayerKitGiveEvent event)
	// {
	// if (!_runners.HasPlayer(event.GetPlayer()))
	// return;
	//
	// //Player Lives
	// if (!_lives.containsKey(event.GetPlayer()))
	// _lives.put(event.GetPlayer(), _livesPerPlayer);
	//
	// int lives = _lives.get(event.GetPlayer());
	//
	// if (lives <= 0)
	// return;
	//
	// event.GetPlayer().getInventory().setItem(8,
	// ItemStackFactory.Instance.CreateStack(Material.BONE, (byte)0, lives,
	// C.cGreen + C.Bold + lives + " Lives Remaining"));
	// }

	@Override
	public void EndCheck()
	{
		if (!IsLive())
			return;

		// Players Quit
		if (GetPlayers(true).size() < 1)
		{
			SetState(GameState.End);
			_locationsOfBlocks.clear();
		}

		GameTeam winner = null;

		// Wither Win
		if (UtilTime.elapsed(this.GetStateTime(), _gameTime))
			winner = _runners;

		// Runner Win
		if (_runners.GetPlayers(true).isEmpty())
			winner = _withers;

		// Set Win
		if (winner != null)
		{
			AnnounceEnd(winner);

			for (GameTeam team : GetTeamList())
			{
				if (WinnerTeam != null && team.equals(WinnerTeam))
				{
					for (Player player : team.GetPlayers(false))
						AddGems(player, 10, "Winning Team", false, false);
				}

				for (Player player : team.GetPlayers(false))
					if (player.isOnline())
						AddGems(player, 10, "Participation", false, false);
			}

			// End
			SetState(GameState.End);
			_locationsOfBlocks.clear();
		}
	}

	@Override
	@EventHandler
	public void ScoreboardUpdate(UpdateEvent event)
	{
		if (!InProgress())
			return;

		if (event.getType() != UpdateType.FAST)
			return;

		// int lives = 0;
		// for (int i : _lives.values())
		// lives += i;

		// Wipe Last
		Scoreboard.Reset();

		Scoreboard.WriteBlank();
		Scoreboard.Write(_runners.GetColor() + C.Bold + _runners.GetName());
		Scoreboard.Write(_runners.GetColor() + ""
				+ _runners.GetPlayers(true).size() + " Players");

		Scoreboard.WriteBlank();
		Scoreboard.Write(_withers.GetColor() + C.Bold + _withers.GetName());
		Scoreboard.Write(_withers.GetColor() + ""
				+ _withers.GetPlayers(true).size() + " Players");

		// Scoreboard.WriteBlank();
		// Scoreboard.Write(C.cYellow + C.Bold + "Humans Alive");
		// Scoreboard.Write(lives + " / " + (_lives.size() * _livesPerPlayer));

		Scoreboard.WriteBlank();
		Scoreboard.Write(C.cYellow + C.Bold + "Time Left");
		Scoreboard.Write(UtilTime.MakeStr(
				Math.max(
						0,
						_gameTime
								- (System.currentTimeMillis() - this
										.GetStateTime())), 1));

		Scoreboard.Draw();
	}

	@Override
	public GameTeam ChooseTeam(Player player)
	{
		if (CanJoinTeam(_withers))
			return _withers;

		return _runners;
	}

	@Override
	public boolean CanJoinTeam(GameTeam team)
	{
		if (team.equals(_withers))
		{
			return team.GetSize() < getRequiredWithers();
		}

		return team.GetSize() < GetPlayers(true).size() - getRequiredWithers();
	}

	public int getRequiredWithers()
	{
		return (int) (GetPlayers(true).size() / _witherFactor);
	}

	@EventHandler
	public void witherMovement(UpdateEvent event)
	{
		if (!IsLive())
			return;

		if (event.getType() != UpdateType.FASTER)
			return;

		Location _spawn = GetTeamList().get(1).GetSpawn();
		for (Player player : _withers.GetPlayers(true))
		{
			ArrayList<Location> collisions = new ArrayList<Location>();

			// Fly Speed
			
			double distanceToGround = player.getLocation().distance(new Location(_spawn.getWorld(), player.getLocation().getX(), _spawn.getY(), player.getLocation().getZ()));
			double speed;

			if (distanceToGround < 8)
			{
				speed = 0.16;
			}
			else
			{
				speed = 0.09 - (_yLimit - player.getLocation().getY()) * 0.006;
			}

			if (distanceToGround < 4)
			{
				speed = 0.016;
			}
			else
			{
				speed = 0.09 - (_yLimit - player.getLocation().getY()) * 0.006;
			}

			player.setFlySpeed((float) speed);

			// Bump
			for (Block block : UtilBlock.getInRadius(
					player.getLocation().add(0, 0.5, 0), 1.5d).keySet())
			{
				if (!UtilBlock.airFoliage(block))
				{
					collisions.add(block.getLocation().add(0.5, 0.5, 0.5));
				}
			}

			Vector vec = UtilAlg.getAverageBump(player.getLocation(),
					collisions);

			if (vec == null)
				continue;

			UtilAction.velocity(player, vec, 0.6, false, 0, 0.4, 10, true);

			// if (player.getLocation().getY() < _yLimit + 6)
			// UtilAction.velocity(player, new Vector(0, 1, 0), 0.6, false, 0,
			// 0, 10, true);
		}
	}

	@Override
	public void addBlocks(Set<Block> blocks)
	{
		Iterator<Block> blockIter = blocks.iterator();

		while (blockIter.hasNext())
		{
			Block block = blockIter.next();

			if (block.getType() == Material.BEDROCK
					|| block.getType() == Material.IRON_BLOCK)
				blockIter.remove();

			else if (!isInsideMap(block.getLocation()))
				blockIter.remove();

			// else if (UtilAlg.inBoundingBox(block.getLocation(), _safeA,
			// _safeB) || UtilAlg.inBoundingBox(block.getLocation(), _spawnA,
			// _spawnB))
			// blockIter.remove();
		}

		for (Block block : blocks)
			_blocks.add(new BlockData(block));
	}

	@Override
	public void restoreBlock(Location loc, double radius)
	{
		Iterator<BlockData> dataIt = _blocks.iterator();

		while (dataIt.hasNext())
		{
			BlockData data = dataIt.next();

			double dist = UtilMath.offset(loc,
					data.Block.getLocation().add(0.5, 0.5, 0.5));

			if (dist < radius)
			{
				Manager.GetBlockRestore().Add(data.Block, 0, (byte) 0,
						data.Material.ordinal(), data.Data,
						(long) (6000 * (dist / radius)));
				dataIt.remove();
			}
		}
	}

	// @EventHandler
	// public void cleanLives(PlayerQuitEvent event)
	// {
	// _lives.remove(event.getPlayer());
	// }

	// @EventHandler
	// public void livesUpdate(UpdateEvent event)
	// {
	// if (!IsLive())
	// return;
	//
	// if (event.getType() != UpdateType.FASTER)
	// return;
	//
	// Iterator<Player> playerIter = _lives.keySet().iterator();
	//
	// while (playerIter.hasNext())
	// {
	// Player player = playerIter.next();
	//
	// if (!player.isOnline() || !_runners.HasPlayer(player))
	// playerIter.remove();
	// }
	// }

	@EventHandler
	public void arrowDamage(EntityDamageByEntityEvent event)
	{
		if (event.getDamager() == null)
			return;

		event.setDamage(event.getDamage() * 0.75);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void damageOut(EntityDamageByEntityEvent event)
	{
		if (event.isCancelled())
			return;

		if (!(event.getEntity() instanceof Player))
			return;

		final Player victim = (Player) event.getEntity();

		if (event.getDamage() < victim.getHealth())
			return;

		event.setCancelled(true);

		victim.playEffect(EntityEffect.HURT);

		playerOut(victim);

		if (event.getDamager() instanceof Player)
		{
			final Player damager = (Player) event.getDamager();
			AddGems(damager, 2, "Humans Stunned", true,
					true);

			Bukkit.broadcastMessage(C.cBlue + "Death> " + C.cGreen
					+ victim.getName() + C.cGray
					+ " was killed by " + C.cRed
					+ damager.getName() + C.cGray + ".");
		}
		else
		{
			Bukkit.broadcastMessage(C.cBlue + "Death> " + C.cGreen
					+ victim.getName() + C.cGray
					+ " was killed.");
		}
	}

	public void playerOut(Player player)
	{
		// State
		SetPlayerState(player, PlayerState.OUT);
		player.setHealth(20);

		player.setFlySpeed(0.1f);

		// Conditions
//		//Manager.GetCondition().Factory()
//				//.Blind("Hit", player, player, 1.5, 0, false, false, false);
//		//Manager.GetCondition().Factory()
//				//.Cloak("Hit", player, player, 9999, false, false);

		// Settings
		player.setGameMode(GameMode.ADVENTURE);
		player.setAllowFlight(true);
		player.setFlying(true);
		player.setInvisible(true);
		player.setCollidable(false);
		player.setMetadata("spectator", new org.bukkit.metadata.FixedMetadataValue(org.bukkit.plugin.java.JavaPlugin.getPlugin(com.houzicore.arcade.Arcade.class), true));

		player.setVelocity(new Vector(0, 1.2, 0));

		_doubles.put(player, new PlayerCopy(this, player, ChatColor.YELLOW));
	}
	
	public void playerIn(final Player player, final LivingEntity copy, Player revivedBy)
	{
		// State
		SetPlayerState(player, PlayerState.IN);
		player.setHealth(20);

		// Teleport
		if (copy != null)
		{
			Location loc = player.getLocation();
			loc.setX(copy.getLocation().getX());
			loc.setY(copy.getLocation().getY());
			loc.setZ(copy.getLocation().getZ());
			player.teleport(loc);
		}

		// Settings
		player.setGameMode(GameMode.SURVIVAL);
		player.setAllowFlight(false);
		player.setFlying(false);
		player.setInvisible(false);
		player.setCollidable(true);
		player.removeMetadata("spectator", org.bukkit.plugin.java.JavaPlugin.getPlugin(com.houzicore.arcade.Arcade.class));

		// Items
		player.getInventory().remove(Material.CLOCK);
		player.getInventory().remove(Material.COMPASS);

		// Inform
		if(revivedBy != null)
		{
			UtilPlayer.message(player, F.main("Game", "You have been revived by " + C.cGold + revivedBy.getName()));
		}
		else
		{
			UtilPlayer.message(player, F.main("Game", "You have been revived!"));
		}

		// Delayed Visibility
		if (copy != null)
		{
			UtilServer
					.getServer()
					.getScheduler()
					.scheduleSyncDelayedTask(Manager.getPlugin(),
							new Runnable()
							{
								public void run()
								{
									// Remove Invis
//									if (IsAlive(player))
//										//Manager.GetCondition().EndCondition(
//												//player, ConditionType.CLOAK,
//												//null);

									// Remove Copy
									copy.remove();
								}
							}, 4);
		}
	}

	@EventHandler
	public void revive(ProjectileHitEvent event)
	{
		if (!IsLive())
			return;

		if (!(event.getEntity() instanceof ThrownPotion))
			return;

		if (event.getEntity().getShooter() == null)
			return;

		if (!(event.getEntity().getShooter() instanceof Player))
			return;

		Player thrower = (Player) event.getEntity().getShooter();

		GameTeam throwerTeam = GetTeam(thrower);
		if (throwerTeam == null)
			return;

		// Revive a copy
		Iterator<PlayerCopy> copyIterator = _doubles.values().iterator();
		while (copyIterator.hasNext())
		{
			PlayerCopy copy = copyIterator.next();

			GameTeam otherTeam = GetTeam(copy.GetPlayer());
			if (otherTeam == null || !otherTeam.equals(throwerTeam))
				continue;

			if (UtilMath.offset(copy.GetEntity().getLocation().add(0, 1, 0),
					event.getEntity().getLocation()) > 3)
				continue;

			playerIn(copy.GetPlayer(), copy.GetEntity(), thrower);
			copyIterator.remove();

			AddGems(thrower, 3, "Revived Ally", true, true);
			
			Bukkit.getPluginManager().callEvent(new HumanReviveEvent(thrower, copy.GetPlayer()));
		}

		// Revive a player
//		for (Player player : GetPlayers(true))
//		{
//			GameTeam otherTeam = GetTeam(player);
//			if (otherTeam == null || !otherTeam.equals(throwerTeam))
//				continue;
//
//			if (UtilMath.offset(player.getLocation().add(0, 1, 0), event
//					.getEntity().getLocation()) > 3)
//				continue;
//
//			playerIn(player, null, thrower);
//		}
	}

	@EventHandler
	public void removePotionEffect(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		for (Player player : GetPlayers(true))
			player.removePotionEffect(PotionEffectType.WATER_BREATHING);
	}

	@EventHandler
	public void skeletonDamage(EntityDamageByEntityEvent event)
	{
		for (PlayerCopy copy : _doubles.values())
		{
			if (copy.GetEntity().equals(event.getEntity()))
			{
				event.setCancelled(true);
				break;
			}
		}
	}

	@EventHandler
	public void skeletonCombust(EntityCombustEvent event)
	{
		for (PlayerCopy copy : _doubles.values())
		{
			if (copy.GetEntity().equals(event.getEntity()))
			{
				event.setCancelled(true);
				break;
			}
		}
	}
}
