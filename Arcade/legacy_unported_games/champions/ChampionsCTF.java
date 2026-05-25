package nautilus.game.arcade.game.games.champions;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import com.houzicore.shared.core.common.util.UtilPlayer;
import com.houzicore.shared.core.updater.UpdateType;
import com.houzicore.shared.core.updater.event.UpdateEvent;
import com.houzicore.shared.minecraft.game.core.combat.DeathMessageType;

import com.houzicore.arcade.nautilus.game.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.GameType;
import com.houzicore.arcade.nautilus.game.arcade.game.GameTeam;
import com.houzicore.arcade.nautilus.game.arcade.game.games.champions.kits.KitAssassin;
import com.houzicore.arcade.nautilus.game.arcade.game.games.champions.kits.KitBrute;
import com.houzicore.arcade.nautilus.game.arcade.game.games.champions.kits.KitKnight;
import com.houzicore.arcade.nautilus.game.arcade.game.games.champions.kits.KitMage;
import com.houzicore.arcade.nautilus.game.arcade.game.games.champions.kits.KitRanger;
import com.houzicore.arcade.nautilus.game.arcade.game.games.common.CaptureTheFlag;
import com.houzicore.arcade.nautilus.game.arcade.game.modules.ChampionsModule;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;
import com.houzicore.arcade.nautilus.game.arcade.managers.chat.ChatStatData;
import com.houzicore.arcade.nautilus.game.arcade.stats.CapturesStatTracker;
import com.houzicore.arcade.nautilus.game.arcade.stats.ClutchStatTracker;
import com.houzicore.arcade.nautilus.game.arcade.stats.ElectrocutionStatTracker;
import com.houzicore.arcade.nautilus.game.arcade.stats.KillReasonStatTracker;
import com.houzicore.arcade.nautilus.game.arcade.stats.SeismicSlamStatTracker;
import com.houzicore.arcade.nautilus.game.arcade.stats.SpecialWinStatTracker;
import com.houzicore.arcade.nautilus.game.arcade.stats.TheLongestShotStatTracker;

public class ChampionsCTF extends CaptureTheFlag
{
	public ChampionsCTF(ArcadeManager manager)
	{
		super(manager, GameType.ChampionsCTF,

				new Kit[]
						{
				new KitBrute(manager),
				new KitRanger(manager),
				new KitKnight(manager),
				new KitMage(manager),
				new KitAssassin(manager),
						});

		_help = new String[]
				{
				"Make sure you use all of your Skill/Item Tokens",
				"Collect Resupply Chests to restock your inventory",
				"Customize your Class to suit your play style",
				"Gold Sword boosts Sword Skill by 2 Levels",
				"Gold Axe boosts Axe Skill by 2 Levels",
				"Gold/Iron Weapons deal 6 damage",
				"Diamond Weapons deal 7 damage",
				  
				};

		Manager.GetDamage().UseSimpleWeaponDamage = false;

		Manager.getClassManager().GetItemFactory().getProximityManager().setProxyLimit(6);
		
		InventoryOpenChest = true;
		
		EloStart = 1000;

		this.DisableKillCommand = false;

		AllowParticles = false;

		registerStatTrackers(
				new KillReasonStatTracker(this, "Backstab", "Assassination", false),
				new ElectrocutionStatTracker(this),
				new TheLongestShotStatTracker(this),
				new SeismicSlamStatTracker(this),
				new CapturesStatTracker(this, "Captures"),
				new ClutchStatTracker(this, "Clutch"),
				new SpecialWinStatTracker(this, "SpecialWin")
		);

		registerChatStats(
				Kills,
				Deaths,
				KDRatio,
				BlankLine,
				Assists,
				DamageDealt,
				DamageTaken,
				BlankLine,
				new ChatStatData("Captures", "Flag Captures", true)
		);
		
		new ChampionsModule().register(this);
	} 
	
	@Override    
	public void ValidateKit(Player player, GameTeam team)
	{ 
		//Set to Default Knight
		if (GetKit(player) == null)
		{
			SetKit(player, GetKits()[2], true);
			UtilPlayer.closeInventoryIfOpen(player);
		}
	}
	
	@Override
	public DeathMessageType GetDeathMessageType()
	{
		return DeathMessageType.Detailed;
	}
	
	@EventHandler
	public void cleanProximities(UpdateEvent event)
	{
		if (!IsLive())
		{
			return;
		}
		
		if (event.getType() != UpdateType.FAST)
		{
			return;
		}
		
		for (Location loc : getLocations(true))
		{
			Manager.getClassManager().GetItemFactory().getProximityManager().clean(loc, 12);
		}
	}
}
