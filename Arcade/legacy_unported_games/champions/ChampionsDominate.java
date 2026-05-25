package nautilus.game.arcade.game.games.champions;

import org.bukkit.entity.Player;

import com.houzicore.shared.core.common.util.UtilPlayer;
import com.houzicore.shared.minecraft.game.core.combat.DeathMessageType;

import com.houzicore.arcade.nautilus.game.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.GameType;
import com.houzicore.arcade.nautilus.game.arcade.game.GameTeam;
import com.houzicore.arcade.nautilus.game.arcade.game.games.champions.kits.KitAssassin;
import com.houzicore.arcade.nautilus.game.arcade.game.games.champions.kits.KitBrute;
import com.houzicore.arcade.nautilus.game.arcade.game.games.champions.kits.KitKnight;
import com.houzicore.arcade.nautilus.game.arcade.game.games.champions.kits.KitMage;
import com.houzicore.arcade.nautilus.game.arcade.game.games.champions.kits.KitRanger;
import com.houzicore.arcade.nautilus.game.arcade.game.games.common.Domination;
import com.houzicore.arcade.nautilus.game.arcade.game.modules.ChampionsModule;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;
import com.houzicore.arcade.nautilus.game.arcade.stats.ElectrocutionStatTracker;
import com.houzicore.arcade.nautilus.game.arcade.stats.KillReasonStatTracker;
import com.houzicore.arcade.nautilus.game.arcade.stats.SeismicSlamStatTracker;
import com.houzicore.arcade.nautilus.game.arcade.stats.TheLongestShotStatTracker;
  
public class ChampionsDominate extends Domination
{       
	public ChampionsDominate(ArcadeManager manager)
	{   
		super(manager, GameType.ChampionsDominate,
      
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
				"Capture Beacons faster with more people!",
				"Make sure you use all of your Skill/Item Tokens",
				"Collect Emeralds to get 300 Points",
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

		registerStatTrackers(
				new KillReasonStatTracker(this, "Backstab", "Assassination", false),
				new ElectrocutionStatTracker(this),
				new TheLongestShotStatTracker(this),
				new SeismicSlamStatTracker(this)
		);

		registerChatStats(
				Kills,
				Deaths,
				KDRatio,
				BlankLine,
				Assists,
				DamageDealt,
				DamageTaken
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
}
