package com.houzicore.shared.core.creature.command;

import java.util.HashMap;
import java.util.HashSet;

import org.bukkit.World;
////
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.Zombie;

import com.houzicore.shared.core.command.MultiCommandBase;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilEnt;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.core.creature.Creature;

public class MobCommand extends MultiCommandBase<Creature> {
	public MobCommand(Creature plugin) {
		super(plugin, Rank.ADMIN, "mob");

		AddCommand(new KillCommand(Plugin));
	}

	@Override
	protected void Help(Player caller, String[] args) {
		if (args == null) {
			final HashMap<EntityType, Integer> entMap = new HashMap<>();

			int count = 0;
			for (final World world : UtilServer.getServer().getWorlds()) {
				for (final Entity ent : world.getEntities()) {
					if (!entMap.containsKey(ent.getType())) {
						entMap.put(ent.getType(), 0);
					}

					entMap.put(ent.getType(), 1 + entMap.get(ent.getType()));
					count++;
				}
			}

			UtilPlayer.message(caller, F.main(Plugin.getName(), "Listing Entities:"));
			for (final EntityType cur : entMap.keySet()) {
				UtilPlayer.message(caller, F.desc(UtilEnt.getName(cur), entMap.get(cur) + ""));
			}

			UtilPlayer.message(caller, F.desc("Total", count + ""));
		} else {
			final EntityType type = UtilEnt.searchEntity(caller, args[0], true);

			if (type == null)
				return;

			UtilPlayer.message(caller, F.main(Plugin.getName(), "Spawning Creature(s);"));

			// Store Args
			final HashSet<String> argSet = new HashSet<>();
			for (int i = 1; i < args.length; i++)
				if (args[i].length() > 0) {
					argSet.add(args[i]);
				}

			// Search Count
			int count = 1;
			final HashSet<String> argHandle = new HashSet<>();
			for (final String arg : argSet) {
				try {
					final int newCount = Integer.parseInt(arg);

					if (newCount <= 0) {
						continue;
					}

					// Set Count
					count = newCount;
					UtilPlayer.message(caller, F.desc("Amount", count + ""));

					// Flag Arg
					argHandle.add(arg);
					break;
				} catch (final Exception e) {
					// None
				}
			}
			for (final String arg : argHandle) {
				argSet.remove(arg);
			}

			// Spawn
			final HashSet<Entity> entSet = new HashSet<>();
			for (int i = 0; i < count; i++) {
				entSet.add(Plugin.SpawnEntity(caller.getTargetBlock(null, 0).getLocation().add(0.5, 1, 0.5), type));
			}

			// Search Vars
			for (final String arg : argSet) {
				if (arg.length() == 0) {
					continue;
				} else if (arg.equalsIgnoreCase("baby") || arg.equalsIgnoreCase("b")) {
					for (final Entity ent : entSet) {
						if (ent instanceof Ageable) {
							((Ageable) ent).setBaby();
						} else if (ent instanceof Zombie) {
							((Zombie) ent).setBaby(true);
						}
					}

					UtilPlayer.message(caller, F.desc("Baby", "True"));
					argHandle.add(arg);
				}

				// Lock
				else if (arg.equalsIgnoreCase("age") || arg.equalsIgnoreCase("lock")) {
					for (final Entity ent : entSet)
						if (ent instanceof Ageable) {
							((Ageable) ent).setAgeLock(true);
							UtilPlayer.message(caller, F.desc("Age", "False"));
						}

					argHandle.add(arg);
				}

				// Angry
				else if (arg.equalsIgnoreCase("angry") || arg.equalsIgnoreCase("a")) {
					for (final Entity ent : entSet)
						if (ent instanceof Wolf) {
							((Wolf) ent).setAngry(true);
						}

					// Note: In 1.21+, Wither Skeletons are EntityType.WITHER_SKELETON, not a sub-type.
					// The 'angry' flag on regular Skeleton has no Wither sub-type equivalent.

					UtilPlayer.message(caller, F.desc("Angry", "True"));
					argHandle.add(arg);
				}

				// Profession
				else if (arg.toLowerCase().charAt(0) == 'p') {
					try {
						final String prof = arg.substring(1, arg.length());

						Profession profession = null;
						for (final Profession cur : Profession.values())
							if (cur.name().toLowerCase().contains(prof.toLowerCase())) {
								profession = cur;
							}

						UtilPlayer.message(caller, F.desc("Profession", profession.name()));

						for (final Entity ent : entSet)
							if (ent instanceof Villager) {
								((Villager) ent).setProfession(profession);
							}
					} catch (final Exception e) {
						UtilPlayer.message(caller, F.desc("Profession", "Invalid [" + arg + "] on " + type.name()));
					}
					argHandle.add(arg);
				}

				// Size
				else if (arg.toLowerCase().charAt(0) == 's') {
					try {
						final String size = arg.substring(1, arg.length());

						UtilPlayer.message(caller, F.desc("Size", Integer.parseInt(size) + ""));

						for (final Entity ent : entSet)
							if (ent instanceof Slime) {
								((Slime) ent).setSize(Integer.parseInt(size));
							}
					} catch (final Exception e) {
						UtilPlayer.message(caller, F.desc("Size", "Invalid [" + arg + "] on " + type.name()));
					}
					argHandle.add(arg);
				}

				else if (arg.toLowerCase().charAt(0) == 'n' && arg.length() > 1) {
					try {
						String name = "";

						for (final char c : arg.substring(1, arg.length()).toCharArray()) {
							if (c != '_') {
								name += c;
							} else {
								name += " ";
							}
						}

						for (final Entity ent : entSet) {
							if (ent instanceof org.bukkit.entity.LivingEntity) {
								final org.bukkit.entity.LivingEntity cEnt = (org.bukkit.entity.LivingEntity) ent;
								cEnt.setCustomName(name);
								cEnt.setCustomNameVisible(true);
							}
						}
					} catch (final Exception e) {
						UtilPlayer.message(caller, F.desc("Size", "Invalid [" + arg + "] on " + type.name()));
					}
					argHandle.add(arg);
				}
			}
			for (final String arg : argHandle) {
				argSet.remove(arg);
			}

			for (final String arg : argSet) {
				UtilPlayer.message(caller, F.desc("Unhandled", arg));
			}

			// Inform
			UtilPlayer.message(caller,
					F.main(Plugin.getName(), "Spawned " + count + " " + UtilEnt.getName(type) + "."));
		}
	}
}
