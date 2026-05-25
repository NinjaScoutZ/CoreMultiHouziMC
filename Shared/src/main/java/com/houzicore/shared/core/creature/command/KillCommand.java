package com.houzicore.shared.core.creature.command;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import com.houzicore.shared.core.command.CommandBase;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilEnt;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.core.creature.Creature;
import com.houzicore.shared.core.creature.event.CreatureKillEntitiesEvent;

public class KillCommand extends CommandBase<Creature> {
	public KillCommand(Creature plugin) {
		super(plugin, Rank.ADMIN, "kill", "k");
	}

	@Override
	public void Execute(Player caller, String[] args) {
		if (args == null || args.length == 0) {
			UtilPlayer.message(caller, F.main(Plugin.getName(), "Missing Entity Type Parameter."));
			return;
		}

		final EntityType type = UtilEnt.searchEntity(caller, args[0], true);

		if (type == null && !args[0].equalsIgnoreCase("all"))
			return;

		int count = 0;
		final List<Entity> killList = new ArrayList<>();

		for (final World world : UtilServer.getServer().getWorlds()) {
			for (final Entity ent : world.getEntities()) {
				if (ent.getType() == EntityType.PLAYER) {
					continue;
				}

				if (type == null || ent.getType() == type) {
					killList.add(ent);
				}
			}
		}

		final CreatureKillEntitiesEvent event = new CreatureKillEntitiesEvent(killList);
		Plugin.getPlugin().getServer().getPluginManager().callEvent(event);

		for (final Entity entity : event.GetEntities()) {
			entity.remove();
			count++;
		}

		String target = "ALL";
		if (type != null) {
			target = UtilEnt.getName(type);
		}

		UtilPlayer.message(caller, F.main(Plugin.getName(), "Killed " + target + ". " + count + " Removed."));
	}
}
