package com.houzicore.arcade.nautilus.game.arcade.game.games.hideseek.forms;

import java.util.Map;

import com.houzicore.shared.api.disguise.DisguiseArchetype;
import com.houzicore.shared.api.disguise.DisguiseRequest;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilEnt;
import com.houzicore.shared.common.util.UtilInv;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.arcade.nautilus.game.arcade.game.games.hideseek.HideSeek;

import org.bukkit.Material;
import org.bukkit.Sound;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CreatureForm extends Form
{
	private EntityType _type;

	private DisguiseRequest _disguiseRequest;

	public CreatureForm(HideSeek host, Player player, EntityType entityType) 
	{
		super(host, player);

		_type = entityType;
	}

	public EntityType GetEntityType()
	{
		return _type;
	}

	@Override
	public void applyUsingExistingEngineState()
	{
		applyInternal(true);
	}

	@Override
	public void Apply() 
	{
		applyInternal(false);
	}

	private void applyInternal(boolean isReplace)
	{
		Material icon = Material.PORKCHOP;

		if (_type == EntityType.CHICKEN)			{ icon = Material.FEATHER; }
		else if (_type == EntityType.COW)			{ icon = Material.LEATHER; }
		else if (_type == EntityType.SHEEP)			{ icon = Material.WHITE_WOOL; }
		else if (_type == EntityType.PIG)			{ icon = Material.PORKCHOP; }
		else if (_type == EntityType.CAT)			{ icon = Material.STRING; }
		else if (_type == EntityType.RABBIT)		{ icon = Material.RABBIT_FOOT; }

		_disguiseRequest = new DisguiseRequest(
				Player.getUniqueId(),
				DisguiseArchetype.MOB,
				_type.name(),
				true,
				false,
				false,
				null,
				false,
				Map.of(
						"notifyBar", "false",
						"modifyBoundingBox", "false"));

		if (!isReplace) {
			// Temporarily allow creature spawning so the NativeDisguiseEngine can spawn
			// the self-view entity without being blocked by GameFlagManager.WorldCreature
			Host.CreatureAllowOverride = true;
			try {
				Host.Manager.GetDisguise().getService().apply(Player, _disguiseRequest);
			} finally {
				Host.CreatureAllowOverride = false;
			}
		}

		Player.setVelocity(new org.bukkit.util.Vector(0, 0, 0));

		//Inform
		UtilPlayer.message(Player, F.main("Game", C.cWhite + "You are now a " + F.elem(UtilEnt.getName(_type)) + "!"));

		//Give Item (Morph Tool)
		ItemStack morphTool = com.houzicore.arcade.nautilus.game.arcade.game.games.hideseek.runtime.PropRushAbilityDefinition.PROP_SWAP.createItem(Player);
		morphTool.setType(Host.GetItemEquivilent(icon));
		Player.getInventory().setItem(com.houzicore.arcade.nautilus.game.arcade.game.games.hideseek.runtime.PropRushAbilityDefinition.PROP_SWAP.getSlot(), morphTool);
		UtilInv.Update(Player);

		//Sound
		Player.playSound(Player.getLocation(), Sound.ENTITY_ZOMBIE_VILLAGER_CONVERTED, 2f, 2f);
	}

	@Override
	public void Remove() 
	{
		Host.Manager.GetDisguise().getService().clear(Player);
	}
}
