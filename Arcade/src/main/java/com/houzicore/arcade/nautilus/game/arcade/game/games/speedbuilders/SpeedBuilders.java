package com.houzicore.arcade.nautilus.game.arcade.game.games.speedbuilders;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.material.Bed;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;


import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.NautHashMap;
import com.houzicore.shared.common.util.UtilAction;
import com.houzicore.shared.common.util.UtilAlg;
import com.houzicore.shared.common.util.UtilBlock;
import com.houzicore.shared.common.util.UtilEnt;
import com.houzicore.shared.common.util.UtilEvent;
import com.houzicore.shared.common.util.UtilEvent.ActionType;
import com.houzicore.shared.common.util.UtilInv;
import com.houzicore.shared.common.util.UtilMath;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.common.util.UtilTextBottom;
import com.houzicore.shared.common.util.UtilTextMiddle;
import com.houzicore.shared.common.util.UtilTime;
import com.houzicore.shared.core.disguise.disguises.DisguiseGuardian;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;

import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.GameType;
import com.houzicore.arcade.nautilus.game.arcade.events.GameStateChangeEvent;
import com.houzicore.arcade.nautilus.game.arcade.game.GameTeam;
import com.houzicore.arcade.nautilus.game.arcade.game.GameTeam.PlayerState;
import com.houzicore.arcade.nautilus.game.arcade.game.SoloGame;
import com.houzicore.arcade.nautilus.game.arcade.game.games.speedbuilders.data.BuildData;
import com.houzicore.arcade.nautilus.game.arcade.game.games.speedbuilders.data.DemolitionData;
import com.houzicore.arcade.nautilus.game.arcade.game.games.speedbuilders.data.MobData;
import com.houzicore.arcade.nautilus.game.arcade.game.games.speedbuilders.data.RecreationData;
import com.houzicore.arcade.nautilus.game.arcade.game.games.speedbuilders.events.PerfectBuildEvent;
import com.houzicore.arcade.nautilus.game.arcade.game.games.speedbuilders.kits.DefaultKit;
import com.houzicore.arcade.nautilus.game.arcade.game.games.speedbuilders.stattrackers.DependableTracker;
import com.houzicore.arcade.nautilus.game.arcade.game.games.speedbuilders.SpeedBuildersStage;
import com.houzicore.arcade.nautilus.game.arcade.game.games.speedbuilders.lang.SpeedBuildersLang;
import com.houzicore.arcade.nautilus.game.arcade.game.games.speedbuilders.gui.SpectatorVoteShop;
import com.houzicore.shared.core.itemstack.ItemStackFactory;
import com.houzicore.arcade.nautilus.game.arcade.game.games.speedbuilders.stattrackers.FirstBuildTracker;
import com.houzicore.arcade.nautilus.game.arcade.game.games.speedbuilders.stattrackers.PerfectionistTracker;
import com.houzicore.arcade.nautilus.game.arcade.game.games.speedbuilders.stattrackers.SpeediestBuilderizerTracker;
import com.houzicore.arcade.nautilus.game.arcade.game.modules.CompassModule;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;



public class SpeedBuilders extends SoloGame
{

	private static final String GUARDIAN_NAME = C.cPurple + C.Bold + "Houra";

	//Build Size and some other values used commonly
	public int BuildSize = 7;
	public int BuildSizeDiv2 = BuildSize / 2;
	public int BuildSizeMin1 = BuildSize - 1;
	public int BuildSizePow3 = BuildSize * BuildSize * BuildSize;

	public boolean InstaBreak = true;

	private SpeedBuildersState _state = SpeedBuildersState.VIEWING;
	private long _stateTime = System.currentTimeMillis();

	private int _roundsPlayed;

	private int _buildCountStage;
	private int _viewCountStage;

	private int _buildTimeTracker = 40;
	private int _buildTime = 40;
	private int _viewTime = 10;

	private Location _buildMiddle;

	private ArrayList<BuildData> _buildData = new ArrayList<BuildData>();
	private ArrayList<BuildData> _usedBuilds = new ArrayList<>();
	private BuildData _currentBuild;

	private BlockState[][] _defaultMiddleGround = new BlockState[BuildSize][BuildSize];
	private ArrayList<Entity> _middleMobs = new ArrayList<Entity>();

	private NautHashMap<Player, RecreationData> _buildRecreations = new NautHashMap<Player, RecreationData>();

	private static final boolean USE_ARMOR_STAND_JUDGE = false;
	private LivingEntity _judgeEntity;
	private Location _judgeSpawn;
	private ArmorStand _judgeLaserTarget;
	private double _dragonAngle = 0.0;
	private Integer _fireBreathTaskId = null;

	public enum JudgeState
	{
		ORBITING,
		GLIDING_TO_TARGET,
		BREATHING,
		GLIDING_TO_ORBIT
	}

	private JudgeState _judgeState = JudgeState.ORBITING;
	private int _transitionTicks = 0;
	private final int TRANSITION_DURATION = 15; // 15 ticks = 0.75 seconds
	private Location _startTransitionLoc;
	private Location _targetTransitionLoc;
	private Location _targetLocForBreath;

	private double lerp(double a, double b, double t)
	{
		return a + (b - a) * t;
	}

	private float lerpAngle(float a, float b, float t)
	{
		float diff = b - a;
		while (diff > 180f) diff -= 360f;
		while (diff < -180f) diff += 360f;
		return a + diff * t;
	}

	private ArrayList<RecreationData> _toEliminate = new ArrayList<RecreationData>();
	private long _lastElimination;
	private boolean _eliminating;
	// Track the time we switch to review so we can give players 8 seconds to look around
	private long _reviewStartTime;

	private NautHashMap<Player, Long> _perfectBuild = new NautHashMap<Player, Long>();
	private boolean _allPerfect;

	private final NautHashMap<Player, Integer> _cumulativeScores = new NautHashMap<>();
	private final NautHashMap<Player, Integer> _comboStreaks = new NautHashMap<>();
	private final NautHashMap<Player, Integer> _speedBonusCumulative = new NautHashMap<>();
	private final NautHashMap<Player, Integer> _highestSingleRoundScore = new NautHashMap<>();
	private final NautHashMap<Player, Integer> _votesReceived = new NautHashMap<>();
	private final NautHashMap<Player, Player> _spectatorVotes = new NautHashMap<>();
	private final NautHashMap<Player, Integer> _perfectCounts = new NautHashMap<>();
	private final NautHashMap<Player, Long> _lastHeartbeat = new NautHashMap<>();
	private final NautHashMap<java.util.UUID, com.houzicore.arcade.nautilus.game.arcade.game.games.speedbuilders.data.RecreationData> _disconnectedRecreations = new NautHashMap<>();

	public enum ParticleColor { BLUE, GOLD, RED }
	private final ArrayList<RecreationData> _judgingQueue = new ArrayList<>();
	private final ArrayList<ParticleColor> _judgingColors = new ArrayList<>();
	private ParticleColor _activeBreathColor = ParticleColor.RED;

	private SpectatorVoteShop _voteShop;

	private Location _lookTarget;
	private ArmorStand _lookStand;
	private long _targetReached;
	private long _stayTime;
	private RecreationData _lastRecreationTarget;
	private double _standMoveProgress;
	private Location _standStart;

	private BuildData _nextBuild;

	public SpeedBuilders(ArcadeManager manager)
	{
		super(manager, GameType.SpeedBuilders,
				new Kit[]
						{
								new DefaultKit(manager)
						},
				new String[]
						{
								"Recreate the build shown to you.",
								"The least correct build is eliminated.",
								"Last person left wins!"
						});

		Damage = false;

		HungerSet = 20;
		HealthSet = 20;

		DeathMessages = false;

		// FixSpawnFacing = false;

		AllowParticles = false;

		InventoryClick = true;
		
		// AnticheatDisabled = true;

		registerStatTrackers(
				new DependableTracker(this),
				new FirstBuildTracker(this),
				new PerfectionistTracker(this),
				new SpeediestBuilderizerTracker(this)
				// new BlockPlaceStatTracker(this, new Material[]{})
		);

		_voteShop = new SpectatorVoteShop(this, manager, manager.GetClients(), manager.GetDonation());

		/*
		registerChatStats(
				new ChatStatData("BlocksPlaced", "Blocks Placed", true),
				new ChatStatData("BlocksBroken", "Blocks Broken", true)
		);
		*/

		new CompassModule(this)
				.setGiveItem(true)
				.register();

		/*
		registerDebugCommand("setnext", Perm.DEBUG_SETNEXT_COMMAND, PermissionGroup.BUILDER, (caller, args) ->
		{
			if (!UtilServer.isTestServer())
			{
				UtilPlayer.message(caller, F.main("Build", C.cYellow + "You can only use this on testing servers!"));

				return;
			}

			if (args.length == 0)
			{
				UtilPlayer.message(caller, F.main("Build", C.cYellow + "You need to specify a next build!"));

				return;
			}

			String buildName = Arrays.asList(args).stream().collect(Collectors.joining(" "));

			BuildData build = null;

			for (BuildData buildData : _buildData)
			{
				if (buildData.BuildText.toUpperCase().startsWith(buildName.toUpperCase()))
				{
					build = buildData;

					break;
				}
			}

			if (build == null)
			{
				UtilPlayer.message(caller, F.main("Build", "That build does not exist!"));
			} else
			{
				_nextBuild = build;

				UtilPlayer.message(caller, F.main("Build", "Set next build to " + F.elem(build.BuildText)));
			}
		});
		*/
	}

	@Override
	public void ParseData()
	{
		ArrayList<Location> redLocs = WorldData.GetDataLocs("RED");
		if (redLocs == null || redLocs.isEmpty())
		{
			System.out.println("[SpeedBuilders-Error] No RED locations found in map: " + WorldData.MapName + " (File: " + WorldData.File + ")! Using default location.");
			_buildMiddle = WorldData.World.getSpawnLocation().clone();
		}
		else
		{
			_buildMiddle = redLocs.get(0).clone().subtract(0.5, 0, 0.5);
		}
		
		_judgeSpawn = _buildMiddle.clone().add(0.5, BuildSize, 0.5);
		
		Location groundMin = _buildMiddle.clone().subtract(BuildSizeDiv2, 1, BuildSizeDiv2);
		
		for (int x = 0; x < BuildSize; x++)
		{
			for (int z = 0; z < BuildSize; z++)
			{
				_defaultMiddleGround[x][z] = groundMin.clone().add(x, 0, z).getBlock().getState();
			}
		}
		
		for (Entry<String, ArrayList<Location>> entry : WorldData.GetAllCustomLocs().entrySet())
		{
			if (entry.getValue() == null || entry.getValue().isEmpty())
				continue;
			BuildData buildData = new BuildData(entry.getValue().get(0).clone().subtract(0.5, 0, 0.5), ChatColor.translateAlternateColorCodes('&', entry.getKey()), this);
			boolean add = false;
			for (int x = 0; x < BuildSize && !add; x++)
			{
				for (int y = 0; y < BuildSize && !add; y++)
				{
					for (int z = 0; z < BuildSize && !add; z++)
					{
						if (buildData.Build[x][y][z] != null && buildData.Build[x][y][z].getType() != Material.AIR)
							add = true;
					}
				}
			}
			
			if (!buildData.Mobs.isEmpty())
				add = true;

			if (add)
				_buildData.add(buildData);
		}
		
		for (Location loc : WorldData.GetDataLocs("YELLOW"))
		{
			loc.subtract(0.5, 0, 0.5);
		}
		
		for (Location loc : GetTeamList().get(0).GetSpawns())
		{
			loc.setDirection(UtilAlg.getTrajectory(loc, _buildMiddle.clone().add(0.5, 0, 0.5)));
		}
	}

	public void setSpeedBuilderState(SpeedBuildersState state)
	{
		_state = state;
		_stateTime = System.currentTimeMillis();
		for (RecreationData recreation : _buildRecreations.values())
		{
			recreation.updateHologramProgress();
		}
	}

	public SpeedBuildersState getSpeedBuilderState()
	{
		return _state;
	}

	public com.houzicore.arcade.nautilus.game.arcade.game.games.speedbuilders.data.BuildData getCurrentBuild()
	{
		return _currentBuild;
	}

	public long getSpeedBuilderStateTime()
	{
		return _stateTime;
	}

	public int getRoundsPlayed()
	{
		return _roundsPlayed;
	}

	private void setAir(Block block)
	{
		block.setType(Material.AIR, true);
	}

	public void clearCenterArea(boolean resetGround)
	{
		Location buildMin = _buildMiddle.clone().subtract(BuildSizeDiv2, 0, BuildSizeDiv2);
		Location buildMax = _buildMiddle.clone().add(BuildSizeDiv2, BuildSizeMin1, BuildSizeDiv2);
		
		for (Block block : UtilBlock.getInBoundingBox(buildMin, buildMax))
		{
			setAir(block);
		}
		
		for (Entity entity : _middleMobs)
		{
			entity.remove();
		}
		
		_middleMobs.clear();
		
		if (resetGround)
		{
			for (int x = 0; x < BuildSize; x++)
			{
				for (int z = 0; z < BuildSize; z++)
				{
					Block block = buildMin.clone().add(x, -1, z).getBlock();
					block.setBlockData(_defaultMiddleGround[x][z].getBlockData(), true);
				}
			}
		}
	}

	public void pasteBuildInCenter(BuildData buildData)
	{
		clearCenterArea(true);
		
		Location groundMin = _buildMiddle.clone().subtract(BuildSizeDiv2, 1, BuildSizeDiv2);
		
		for (int x = 0; x < BuildSize; x++)
		{
			for (int z = 0; z < BuildSize; z++)
			{
				Block block = groundMin.clone().add(x, 0, z).getBlock();
				block.setBlockData(buildData.Ground[x][z].getBlockData(), true);
			}
		}
		
		Location buildMin = _buildMiddle.clone().subtract(BuildSizeDiv2, 0, BuildSizeDiv2);
		
		for (int x = 0; x < BuildSize; x++)
		{
			for (int y = 0; y < BuildSize; y++)
			{
				for (int z = 0; z < BuildSize; z++)
				{
					Block block = buildMin.clone().add(x, y, z).getBlock();
					block.setBlockData(buildData.Build[x][y][z].getBlockData(), true);
				}
			}
		}
		
		CreatureAllowOverride = true;
		
		for (MobData mobData : buildData.Mobs)
		{
			Location loc = buildMin.clone().add(mobData.DX + 0.5, mobData.DY, mobData.DZ + 0.5);
			
			Entity entity = loc.getWorld().spawnEntity(loc, mobData.EntityType);
			
			UtilEnt.Vegetate(entity, true);
			UtilEnt.ghost(entity, true, false);
			
			_middleMobs.add(entity);
		}
		
		CreatureAllowOverride = false;
	}

	public void spawnJudge()
	{
		CreatureAllowOverride = true;
		
		Location safeSpawn = _judgeSpawn.clone();
		if (safeSpawn.getY() < 10) {
			safeSpawn.setY(60 + BuildSize);
		}
		
		if (USE_ARMOR_STAND_JUDGE)
		{
			_judgeEntity = (LivingEntity) safeSpawn.getWorld().spawnEntity(safeSpawn, EntityType.ARMOR_STAND);
			_judgeEntity.setSilent(true);
			_judgeEntity.setGravity(false);
			_judgeEntity.setInvulnerable(true);
			_judgeEntity.setCustomName(GUARDIAN_NAME);
			_judgeEntity.setCustomNameVisible(true);
			_judgeEntity.setPersistent(true);

			ArmorStand stand = (ArmorStand) _judgeEntity;
			stand.setVisible(false);
			stand.setSmall(true);
			stand.setHelmet(new ItemStack(Material.DRAGON_HEAD));
			stand.setHeadPose(new org.bukkit.util.EulerAngle(Math.toRadians(20), 0, 0));
		}
		else
		{
			EnderDragon dragon = safeSpawn.getWorld().spawn(safeSpawn, EnderDragon.class);
			dragon.setAI(true);
			dragon.setGravity(false);
			dragon.setSilent(true);
			dragon.setInvulnerable(true);
			dragon.setCustomName(GUARDIAN_NAME);
			dragon.setCustomNameVisible(true);
			dragon.setPersistent(true);
			dragon.setRemoveWhenFarAway(false);
			dragon.setPhase(org.bukkit.entity.EnderDragon.Phase.HOVER);

			org.bukkit.attribute.AttributeInstance scaleAttr = dragon.getAttribute(org.bukkit.attribute.Attribute.SCALE);
			if (scaleAttr != null)
			{
				scaleAttr.setBaseValue(0.4);
			}

			_judgeEntity = dragon;
		}
		
		CreatureAllowOverride = false;
	}

	public void despawnJudge()
	{
		if (_fireBreathTaskId != null)
		{
			Bukkit.getScheduler().cancelTask(_fireBreathTaskId);
			_fireBreathTaskId = null;
		}
		if (_judgeLaserTarget != null)
		{
			_judgeLaserTarget.remove();
			_judgeLaserTarget = null;
		}
		if (_judgeEntity != null)
		{
			_judgeEntity.remove();
			_judgeEntity = null;
		}
		_judgeState = JudgeState.ORBITING;
	}

	@EventHandler
	public void onGameStateChange(GameStateChangeEvent event)
	{
		if (event.GetState() == GameState.End || event.GetState() == GameState.Dead)
		{
			despawnJudge();
		}
	}

	public void judgeTargetLocation(Location loc)
	{
		if (loc == null)
		{
			if (_fireBreathTaskId != null)
			{
				Bukkit.getScheduler().cancelTask(_fireBreathTaskId);
				_fireBreathTaskId = null;
			}
			if (_judgeLaserTarget != null)
			{
				_judgeLaserTarget.remove();
				_judgeLaserTarget = null;
			}
			if (_judgeEntity != null && _judgeEntity.isValid())
			{
				if (_judgeEntity instanceof EnderDragon)
				{
					((EnderDragon) _judgeEntity).setPhase(org.bukkit.entity.EnderDragon.Phase.HOVER);
				}
				if (_judgeState == JudgeState.BREATHING)
				{
					_judgeState = JudgeState.GLIDING_TO_ORBIT;
					_startTransitionLoc = _judgeEntity.getLocation().clone();
					_transitionTicks = 0;
				}
			}
		}
		else
		{
			if (_judgeLaserTarget != null || _fireBreathTaskId != null)
				judgeTargetLocation(null);
			
			if (_judgeEntity == null)
				return;
			
			// Calculate the target dragon location (dragonLoc)
			Vector dir = UtilAlg.getTrajectory(_buildMiddle, loc).normalize();
			Location dragonLoc = loc.clone().subtract(dir.clone().multiply(10));
			dragonLoc.setY(loc.getY() + 5);
			Vector faceDir = UtilAlg.getTrajectory(dragonLoc, loc);
			dragonLoc.setDirection(faceDir);
			dragonLoc.setYaw(dragonLoc.getYaw() + 180f); // Fix dragon rendering backwards
			
			// Instead of instant teleport, we start gliding
			_judgeState = JudgeState.GLIDING_TO_TARGET;
			_startTransitionLoc = _judgeEntity.getLocation().clone();
			_targetTransitionLoc = dragonLoc;
			_transitionTicks = 0;
			_targetLocForBreath = loc;
		}
	}

	private void startBreathAttack(Location loc)
	{
		if (_judgeEntity == null || !_judgeEntity.isValid() || loc == null)
			return;

		if (_judgeEntity instanceof EnderDragon)
		{
			((EnderDragon) _judgeEntity).setPhase(org.bukkit.entity.EnderDragon.Phase.BREATH_ATTACK);
		}

		CreatureAllowOverride = true;
		_judgeLaserTarget = _judgeEntity.getWorld().spawn(loc, ArmorStand.class);
		CreatureAllowOverride = false;

		_judgeLaserTarget.setVisible(false);
		_judgeLaserTarget.setGravity(false);
		_judgeLaserTarget.setSmall(true);

		_fireBreathTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(Manager.getPlugin(), new Runnable()
		{
			private int ticks = 0;

			@Override
			public void run()
			{
				if (_judgeEntity == null || !_judgeEntity.isValid() || _judgeLaserTarget == null)
				{
					if (_fireBreathTaskId != null)
					{
						Bukkit.getScheduler().cancelTask(_fireBreathTaskId);
						_fireBreathTaskId = null;
					}
					return;
				}

				Location start = (_judgeEntity instanceof EnderDragon) ? ((EnderDragon) _judgeEntity).getEyeLocation() : _judgeEntity.getLocation().add(0, 1.62, 0);
				Location end = _judgeLaserTarget.getLocation();

				if (_judgeEntity instanceof EnderDragon)
				{
					Vector look = UtilAlg.getTrajectory(start, end).normalize();
					start.add(look.multiply(3.2));
				}

				Vector direction = UtilAlg.getTrajectory(start, end);
				double distance = start.distance(end);
				org.bukkit.Particle particle = org.bukkit.Particle.FLAME;
				boolean isDust = false;
				org.bukkit.Color dustColor = org.bukkit.Color.YELLOW;

				if (_activeBreathColor == ParticleColor.BLUE)
				{
					particle = org.bukkit.Particle.SOUL_FIRE_FLAME;
				}
				else if (_activeBreathColor == ParticleColor.GOLD)
				{
					isDust = true;
					dustColor = org.bukkit.Color.fromRGB(255, 215, 0); // Gold
				}

				for (double d = 0; d < distance; d += 0.5)
				{
					Location point = start.clone().add(direction.clone().multiply(d));
					if (isDust)
					{
						point.getWorld().spawnParticle(org.bukkit.Particle.DUST, point, 2, 0.1, 0.1, 0.1, 0.01, new org.bukkit.Particle.DustOptions(dustColor, 1.0f));
					}
					else
					{
						point.getWorld().spawnParticle(particle, point, 2, 0.1, 0.1, 0.1, 0.01);
						if (_activeBreathColor == ParticleColor.RED)
						{
							point.getWorld().spawnParticle(org.bukkit.Particle.DRAGON_BREATH, point, 1, 0.05, 0.05, 0.05, 0.01, 1.0f);
						}
					}
				}

				if (ticks % 10 == 0)
				{
					start.getWorld().playSound(start, Sound.ENTITY_ENDER_DRAGON_GROWL, 2.0f, 1.0f);
					start.getWorld().playSound(start, Sound.ENTITY_ENDER_DRAGON_SHOOT, 1.5f, 1.0f);
				}
				ticks++;
			}
		}, 0L, 2L);
	}

	public void moveToGuardians(Player player, boolean elimination)
	{
		if (elimination)
		{
			GetTeamList().get(0).SetPlacement(player, PlayerState.OUT);
			GetTeamList().get(0).RemovePlayer(player);
		}
		
		GetTeamList().get(1).AddPlayer(player, true);
		
		DisguiseGuardian disguise = new DisguiseGuardian(player);
		disguise.setName(C.cGray + player.getName());
		disguise.setCustomNameVisible(true);
		
		Manager.GetDisguise().disguise(disguise);
		
		player.getInventory().clear();
		updateSpectatorHotbarItem(player);
		
		player.setGameMode(GameMode.SURVIVAL);
		
		player.setAllowFlight(true);
		player.setFlying(true);
		
		EndCheck();
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPrepare(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Prepare)
			return;
		
		//Add 1 spawn so it doesn't freak out
		ArrayList<Location> spawns = new ArrayList<Location>();
		spawns.add(GetSpectatorLocation());
		
		GameTeam guardians = new GameTeam(this, "Guardians", ChatColor.GRAY, spawns);
		
		AddTeam(guardians);
		
		spawnJudge();
		//GUARDIAN LAZORZ WILL ROXORZ YOUR BOXORZ
		
		ArrayList<Player> players = GetPlayers(true);
		
		for (int i = 0; i < players.size(); i++)
		{
			if (i >= WorldData.GetDataLocs("YELLOW").size())
			{
				GetTeamList().get(0).RemovePlayer(players.get(i));
				Manager.addSpectator(players.get(i), true);
			}
		}
	}
	
	

	@EventHandler
	public void onLive(GameStateChangeEvent event)
	{
		if (!IsLive())
			return;

		resetVotesAndHotbars();
		
		if (WorldData.GetDataLocs("YELLOW").size() < GetTeamList().get(0).GetPlayers(true).size())
		{
			Announce(C.Bold + "Too many players...");
			SetState(GameState.End);
			return;
		}
		
		if (WorldData.GetDataLocs("YELLOW").isEmpty())
		{
			System.out.println("[SpeedBuilders-Error] No YELLOW (island center) locations found in map: " + WorldData.MapName);
			Announce(C.cRed + C.Bold + "Error: No island center markers (YELLOW) found on this map!");
			SetState(GameState.End);
			return;
		}

		if (_nextBuild != null)
			_currentBuild = _nextBuild;
		else
		{
			if (_buildData.isEmpty())
			{
				System.out.println("[SpeedBuilders-Error] No build templates found! Game cannot proceed.");
				Announce(C.cRed + C.Bold + "Error: No build templates found for this game!");
				SetState(GameState.End);
				return;
			}
			List<BuildData> pool = _buildData.stream().filter(b -> !_usedBuilds.contains(b)).collect(Collectors.toList());
			if (pool.isEmpty()) pool = _buildData;
			_currentBuild = UtilAlg.Random(pool);
		}
		
		_nextBuild = null;
		_usedBuilds.add(_currentBuild);
		
		int unmodifiedTime = 40;
		SpeedBuildersStage stage = getActiveStage();
		if (stage == SpeedBuildersStage.WARM_UP) unmodifiedTime = 60;
		else if (stage == SpeedBuildersStage.MAIN_GAME) unmodifiedTime = 45;
		else if (stage == SpeedBuildersStage.SUDDEN_DEATH) unmodifiedTime = 30;
		_buildTime = _currentBuild.getBuildTime(unmodifiedTime);
		
		HashSet<Location> usedBuildLocs = new HashSet<Location>();
		
		for (Player player : GetTeamList().get(0).GetPlayers(true))
		{
			Location buildLoc = UtilAlg.findClosest(player.getLocation(), WorldData.GetDataLocs("YELLOW"));
			Location spawnLoc = GetTeamList().get(0).GetSpawns().isEmpty() ? (buildLoc != null ? buildLoc : player.getLocation()) : UtilAlg.findClosest(buildLoc, GetTeamList().get(0).GetSpawns());
			
			if (buildLoc == null)
			{
				buildLoc = player.getLocation();
			}
			
			_buildRecreations.put(player, new RecreationData(this, player, buildLoc, spawnLoc));
			
			_buildRecreations.get(player).pasteBuildData(_currentBuild);
			
			usedBuildLocs.add(buildLoc);
		}
		
		for (Location loc : WorldData.GetDataLocs("YELLOW"))
		{
			if (!usedBuildLocs.contains(loc))
			{
				HashSet<Block> blocks = findConnectedBlocks(loc.getBlock(), 2000, 8);
				
				Manager.GetExplosion().BlockExplosion(blocks, loc, false, true);
			}
		}
		
		for (Player player : GetTeamList().get(0).GetPlayers(true))
		{
			UtilPlayer.message(player, F.main("Build", "สร้างตามเป้าหมายได้เลย!"));
		}
		
		UtilTextMiddle.display("เป้าหมายรอบนี้คือ", C.cGold + _currentBuild.BuildText, 0, 80, 10);
		
		_roundsPlayed++;
		
		triggerThemeReveal();
		setSpeedBuilderState(SpeedBuildersState.VIEWING);
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		Player player = event.getPlayer();
		if (GetTeamList().size() > 1 && GetTeamList().get(1).HasPlayer(player))
			GetTeamList().get(1).RemovePlayer(player);
		
		if (_buildRecreations.containsKey(player))
		{
			RecreationData recreation = _buildRecreations.remove(player);
			if (recreation != null)
			{
				_disconnectedRecreations.put(player.getUniqueId(), recreation);
				UtilServer.broadcast(C.cYellow + "⚠ " + player.getName() + " disconnected. Island preserved!");
			}
		}
	}

	@EventHandler
	public void onPlayerJoin(org.bukkit.event.player.PlayerJoinEvent event)
	{
		if (!IsLive()) return;
		Player player = event.getPlayer();
		if (_disconnectedRecreations.containsKey(player.getUniqueId()))
		{
			RecreationData recreation = _disconnectedRecreations.remove(player.getUniqueId());
			if (recreation != null)
			{
				recreation.Player = player;
				_buildRecreations.put(player, recreation);
				
				player.teleport(recreation.PlayerSpawn);
				
				if (_state == SpeedBuildersState.REVIEWING)
				{
					player.setGameMode(org.bukkit.GameMode.ADVENTURE);
					player.setAllowFlight(true);
					player.setFlying(true);
					player.setCollidable(false);
				}
				else
				{
					player.setGameMode(org.bukkit.GameMode.SURVIVAL);
					player.setAllowFlight(false);
					player.setFlying(false);
					player.setCollidable(true);
				}
				
				recreation.updateHologramProgress();
				
				UtilPlayer.message(player, C.cGreen + "✓ Welcome back! Your build island has been restored.");
				UtilServer.broadcast(C.cYellow + "✓ " + player.getName() + " reconnected and returned to their island.");
			}
		}
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event)
	{
		if (_state != SpeedBuildersState.BUILDING)
			return;
		
		Player player = event.getPlayer();
		if (!_buildRecreations.containsKey(player))
			return;
		
		if (_perfectBuild.containsKey(player))
		{
			event.setCancelled(true);
			return;
		}
		
		if (_buildRecreations.get(player).isQueuedForDemolition(event.getBlock()))
		{
			event.setCancelled(true);
			return;
		}
		
		RecreationData recreation = _buildRecreations.get(player);
		boolean isBed = event.getBlock().getType().name().endsWith("_BED");
		boolean allowed = false;
		if (recreation.inBuildArea(event.getBlock()) && !isBed)
			allowed = true;
		else if (isBed)
		{
			org.bukkit.block.data.type.Bed bed = (org.bukkit.block.data.type.Bed) event.getBlock().getBlockData();
			if (bed.getPart() == org.bukkit.block.data.type.Bed.Part.HEAD)
			{
				Block foot = event.getBlock().getRelative(bed.getFacing().getOppositeFace());
				if (recreation.inBuildArea(foot))
					allowed = true;
			}
			else
			{
				Block head = event.getBlock().getRelative(bed.getFacing());
				if (recreation.inBuildArea(head))
					allowed = true;
			}
		}
		
		if (allowed)
		{
			int dx = event.getBlock().getX() - recreation.CornerA.getBlockX();
			int dy = event.getBlock().getY() - recreation.CornerA.getBlockY();
			int dz = event.getBlock().getZ() - recreation.CornerA.getBlockZ();
			
			if (dx >= 0 && dx < BuildSize && dy >= 0 && dy < BuildSize && dz >= 0 && dz < BuildSize)
			{
				org.bukkit.block.BlockState expected = _currentBuild.Build[dx][dy][dz];
				if (expected.getType() == event.getBlock().getType())
				{
					player.playSound(event.getBlock().getLocation(), org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.5f);
					event.getBlock().getWorld().spawnParticle(org.bukkit.Particle.HAPPY_VILLAGER, event.getBlock().getLocation().add(0.5, 0.5, 0.5), 8, 0.2, 0.2, 0.2, 0.0);
				}
				else
				{
					player.playSound(event.getBlock().getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_BASS, 0.5f, 0.5f);
				}
			}
			return;
		}
		
		event.setCancelled(true);
		UtilPlayer.message(player, F.main("Build", "Cannot build outside your area!"));
	}

	private void triggerThemeReveal()
	{
		if (_buildMiddle == null)
			return;

		Location center = _buildMiddle.clone().add(0.5, 3.0, 0.5);

		for (Player p : UtilServer.getPlayers())
		{
			p.playSound(p.getEyeLocation(), org.bukkit.Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 0.8f);
			p.playSound(p.getEyeLocation(), org.bukkit.Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1.0f, 1.0f);
		}

		for (int i = 0; i < 3; i++)
		{
			final int idx = i;
			Manager.runSyncLater(() ->
			{
				org.bukkit.entity.Firework fw = center.getWorld().spawn(center.clone().add(0, idx * 2.0, 0), org.bukkit.entity.Firework.class);
				org.bukkit.inventory.meta.FireworkMeta meta = fw.getFireworkMeta();
				org.bukkit.FireworkEffect effect = org.bukkit.FireworkEffect.builder()
					.flicker(true)
					.withColor(org.bukkit.Color.ORANGE, org.bukkit.Color.YELLOW, org.bukkit.Color.RED)
					.withFade(org.bukkit.Color.WHITE)
					.with(org.bukkit.FireworkEffect.Type.BURST)
					.build();
				meta.addEffect(effect);
				meta.setPower(1);
				fw.setFireworkMeta(meta);
			}, i * 10L);
		}
	}

	@EventHandler
	public void onPlayerInteractGriefProtect(PlayerInteractEvent event)
	{
		if (!IsLive() || _state != SpeedBuildersState.BUILDING)
			return;
		if (event.getClickedBlock() == null)
			return;

		Player player = event.getPlayer();
		if (!_buildRecreations.containsKey(player))
			return;

		RecreationData recreation = _buildRecreations.get(player);
		Material mat = event.getClickedBlock().getType();
		String name = mat.name();
		boolean isInteractable = name.contains("DOOR") || name.contains("CHEST") || name.contains("BUTTON") || 
		                         name.contains("LEVER") || name.contains("TRAPDOOR") || name.contains("GATE");

		if (isInteractable && !recreation.inBuildArea(event.getClickedBlock()))
		{
			event.setCancelled(true);
			UtilPlayer.message(player, C.cRed + "✗ You cannot interact outside your island build area!");
		}
	}

	@EventHandler
	public void onBlockBreak(org.bukkit.event.block.BlockBreakEvent event)
	{
		if (_state != SpeedBuildersState.BUILDING)
		{
			event.setCancelled(true);
			return;
		}

		Player player = event.getPlayer();
		if (!_buildRecreations.containsKey(player))
		{
			event.setCancelled(true);
			return;
		}

		if (_perfectBuild.containsKey(player))
		{
			event.setCancelled(true);
			return;
		}

		RecreationData recreation = _buildRecreations.get(player);
		if (recreation.isQueuedForDemolition(event.getBlock()))
		{
			event.setCancelled(true);
			return;
		}

		if (!recreation.inBuildArea(event.getBlock()))
		{
			event.setCancelled(true);
			UtilPlayer.message(player, F.main("Build", "Cannot break blocks outside your area!"));
			return;
		}
	}


	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBuildFinish(final BlockPlaceEvent event)
	{
		checkPerfectBuild(event.getPlayer());
	}

	//This is here because if you open a door then close it you won't be informed of a perfect build
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void interactInformSuccess(PlayerInteractEvent event)
	{
		checkPerfectBuild(event.getPlayer());
	}

	public void checkPerfectBuild(Player player)
	{
		Manager.runSyncLater(() ->
		{
			if (!IsLive() || _state != SpeedBuildersState.BUILDING || !_buildRecreations.containsKey(player))
			{
				return;
			}

			_buildRecreations.get(player).updateHologramProgress();

			if (_perfectBuild.containsKey(player))
			{
				return;
			}

			if (_buildRecreations.get(player).calculateScoreFromBuild(_currentBuild) == _currentBuild.getPerfectScore())
			{
				long timeElapsed = System.currentTimeMillis() - _stateTime;
				PerfectBuildEvent perfectBuildEvent = new PerfectBuildEvent(player, timeElapsed, SpeedBuilders.this);

				Bukkit.getServer().getPluginManager().callEvent(perfectBuildEvent);

				player.playSound(player.getEyeLocation(), Sound.ENTITY_PLAYER_LEVELUP, 10F, 1F);
				player.setWorldBorder(null); // Clear vignette

				RecreationData recreation = _buildRecreations.get(player);
				Location center = recreation.OriginalBuildLocation.clone().add(0.5, 1.0, 0.5);
				for (int i = 0; i < 36; i++)
				{
					double angle = Math.toRadians(i * 10);
					double x = Math.cos(angle) * 3.5;
					double z = Math.sin(angle) * 3.5;
					
					java.awt.Color awtColor = java.awt.Color.getHSBColor(i / 36.0f, 1.0f, 1.0f);
					org.bukkit.Color color = org.bukkit.Color.fromRGB(awtColor.getRed(), awtColor.getGreen(), awtColor.getBlue());
					org.bukkit.Particle.DustOptions dust = new org.bukkit.Particle.DustOptions(color, 1.5f);
					
					center.getWorld().spawnParticle(org.bukkit.Particle.DUST, center.clone().add(x, 0, z), 3, 0.1, 0.2, 0.1, 0.01, dust);
				}

				Location fwLoc = center.clone().add(0, 4, 0);
				org.bukkit.entity.Firework fw = fwLoc.getWorld().spawn(fwLoc, org.bukkit.entity.Firework.class);
				org.bukkit.inventory.meta.FireworkMeta fwm = fw.getFireworkMeta();
				org.bukkit.FireworkEffect effect = org.bukkit.FireworkEffect.builder()
					.flicker(true)
					.withColor(org.bukkit.Color.GREEN, org.bukkit.Color.AQUA, org.bukkit.Color.YELLOW, org.bukkit.Color.FUCHSIA)
					.withFade(org.bukkit.Color.WHITE)
					.with(org.bukkit.FireworkEffect.Type.BALL_LARGE)
					.build();
				fwm.addEffect(effect);
				fwm.setPower(1);
				fw.setFireworkMeta(fwm);

				String time = UtilTime.convertString(timeElapsed, 1, UtilTime.TimeUnit.SECONDS);
				Announce(F.main("Build", F.name(player.getName()) + " สร้างเสร็จภายใน " + F.time(time) + "!"));

				_perfectBuild.put(player, System.currentTimeMillis());

				if (_perfectBuild.size() == _buildRecreations.size())
				{
					// Everyone has a perfect build
					_allPerfect = true;
				}
				else
				{
					// Don't display middle text if everyone now has a perfect build
					UtilTextMiddle.display("ยอดเยี่ยมมาก", C.cGreen + "Perfect Match", 0, 30, 10, player);
				}
			}
		}, 0);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerPickupItem(PlayerPickupItemEvent event)
	{
		if (_state != SpeedBuildersState.BUILDING)
			return;
		
		if (!_buildRecreations.containsKey(event.getPlayer()))
			return;
		
		if (_buildRecreations.get(event.getPlayer()).DroppedItems.containsKey(event.getItem()))
			_buildRecreations.get(event.getPlayer()).DroppedItems.remove(event.getItem());
		else
			event.setCancelled(true);
	}

	@EventHandler
	public void stopItemMerge(ItemMergeEvent event)
	{
		event.setCancelled(true);
	}

	@EventHandler
	public void stopMoveOffArea(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;
		
		if (!IsLive())
			return;
		
		for (Player player : GetPlayers(true))
		{
			if (!_buildRecreations.containsKey(player))
				continue;
			
			RecreationData recreation = _buildRecreations.get(player);
			double dist = UtilMath.offsetSquared(player.getLocation(), recreation.OriginalBuildLocation.clone().add(0.5, 0, 0.5));
			
			for (Location loc : WorldData.GetDataLocs("YELLOW"))
			{
				if (loc.equals(recreation.OriginalBuildLocation))
					continue;
				
				double distFromOther = UtilMath.offsetSquared(player.getLocation(), loc.clone().add(0.5, 0, 0.5));
				
				if (!UtilPlayer.isSpectator(player) && _state != SpeedBuildersState.REVIEWING && (dist > distFromOther || player.getLocation().getY() < recreation.OriginalBuildLocation.getY() - 2))
				{
					player.teleport(recreation.PlayerSpawn);

					UtilPlayer.message(player, F.main("Build", "You cannot leave your area!"));
					UtilTextMiddle.display("", C.cRed + "You cannot leave your area!", 0, 30, 10, player);

					break;
				}
			}
		}
	}

	@EventHandler
	public void stopGuardiansBuildEnter(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTER)
			return;
		
		if (!IsLive())
			return;
		
		for (Player player : GetTeamList().get(1).GetPlayers(false))
		{
			for (RecreationData recreation : _buildRecreations.values())
			{
				Vector vec = UtilAlg.getTrajectory(recreation.getMidpoint(), player.getLocation());
				
				if (UtilMath.offsetSquared(player.getLocation(), recreation.getMidpoint()) < 64)
				{
					Location tpLoc = recreation.getMidpoint().add(vec.clone().multiply(8));
					tpLoc.setDirection(player.getLocation().getDirection());
					
					//First tp out this combats hacked clients with anti-KB
					player.teleport(tpLoc);
					
					//Then apply velocity as normal
					UtilAction.velocity(player, vec, 1.8, false, 0, 0.4, vec.length(), false);
					
					player.playSound(player.getEyeLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 10F, 0.5F);
				}
			}
		}
	}

	@EventHandler
	public void border(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;
		
		if (!InProgress())
			return;
		
		Location specLocation = GetSpectatorLocation();
		
		//This can be done like this cause nobody should be outside
		for (Player player : UtilServer.getPlayers())
		{
			if (!isInsideMap(player))
				player.teleport(specLocation);
		}		
	}

	@EventHandler
	public void stateUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;
		
		if (!IsLive())
			return;
		
		if (_state == SpeedBuildersState.VIEWING)
		{
			if (UtilTime.elapsed(_stateTime, _viewTime * 1000))
			{
				for (RecreationData recreation : _buildRecreations.values())
				{
					recreation.breakAndDropItems();
				}
				
				ItemPickup = true;
				BlockPlace = true;
				BlockBreak = true;
				
				_buildCountStage = 0;
				
				//Sometimes it doesn't show in the update method
				UtilTextMiddle.display("หมดเวลาให้จดจำ", C.cRed + "View Time Over!", 0, 30, 10);
				
				for (Player player : GetTeamList().get(0).GetPlayers(true))
				{
					UtilPlayer.message(player, F.main("Build", "โครงสร้างเป้าหมายปรากฏแล้ว"));
				}
				
				setSpeedBuilderState(SpeedBuildersState.BUILDING);
			}
		}
		else if (_state == SpeedBuildersState.BUILDING)
		{
			if (UtilTime.elapsed(_stateTime, _buildTime * 1000) || _allPerfect)
			{
				for (RecreationData recreation : _buildRecreations.values())
				{
					for (Item item : recreation.DroppedItems.keySet())
					{
						item.remove();
					}
					
					recreation.DroppedItems.clear();
					
					UtilInv.
					Clear(recreation.Player);
				}
				
				UtilTextBottom.displayProgress("Time Left:", 0, UtilTime.MakeStr(0), UtilServer.getPlayers());

				if (_allPerfect)
				{
					UtilTextMiddle.display("", C.cAqua + GUARDIAN_NAME + " is Impressed!", 0, 100, 10);
					_allPerfect = false;
				}
				else
				{
					UtilTextMiddle.display("", C.cRed + "TIME'S UP!", 0, 30, 10);

					Manager.runSyncLater(new Runnable()
					{
						@Override
						public void run()
						{
							UtilTextMiddle.display("", GUARDIAN_NAME + C.cAqua + " กำลังตัดสิน", 0, 40, 10);
						}
					}, 40L);
				}
				
				for (Player player : UtilServer.getPlayers())
				{
					player.showElderGuardian(false);
					player.setWorldBorder(null);
				}
				_lastHeartbeat.clear();
				
				_perfectBuild.clear();
				
				ItemPickup = false;
				BlockPlace = false;
				BlockBreak = false;

				// Close spectator vote shops
				for (Player p : UtilServer.getPlayers())
				{
					if (GetTeamList().size() > 1 && GetTeamList().get(1).HasPlayer(p))
					{
						p.closeInventory();
						p.sendMessage(SpeedBuildersLang.get().get(p, "speedbuilders.gui.vote.ended"));
					}
				}

				// Calculate score and streak
				NautHashMap<Player, Integer> baseScores = new NautHashMap<>();
				NautHashMap<Player, Integer> speedBonuses = new NautHashMap<>();
				NautHashMap<Player, Double> comboMultipliers = new NautHashMap<>();
				NautHashMap<Player, Integer> roundScores = new NautHashMap<>();

				for (RecreationData recreation : _buildRecreations.values())
				{
					Player p = recreation.Player;
					int match = recreation.calculateScoreFromBuild(_currentBuild);
					int perfect = _currentBuild.getPerfectScore();

					double matchRatio = perfect > 0 ? (double) match / perfect : 0.0;
					int baseScore = 0;
					if (match == perfect)
					{
						baseScore = 100;
						_perfectCounts.put(p, (_perfectCounts.containsKey(p) ? _perfectCounts.get(p) : 0) + 1);
					}
					else if (matchRatio >= 0.25)
					{
						baseScore = (int) (matchRatio * 100);
					}

					baseScores.put(p, baseScore);

					int speedBonus = 0;
					if (match == perfect && _perfectBuild.containsKey(p))
					{
						long finishTime = _perfectBuild.get(p);
						long timeElapsed = finishTime - _stateTime;
						long totalTime = _buildTime * 1000L;
						double timeFraction = (double) (totalTime - timeElapsed) / totalTime;
						speedBonus = (int) Math.max(0, timeFraction * 50);
					}
					speedBonuses.put(p, speedBonus);
					Integer sp = _speedBonusCumulative.get(p);
					int currentSp = (sp != null) ? sp : 0;
					_speedBonusCumulative.put(p, currentSp + speedBonus);

					if (match == perfect)
					{
						Integer st = _comboStreaks.get(p);
						int currentSt = (st != null) ? st : 0;
						int newStreak = currentSt + 1;
						_comboStreaks.put(p, newStreak);
					}
					else
					{
						_comboStreaks.put(p, 0); // Reset combo streak on partial/failed
					}

					double comboMult = 1.0;
					Integer stk = _comboStreaks.get(p);
					int streak = (stk != null) ? stk : 0;
					if (streak == 2) comboMult = 1.2;
					else if (streak == 3) comboMult = 1.5;
					else if (streak >= 4) comboMult = 2.0;
					comboMultipliers.put(p, comboMult);

					Integer vt = _votesReceived.get(p);
					int votes = (vt != null) ? vt : 0;
					int creativityBonus = Math.min(30, votes * 10);

					double stageMult = (getActiveStage() == SpeedBuildersStage.SUDDEN_DEATH) ? 2.0 : 1.0;

					int roundScore = (int) ( (baseScore + speedBonus + creativityBonus) * comboMult * stageMult );
					roundScores.put(p, roundScore);

					Integer cc = _cumulativeScores.get(p);
					int currentCumulative = (cc != null) ? cc : 0;
					_cumulativeScores.put(p, currentCumulative + roundScore);

					Integer hs = _highestSingleRoundScore.get(p);
					int highestSingle = (hs != null) ? hs : 0;
					if (roundScore > highestSingle)
					{
						_highestSingleRoundScore.put(p, roundScore);
					}

					p.sendMessage(SpeedBuildersLang.get().get(p, "speedbuilders.chat.score_summary",
						String.valueOf(roundScore), String.valueOf(baseScore), String.valueOf(speedBonus), String.valueOf(comboMult)));
				}

				// Find best builder this round to reward
				Player roundWinner = null;
				int highestRoundScore = -1;
				for (Entry<Player, Integer> entry : roundScores.entrySet())
				{
					if (entry.getValue() > highestRoundScore)
					{
						highestRoundScore = entry.getValue();
						roundWinner = entry.getKey();
					}
				}

				// Check elimination
				boolean isEliminationRound = false;
				SpeedBuildersStage stage = getActiveStage();
				if (stage == SpeedBuildersStage.MAIN_GAME && (_roundsPlayed == 6 || _roundsPlayed == 9))
				{
					isEliminationRound = true;
				}
				else if (stage == SpeedBuildersStage.SUDDEN_DEATH)
				{
					isEliminationRound = true;
				}

				_judgingQueue.clear();
				_judgingColors.clear();
				_toEliminate.clear();

				// Add best builder to judging queue (Gold 🟡 or Blue 🔵 fire)
				if (roundWinner != null && _buildRecreations.containsKey(roundWinner))
				{
					_judgingQueue.add(_buildRecreations.get(roundWinner));
					int match = _buildRecreations.get(roundWinner).calculateScoreFromBuild(_currentBuild);
					if (match == _currentBuild.getPerfectScore())
					{
						_judgingColors.add(ParticleColor.GOLD);
					}
					else
					{
						_judgingColors.add(ParticleColor.BLUE);
					}
				}

				if (isEliminationRound)
				{
					List<Player> aliveBuilders = new ArrayList<>(_buildRecreations.keySet());
					if (!aliveBuilders.isEmpty())
					{
						List<Player> sorted = getSortedPlayers(aliveBuilders);
						Player toIncinerate = sorted.get(sorted.size() - 1);
						RecreationData elRecreation = _buildRecreations.get(toIncinerate);
						
						_toEliminate.add(elRecreation);
						
						// Add eliminated player to queue (Red 🔴 fire)
						if (!_judgingQueue.contains(elRecreation))
						{
							_judgingQueue.add(elRecreation);
							_judgingColors.add(ParticleColor.RED);
						}
						else
						{
							int idx = _judgingQueue.indexOf(elRecreation);
							_judgingColors.set(idx, ParticleColor.RED);
						}
					}
				}

				// Passive judging for the remaining safe players
				for (RecreationData rec : _buildRecreations.values())
				{
					boolean active = false;
					for (RecreationData jRec : _judgingQueue)
					{
						if (jRec.Player.equals(rec.Player))
						{
							active = true;
							break;
						}
					}

					if (!active)
					{
						Integer rScore = roundScores.get(rec.Player);
						int score = (rScore != null) ? rScore : 0;
						spawnPassiveJudgeEffects(rec.Player, score);
					}
				}

				_lastElimination = System.currentTimeMillis();
				_reviewStartTime = System.currentTimeMillis();
				
				pasteBuildInCenter(_currentBuild);
				
				setSpeedBuilderState(SpeedBuildersState.REVIEWING);

				for (Player player : GetTeamList().get(0).GetPlayers(true))
				{
					player.setGameMode(GameMode.ADVENTURE);
					player.setAllowFlight(true);
					player.setFlying(true);
					player.setCollidable(false);
				}
			}
		}
		else if (_state == SpeedBuildersState.REVIEWING)
		{	
			if (_judgingQueue.isEmpty())
			{
				if (!UtilTime.elapsed(_lastElimination, 3000))
					return;
				
				clearCenterArea(true);

				if (!_disconnectedRecreations.isEmpty())
				{
					for (java.util.UUID uuid : new java.util.ArrayList<>(_disconnectedRecreations.keySet()))
					{
						RecreationData recreation = _disconnectedRecreations.remove(uuid);
						if (recreation != null)
						{
							HashSet<Block> blocks = findConnectedBlocks(recreation.OriginalBuildLocation.getBlock(), 2000, 8);
							blocks.addAll(recreation.getBlocks());
							Manager.GetExplosion().BlockExplosion(blocks, recreation.getMidpoint(), false, true);
							recreation.clearBuildArea(false);
							recreation.removeHologram();
							UtilServer.broadcast(C.cRed + "✗ " + recreation.Player.getName() + " failed to reconnect and was eliminated!");
						}
					}
				}
				
				resetVotesAndHotbars();
				
				if (_nextBuild != null)
					_currentBuild = _nextBuild;
				else
				{
					List<BuildData> pool = _buildData.stream().filter(b -> !_usedBuilds.contains(b)).collect(Collectors.toList());
					if (pool.isEmpty()) pool = _buildData;
					_currentBuild = UtilAlg.Random(pool);
				}
				
				_nextBuild = null;
				_usedBuilds.add(_currentBuild);
				
				int unmodifiedTime = 40;
				SpeedBuildersStage stage = getActiveStage();
				if (stage == SpeedBuildersStage.WARM_UP) unmodifiedTime = 60;
				else if (stage == SpeedBuildersStage.MAIN_GAME) unmodifiedTime = 45;
				else if (stage == SpeedBuildersStage.SUDDEN_DEATH) unmodifiedTime = 30;
				_buildTime = _currentBuild.getBuildTime(unmodifiedTime);

				for (Player player : GetTeamList().get(0).GetPlayers(true))
				{
					player.setGameMode(GameMode.SURVIVAL);
					player.setAllowFlight(false);
					player.setFlying(false);
					player.setCollidable(true);
				}
				
				for (RecreationData recreation : _buildRecreations.values())
				{
					recreation.Player.teleport(recreation.PlayerSpawn);
					recreation.pasteBuildData(_currentBuild);
				}
				
				_roundsPlayed++;
				
				_viewCountStage = 0;
				
				for (Player player : GetTeamList().get(0).GetPlayers(true))
				{
					UtilPlayer.message(player, F.main("Build", "สร้างตามเป้าหมายได้เลย!"));
				}
				
				UtilTextMiddle.display("เป้าหมายรอบนี้คือ", C.cGold + _currentBuild.BuildText, 0, 80, 10);
				
				triggerThemeReveal();
				setSpeedBuilderState(SpeedBuildersState.VIEWING);
			}
			else
			{
				if (UtilTime.elapsed(_reviewStartTime, 3000) && UtilTime.elapsed(_lastElimination, 3000) && !_eliminating)
				{
					final RecreationData currentTarget = _judgingQueue.get(0);
					final ParticleColor color = _judgingColors.get(0);
					_activeBreathColor = color;
					
					judgeTargetLocation(currentTarget.OriginalBuildLocation.clone().subtract(0, 1.7, 0));
					
					// Suspense title before dragon breathes fire
					UtilTextMiddle.display(C.cYellow + currentTarget.Player.getName(), C.cWhite + "กำลังตัดสิน... / Judging...", 0, 30, 10);
					
					for (Player p : UtilServer.getPlayers())
					{
						p.playSound(p.getEyeLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 2.0F, 1.0F);
					}
					
					_eliminating = true;
					
					Manager.runSyncLater(new Runnable()
					{
						@Override
						public void run()
						{
							_lastElimination = System.currentTimeMillis();
							_eliminating = false;
							
							if (color == ParticleColor.RED)
							{
								UtilTextMiddle.display("", C.cRed + currentTarget.Player.getName() + " ถูกคัดออกแล้ว!", 0, 40, 10);
								currentTarget.getMidpoint().getWorld().strikeLightningEffect(currentTarget.OriginalBuildLocation);
								for (Player player : UtilServer.getPlayers())
								{
									player.playSound(player.getEyeLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 2.0f, 0.5f);
									player.playSound(player.getEyeLocation(), Sound.ENTITY_ZOMBIE_VILLAGER_CURE, 1F, 1F);
								}
								currentTarget.getMidpoint().getWorld().spawnParticle(org.bukkit.Particle.EXPLOSION_EMITTER, currentTarget.getMidpoint(), 1);
								
								HashSet<Block> blocks = findConnectedBlocks(currentTarget.OriginalBuildLocation.getBlock(), 2000, 8);
								blocks.addAll(currentTarget.getBlocks());

								Manager.GetExplosion().BlockExplosion(blocks, currentTarget.getMidpoint(), false, true);
								
								currentTarget.clearBuildArea(false);
								currentTarget.removeHologram();
								
								judgeTargetLocation(null);
								
								_buildRecreations.remove(currentTarget.Player);
								moveToGuardians(currentTarget.Player, true);
								
								Announce(SpeedBuildersLang.get().get(null, "speedbuilders.eliminated", currentTarget.Player.getName()));
							}
							else if (color == ParticleColor.GOLD)
							{
								UtilTextMiddle.display("", C.cGold + currentTarget.Player.getName() + " Perfect Match!", 0, 40, 10);
								judgeTargetLocation(null);
							}
							else
							{
								UtilTextMiddle.display("", C.cAqua + currentTarget.Player.getName() + " ผ่านรอบนี้!", 0, 40, 10);
								judgeTargetLocation(null);
							}
							
							_judgingQueue.remove(0);
							_judgingColors.remove(0);
						}
					}, 100L);
				}
			}
		}
	}

	@EventHandler
	public void updateJudgeFlight(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;
		
		if (!IsLive())
			return;
		
		if (_judgeEntity == null || !_judgeEntity.isValid())
			return;

		Location center = _judgeSpawn;
		if (center == null)
			return;

		// Always update the orbit angle so the orbit path stays active
		_dragonAngle += 0.025;
		if (_dragonAngle > 2 * Math.PI)
		{
			_dragonAngle -= 2 * Math.PI;
		}

		// Calculate the current orbit location and direction
		double radius = 15.0;
		double orbitX = center.getX() + radius * Math.cos(_dragonAngle);
		double orbitZ = center.getZ() + radius * Math.sin(_dragonAngle);
		Location orbitLoc = new Location(center.getWorld(), orbitX, center.getY(), orbitZ);
		double dx = -Math.sin(_dragonAngle);
		double dz = Math.cos(_dragonAngle);
		Vector orbitDirection = new Vector(dx, 0, dz).normalize();
		orbitLoc.setDirection(orbitDirection);
		orbitLoc.setYaw(orbitLoc.getYaw() + 180f); // Fix dragon rendering backwards

		switch (_judgeState)
		{
			case ORBITING:
				_judgeEntity.setVelocity(new org.bukkit.util.Vector(0, 0, 0));
				_judgeEntity.teleport(orbitLoc);
				break;

			case GLIDING_TO_TARGET:
				_transitionTicks++;
				double tTarget = Math.min(1.0, (double) _transitionTicks / TRANSITION_DURATION);

				double xTarget = lerp(_startTransitionLoc.getX(), _targetTransitionLoc.getX(), tTarget);
				double yTarget = lerp(_startTransitionLoc.getY(), _targetTransitionLoc.getY(), tTarget);
				double zTarget = lerp(_startTransitionLoc.getZ(), _targetTransitionLoc.getZ(), tTarget);

				// Look at the target build
				Location targetOrientationLoc = _targetTransitionLoc.clone();
				Vector faceTarget = UtilAlg.getTrajectory(_targetTransitionLoc, _targetLocForBreath);
				targetOrientationLoc.setDirection(faceTarget);
				targetOrientationLoc.setYaw(targetOrientationLoc.getYaw() + 180f); // Fix model backwards

				float smoothYawTarget = lerpAngle(_startTransitionLoc.getYaw(), targetOrientationLoc.getYaw(), (float) tTarget);
				float smoothPitchTarget = lerpAngle(_startTransitionLoc.getPitch(), targetOrientationLoc.getPitch(), (float) tTarget);

				Location nextLocTarget = new Location(_judgeEntity.getWorld(), xTarget, yTarget, zTarget, smoothYawTarget, smoothPitchTarget);
				_judgeEntity.setVelocity(new org.bukkit.util.Vector(0, 0, 0));
				_judgeEntity.teleport(nextLocTarget);

				if (tTarget >= 1.0)
				{
					_judgeState = JudgeState.BREATHING;
					startBreathAttack(_targetLocForBreath);
				}
				break;

			case BREATHING:
				// Stay at the target location facing the build
				Location breathingLoc = _targetTransitionLoc.clone();
				Vector faceBreathing = UtilAlg.getTrajectory(_targetTransitionLoc, _targetLocForBreath);
				breathingLoc.setDirection(faceBreathing);
				breathingLoc.setYaw(breathingLoc.getYaw() + 180f); // Fix model backwards
				_judgeEntity.setVelocity(new org.bukkit.util.Vector(0, 0, 0));
				_judgeEntity.teleport(breathingLoc);
				break;

			case GLIDING_TO_ORBIT:
				_transitionTicks++;
				double tOrbit = Math.min(1.0, (double) _transitionTicks / TRANSITION_DURATION);

				double xOrbit = lerp(_startTransitionLoc.getX(), orbitLoc.getX(), tOrbit);
				double yOrbit = lerp(_startTransitionLoc.getY(), orbitLoc.getY(), tOrbit);
				double zOrbit = lerp(_startTransitionLoc.getZ(), orbitLoc.getZ(), tOrbit);

				float smoothYawOrbit = lerpAngle(_startTransitionLoc.getYaw(), orbitLoc.getYaw(), (float) tOrbit);
				float smoothPitchOrbit = lerpAngle(_startTransitionLoc.getPitch(), orbitLoc.getPitch(), (float) tOrbit);

				Location nextLocOrbit = new Location(_judgeEntity.getWorld(), xOrbit, yOrbit, zOrbit, smoothYawOrbit, smoothPitchOrbit);
				_judgeEntity.setVelocity(new org.bukkit.util.Vector(0, 0, 0));
				_judgeEntity.teleport(nextLocOrbit);

				if (tOrbit >= 1.0)
				{
					_judgeState = JudgeState.ORBITING;
				}
				break;
		}
	}

	private String getPercentPrefix(int percent)
	{
		if (percent >= 75)
			return C.cAqua;
		else if (percent >= 50)
			return C.cGreen;
		else if (percent >= 25)
			return C.cYellow;
		else
			return C.cRed;
	}

	private void spawnPassiveJudgeEffects(Player player, int score)
	{
		org.bukkit.Location loc = player.getLocation().add(0, 2, 0);

		// 1. Floating Text (Subtitle / Title)
		UtilTextMiddle.display("", C.cGreen + "+" + score + " pts", 0, 40, 10, player);

		// 2. Particle Ring & Sounds
		for (int i = 0; i < 3; i++)
		{
			final int index = i;
			Manager.runSyncLater(() ->
			{
				if (player.isOnline())
				{
					player.getWorld().spawnParticle(org.bukkit.Particle.SOUL_FIRE_FLAME, loc, 30, 1.5, 0.5, 1.5, 0.05);
					player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.0f, 1.0f + index * 0.1f);
				}
			}, i * 10L);
		}

		// 3. ActionBar & Chat
		UtilTextBottom.display(C.cAqua + "🐉 The Dragon nods at your build! " + C.cGreen + "(+" + score + " pts)", player);
		UtilPlayer.message(player, C.cAqua + "🐉 The Dragon nods at your build! " + C.cGreen + "(+" + score + " pts)");
	}

	@EventHandler
	public void buildTimeProgressBar(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;
		
		if (!IsLive())
			return;
		
		if (_state != SpeedBuildersState.BUILDING)
			return;
		
		long timeLeft = 1000 * _buildTime - (System.currentTimeMillis() - _stateTime);
		
		if (timeLeft < 0)
			timeLeft = 0;
		
		double timeFraction = (double) timeLeft / (_buildTime * 1000.0D);
		String timeLeftStr = UtilTime.MakeStr(timeLeft);

		for (Player p : UtilServer.getPlayers())
		{
			if (_buildRecreations.containsKey(p))
			{
				RecreationData recreation = _buildRecreations.get(p);
				int match = recreation.calculateScoreFromBuild(_currentBuild);
				int perfect = _currentBuild.getPerfectScore();
				int percent = perfect > 0 ? (int) (((double) match / perfect) * 100d) : 0;
				
				int speedBonus = 0;
				if (_perfectBuild.containsKey(p))
				{
					p.setWorldBorder(null);
					long finishTime = _perfectBuild.get(p);
					long timeElapsed = finishTime - _stateTime;
					long totalTime = _buildTime * 1000L;
					double fraction = (double) (totalTime - timeElapsed) / totalTime;
					speedBonus = (int) Math.max(0, fraction * 50);
				}
				else
				{
					speedBonus = (int) Math.max(0, timeFraction * 50);
					if (timeLeft <= 10000 && timeLeft > 0)
					{
						long now = System.currentTimeMillis();
						long lastHeart = _lastHeartbeat.containsKey(p) ? _lastHeartbeat.get(p) : 0L;
						if (now - lastHeart >= 1000)
						{
							_lastHeartbeat.put(p, now);
							p.playSound(p.getLocation(), org.bukkit.Sound.ENTITY_WARDEN_HEARTBEAT, 1.0f, 1.0f);
						}

						if (p.getWorldBorder() == null || p.getWorldBorder().getSize() > 9000)
						{
							org.bukkit.WorldBorder wb = org.bukkit.Bukkit.createWorldBorder();
							wb.setCenter(p.getLocation());
							wb.setSize(10000);
							wb.setWarningDistance(10000);
							p.setWorldBorder(wb);
						}
					}
				}
				
				String message = SpeedBuildersLang.get().get(p, "speedbuilders.actionbar.progress", String.valueOf(percent), String.valueOf(speedBonus));
				message = message + " §7| §f" + timeLeftStr;
				
				UtilTextBottom.display(message, p);
			}
			else
			{
				String message = "§e§lTIME LEFT §7| §f" + timeLeftStr;
				UtilTextBottom.display(message, p);
			}
		}
	}

	@EventHandler
	public void buildEndCountdown(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;
		
		if (!IsLive())
			return;
		
		if (_state != SpeedBuildersState.BUILDING)
			return;
		
		if (UtilTime.elapsed(_stateTime, 1000 * _buildCountStage))
		{
			ArrayList<Player> players = new ArrayList<Player>(UtilServer.getServer().getOnlinePlayers());
			
			for (Entry<Player, Long> entry : _perfectBuild.entrySet())
			{
				if (!UtilTime.elapsed(entry.getValue(), 5000))
					players.remove(entry.getKey());
			}
			
			if (_buildCountStage == _buildTime)
				UtilTextMiddle.display("", C.cRed + "TIME'S UP!", 0, 30, 10);
			else if (_buildCountStage >= _buildTime - 5)
				UtilTextMiddle.display("", C.cGreen + (_buildTime - _buildCountStage), 0, 30, 10, players.toArray(new Player[players.size()]));
			
			if (_buildCountStage >= _buildTime - 5)
			{
				for (Player player : UtilServer.getPlayers())
				{
					player.playSound(player.getEyeLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1F, 1F - (float) (0.1 * (_buildTime - _buildCountStage)));
				}
			}
			
			_buildCountStage++;
		}
	}

	@EventHandler
	public void viewCountdown(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;
		
		if (!IsLive())
			return;
		
		if (_state != SpeedBuildersState.VIEWING)
			return;
		
		if (UtilTime.elapsed(_stateTime, _viewCountStage * 1000))
		{
			if (_viewCountStage == _viewTime)
				UtilTextMiddle.display("หมดเวลาให้จดจำ", C.cRed + "View Time Over!", 0, 30, 10);
			else if (_viewCountStage > 3)
				UtilTextMiddle.display("", C.cGreen + (_viewTime - _viewCountStage), 0, 30, 10);
			
			if (_viewCountStage > 3)
			{
				for (Player player : UtilServer.getPlayers())
				{
					player.playSound(player.getEyeLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1F, 1F - (float) (0.1 * (_viewTime - _viewCountStage)));
				}
			}
			
			_viewCountStage++;
		}
	}

	@EventHandler
	public void markBlockForDemolition(PlayerInteractEvent event)
	{
		if (_state != SpeedBuildersState.BUILDING)
			return;
		
		if (!_buildRecreations.containsKey(event.getPlayer()))
			return;
		
		if (!UtilEvent.isAction(event, ActionType.L_BLOCK))
			return;
		
		if (_perfectBuild.containsKey(event.getPlayer()))
			return;
		
		if (!_buildRecreations.get(event.getPlayer()).inBuildArea(event.getClickedBlock()))
			return;
		
		if (event.getClickedBlock().getType() == Material.AIR)
			return;
		
		_buildRecreations.get(event.getPlayer()).addToDemolition(event.getClickedBlock());
	}

	@EventHandler
	public void markMobForDemolition(EntityDamageByEntityEvent event)
	{
		if (!(event.getDamager() instanceof Player))
			return;
		
		Player player = (Player) event.getDamager();
		
		if (_state != SpeedBuildersState.BUILDING)
			return;
		
		if (!_buildRecreations.containsKey(player))
			return;
		
		if (_perfectBuild.containsKey(player))
			return;
		
		if (!_buildRecreations.get(player).inBuildArea(event.getEntity().getLocation()))
			return;
		
		boolean hasMobType = false;
		
		for (MobData mobData : _currentBuild.Mobs)
		{
			if (mobData.EntityType == event.getEntityType())
			{
				hasMobType = true;
				
				break;
			}
		}
		
		if (!hasMobType)
			return;
		
		_buildRecreations.get(player).addToDemolition(event.getEntity());
	}

	@EventHandler
	public void stopBabyEgg(PlayerInteractEntityEvent event)
	{
		if (!IsLive())
			return;
		
		if (event.getPlayer().getInventory().getItemInMainHand().getType().name().endsWith("_SPAWN_EGG"))
			event.setCancelled(true);
	}

	@EventHandler
	public void updateDemolitionBlocks(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;
		
		for (RecreationData recreation : _buildRecreations.values())
		{
			ArrayList<DemolitionData> blocksForDemolition = new ArrayList<DemolitionData>(recreation.BlocksForDemolition);
			
			for (DemolitionData demolition : blocksForDemolition)
			{
				if (_state != SpeedBuildersState.BUILDING || _perfectBuild.containsKey(demolition.Parent.Player))
					demolition.cancelBreak();
				else
					demolition.update();
			}
		}
	}

	@EventHandler
	public void preventBlockGrowth(BlockGrowEvent event)
	{
		event.setCancelled(true);
	}

	@EventHandler
	public void preventStructureGrowth(StructureGrowEvent event)
	{
		event.setCancelled(true);
	}



	private void moveEntity(Location loc, Entity entity)
	{
		entity.teleport(loc);
	}

	@EventHandler
	public void specNightVision(UpdateEvent event)
	{
		if (!InProgress())
			return;
		
		if (event.getType() != UpdateType.SEC)
			return;
		
		for (Player player : UtilServer.getPlayers())
		{
			if (UtilPlayer.isSpectator(player) || (GetTeamList().size() > 1 && GetTeamList().get(1).HasPlayer(player)))
			{
				player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0, true, false), true);
				player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, Integer.MAX_VALUE, 0, true, false), true);
			}
		}
	}

//	@EventHandler
//	public void stopJudgeUnspec(UpdateEvent event)
//	{
//		if (event.getType() != UpdateType.TICK)
//			return;
//		
//		if (!IsLive())
//			return;
//		
//		if (_state != SpeedBuilderState.REVIEWING)
//			return;
//		
//		for (Player player : UtilServer.getPlayers())
//		{
//			player.setGameMode(GameMode.SPECTATOR);
//			player.setSpectatorTarget(_judgeEntity);
//			
//			if (!Manager.GetCondition().HasCondition(player, ConditionType.CLOAK, "Guardian POV"))
//				Manager.GetCondition().Factory().Cloak("Guardian POV", player, null, 999999999, false, false);
//		}
//	}

	@EventHandler
	public void stopGuardianSpecPickup(PlayerPickupItemEvent event)
	{
		if (GetState().ordinal() < GameState.Prepare.ordinal())
			return;
		
		if (Manager.isSpectator(event.getPlayer()) || (GetTeamList().size() > 1 && GetTeamList().get(1).HasPlayer(event.getPlayer())))
			event.setCancelled(true);
	}

	@EventHandler
	public void stopGuardianSpecPlace(BlockPlaceEvent event)
	{
		if (GetState().ordinal() < GameState.Prepare.ordinal())
			return;
		
		if (Manager.isSpectator(event.getPlayer()) || (GetTeamList().size() > 1 && GetTeamList().get(1).HasPlayer(event.getPlayer())))
			event.setCancelled(true);
	}

	@EventHandler
	public void stopEntityChangeBlock(EntityChangeBlockEvent event)
	{
		if (event.getEntityType() == EntityType.ENDER_DRAGON)
		{
			event.setCancelled(true);
			return;
		}

		if (!IsLive())
			return;
		
		// Falling blocks disappear for some reason so we update to make it reappear
		event.getBlock().getState().update(true, false);
		
		event.setCancelled(true);
	}

	@EventHandler
	public void stopDragonExplosion(EntityExplodeEvent event)
	{
		if (event.getEntityType() == EntityType.ENDER_DRAGON)
		{
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void stopBlockFade(BlockFadeEvent event)
	{
		if (!IsLive())
			return;
		
		event.setCancelled(true);
	}

	@EventHandler
	public void stopBlockBurn(BlockBurnEvent event)
	{
		if (!IsLive())
			return;
		
		event.setCancelled(true);
	}

	@EventHandler
	public void stopLeavesDecay(LeavesDecayEvent event)
	{
		if (!IsLive())
			return;
		
		event.setCancelled(true);
	}

	@EventHandler
	public void stopBlockForm(BlockFormEvent event)
	{
		if (!IsLive())
			return;
		
		event.setCancelled(true);
	}

	@EventHandler
	public void stopBlockSpread(BlockSpreadEvent event)
	{
		if (!IsLive())
			return;
		
		event.setCancelled(true);
	}

	@EventHandler
	public void stopLiquidLeaks(BlockFromToEvent event)
	{
		for (RecreationData recreation : _buildRecreations.values())
		{
			if ((recreation.inBuildArea(event.getBlock()) && !recreation.inBuildArea(event.getToBlock())) || (!recreation.inBuildArea(event.getBlock()) && recreation.inBuildArea(event.getToBlock())))
				event.setCancelled(true);
		}
	}

	@EventHandler
	public void stopPhysics(BlockPhysicsEvent event)
	{
		if (!IsLive())
			return;
		
		if (event.getBlock().isLiquid())
			return;
		
		event.setCancelled(true);
	}

	@EventHandler
	public void stopInventoryPickup(InventoryPickupItemEvent event)
	{
		if (!IsLive())
			return;
		
		event.setCancelled(true);
	}

	@EventHandler
	public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event)
	{
		if (!IsLive())
			return;
		
		if (_state != SpeedBuildersState.BUILDING)
			return;
		
		if (!_buildRecreations.containsKey(event.getPlayer()))
			return;
		
		if (_perfectBuild.containsKey(event.getPlayer()))
		{
			event.setCancelled(true);
			return;
		}
		
		Block liquid = event.getBlockClicked().getRelative(event.getBlockFace());
		
		if (!_buildRecreations.get(event.getPlayer()).inBuildArea(liquid))
		{
			event.setCancelled(true);
			
			UtilPlayer.message(event.getPlayer(), F.main("Build", "Cannot build outside your area!"));
		}
		else 
		{
			if (liquid.getType() == Material.WATER)
			{
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onPlayerBucketFill(PlayerBucketFillEvent event)
	{
		if (!IsLive())
			return;
		
		if (_state != SpeedBuildersState.BUILDING)
			return;
		
		if (!_buildRecreations.containsKey(event.getPlayer()))
			return;
		
		if (_perfectBuild.containsKey(event.getPlayer()))
		{
			event.setCancelled(true);
			return;
		}
		
		Block liquid = event.getBlockClicked().getRelative(event.getBlockFace());
		
		if (!_buildRecreations.get(event.getPlayer()).inBuildArea(liquid))
		{
			event.setCancelled(true);
			
			UtilPlayer.message(event.getPlayer(), F.main("Build", "Cannot build outside your area!"));
		}
	}

	@EventHandler
	public void addMob(PlayerInteractEvent event)
	{
		if (!IsLive())
			return;
		
		if (_state != SpeedBuildersState.BUILDING)
			return;
		
		if (!UtilEvent.isAction(event, ActionType.R_BLOCK))
			return;
		
		if (event.getItem() == null)
			return;
		
		EntityType type = getEntityTypeFromSpawnEgg(event.getItem().getType());
		if (type == null)
			return;
		
		if (!_buildRecreations.containsKey(event.getPlayer()))
			return;
		
		Block block = event.getClickedBlock().getRelative(event.getBlockFace());
		
		if (!_buildRecreations.get(event.getPlayer()).inBuildArea(block))
			return;
		
		CreatureAllowOverride = true;
		
		Entity entity = block.getWorld().spawnEntity(block.getLocation().add(0.5, 0, 0.5), type);
		
		UtilEnt.Vegetate(entity, true);
		UtilEnt.ghost(entity, true, false);
		
		CreatureAllowOverride = false;
		
		_buildRecreations.get(event.getPlayer()).Mobs.add(entity);
		
		UtilInv.remove(event.getPlayer(), event.getItem().getType(), (byte) 0, 1);
	}

	@EventHandler
	public void stopCombust(EntityCombustEvent event)
	{
		if (!IsLive())
			return;
		
		event.setCancelled(true);
	}

	@EventHandler
	public void moveSetFlight(PlayerMoveEvent event)
	{
		if (!IsLive())
			return;
		
		if (_state != SpeedBuildersState.BUILDING)
			return;
		
		if (GetTeamList().isEmpty() || !GetTeamList().get(0).HasPlayer(event.getPlayer()))
			return;
		
		if (UtilEnt.isGrounded(event.getPlayer()) && !event.getPlayer().isFlying())
			event.getPlayer().setAllowFlight(true);
	}

	@EventHandler
	public void flightToggleJump(PlayerToggleFlightEvent event)
	{
		if (!IsLive())
			return;
		
		if (_state != SpeedBuildersState.BUILDING)
			return;
		
		if (GetTeamList().isEmpty() || !GetTeamList().get(0).HasPlayer(event.getPlayer()))
			return;
		
		event.setCancelled(true);
		
		event.getPlayer().setAllowFlight(false);
		
		event.getPlayer().playSound(event.getPlayer().getEyeLocation(), Sound.ENTITY_GHAST_SHOOT, 1f, 1f);
		
		event.getPlayer().setVelocity(new Vector(0, 1, 0));
	}
	
	@EventHandler
	public void fixDoorToggling(PlayerInteractEvent event)
	{
		if (!IsLive())
			return;
		
		if (_state != SpeedBuildersState.BUILDING)
			return;
		
		if (!_buildRecreations.containsKey(event.getPlayer()))
			return;
		
		if (_perfectBuild.containsKey(event.getPlayer()))
		{
			event.setCancelled(true);
			return;
		}

		if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.LEFT_CLICK_BLOCK)
		{
			return;
		}

		Block clickedBlock = event.getClickedBlock();
		if (clickedBlock == null)
		{
			return;
		}

		RecreationData recreation = _buildRecreations.get(event.getPlayer());

		if (event.getAction() == Action.LEFT_CLICK_BLOCK)
		{
			if (!recreation.inBuildArea(clickedBlock))
			{
				event.setCancelled(true);
			}
			return;
		}

		// Action.RIGHT_CLICK_BLOCK
		ItemStack item = event.getItem();
		boolean isPlaceable = item != null && (item.getType().isBlock() 
				|| item.getType().name().endsWith("_BUCKET") 
				|| item.getType().name().endsWith("_SPAWN_EGG") 
				|| item.getType() == Material.BONE_MEAL);

		boolean isInteract = clickedBlock.getType().isInteractable() && !event.getPlayer().isSneaking();
		Block targetBlock = (isPlaceable && !isInteract) ? clickedBlock.getRelative(event.getBlockFace()) : clickedBlock;

		if (!recreation.inBuildArea(targetBlock))
		{
			event.setCancelled(true);
			if (isPlaceable && !isInteract)
			{
				UtilPlayer.message(event.getPlayer(), F.main("Build", "Cannot build outside your area!"));
			}
		}
	}

	@Override
	public void EndCheck()
	{
		if (!IsLive())
			return;
		
		GameTeam playersTeam = GetTeamList().get(0);

		if (playersTeam.GetPlayers(true).size() <= 1)
		{	
			List<Player> places = playersTeam.GetPlacements(true);
			
			//Announce
			AnnounceEnd(places);

			//Gems
			if (places.size() >= 1)
				AddGems(places.get(0), 20, "1st Place", false, false);

			if (places.size() >= 2)
				AddGems(places.get(1), 15, "2nd Place", false, false);

			if (places.size() >= 3)
				AddGems(places.get(2), 10, "3rd Place", false, false);

			ArrayList<Player> participants = new ArrayList<Player>();
			
			ArrayList<Player> guardians = GetTeamList().get(1).GetPlayers(false);
			
			participants.addAll(playersTeam.GetPlayers(true));
			
			guardians.retainAll(playersTeam.GetPlacements(true));
			
			participants.addAll(guardians);
			
			for (Player player : participants)
				if (player.isOnline())
					AddGems(player, 10, "Participation", false, false);

			//End
			SetState(GameState.End);
		}
	}

	@Override
	public List<Player> getLosers()
	{
		List<Player> winners = getWinners();

		if (winners == null)
			return null;
		
		if (GetTeamList().size() < 2)
			return new ArrayList<Player>();

		List<Player> losers = GetTeamList().get(1).GetPlayers(false);

		losers.removeAll(winners);
		losers.retainAll(GetTeamList().get(0).GetPlacements(true));

		return losers;
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
		
		List<Player> playersAlive = GetTeamList().get(0).GetPlayers(true);
		List<Player> playersDead = new ArrayList<Player>();
		if (GetTeamList().size() > 1)
			playersDead.addAll(GetTeamList().get(1).GetPlayers(false));
		
		// Use player context for localizing their scoreboard if possible, or fallback to default
		// Bukkit scoreboard is shared, so we render a generic bilingual or English-based fallback scoreboard.
		// Wait, Spigot Scoreboard is shared per-player or global? In HouziCore's Board library, each player has a personalized board!
		// Let's check: Scoreboard.Write(Player, String) or Scoreboard.Write(String)?
		// The original code uses Scoreboard.Write(String). This means HouziCore Scoreboard is global (shared) or Board is per-player but drawn globally.
		// Wait, if it draws globally, we can use bilingual formatting (EN / TH) on the same line or use default locale.
		// Let's do elegant bilingual naming or clean presentation. E.g., title: "SPEED BUILDERS", Round: "Round/รอบ", Stage: "Stage/ช่วง"
		// Better yet, since we have the LangManager, we can use the English fallback for global display, but with beautiful style.
		
		Scoreboard.WriteBlank();
		Scoreboard.Write(C.cYellow + C.Bold + "STAGE");
		SpeedBuildersStage stage = getActiveStage();
		if (stage == SpeedBuildersStage.WARM_UP)
			Scoreboard.Write(C.cGreen + "Warm-up");
		else if (stage == SpeedBuildersStage.MAIN_GAME)
			Scoreboard.Write(C.cGold + "Main Game");
		else
			Scoreboard.Write(C.cRed + "Sudden Death");
		
		Scoreboard.WriteBlank();
		Scoreboard.Write(C.cYellow + C.Bold + "ROUND");
		Scoreboard.Write(C.cWhite + String.valueOf(_roundsPlayed));
		
		if (_state == SpeedBuildersState.BUILDING)
		{
			Scoreboard.WriteBlank();
			Scoreboard.Write(C.cYellow + C.Bold + "TIME LEFT");
			Scoreboard.Write(C.cGreen + "Building: " + (_buildTime - _buildCountStage) + "s");
		}
		
		Scoreboard.WriteBlank();
		Scoreboard.Write(C.cYellow + C.Bold + "RANKINGS");
		
		List<Player> sorted = getSortedPlayers(playersAlive);
		int rank = 1;
		for (Player p : sorted)
		{
			Integer cm = _comboStreaks.get(p);
			int combo = (cm != null) ? cm : 0;
			String prefix = "";
			if (rank == 1) prefix = "🥇 ";
			else if (rank == 2) prefix = "🥈 ";
			else if (rank == 3) prefix = "🥉 ";
			else prefix = rank + ". ";
			
			String comboSuffix = combo > 0 ? " §d(x" + combo + ")" : "";
			Scoreboard.Write(prefix + C.cWhite + p.getName() + comboSuffix);
			rank++;
		}
		
		for (Player p : playersDead)
		{
			Scoreboard.Write(C.cDGray + C.Strike + p.getName());
		}
		
		Scoreboard.Draw();
	}

	public Location getJudgeSpawn()
	{
		return _judgeSpawn;
	}

	private HashSet<Block> findConnectedBlocks(Block start, int limit, int radius)
	{
		HashSet<Block> visited = new HashSet<>();
		java.util.Queue<Block> queue = new java.util.LinkedList<>();
		queue.add(start);
		visited.add(start);
		
		Location startLoc = start.getLocation();
		org.bukkit.block.BlockFace[] faces = {
			org.bukkit.block.BlockFace.UP, org.bukkit.block.BlockFace.DOWN,
			org.bukkit.block.BlockFace.NORTH, org.bukkit.block.BlockFace.SOUTH,
			org.bukkit.block.BlockFace.EAST, org.bukkit.block.BlockFace.WEST
		};
		
		while (!queue.isEmpty() && visited.size() < limit)
		{
			Block current = queue.poll();
			for (org.bukkit.block.BlockFace face : faces)
			{
				Block next = current.getRelative(face);
				if (next.getType() == Material.AIR) continue;
				if (visited.contains(next)) continue;
				if (UtilMath.offset(startLoc, next.getLocation()) > radius) continue;
				
				visited.add(next);
				queue.add(next);
			}
		}
		return visited;
	}

	private EntityType getEntityTypeFromSpawnEgg(Material material)
	{
		String name = material.name();
		if (!name.endsWith("_SPAWN_EGG")) return null;
		String entityName = name.substring(0, name.length() - 10);
		if (entityName.equals("MOOSHROOM")) entityName = "MUSHROOM_COW";
		try
		{
			return EntityType.valueOf(entityName);
		}
		catch (IllegalArgumentException e)
		{
			return null;
		}
	}

	public NautHashMap<Player, Integer> getCumulativeScores()
	{
		return _cumulativeScores;
	}

	public NautHashMap<Player, Integer> getComboStreaks()
	{
		return _comboStreaks;
	}

	public NautHashMap<Player, Integer> getVotesReceived()
	{
		return _votesReceived;
	}

	public NautHashMap<Player, Player> getSpectatorVotes()
	{
		return _spectatorVotes;
	}

	public NautHashMap<Player, RecreationData> getBuildRecreations()
	{
		return _buildRecreations;
	}

	public SpeedBuildersStage getActiveStage()
	{
		if (_roundsPlayed <= 1) return SpeedBuildersStage.WARM_UP;
		if (_roundsPlayed <= 9) return SpeedBuildersStage.MAIN_GAME;
		return SpeedBuildersStage.SUDDEN_DEATH;
	}

	private List<Player> getSortedPlayers(List<Player> players)
	{
		List<Player> sorted = new ArrayList<>(players);
		sorted.sort((p1, p2) -> {
			Integer s1 = _cumulativeScores.get(p1);
			int score1 = (s1 != null) ? s1 : 0;
			Integer s2 = _cumulativeScores.get(p2);
			int score2 = (s2 != null) ? s2 : 0;
			if (score1 != score2) return Integer.compare(score2, score1);

			Integer pc1 = _perfectCounts.get(p1);
			int perf1 = (pc1 != null) ? pc1 : 0;
			Integer pc2 = _perfectCounts.get(p2);
			int perf2 = (pc2 != null) ? pc2 : 0;
			if (perf1 != perf2) return Integer.compare(perf2, perf1);

			Integer sp1 = _speedBonusCumulative.get(p1);
			int speed1 = (sp1 != null) ? sp1 : 0;
			Integer sp2 = _speedBonusCumulative.get(p2);
			int speed2 = (sp2 != null) ? sp2 : 0;
			double avgSpeed1 = (double) speed1 / Math.max(1, _roundsPlayed);
			double avgSpeed2 = (double) speed2 / Math.max(1, _roundsPlayed);
			if (Double.compare(avgSpeed2, avgSpeed1) != 0) return Double.compare(avgSpeed2, avgSpeed1);

			Integer h1 = _highestSingleRoundScore.get(p1);
			int high1 = (h1 != null) ? h1 : 0;
			Integer h2 = _highestSingleRoundScore.get(p2);
			int high2 = (h2 != null) ? h2 : 0;
			if (high1 != high2) return Integer.compare(high2, high1);

			return p1.getName().compareTo(p2.getName());
		});
		return sorted;
	}

	@EventHandler
	public void onSpectatorInteract(PlayerInteractEvent event)
	{
		if (!IsLive()) return;
		Player player = event.getPlayer();
		if (GetTeamList().size() > 1 && GetTeamList().get(1).HasPlayer(player))
		{
			if (event.getItem() != null && event.getItem().getType() == Material.NETHER_STAR)
			{
				openVoteShop(player);
				event.setCancelled(true);
			}
		}
	}

	public void openVoteShop(Player player)
	{
		_voteShop.attemptShopOpen(player);
	}

	@EventHandler
	public void giveSpectatorStar(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
			return;
		if (!IsLive())
			return;
		if (GetTeamList().size() < 2)
			return;
		for (Player player : GetTeamList().get(1).GetPlayers(false))
		{
			if (player.getGameMode() == GameMode.SPECTATOR || player.getAllowFlight())
			{
				if (!player.getInventory().contains(Material.NETHER_STAR))
				{
					updateSpectatorHotbarItem(player);
				}
			}
		}
	}

	public void updateSpectatorHotbarItem(Player player)
	{
		if (GetTeamList().size() < 2 || !GetTeamList().get(1).HasPlayer(player))
			return;

		boolean hasVoted = _spectatorVotes.containsKey(player);
		String titleKey = hasVoted ? "speedbuilders.gui.vote.title_voted" : "speedbuilders.gui.vote.title";
		String title = SpeedBuildersLang.get().get(player, titleKey);

		ItemStack voteStar = ItemStackFactory.Instance.CreateStack(
			Material.NETHER_STAR, (byte) 0, 1, title
		);

		if (!hasVoted)
		{
			org.bukkit.inventory.meta.ItemMeta meta = voteStar.getItemMeta();
			if (meta != null)
			{
				meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
				meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
				voteStar.setItemMeta(meta);
			}
		}

		player.getInventory().setItem(4, voteStar);
	}

	private void resetVotesAndHotbars()
	{
		_votesReceived.clear();
		_spectatorVotes.clear();
		if (GetTeamList().size() > 1)
		{
			for (Player player : GetTeamList().get(1).GetPlayers(false))
			{
				updateSpectatorHotbarItem(player);
			}
		}
	}
}
