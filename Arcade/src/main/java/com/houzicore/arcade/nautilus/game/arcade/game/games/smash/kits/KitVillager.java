package com.houzicore.arcade.nautilus.game.arcade.game.games.smash.kits;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.UtilInv;
import com.houzicore.shared.core.disguise.disguises.DisguiseBase;
import com.houzicore.shared.core.disguise.disguises.DisguiseVillager;
import com.houzicore.shared.core.game.kit.GameKit;
import com.houzicore.arcade.nautilus.game.arcade.kit.KitAvailability;
import com.houzicore.shared.core.itemstack.ItemBuilder;
import com.houzicore.shared.core.itemstack.ItemStackFactory;
import com.houzicore.shared.recharge.Recharge;

import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.game.Game.GameState;
import com.houzicore.arcade.nautilus.game.arcade.game.games.smash.SuperSmash;
import com.houzicore.arcade.nautilus.game.arcade.game.games.smash.events.SmashActivateEvent;
import com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.PerkSmashStats;
import com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.SmashKit;
import com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.villager.PerkArts;
import com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.villager.PerkSonicBoom;
import com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.villager.PerkVillagerShot;
import com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.villager.SmashVillager;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.PerkDoubleJump;

public class KitVillager extends SmashKit
{

	private static final Perk[] PERKS = {
			new PerkSmashStats(),
			new PerkDoubleJump("Double Jump"),
			new PerkSonicBoom(),
			new PerkVillagerShot(),
			new PerkArts(),
			new SmashVillager()
	};

	private static final ItemStack IN_HAND = new ItemStack(Material.WHEAT);

	private static final ItemStack[] PLAYER_ITEMS = {
			ItemStackFactory.Instance.CreateStack(Material.IRON_AXE, (byte) 0, 1,
					C.cYellow + C.Bold + "Right-Click" + (C.cWhite + C.Bold) + " - " + (C.cGreen + C.Bold) + "Sonic Hurr",
					new String[]{
							ChatColor.RESET + "Screech at the top of your lungs piercing players ears",
							ChatColor.RESET + "dealing damage and knockback in front of you.",
					}),
			ItemStackFactory.Instance.CreateStack(Material.IRON_HOE, (byte) 0, 1,
					C.cYellow + C.Bold + "Right-Click" + (C.cWhite + C.Bold) + " - " + (C.cGreen + C.Bold) + "Trade Scatter",
					new String[]{
							ChatColor.RESET + "After a hard days work of trading with the players,",
							ChatColor.RESET + "you unload your goods upon your enemies,",
							ChatColor.RESET + "propelling you back or forth depending on your trade skills",
							ChatColor.RESET + "and throwing your favorite items in the opposite direction."
					}),
			ItemStackFactory.Instance.CreateStack(Material.IRON_SHOVEL, (byte) 0, 1,
					C.cYellow + C.Bold + "Right-Click" + (C.cWhite + C.Bold) + " - " + (C.cGreen + C.Bold) + "Cycle Arts",
					new String[]{
							ChatColor.RESET + "Use your schooling from villager academy to hone in on one of",
							ChatColor.RESET + "three arts you specialized in and that give you different stats.",
							ChatColor.RESET + "Press right click to switch between arts and drop to activate.",
					}),
			ItemStackFactory.Instance.CreateStack(Material.NETHER_STAR, (byte) 0, 1,
					C.cYellow + C.Bold + "Smash Crystal" + (C.cWhite + C.Bold) + " - " + (C.cGreen + C.Bold) + "Perfection",
					new String[]{
							ChatColor.RESET + "Master all of the three arts and reaching perfection!",
							ChatColor.RESET + "You gain all of the positive effects from all three arts."
					}),
	};

	public static final int ART_ACTIVE_SLOT = 2;
	public static final int ART_VISUAL_SLOT = 7;

	private static final ItemStack[] PLAYER_ARMOR_NORMAL = {
			ItemStackFactory.Instance.CreateStack(Material.CHAINMAIL_BOOTS),
			ItemStackFactory.Instance.CreateStack(Material.CHAINMAIL_LEGGINGS),
			ItemStackFactory.Instance.CreateStack(Material.CHAINMAIL_CHESTPLATE),
			null,
	};
	private static final ItemStack[] PLAYER_ARMOR_ATTACK = {
			ItemStackFactory.Instance.CreateStack(Material.GOLDEN_BOOTS),
			ItemStackFactory.Instance.CreateStack(Material.GOLDEN_LEGGINGS),
			ItemStackFactory.Instance.CreateStack(Material.GOLDEN_CHESTPLATE),
			null,
	};

	private static final ItemStack[] PLAYER_ARMOR_DEFENSE = {
			ItemStackFactory.Instance.CreateStack(Material.DIAMOND_BOOTS),
			ItemStackFactory.Instance.CreateStack(Material.DIAMOND_LEGGINGS),
			ItemStackFactory.Instance.CreateStack(Material.IRON_CHESTPLATE),
			null,
	};

	private static final ItemStack[] PLAYER_ARMOR_SPEED = {
			null,
			null,
			ItemStackFactory.Instance.CreateStack(Material.DIAMOND_CHESTPLATE),
			null,
	};


	private final Map<Player, VillagerType> _types = new HashMap<>();

	public KitVillager(ArcadeManager manager)
	{
		super(manager, GameKit.SSM_VILLAGER, PERKS, DisguiseVillager.class);
	}

	@Override
	public void GiveItems(Player player)
	{
		_types.putIfAbsent(player, VillagerType.ATTACK);
		VillagerType type = get(player);

		disguise(player);

		UtilInv.Clear(player);

		player.getInventory().addItem(PLAYER_ITEMS[0], PLAYER_ITEMS[1]);

		if (Manager.GetGame().GetState() == GameState.Recruit)
		{
			player.getInventory().addItem(PLAYER_ITEMS[2], PLAYER_ITEMS[3]);
		}
		else
		{
			player.getInventory().setItem(ART_ACTIVE_SLOT, getArtItem(type, false));
			player.getInventory().setItem(ART_VISUAL_SLOT, getArtVisualItem(player, type));
		}

		giveArmour(player, false);
	}

	public void giveArmour(Player player, boolean active)
	{
		if (!active)
		{
			player.getInventory().setArmorContents(PLAYER_ARMOR_NORMAL);
			return;
		}

		VillagerType type = get(player);
		player.getInventory().setArmorContents(type.getArmour());
	}

	public void updateDisguise(Player player, Profession profession)
	{
		DisguiseBase disguise = Manager.GetDisguise().getDisguise(player);

		if (disguise == null || !(disguise instanceof DisguiseVillager))
		{
			return;
		}

		// ((DisguiseVillager) disguise).setProfession(profession);
		// Manager.GetDisguise().updateDisguise(disguise);
	}

	@EventHandler
	public void playerQuit(PlayerQuitEvent event)
	{
		_types.remove(event.getPlayer());
	}

	@EventHandler
	public void smashActivate(SmashActivateEvent event)
	{
		Player player = event.getPlayer();

		if (HasKit(player))
		{
			player.getInventory().setArmorContents(PLAYER_ARMOR_DEFENSE);
		}
	}

	public VillagerType get(Player player)
	{
		return _types.get(player);
	}

	public void set(Player player, VillagerType type)
	{
		_types.put(player, type);
	}

	public ItemStack getArtItem(Player player, boolean active)
	{
		return getArtItem(get(player), active);
	}

	public ItemStack getArtItem(VillagerType type, boolean active)
	{
		return new ItemBuilder(Material.IRON_SHOVEL)
				.setTitle(C.cYellow + C.Bold + "Right-Click/Drop" + (C.cWhite + C.Bold) + " - " + (active ? ChatColor.GRAY : type.getChatColour()) + C.Bold + type.getName())
				.setUnbreakable(true)
				.build();
	}

	public ItemStack getArtVisualItem(Player player, VillagerType type)
	{
		return getArtVisualItem(type, !Recharge.Instance.usable(player, type.getName()));
	}

	public ItemStack getArtVisualItem(VillagerType type, boolean active)
	{
		if (!active)
		{
			return new ItemBuilder(type.getDyeMaterial())
					.setTitle(type.getChatColour() + C.Bold + type.getName())
					.build();
		}
		else
		{
			return new ItemBuilder(type.getGlassMaterial())
					.setTitle(C.cGray + C.Bold + type.getName())
					.build();
		}
	}

	public VillagerType getActiveArt(Player player)
	{
		for (Perk perk : GetPerks())
		{
			if (perk instanceof PerkArts)
			{
				return ((PerkArts) perk).getActiveArt(player);
			}
		}

		return null;
	}

	public enum VillagerType
	{

		ATTACK("Butcher", ChatColor.RED, Color.RED, 1, Profession.BUTCHER, PLAYER_ARMOR_ATTACK, Material.RED_DYE, Material.RED_STAINED_GLASS_PANE),
		DEFENSE("Blacksmith", ChatColor.GOLD, Color.ORANGE, 14, Profession.ARMORER, PLAYER_ARMOR_DEFENSE, Material.ORANGE_DYE, Material.ORANGE_STAINED_GLASS_PANE),
		SPEED("Speedster", ChatColor.GREEN, Color.LIME, 10, Profession.LIBRARIAN, PLAYER_ARMOR_SPEED, Material.LIME_DYE, Material.LIME_STAINED_GLASS_PANE);

		private final String _name;
		private final ChatColor _chatColour;
		private final Color _colour;
		private final byte _dyeData;
		private final Profession _profession;
		private final ItemStack[] _armour;
		private final Material _dyeMaterial;
		private final Material _glassMaterial;

		VillagerType(String name, ChatColor chatColour, Color colour, int dyeData, Profession profession, ItemStack[] armour, Material dyeMaterial, Material glassMaterial)
		{
			_name = name;
			_chatColour = chatColour;
			_colour = colour;
			_dyeData = (byte) dyeData;
			_profession = profession;
			_armour = armour;
			_dyeMaterial = dyeMaterial;
			_glassMaterial = glassMaterial;
		}

		public String getName()
		{
			return _name;
		}

		public ChatColor getChatColour()
		{
			return _chatColour;
		}

		public Color getColour()
		{
			return _colour;
		}

		public byte getDyeData()
		{
			return _dyeData;
		}

		public Profession getProfession()
		{
			return _profession;
		}

		public ItemStack[] getArmour()
		{
			return _armour;
		}

		public Material getDyeMaterial()
		{
			return _dyeMaterial;
		}

		public Material getGlassMaterial()
		{
			return _glassMaterial;
		}

		public VillagerType getNext()
		{
			return ordinal() == values().length - 1 ? values()[0] : values()[ordinal() + 1];
		}
	}
}
