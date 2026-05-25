package com.houzicore.arcade.nautilus.game.arcade.command;

import com.houzicore.shared.core.command.CommandBase;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.GameType;
import com.houzicore.arcade.legacy.LegacyGameTypeResolver;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class SetCommand extends CommandBase<ArcadeManager>
{ 
	public SetCommand(ArcadeManager plugin)
	{
		super(plugin, Rank.ADMIN, new Rank[] {Rank.MAPLEAD, Rank.JNR_DEV}, "set");
	}
 
	@Override 
	public void Execute(Player caller, String[] args)
	{ 
		if (Plugin.GetGame() == null)
			return;
		 
		if (args == null || args.length == 0)
		{
			caller.sendMessage(F.help("/game set <GameType> (MapSource) (Map)", "Set the current game or next game", Rank.ADMIN));
			return; 
		}
		
		String game = args[0].toLowerCase();
			
		if (args.length >= 2)
		{
			String map = "";
			String source = "";
			if(args.length == 3)
			{
				Plugin.GetGameCreationManager().MapSource = args[1];
				Plugin.GetGameCreationManager().MapPref = args[2];
				source = args[1];
				map = args[2]; 
			} 
			else 
			{
				Plugin.GetGameCreationManager().MapSource = args[0];
				Plugin.GetGameCreationManager().MapPref = args[1];
				source = args[0];
				map = args[1];
			}
			UtilPlayer.message(caller, C.cAqua + C.Bold + "Map Preference: " + ChatColor.RESET + source + ":" + map);
		}
		
		GameType legacyMatch = LegacyGameTypeResolver.resolvePlayable(args[0]).orElse(null);
		if (legacyMatch != null)
		{
			Plugin.GetGame().setGame(legacyMatch, caller, true);
			return;
		}

		//Parse Game
		ArrayList<GameType> matches = new ArrayList<>();
		for (GameType type : GameType.values())
		{
			if (!LegacyGameTypeResolver.isPlayable(type))
				continue;

			if (type.toString().toLowerCase().equals(game))
			{
				matches.clear();
				matches.add(type);
				break;
			}
			
			if (type.toString().toLowerCase().contains(game))
			{
				matches.add(type);
			}
		}
		
		if (matches.size() == 0)
		{
			caller.sendMessage("No results for: " + game);
			return;
		}
		
		if (matches.size() > 1)
		{
			caller.sendMessage("Matched multiple games;");
			for (GameType cur : matches)
				caller.sendMessage(cur.toString());
			return;
		}
		
		GameType type = matches.get(0);
		Plugin.GetGame().setGame(type, caller, true);
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String commandLabel, String[] args)
	{
		List<String> completions = new ArrayList<>();
		
		if (args.length == 2) {
			for (GameType type : GameType.values()) {
				if (LegacyGameTypeResolver.isPlayable(type) && type.name().toLowerCase().startsWith(args[1].toLowerCase())) {
					completions.add(type.name());
				}
			}
			return completions;
		}

		if (args.length == 3) {
			GameType type = LegacyGameTypeResolver.resolvePlayable(args[1]).orElse(null);

			if (type != null) {
				// Try to find the GameType maps folder
				java.io.File mapsDir = getMapsDirectory();
				if (mapsDir != null && mapsDir.exists() && mapsDir.isDirectory()) {
					java.io.File gameDir = new java.io.File(mapsDir, type.GetMapFolderName());
					if (gameDir.exists() && gameDir.isDirectory()) {
						java.io.File[] mapFiles = gameDir.listFiles((d, name) -> name.toLowerCase().endsWith(".zip"));
						if (mapFiles != null) {
							for (java.io.File file : mapFiles) {
								String name = file.getName().substring(0, file.getName().length() - 4);
								if (name.toLowerCase().startsWith(args[2].toLowerCase())) {
									completions.add(name);
								}
							}
						}
					}
				}
			}
		}

		return completions;
	}

	private java.io.File getMapsDirectory() {
		java.io.File[] possiblePaths = {
			new java.io.File("../../Maps"),
			new java.io.File("../Maps"),
			new java.io.File("Maps") // When running directly from server root
		};
		for (java.io.File path : possiblePaths) {
			if (path.exists() && path.isDirectory()) {
				return path;
			}
		}
		return null;
	}
}
