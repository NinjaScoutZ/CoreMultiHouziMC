package com.houzicore.arcade;

import com.houzicore.shared.core.game.GameCategory;
import com.houzicore.shared.core.game.GameDisplay;
import com.houzicore.arcade.nautilus.game.arcade.game.Game;
import com.houzicore.arcade.nautilus.game.arcade.game.games.barbarians.Barbarians;
import com.houzicore.arcade.nautilus.game.arcade.game.games.cards.Cards;
import com.houzicore.arcade.nautilus.game.arcade.game.games.castlesiege.CastleSiege;
import com.houzicore.arcade.nautilus.game.arcade.game.games.event.EventGame;
import com.houzicore.arcade.nautilus.game.arcade.game.games.evolution.Evolution;
import com.houzicore.arcade.nautilus.game.arcade.game.games.hideseek.HideSeek;
import com.houzicore.arcade.nautilus.game.arcade.game.games.holeinwall.HoleInTheWall;
import com.houzicore.arcade.nautilus.game.arcade.game.games.horsecharge.Horse;
import com.houzicore.arcade.nautilus.game.arcade.game.games.lobbers.BombLobbers;
import com.houzicore.arcade.nautilus.game.arcade.game.games.minestrike.MineStrike;
import com.houzicore.arcade.nautilus.game.arcade.game.games.mineware.MineWare;
import com.houzicore.arcade.nautilus.game.arcade.game.games.searchanddestroy.SearchAndDestroy;
import com.houzicore.arcade.nautilus.game.arcade.game.games.skywars.SoloSkywars;
import com.houzicore.arcade.nautilus.game.arcade.game.games.skywars.TeamSkywars;
import com.houzicore.arcade.nautilus.game.arcade.game.games.sneakyassassins.SneakyAssassins;
import com.houzicore.arcade.nautilus.game.arcade.game.games.snowfight.SnowFight;
import com.houzicore.arcade.nautilus.game.arcade.game.games.squidshooters.SquidShooters;
import com.houzicore.arcade.nautilus.game.arcade.game.games.stacker.Stacker;
import com.houzicore.arcade.nautilus.game.arcade.game.games.primalgames.SoloPrimalGames;
import com.houzicore.arcade.nautilus.game.arcade.game.games.primalgames.TeamPrimalGames;
import com.houzicore.arcade.nautilus.game.arcade.game.games.tug.Tug;
import com.houzicore.arcade.nautilus.game.arcade.game.games.wither.WitherGame;
import com.houzicore.arcade.nautilus.game.arcade.game.games.wizards.Wizards;
import com.houzicore.arcade.nautilus.game.arcade.game.games.smash.SuperSmash;
import com.houzicore.arcade.nautilus.game.arcade.game.games.smash.TeamSuperSmash;
import com.houzicore.arcade.nautilus.game.arcade.game.games.speedbuilders.SpeedBuilders;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.Bedwars;

import org.bukkit.Material;

public enum GameType
{	
	//Mini
	Barbarians(Barbarians.class, GameDisplay.Barbarians),
	CastleSiege(CastleSiege.class, GameDisplay.CastleSiege),
	Arena(com.houzicore.arcade.nautilus.game.arcade.game.games.arena.ArenaGame.class, GameDisplay.Arena),
	DragonRiders(com.houzicore.arcade.nautilus.game.arcade.game.games.dragonriders.DragonRiders.class, GameDisplay.DragonRiders),

	Evolution(Evolution.class, GameDisplay.Evolution),
	PropRush(HideSeek.class, GameDisplay.PropRush),
	HoleInTheWall(HoleInTheWall.class, GameDisplay.HoleInTheWall),
	Horse(Horse.class, GameDisplay.Horse),
	Lobbers(BombLobbers.class, GameDisplay.Lobbers),
	MineStrike(MineStrike.class, GameDisplay.MineStrike, "http://chivebox.com/file/c/assets.zip", true),// Temp set to CHAMPIONS to fix UI bug
	MineWare(MineWare.class, GameDisplay.MineWare),
	SearchAndDestroy(SearchAndDestroy.class, GameDisplay.SearchAndDestroy),
	
	SneakyAssassins(SneakyAssassins.class, GameDisplay.SneakyAssassins),
	SnowFight(SnowFight.class, GameDisplay.SnowFight),
	SquidShooter(SquidShooters.class, GameDisplay.SquidShooter),
	Stacker(Stacker.class, GameDisplay.Stacker),
	SurvivalPrimalGame(SoloPrimalGames.class, GameDisplay.SurvivalPrimalGame),
	SurvivalPrimalGameTeams(TeamPrimalGames.class, GameDisplay.SurvivalPrimalGameTeams, new GameType[]{GameType.SurvivalPrimalGame}, false),
	Tug(Tug.class, GameDisplay.Tug),
	WitherAssault(WitherGame.class, GameDisplay.WitherAssault),
	Wizards(Wizards.class, GameDisplay.Wizards, "http://chivebox.com/file/c/ResWizards.zip", true),
	Cards(Cards.class, GameDisplay.Cards),
	Skywars(SoloSkywars.class, GameDisplay.Skywars),
	SkywarsTeams(TeamSkywars.class, GameDisplay.SkywarsTeams, new GameType[]{GameType.Skywars}, false),
	Build(null, GameDisplay.Build),
	SuperSmash(SuperSmash.class, GameDisplay.Smash),
	SmashTeams(TeamSuperSmash.class, GameDisplay.SmashTeams, new GameType[]{GameType.SuperSmash}, false),
	SpeedBuilders(SpeedBuilders.class, GameDisplay.SpeedBuilders),
	Bedwars(Bedwars.class, GameDisplay.Bedwars),
	
	Event(EventGame.class, GameDisplay.Event, new GameType[]{
		GameType.Barbarians, GameType.Arena,
		GameType.Cards, GameType.CastleSiege, 
		GameType.Evolution, GameType.PropRush,
		GameType.HoleInTheWall, GameType.Horse, GameType.MineStrike, GameType.MineWare,
		GameType.SearchAndDestroy,
		GameType.Lobbers, GameType.DragonRiders, GameType.Build,
		GameType.Skywars, GameType.SkywarsTeams,
		GameType.SneakyAssassins, GameType.SnowFight, GameType.SquidShooter,
		GameType.Stacker, GameType.SurvivalPrimalGame, GameType.SurvivalPrimalGameTeams, GameType.Tug,
		GameType.WitherAssault, GameType.Wizards,
		GameType.SuperSmash, GameType.SmashTeams, GameType.SpeedBuilders, GameType.Bedwars}, true);

	GameDisplay _display;
	boolean _enforceResourcePack;
	GameType[] _mapSource;
	boolean _ownMaps;
	String _resourcePack;
	Class<? extends Game> _gameClass;
	
	private int _gameId;	// Unique identifying id for this gamemode (used for statistics)
	public int getGameId() { return _gameId; }

	GameType(Class<? extends Game> gameClass, GameDisplay display)
	{
		this(gameClass, display, null, false, null, true);
	}

	GameType(Class<? extends Game> gameClass, GameDisplay display, String resourcePackUrl, boolean enforceResourcePack)
	{
		this(gameClass, display, resourcePackUrl, enforceResourcePack, null, true);
	}
	
	GameType(Class<? extends Game> gameClass, GameDisplay display, GameType[] mapSource, boolean ownMap)
	{
		this(gameClass, display, null, false, mapSource, ownMap);
	}
	
	GameType(Class<? extends Game> gameClass, GameDisplay display, String resourcePackUrl, boolean enforceResourcePack, GameType[] mapSource, boolean ownMaps)
	{
		_display = display;
		_gameClass = gameClass;
		_resourcePack = resourcePackUrl;
		_enforceResourcePack = enforceResourcePack;
		_mapSource = mapSource;
		_ownMaps = ownMaps;
	}
	
	public Class<? extends Game> getGameClass()
	{
		return _gameClass;
	}

	public boolean isEnforceResourcePack()
	{
		return _enforceResourcePack;
	}	

	public String getResourcePackUrl()
	{
		return _resourcePack;
	}
	
	public GameType[] getMapSource()
	{
		return _mapSource;
	}
	
	public boolean ownMaps()
	{
		return _ownMaps;
	}

	public String GetName()
	{
		return _display.getName();
	}
	
	public String GetMapFolderName()
	{
		if (this == GameType.PropRush) return "Block Hunt";
		if (this == GameType.SquidShooter) return "Squid Shooter";
		if (this == GameType.SurvivalPrimalGame || this == GameType.SurvivalPrimalGameTeams) return "Survival Primal Games";
		if (this == GameType.Skywars || this == GameType.SkywarsTeams) return "Skywars";
		return _display.getName();
	}
	
	public String GetLobbyName()
	{
		return _display.getLobbyName();
	}
	
	public Material GetMaterial()
	{
		return _display.getMaterial();
	}
	
	public byte GetMaterialData()
	{
		return _display.getMaterialData();
	}

	public GameCategory getGameCategory()
	{
		return _display.getGameCategory();
	}
	
}
