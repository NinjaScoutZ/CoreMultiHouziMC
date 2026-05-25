package nautilus.game.arcade.game.games.smash.perks;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.houzicore.shared.core.common.util.C;
import com.houzicore.shared.core.disguise.DisguiseManager;
import com.houzicore.shared.core.disguise.disguises.DisguiseInsentient;
import com.houzicore.shared.core.game.kit.GameKit;

import com.houzicore.arcade.nautilus.game.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.game.GameTeam;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;

public abstract class SmashKit extends Kit
{

	private final Class<? extends DisguiseInsentient> _clazz;
	private String[] _description;

	public SmashKit(ArcadeManager manager, GameKit gameKit, Perk[] perks, Class<? extends DisguiseInsentient> clazz)
	{
		super(manager, gameKit, perks);

		_clazz = clazz;
	}

	public boolean isSmashActive(Player player)
	{
		for (Perk perk : GetPerks())
		{
			if (!(perk instanceof SmashUltimate))
			{
				continue;
			}

			SmashUltimate ultimate = (SmashUltimate) perk;

			if (ultimate.isUsingUltimate(player))
			{
				return true;
			}
		}

		return false;
	}
	
	public void disguise(Player player)
	{
		disguise(player, _clazz);
	}
	
	public void disguise(Player player, Class<? extends DisguiseInsentient> clazz)
	{
		if (clazz == null)
		{
			return;
		}
		
		DisguiseManager disguiseManager = Manager.GetDisguise();
				
		try
		{
			DisguiseInsentient disguise = clazz.getConstructor(Entity.class).newInstance(player);
			GameTeam gameTeam = Manager.GetGame().GetTeam(player);
			
			if (gameTeam != null)
			{
				disguise.setName(gameTeam.GetColor() + player.getName());
			}
			else
			{
				disguise.setName(player.getName());
			}

			disguise.setCustomNameVisible(true);
			disguiseManager.disguise(disguise);
		}
		catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e)
		{
			e.printStackTrace();
		}
	}
	
	@Override
	public String[] GetDesc()
	{
		if (_description == null)
		{
			List<String> description = new ArrayList<>();

			description.add(C.cGreen + "Kit - " + C.cWhiteB + GetName());

			for (Perk perk : GetPerks())
			{
				if (!perk.IsVisible())
				{
					continue;
				}

				for (String line : perk.GetDesc())
				{
					description.add(C.cGray + "  " + line);
				}
			}

			_description = description.toArray(new String[description.size()]);
		}

		return _description;
	}

}
