package com.houzicore.arcade.nautilus.game.arcade.game.games.mineware.challenges;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilEnt;
import com.houzicore.shared.common.util.UtilMath;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilTime;
import com.houzicore.shared.common.util.UtilTime.TimeUnit;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import com.houzicore.arcade.nautilus.game.arcade.game.games.mineware.ChallengeSeperateRooms;
import com.houzicore.arcade.nautilus.game.arcade.game.games.mineware.MineWare;

public class ChallengeNameThatSound extends ChallengeSeperateRooms
{
	private HashMap<EntityType, Sound[]> _sounds = new HashMap<EntityType, Sound[]>();
	private ArrayList<Entry<Entry<EntityType, Sound>, Float>> _toPlay = new ArrayList<Entry<Entry<EntityType, Sound>, Float>>();
	private HashMap<String, Integer> _currentState = new HashMap<String, Integer>();
	private HashMap<String, ArrayList<Entity>> _mobs = new HashMap<String, ArrayList<Entity>>();
	private HashMap<String, Long> _lastGuess = new HashMap<String, Long>();

	public ChallengeNameThatSound(MineWare host)
	{
		super(host, ChallengeType.FirstComplete, "Hit the creature that makes the noises");

		_sounds.put(EntityType.ZOMBIE, new Sound[]
			{
					Sound.ENTITY_ZOMBIE_DEATH, Sound.ENTITY_ZOMBIE_HURT, Sound.ENTITY_ZOMBIE_AMBIENT, Sound.ENTITY_ZOMBIE_INFECT, Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR,
					Sound.ENTITY_ZOMBIE_VILLAGER_CURE, Sound.ENTITY_ZOMBIE_VILLAGER_CONVERTED
			});

		_sounds.put(EntityType.PIG, new Sound[]
			{
					Sound.ENTITY_PIG_DEATH, Sound.ENTITY_PIG_AMBIENT
			});

		_sounds.put(EntityType.CHICKEN, new Sound[]
			{
					Sound.ENTITY_CHICKEN_EGG, Sound.ENTITY_CHICKEN_HURT, Sound.ENTITY_CHICKEN_AMBIENT
			});

		/*_sounds.put(EntityType.SPIDER, new Sound[]
			{
					Sound.ENTITY_SPIDER_DEATH, Sound.ENTITY_SPIDER_AMBIENT
			});*/

		_sounds.put(EntityType.IRON_GOLEM, new Sound[]
			{
					Sound.ENTITY_IRON_GOLEM_DEATH, Sound.ENTITY_IRON_GOLEM_HURT, Sound.ENTITY_IRON_GOLEM_ATTACK
			});

		_sounds.put(EntityType.ENDERMAN, new Sound[]
			{
					Sound.ENTITY_ENDERMAN_DEATH, Sound.ENTITY_ENDERMAN_HURT, Sound.ENTITY_ENDERMAN_AMBIENT, Sound.ENTITY_ENDERMAN_SCREAM, Sound.ENTITY_ENDERMAN_TELEPORT
			});

		_sounds.put(EntityType.COW, new Sound[]
			{
					Sound.ENTITY_COW_HURT, Sound.ENTITY_COW_AMBIENT, Sound.ENTITY_COW_STEP
			});

		_sounds.put(EntityType.HORSE, new Sound[]
			{
					Sound.ENTITY_HORSE_ANGRY, Sound.ENTITY_HORSE_BREATHE, Sound.ENTITY_HORSE_DEATH, Sound.ENTITY_HORSE_GALLOP, Sound.ENTITY_HORSE_ARMOR,
					Sound.ENTITY_HORSE_HURT, Sound.ENTITY_HORSE_AMBIENT
			});

		_sounds.put(EntityType.OCELOT, new Sound[]
			{
					Sound.ENTITY_CAT_HISS, Sound.ENTITY_CAT_HURT, Sound.ENTITY_CAT_AMBIENT, Sound.ENTITY_CAT_PURR, Sound.ENTITY_CAT_PURREOW
			});

		_sounds.put(EntityType.VILLAGER, new Sound[]
			{
					Sound.ENTITY_VILLAGER_DEATH, Sound.ENTITY_VILLAGER_TRADE, Sound.ENTITY_VILLAGER_HURT, Sound.ENTITY_VILLAGER_AMBIENT, Sound.ENTITY_VILLAGER_NO,
					Sound.ENTITY_VILLAGER_YES
			});

		_sounds.put(EntityType.WOLF, new Sound[]
			{
					Sound.ENTITY_WOLF_AMBIENT, Sound.ENTITY_WOLF_DEATH, Sound.ENTITY_WOLF_GROWL, Sound.ENTITY_WOLF_HURT, Sound.ENTITY_WOLF_PANT, Sound.ENTITY_WOLF_SHAKE,
					Sound.ENTITY_WOLF_WHINE
			});

		_sounds.put(EntityType.ZOMBIFIED_PIGLIN, new Sound[]
			{
					Sound.ENTITY_ZOMBIFIED_PIGLIN_ANGRY, Sound.ENTITY_ZOMBIFIED_PIGLIN_DEATH, Sound.ENTITY_ZOMBIFIED_PIGLIN_HURT, Sound.ENTITY_ZOMBIFIED_PIGLIN_AMBIENT
			});

		_sounds.put(EntityType.SHEEP, new Sound[]
			{
				Sound.ENTITY_SHEEP_AMBIENT
			});

		_sounds.put(EntityType.SKELETON, new Sound[]
			{
					Sound.ENTITY_SKELETON_DEATH, Sound.ENTITY_SKELETON_HURT, Sound.ENTITY_SKELETON_AMBIENT
			});

		/*_sounds.put(EntityType.SLIME, new Sound[]
			{
					Sound.ENTITY_SLIME_ATTACK, Sound.ENTITY_SLIME_SQUISH, Sound.ENTITY_SLIME_JUMP, Sound.ENTITY_MAGMA_CUBE_JUMP, Sound.ENTITY_MAGMA_CUBE_SQUISH,
					Sound.ENTITY_MAGMA_CUBE_SQUISH
			});*/

		while (_toPlay.size() < 3)
		{
			EntityType entityType = EntityType.values()[UtilMath.r(EntityType.values().length)];

			if (!_sounds.containsKey(entityType))
			{
				continue;
			}

			boolean allClear = true;

			for (Entry<Entry<EntityType, Sound>, Float> entry : _toPlay)
			{
				if (entry.getKey().getKey() == entityType)
				{
					allClear = false;
					break;
				}
			}

			if (!allClear)
			{
				continue;
			}

			Sound sound = _sounds.get(entityType)[UtilMath.r(_sounds.get(entityType).length)];

			_toPlay.add(new HashMap.SimpleEntry(new HashMap.SimpleEntry(entityType, sound), UtilMath.random.nextFloat() + 0.5));
		}
	}

	private void spawnMobs(Player player, int stage)
	{
		if (!_mobs.containsKey(player.getName()))
		{
			return;
		}

		for (Entity entity : _mobs.get(player.getName()))
		{
			entity.remove();
		}

		if (stage > 2)
			return;

		ArrayList<EntityType> entityType = new ArrayList<EntityType>();

		for (EntityType type : _sounds.keySet())
		{
			entityType.add(type);
		}

		entityType.remove(_toPlay.get(stage).getKey().getKey());

		while (entityType.size() > 8)
		{
			entityType.remove(UtilMath.r(entityType.size()));
		}

		entityType.add(_toPlay.get(stage).getKey().getKey());

		Collections.shuffle(entityType);

		Host.CreatureAllowOverride = true;

		int i = 0;

		for (int x = 1; x <= 9; x++)
		{
			for (int z = 1; z <= 9; z++)
			{
				if ((x == 3 && z == 3) || (x % 3 != 0 || z % 3 != 0))
					continue;

				Location loc = getRoom(player).add(x, 1.1, z);

				Entity entity = loc.getWorld().spawnEntity(loc, entityType.get(i++));

				UtilEnt.Vegetate(entity, true);

				_mobs.get(player.getName()).add(entity);
			}
		}

		Host.CreatureAllowOverride = false;
	}

	@EventHandler
	public void onInteract(PlayerInteractEntityEvent event)
	{
		event.setCancelled(true);

		Player player = event.getPlayer();

		if (UtilPlayer.isSpectator(player))
			return;

		if (_lastGuess.containsKey(player.getName()) && !UtilTime.elapsed(_lastGuess.get(player.getName()), 3000))
		{
			player.sendMessage(F.main(
					"Guess",
					"Wait "
							+ UtilTime.convertString((_lastGuess.get(player.getName()) + 3000) - System.currentTimeMillis(), 1,
									TimeUnit.SECONDS) + " before next guess"));
			return;
		}

		EntityType entityType = event.getRightClicked().getType();

		int stage = _currentState.get(player.getName());

		if (_toPlay.get(stage).getKey().getKey() == entityType)
		{
			stage++;
			displayCount(player, event.getRightClicked().getLocation(), stage == 1 ? C.cRed : stage == 2 ? C.cGreen : C.cDGreen);
			spawnMobs(player, stage);

			if (stage < 3)
			{
				player.playSound(player.getLocation(), _toPlay.get(stage).getKey().getValue(), 5, _toPlay.get(stage).getValue());
			}
			else
			{
				SetCompleted(player);
			}
		}
		else
		{
			_lastGuess.put(player.getName(), System.currentTimeMillis());
			player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASEDRUM, 2, 0);
			player.sendMessage(F.main("Name that sound", "Bad guess!"));
		}
	}

	@EventHandler
	public void onTwoSeconds(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TWOSEC)
		{
			return;
		}

		for (Player player : getChallengers())
		{
			int stage = _currentState.get(player.getName());

			if (stage < 3)
			{
				player.playSound(player.getLocation(), _toPlay.get(stage).getKey().getValue(), 5, _toPlay.get(stage).getValue());
			}
		}
	}

	@EventHandler
	public void onAttack(EntityDamageByEntityEvent event)
	{
		if (((Player) event.getDamager()) == null)
		{
			return;
		}

		onInteract(new PlayerInteractEntityEvent(((Player) event.getDamager()), event.getEntity()));

		event.setCancelled(true);
		event.getEntity().setFireTicks(0);
	}

	@Override
	public void generateRoom(Location loc)
	{
		for (int x = 0; x <= 10; x++)
		{
			for (int z = 0; z <= 10; z++)
			{
				if (x == 0 || x == 10 || z == 0 || z == 10)
				{
					for (int y = 1; y <= 5; y++)
					{
						Block b = loc.getBlock().getRelative(x, y, z);
						b.setType(Material.COAL_BLOCK);
						addBlock(b);
					}
				}

				Block b = loc.getBlock().getRelative(x, 0, z);
				b.setType(Material.WHITE_WOOL);
				addBlock(b);
			}
		}
	}

	@Override
	public int getBorderX()
	{
		return 10;
	}

	@Override
	public int getBorderY()
	{
		return 10;
	}

	@Override
	public int getBorderZ()
	{
		return 10;
	}

	@Override
	public int getDividersX()
	{
		return 5;
	}

	@Override
	public int getDividersZ()
	{
		return 5;
	}

	@Override
	public void cleanupRoom()
	{
		for (ArrayList<Entity> entityList : _mobs.values())
		{
			for (Entity entity : entityList)
			{
				entity.remove();
			}

		}
	}

	@Override
	public void setupPlayers()
	{
		for (Player player : getChallengers())
		{
			_currentState.put(player.getName(), 0);
			_mobs.put(player.getName(), new ArrayList<Entity>());

			spawnMobs(player, 0);
			player.playSound(player.getLocation(), _toPlay.get(0).getKey().getValue(), 5, _toPlay.get(0).getValue());
		}
	}

}
