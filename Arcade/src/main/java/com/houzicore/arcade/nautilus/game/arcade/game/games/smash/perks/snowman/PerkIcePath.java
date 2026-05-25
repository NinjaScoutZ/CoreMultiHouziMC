package com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.snowman;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilAction;
import com.houzicore.shared.common.util.UtilBlock;
import com.houzicore.shared.common.util.UtilEvent;
import com.houzicore.shared.common.util.UtilEvent.ActionType;
import com.houzicore.shared.common.util.UtilItem;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.recharge.Recharge;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.data.IcePathData;

public class PerkIcePath extends Perk
{
	
	private int _cooldown;
	private int _meltTime;
	
	private Set<IcePathData> _data = new HashSet<>();

	public PerkIcePath()
	{
		super("Ice Path", new String[] { C.cYellow + "Right-Click" + C.cGray + " with Axe to " + C.cGreen + "Ice Path" });
	}

	@Override
	public void setupValues()
	{
		_cooldown = getPerkTime("Cooldown");
		_meltTime = getPerkTime("Melt Time");
	}

	@EventHandler
	public void Skill(PlayerInteractEvent event)
	{
		if (event.isCancelled())
		{
			return;
		}

		if (!UtilEvent.isAction(event, ActionType.R))
		{
			return;
		}

		if (UtilBlock.usable(event.getClickedBlock()))
		{
			return;
		}

		Player player = event.getPlayer();

		if (!player.getInventory().getItemInMainHand().getType().name().endsWith("_AXE"))
		{
			return;
		}

		if (!hasPerk(player))
		{
			return;
		}

		if (!Recharge.Instance.use(player, GetName(), _cooldown, true, true))
		{
			return;
		}
		
		player.teleport(player.getLocation().add(0, 1, 0));
		player.setVelocity(new Vector(0, 0.5, 0));

		_data.add(new IcePathData(player));

		// Inform
		UtilPlayer.message(player, F.main("Game", "You used " + F.skill(GetName()) + "."));
	}

	@EventHandler
	public void Freeze(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}
		
		Iterator<IcePathData> dataIterator = _data.iterator();

		while (dataIterator.hasNext())
		{
			IcePathData data = dataIterator.next();

			Block block = data.GetNextBlock();

			if (block == null)
			{
				dataIterator.remove();
			}
			else
			{
				block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, Material.ICE);
				Manager.GetBlockRestore().Add(block, com.houzicore.shared.common.util.IdUtil.getTypeId(Material.ICE), (byte) 0, _meltTime);
			}
		}
	}
}
