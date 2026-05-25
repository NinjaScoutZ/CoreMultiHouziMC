package com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.team;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.core.hologram.Hologram;
import com.houzicore.shared.core.hologram.Hologram.HologramTarget;
import com.houzicore.shared.core.hologram.HologramManager;

import com.houzicore.arcade.nautilus.game.arcade.game.GameTeam;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.Bedwars;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.shop.BedwarsItem;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.shop.BedwarsNetherItem;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.shop.BedwarsResource;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.shop.BedwarsTeamItem;

public class BedwarsTeam
{

	private static final String[] EDGE_HOLOGRAM_TEXT = {
			"§e§lᴛᴇᴀᴍ ᴜᴘɢʀᴀᴅᴇs",
			"§bClick to Upgrade!"
	};
	private static final String[] SHOP_HOLOGRAM_TEXT = {
			"§6§lɪᴛᴇᴍ sʜᴏᴘ",
			"§eClick to Browse!"
	};
	private static final String[] BED_HOLOGRAM_TEXT = {
			"§a§l⛏ ᴘʀᴏᴛᴇᴄᴛ ʏᴏᴜʀ ʙᴇᴅ ⛏",
			"§7Defend it at all costs!"
	};
	private static final String[] GENERATOR_HOLOGRAM_TEXT = {
			"§e§lɢᴇɴᴇʀᴀᴛᴏʀ §6§lᴛɪᴇʀ ɪ",
			"§fɪʀᴏɴ §7& §6ɢᴏʟᴅ",
			"§7Spawning: §a■■■■■■■■■■"
	};

	private final Bedwars _game;
	private final GameTeam _team;
	private final Location _edge;
	private final Location _shop;
	private final Location _chest;
	private final Location _bed;
	private final Location _generator;
	private final Map<BedwarsTeamItem, Integer> _upgrades;
	private final List<Hologram> _tipHolograms;
	private final List<Hologram> _otherHolograms;

	private Hologram _bedHologram;
	private Hologram _generatorHologram;

	BedwarsTeam(Bedwars game, GameTeam team, Location edgeHologram, Location shopHologram, Location chest, Location generator)
	{
		_game = game;
		_team = team;
		_edge = edgeHologram.add(0, 2, 0);
		_shop = shopHologram.add(0, 2, 0);
		_chest = chest.getBlock().getLocation();
		_generator = generator.add(0, 1, 0);

		_bed = game.WorldData.GetDataLocs(team.GetName().toUpperCase()).get(0).getBlock().getLocation().add(0.5, 0.5, 0.5);
		if (!(_bed.getBlock().getBlockData() instanceof org.bukkit.block.data.type.Bed))
		{
			String teamName = team.GetName().toUpperCase();
			Material bedMaterial = Material.RED_BED;
			try
			{
				bedMaterial = Material.valueOf(teamName + "_BED");
			}
			catch (Exception e)
			{
				if (teamName.contains("RED")) bedMaterial = Material.RED_BED;
				else if (teamName.contains("BLUE")) bedMaterial = Material.BLUE_BED;
				else if (teamName.contains("GREEN")) bedMaterial = Material.GREEN_BED;
				else if (teamName.contains("YELLOW")) bedMaterial = Material.YELLOW_BED;
				else if (teamName.contains("PINK")) bedMaterial = Material.PINK_BED;
				else if (teamName.contains("WHITE")) bedMaterial = Material.WHITE_BED;
				else if (teamName.contains("ORANGE")) bedMaterial = Material.ORANGE_BED;
				else if (teamName.contains("PURPLE")) bedMaterial = Material.PURPLE_BED;
				else if (teamName.contains("CYAN")) bedMaterial = Material.CYAN_BED;
				else if (teamName.contains("GRAY")) bedMaterial = Material.GRAY_BED;
				else if (teamName.contains("BLACK")) bedMaterial = Material.BLACK_BED;
			}
			_bed.getBlock().setType(bedMaterial);
		}
		_upgrades = new HashMap<>();

		for (BedwarsItem item : game.generateItems(BedwarsResource.STAR))
		{
			_upgrades.put((BedwarsTeamItem) item, 0);
		}

		_tipHolograms = new ArrayList<>(3);
		_otherHolograms = new ArrayList<>(5);

		setupHolograms();
	}

	private void setupHolograms()
	{
		HologramManager hologramManager = _game.getArcadeManager().getHologramManager();

		_tipHolograms.add(new Hologram(hologramManager, _edge, EDGE_HOLOGRAM_TEXT)
				.setHologramTarget(HologramTarget.WHITELIST)
				.start());

		_tipHolograms.add(new Hologram(hologramManager, _shop, SHOP_HOLOGRAM_TEXT)
				.setHologramTarget(HologramTarget.WHITELIST)
				.start());

		_bedHologram = new Hologram(hologramManager, _bed.clone().add(0.5, 1.3, 0.5), BED_HOLOGRAM_TEXT)
				.setHologramTarget(HologramTarget.WHITELIST)
				.start();
		_tipHolograms.add(_bedHologram);

		_generatorHologram = new Hologram(hologramManager, _generator, GENERATOR_HOLOGRAM_TEXT)
				.setHologramTarget(HologramTarget.WHITELIST)
				.start();
		_otherHolograms.add(_generatorHologram);
	}

	public boolean canRespawn()
	{
		return _bed.getBlock().getBlockData() instanceof org.bukkit.block.data.type.Bed;
	}

	public org.bukkit.block.Block getOtherBedBlock()
	{
		org.bukkit.block.Block block = _bed.getBlock();
		if (block.getBlockData() instanceof org.bukkit.block.data.type.Bed)
		{
			org.bukkit.block.data.type.Bed bedData = (org.bukkit.block.data.type.Bed) block.getBlockData();
			return bedData.getPart() == org.bukkit.block.data.type.Bed.Part.FOOT 
				? block.getRelative(bedData.getFacing()) 
				: block.getRelative(bedData.getFacing().getOppositeFace());
		}
		return null;
	}

	public GameTeam getGameTeam()
	{
		return _team;
	}

	public Location getShop()
	{
		return _shop;
	}

	public Location getChest()
	{
		return _chest;
	}

	public Location getBed()
	{
		return _bed;
	}

	public Location getGenerator()
	{
		return _generator;
	}

	public Map<BedwarsTeamItem, Integer> getUpgrades()
	{
		return _upgrades;
	}

	public List<Hologram> getTipHolograms()
	{
		return _tipHolograms;
	}

	public List<Hologram> getOtherHolograms()
	{
		return _otherHolograms;
	}

	public Hologram getBedHologram()
	{
		return _bedHologram;
	}

	public Hologram getGeneratorHologram()
	{
		return _generatorHologram;
	}
}
