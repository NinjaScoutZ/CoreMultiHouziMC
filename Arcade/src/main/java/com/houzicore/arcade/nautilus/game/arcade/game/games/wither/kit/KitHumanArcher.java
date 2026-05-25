package com.houzicore.arcade.nautilus.game.arcade.game.games.wither.kit;

import org.bukkit.ChatColor;
import org.bukkit.Material;
//import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;

import com.houzicore.shared.core.disguise.disguises.DisguisePlayer;
import com.houzicore.shared.core.itemstack.ItemStackFactory;
import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;
import com.houzicore.arcade.nautilus.game.arcade.kit.KitAvailability;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.PerkDoubleJump;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.PerkFletcher;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.PerkRopedArrow;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.PerkWitherArrowBlind;

public class KitHumanArcher extends Kit
{
	public KitHumanArcher(ArcadeManager manager)
	{
		super(manager, "Human Archer", KitAvailability.Free,

		new String[]
		{
			""
		},

		new Perk[]
		{
				new PerkDoubleJump("Double Jump", 1.2, 1, true, 4000, true),
				new PerkWitherArrowBlind(6),
				new PerkFletcher(4, 4, true),

		}, EntityType.ZOMBIE, null);

	}

	@Override
	public void GiveItems(Player player)
	{
		player.getInventory().addItem(ItemStackFactory.Instance.CreateStack(Material.IRON_SWORD));
		player.getInventory().addItem(ItemStackFactory.Instance.CreateStack(Material.BOW));

		ItemStack potion = new ItemStack(Material.POTION, 2); // 16422
		PotionMeta potionMeta = (PotionMeta) potion.getItemMeta();
		potionMeta.setDisplayName(ChatColor.RESET + "Revival Potion");
		potion.setItemMeta(potionMeta);
		player.getInventory().addItem(potion);

		player.getInventory().addItem(ItemStackFactory.Instance.CreateStack(Material.MUSHROOM_STEW));
		player.getInventory().addItem(ItemStackFactory.Instance.CreateStack(Material.MUSHROOM_STEW));
	}

	@Override
	public void SpawnCustom(LivingEntity ent)
	{

	}
}
