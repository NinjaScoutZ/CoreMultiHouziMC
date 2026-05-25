package com.houzicore.arcade.nautilus.game.arcade.command;

import java.util.Locale;

import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import com.houzicore.shared.api.disguise.DisguiseArchetype;
import com.houzicore.shared.api.disguise.DisguiseRequest;
import com.houzicore.shared.core.command.CommandBase;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.arcade.ArcadeManager;

public class DisguiseCommand extends CommandBase<ArcadeManager>
{
	public DisguiseCommand(ArcadeManager plugin)
	{
		super(plugin, Rank.ADMIN, new Rank[] {Rank.YOUTUBE, Rank.TWITCH, Rank.JNR_DEV}, "disguise");
	}

	@Override
	public void Execute(final Player caller, final String[] args)
	{
		if (args.length == 0)
		{
			UtilPlayer.message(caller, C.cRed + C.Bold + "/disguise <player_name|entity_type>");
			UtilPlayer.message(caller, C.cGray + "Examples: /disguise Notch  |  /disguise ZOMBIE");
			return;
		}

		String target = normalizeMobKey(args[0].toUpperCase(Locale.ROOT));

		DisguiseRequest request;
		if (isMobEntity(target))
		{
			// Treat as MOB disguise.
			request = new DisguiseRequest(
					caller.getUniqueId(),
					DisguiseArchetype.MOB,
					target,
					true,
					false,
					false);
		}
		else
		{
			// Treat as PLAYER disguise (skin lookup not yet implemented).
			UtilPlayer.message(caller, C.cGold + C.Bold + "Player-skin disguise not yet supported via bridge. Use a mob key e.g. /disguise ZOMBIE");
			return;
		}

		Plugin.GetDisguise().getService().clear(caller);
		Plugin.GetDisguise().getService().apply(caller, request);

		UtilPlayer.message(caller, C.cGreen + C.Bold + "Disguise Active: " + ChatColor.RESET + args[0]);
	}

	private boolean isMobEntity(String target)
	{
		try
		{
			EntityType type = EntityType.valueOf(target);
			return type.isAlive() && type != EntityType.PLAYER;
		}
		catch (IllegalArgumentException ignored)
		{
			return false;
		}
	}

	private String normalizeMobKey(String target)
	{
		return switch (target)
		{
			case "PIGZOMBIE", "PIG_ZOMBIE" -> "ZOMBIFIED_PIGLIN";
			case "ZOMBIEVILLAGER" -> "ZOMBIE_VILLAGER";
			case "MAGMACUBE" -> "MAGMA_CUBE";
			case "IRONGOLEM" -> "IRON_GOLEM";
			case "SNOWMAN" -> "SNOW_GOLEM";
			case "MUSHROOMCOW", "MUSHROOM_COW" -> "MOOSHROOM";
			case "CAVESPIDER" -> "CAVE_SPIDER";
			default -> target;
		};
	}
}
