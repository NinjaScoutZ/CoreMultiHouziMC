package com.houzicore.shared.common.util;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Giant;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.Tag;
import java.util.UUID;


public class UtilEnt
{

	private static HashMap<Entity, String> _nameMap = new HashMap<Entity, String>();
	private static HashMap<String, EntityType> creatureMap = new HashMap<String, EntityType>();

	
	public static HashMap<Entity, String> GetEntityNames() 
	{
		return _nameMap;
	}
	
	public static void silence(Entity entity, boolean silence)
	{
		entity.setSilent(silence);
	}
	
	public static void ghost(Entity entity, boolean ghost, boolean invisible)
	{
		entity.setInvisible(invisible);
		if (entity instanceof LivingEntity)
		{
			((LivingEntity)entity).setCollidable(!ghost);
		}
	}

	
	public static void Leash(LivingEntity leashed, Entity holder, boolean pull, boolean breakable)
	{
		// Pulling and breakable leash behavior is now mostly handled by the server.
		leashed.setLeashHolder(holder);
	}

	public static void setOwner(Entity entity, UUID ownerId)
	{
		if (Bukkit.getPluginManager().getPlugins().length > 0) {
			entity.setMetadata("owner", new org.bukkit.metadata.FixedMetadataValue(Bukkit.getPluginManager().getPlugins()[0], ownerId));
		}
	}

	public static UUID getOwner(Entity entity)
	{
		if (entity.hasMetadata("owner"))
		{
			for (org.bukkit.metadata.MetadataValue value : entity.getMetadata("owner"))
			{
				if (value.value() instanceof UUID) {
					return (UUID) value.value();
				} else if (value.value() instanceof String) {
					return UUID.fromString((String) value.value());
				}
			}
		}
		return null;
	}

	public static <T> java.util.Optional<T> getAs(Entity entity, Class<T> clazz) {
		if (entity != null && clazz.isInstance(entity)) {
			return java.util.Optional.of(clazz.cast(entity));
		}
		return java.util.Optional.empty();
	}

	public static void addLookAtPlayerAI(Entity entity, float dist)
	{
		if (entity instanceof Mob)
		{
			// In Paper 1.21.1 this requires custom Goal implementation or NMS
			// Bukkit.getMobGoals().addGoal(mob, 7, VanillaGoal.LOOK_AT_PLAYER.getGoal(mob));
		}
	}

	public static void Vegetate(Entity entity)
	{
		Vegetate(entity, false);
	}
	
	public static void Vegetate(Entity entity, boolean mute)
	{
		if (entity instanceof Mob)
		{
			Mob mob = (Mob) entity;
			Bukkit.getMobGoals().removeAllGoals(mob);
			mob.setSilent(mute);
			// Removed setAware(false) so Pathfinders (Pets) can still move
		}
	}

    		
	public static void removeGoalSelectors(Entity entity)
	{
		if (entity instanceof Mob)
		{
			Bukkit.getMobGoals().removeAllGoals((Mob) entity);
		}
	}

	
	public static void populate()
	{
		if (creatureMap.isEmpty())
		{
			creatureMap.put("Bat", EntityType.BAT);
			creatureMap.put("Blaze", EntityType.BLAZE);
			creatureMap.put("Arrow", EntityType.ARROW);
			creatureMap.put("Cave Spider", EntityType.CAVE_SPIDER);
			creatureMap.put("Chicken", EntityType.CHICKEN);
			creatureMap.put("Cow", EntityType.COW);
			creatureMap.put("Creeper", EntityType.CREEPER);
			creatureMap.put("Ender Dragon", EntityType.ENDER_DRAGON);
			creatureMap.put("Enderman", EntityType.ENDERMAN);
			creatureMap.put("Ghast", EntityType.GHAST);
			creatureMap.put("Giant", EntityType.GIANT);
			creatureMap.put("Horse", EntityType.HORSE);
			creatureMap.put("Iron Golem", EntityType.IRON_GOLEM);
			creatureMap.put("Item", EntityType.ITEM);
			creatureMap.put("Magma Cube", EntityType.MAGMA_CUBE);
			creatureMap.put("Mooshroom", EntityType.MOOSHROOM);
			creatureMap.put("Ocelot", EntityType.OCELOT);
			creatureMap.put("Pig", EntityType.PIG);
			creatureMap.put("Pig Zombie", EntityType.ZOMBIFIED_PIGLIN);
			creatureMap.put("Sheep", EntityType.SHEEP);
			creatureMap.put("Silverfish", EntityType.SILVERFISH);
			creatureMap.put("Skeleton", EntityType.SKELETON);
			creatureMap.put("Slime", EntityType.SLIME);
			creatureMap.put("Snowman", EntityType.SNOW_GOLEM);
			creatureMap.put("Spider", EntityType.SPIDER);
			creatureMap.put("Squid", EntityType.SQUID);
			creatureMap.put("Villager", EntityType.VILLAGER);
			creatureMap.put("Witch", EntityType.WITCH);
			creatureMap.put("Wither", EntityType.WITHER);
			creatureMap.put("WitherSkull", EntityType.WITHER_SKULL);
			creatureMap.put("Wolf", EntityType.WOLF);
			creatureMap.put("Zombie", EntityType.ZOMBIE);
			
			creatureMap.put("Item", EntityType.ITEM);
		}
	}
	
	public static String getName(Entity ent)
	{
		if (ent == null)
			return "Null";
		
		if (ent.getType() == EntityType.PLAYER)
			return ((Player)ent).getName();
		
		if (GetEntityNames().containsKey(ent))
			return GetEntityNames().get(ent);
		
		if (ent instanceof LivingEntity)
		{
			LivingEntity le = (LivingEntity)ent;
			if (le.getCustomName() != null)
				return le.getCustomName();
		}
		
		return getName(ent.getType());  
	}

	public static String getName(EntityType type)
	{
		for (String cur : creatureMap.keySet())
			if (creatureMap.get(cur) == type)
				return cur;

		return type.getKey().getKey();
	}


	public static String searchName(Player caller, String arg, boolean inform)
	{
		populate();

		arg = arg.toLowerCase().replaceAll("_", " ");
		LinkedList<String> matchList = new LinkedList<String>();
		for (String cur : creatureMap.keySet())
		{
			if (cur.equalsIgnoreCase(arg))
				return cur;
			
			if (cur.toLowerCase().contains(arg))
				matchList.add(cur);
		}
			

		//No / Non-Unique
		if (matchList.size() != 1)
		{
			if (!inform)
				return null;

			//Inform
			UtilPlayer.message(caller, F.main("Creature Search", "" +
					C.mCount + matchList.size() +
					C.mBody + " matches for [" +
					C.mElem + arg +
					C.mBody + "]."));

			if (matchList.size() > 0)
			{
				String matchString = "";
				for (String cur : matchList)
					matchString += F.elem(cur) + ", ";
				if (matchString.length() > 1)
					matchString = matchString.substring(0 , matchString.length() - 2);

				UtilPlayer.message(caller, F.main("Creature Search", "" +
						C.mBody + "Matches [" +
						C.mElem + matchString +
						C.mBody + "]."));
			}

			return null;
		}

		return matchList.get(0);
	}

	public static EntityType searchEntity(Player caller, String arg, boolean inform)
	{
		populate();

		arg = arg.toLowerCase();
		LinkedList<EntityType> matchList = new LinkedList<EntityType>();
		for (String cur : creatureMap.keySet())
		{
			if (cur.equalsIgnoreCase(arg))
				return creatureMap.get(cur);
			
			if (cur.toLowerCase().contains(arg))
				matchList.add(creatureMap.get(cur));
		}
			

		//No / Non-Unique
		if (matchList.size() != 1)
		{
			if (!inform)
				return null;

			//Inform
			UtilPlayer.message(caller, F.main("Creature Search", "" +
					C.mCount + matchList.size() +
					C.mBody + " matches for [" +
					C.mElem + arg +
					C.mBody + "]."));

			if (matchList.size() > 0)
			{
				String matchString = "";
				for (EntityType cur : matchList)
					matchString += F.elem(cur.getKey().getKey()) + ", ";

				if (matchString.length() > 1)
					matchString = matchString.substring(0 , matchString.length() - 2);

				UtilPlayer.message(caller, F.main("Creature Search", "" +
						C.mBody + "Matches [" +
						C.mElem + matchString +
						C.mBody + "]."));
			}

			return null;
		}

		return matchList.get(0);
	}
	
	public static HashMap<LivingEntity, Double> getInRadius(Location loc,	double dR) 
	{
		HashMap<LivingEntity, Double> ents = new HashMap<LivingEntity, Double>();

		for (Entity cur : loc.getWorld().getEntities())
		{
			if (!(cur instanceof LivingEntity) || UtilPlayer.isSpectator(cur))
				continue;
			
			LivingEntity ent = (LivingEntity)cur;
			
			double offset = UtilMath.offset(loc, ent.getLocation());
			
			if (offset < dR)
				ents.put(ent, 1 - (offset/dR));
		}

		return ents;
	}
	
	public static boolean hitBox(Location loc, LivingEntity ent, double mult, EntityType disguise)
	{
		if (disguise != null)
		{
			if (disguise == EntityType.SQUID)
			{
				if (UtilMath.offset(loc, ent.getLocation().add(0, 0.4, 0)) < 0.6 * mult)
						return true;
				
				return false;
			}
		}
		
		if (ent instanceof Player)
		{
			Player player = (Player)ent;
			
			if (UtilMath.offset(loc, player.getEyeLocation()) < 0.4 * mult)
			{
				return true;
			}
			else if (UtilMath.offset2d(loc, player.getLocation()) < 0.6 * mult)
			{
				if (loc.getY() > player.getLocation().getY() && loc.getY() < player.getEyeLocation().getY())
				{
					return true;
				}		
			}
		}
		else
		{
			if (ent instanceof Giant)
			{
				if (loc.getY() > ent.getLocation().getY() && loc.getY() < ent.getLocation().getY() + 12)
					if (UtilMath.offset2d(loc, ent.getLocation()) < 4)
						return true;
			}
			else
			{
				if (loc.getY() > ent.getLocation().getY() && loc.getY() < ent.getLocation().getY() + 2)
					if (UtilMath.offset2d(loc, ent.getLocation()) < 0.5 * mult)
						return true;
			}
		}
			
		

		return false;
	}
	
	public static boolean isGrounded(Entity ent) 
	{ 
		return ent.isOnGround();
	}


	public static void PlayDamageSound(LivingEntity damagee) 
	{
		Sound sound = Sound.ENTITY_PLAYER_HURT;
		
		if (damagee.getType() == EntityType.BAT)				sound = Sound.ENTITY_BAT_HURT;
		else if (damagee.getType() == EntityType.BLAZE)			sound = Sound.ENTITY_BLAZE_HURT;
		else if (damagee.getType() == EntityType.CAVE_SPIDER)	sound = Sound.ENTITY_SPIDER_AMBIENT;
		else if (damagee.getType() == EntityType.CHICKEN)		sound = Sound.ENTITY_CHICKEN_HURT;
		else if (damagee.getType() == EntityType.COW)			sound = Sound.ENTITY_COW_HURT;
		else if (damagee.getType() == EntityType.CREEPER)		sound = Sound.ENTITY_CREEPER_HURT;
		else if (damagee.getType() == EntityType.ENDER_DRAGON)	sound = Sound.ENTITY_ENDER_DRAGON_GROWL;
		else if (damagee.getType() == EntityType.ENDERMAN)		sound = Sound.ENTITY_ENDERMAN_HURT;
		else if (damagee.getType() == EntityType.GHAST)			sound = Sound.ENTITY_GHAST_SCREAM;
		else if (damagee.getType() == EntityType.GIANT)			sound = Sound.ENTITY_ZOMBIE_HURT;
		//else if (damagee.getType() == EntityType.HORSE)		sound = Sound.
		else if (damagee.getType() == EntityType.IRON_GOLEM)	sound = Sound.ENTITY_IRON_GOLEM_HURT;
		else if (damagee.getType() == EntityType.MAGMA_CUBE)	sound = Sound.ENTITY_MAGMA_CUBE_JUMP;
		else if (damagee.getType() == EntityType.MOOSHROOM)	sound = Sound.ENTITY_COW_HURT;
		else if (damagee.getType() == EntityType.OCELOT)		sound = Sound.ENTITY_OCELOT_HURT;
		else if (damagee.getType() == EntityType.PIG)			sound = Sound.ENTITY_PIG_AMBIENT;
		else if (damagee.getType() == EntityType.ZOMBIFIED_PIGLIN)	sound = Sound.ENTITY_ZOMBIE_HURT;
		else if (damagee.getType() == EntityType.SHEEP)			sound = Sound.ENTITY_SHEEP_AMBIENT;
		else if (damagee.getType() == EntityType.SILVERFISH)	sound = Sound.ENTITY_SILVERFISH_HURT;
		else if (damagee.getType() == EntityType.SKELETON)		sound = Sound.ENTITY_SKELETON_HURT;
		else if (damagee.getType() == EntityType.SLIME)			sound = Sound.ENTITY_SLIME_ATTACK;
		else if (damagee.getType() == EntityType.SNOW_GOLEM)		sound = Sound.BLOCK_SNOW_STEP;
		else if (damagee.getType() == EntityType.SPIDER)		sound = Sound.ENTITY_SPIDER_AMBIENT;
		//else if (damagee.getType() == EntityType.SQUID)		sound = Sound;
		//else if (damagee.getType() == EntityType.VILLAGER)	sound = Sound;
		//else if (damagee.getType() == EntityType.WITCH)		sound = Sound.;
		else if (damagee.getType() == EntityType.WITHER)		sound = Sound.ENTITY_WITHER_HURT;
		else if (damagee.getType() == EntityType.WOLF)			sound = Sound.ENTITY_WOLF_HURT;
		else if (damagee.getType() == EntityType.ZOMBIE)		sound = Sound.ENTITY_ZOMBIE_HURT;	


		damagee.getWorld().playSound(damagee.getLocation(), sound, 1.5f + (float)(0.5f * Math.random()), 0.8f + (float)(0.4f * Math.random()));
	}

	public static boolean onBlock(Player player) 
	{
		//Side Standing
		double xMod = player.getLocation().getX() % 1;
		if (player.getLocation().getX() < 0)
			xMod += 1;
		
		double zMod = player.getLocation().getZ() % 1;
		if (player.getLocation().getZ() < 0)
			zMod += 1;

		int xMin = 0;
		int xMax = 0;
		int zMin = 0;
		int zMax = 0;
		
		if (xMod < 0.3)	xMin = -1;
		if (xMod > 0.7)	xMax = 1;
		
		if (zMod < 0.3)	zMin = -1;
		if (zMod > 0.7)	zMax = 1;

		for (int x=xMin ; x<=xMax ; x++)
		{
			for (int z=zMin ; z<=zMax ; z++)
			{				
				//Standing on SOMETHING
				if (player.getLocation().add(x, -0.5, z).getBlock().getType() != Material.AIR && !player.getLocation().add(x, -0.5, z).getBlock().isLiquid())
					return true;
				
				//Inside a Lillypad
				if (player.getLocation().add(x, 0, z).getBlock().getType() == Material.LILY_PAD)
					return true;
				
				//Fences/Walls
				Material beneath = player.getLocation().add(x, -1.5, z).getBlock().getType();
				if (player.getLocation().getY() % 0.5 == 0 &&
						(Tag.FENCES.isTagged(beneath) || 
						beneath == Material.OAK_FENCE_GATE || 
						Tag.WALLS.isTagged(beneath)))
					return true;

			}	
		}
		
		return false;
	}

	public static void CreatureMove(Entity ent, Location target, double speed) 
	{
		if (!(ent instanceof Mob))
			return;
		
		Mob mob = (Mob) ent;
		
		if (UtilMath.offset(mob.getLocation(), target) < 0.1)
			return;
		
		mob.getPathfinder().moveTo(target, speed);
	}

	
	public static boolean CreatureMoveFast(Entity ent, Location target, double speed) 
	{
		return CreatureMoveFast(ent, target, speed, true);
	}
	
	public static boolean CreatureMoveFast(Entity ent, Location target, double speed, boolean slow) 
	{
		if (!(ent instanceof Mob))
			return false;
		
		Mob mob = (Mob) ent;
		
		if (UtilMath.offset(mob.getLocation(), target) < 0.1)
			return false;
		
		mob.getPathfinder().moveTo(target, speed);
		
		return true;
	}


	public static int getNewEntityId(boolean modifynumber)
    {
	    // In 1.21.1, accessing entityCount is not recommended and changed.
		// We'll return -1 or use a large random number to avoid collision for custom packets.
	    return -1; 
    }

	
	public static Entity getEntityById(int entityId)
	{
		for (World world : Bukkit.getWorlds())
		{
			for (Entity entity : world.getEntities())
			{
				if (entity.getEntityId() == entityId)
				{
					return entity;
				}
			}
		}
		
		return null;
	}

	public static boolean inWater(LivingEntity ent) 
	{
		return ent.getLocation().getBlock().getType() == Material.WATER;
	}

}
