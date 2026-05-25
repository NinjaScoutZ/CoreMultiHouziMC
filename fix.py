import sys
import re

path = r'E:\Houzicore\Code\Arcade\src\main\java\com\houzicore\arcade\nautilus\game\arcade\game\games\primalgames\PrimalGames.java'
with open(path, 'r', encoding='utf-8') as f:
    text = f.read()

replacement = '''		BlockBreakAllow.add(Material.SHORT_GRASS.ordinal());
		BlockBreakAllow.add(Material.POPPY.ordinal());
		BlockBreakAllow.add(Material.DANDELION.ordinal());
		BlockBreakAllow.add(Material.BROWN_MUSHROOM.ordinal());
		BlockBreakAllow.add(Material.RED_MUSHROOM.ordinal());
		BlockBreakAllow.add(Material.DEAD_BUSH.ordinal());
		BlockBreakAllow.add(Material.CARROT.ordinal());
		BlockBreakAllow.add(Material.POTATO.ordinal());
		BlockBreakAllow.add(Material.SUNFLOWER.ordinal());
		BlockBreakAllow.add(Material.WHEAT.ordinal());
		BlockBreakAllow.add(Material.OAK_SAPLING.ordinal());
		BlockBreakAllow.add(Material.VINE.ordinal());
		BlockBreakAllow.add(Material.LILY_PAD.ordinal());

		// Manager.GetStatsManager().addTable(GetName(), "kills", "deaths", "chestsOpened");

		setupLoot();

		_useEntityPacketHandler = new IPacketHandler()
		{
			@Override
			public void handle(PacketInfo packetInfo)
			{
				// Modernized: We don't need this packet handler for arrows anymore 
			}
		};


		registerStatTrackers(new WinWithoutWearingArmorStatTracker(this), new KillsWithinTimeLimitStatTracker(this, 3, 60,
				"Bloodlust"), new FirstSupplyDropOpenStatTracker(this), new SimultaneousSkeletonStatTracker(this, 5),
				new com.houzicore.arcade.nautilus.game.arcade.stats.ChestOpenStatTracker(this),
				new com.houzicore.arcade.nautilus.game.arcade.stats.WinWithoutKillsStatTracker(this),
				new com.houzicore.arcade.nautilus.game.arcade.stats.BackstabKillStatTracker(this),
				new com.houzicore.arcade.nautilus.game.arcade.stats.WinWithoutTakingPlayerDamageStatTracker(this),
				new com.houzicore.arcade.nautilus.game.arcade.stats.TheLongestShotStatTracker(this));

		_runeManager = new RuneManager(this);
		org.bukkit.Bukkit.getPluginManager().registerEvents(_runeManager, manager.getPlugin());

		_airdropManager = new AirdropManager(this, _runeManager);
		org.bukkit.Bukkit.getPluginManager().registerEvents(_airdropManager, manager.getPlugin());

		_toxicCaveEvent = new ToxicCaveEvent(this);
		org.bukkit.Bukkit.getPluginManager().registerEvents(_toxicCaveEvent, manager.getPlugin());

		_traderManager = new WanderingTraderManager(this, _runeManager);
		org.bukkit.Bukkit.getPluginManager().registerEvents(_traderManager, manager.getPlugin());

		// registerCustomRecipes() moved to GameState.Live to prevent deletion during Prepare
	}

	private void registerCustomRecipes()'''

new_text = text.replace('		BlockBreakAllow.add(Material.SHORT_GRASS.ordinal());\n\tprivate void registerCustomRecipes()', replacement)

if text != new_text:
    with open(path, 'w', encoding='utf-8') as f:
        f.write(new_text)
    print('Fixed successfully.')
else:
    print('Failed to replace. Text was not found.')
