package com.houzicore.arcade.nautilus.game.arcade.game.games.arena.kits;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;
import com.houzicore.arcade.nautilus.game.arcade.kit.KitAvailability;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.PerkDummy;

public class KitGladiator extends Kit {

	public KitGladiator(ArcadeManager manager) {
		super(manager, "Gladiator", KitAvailability.Free, 
				new String[] { "A master of the arena." }, 
				new String[] { "ปรมาจารย์แห่งสังเวียน" },
				new Perk[] {
						new PerkDummy("Tournament", new String[] {"Starts off with Diamond Gear and Blocks."})
				}, 
				EntityType.ZOMBIE,
				new ItemStack(Material.DIAMOND_SWORD));
	}

	@Override
	public void GiveItems(Player player) {
		// Items are given by Arena loadouts instead in ArenaGame.
	}
}
