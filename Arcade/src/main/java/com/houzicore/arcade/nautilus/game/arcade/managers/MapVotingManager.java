package com.houzicore.arcade.nautilus.game.arcade.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import com.houzicore.arcade.nautilus.game.arcade.managers.voting.MapVotingShop;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilAction;
import com.houzicore.shared.common.util.UtilMath;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.core.itemstack.ItemBuilder;
import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.GameType;
import com.houzicore.arcade.nautilus.game.arcade.events.GameStateChangeEvent;
import com.houzicore.arcade.nautilus.game.arcade.game.Game;
import com.houzicore.arcade.nautilus.game.arcade.game.Game.GameState;

public class MapVotingManager implements Listener 
{
	private ArcadeManager Manager;
	
	private boolean _votingActive = false;
	private ArrayList<String> _mapOptions = new ArrayList<>();
	private HashMap<Player, String> _votes = new HashMap<>();
	private MapVotingShop _shop;
	private GameType _gameType;

	public MapVotingManager(ArcadeManager manager)
	{
		Manager = manager;
		_shop = new MapVotingShop(Manager, Manager.GetClients(), Manager.GetDonation(), this);
		Manager.getPluginManager().registerEvents(this, manager.getPlugin());
	}
	
	public void startVoting(final Game game)
	{
		// Generate Map Options
		_mapOptions.clear();
		_votes.clear();
			
		if (game != null)
		{
			_gameType = game.GetType();
			// IMPORTANT: Only use maps belonging to THIS game's specific type.
			// Do NOT flatten all GameType keys — that causes cross-game map contamination.
			ArrayList<String> allMaps = game.GetFiles().getOrDefault(game.GetType(), new ArrayList<>());
				
			// If no maps or only 1 map, skip voting and load immediately
			if (allMaps.size() <= 1)
			{
				game.loadWorld(allMaps.isEmpty() ? null : allMaps.get(0));
				return;
			}

			int mapsToPull = Math.min(5, allMaps.size());
			ArrayList<String> pool = new ArrayList<>(allMaps); // copy to shuffle from
			java.util.Collections.shuffle(pool);
			for (int i = 0; i < mapsToPull; i++)
			{
				_mapOptions.add(pool.get(i));
			}
			
			_votingActive = true;
			giveVoteItemToAll();

			// Voting timer — runs every second while game is in Vote state
			new org.bukkit.scheduler.BukkitRunnable() {
				int ticks = 15;
				boolean generating = false;
				String winnerMap = null;
				
				@Override
				public void run() {
					if (!_votingActive && !generating) {
						this.cancel();
						return;
					}
					
					// Cancel if game state changed unexpectedly
					if (!generating && game.GetState() != GameState.Vote) {
						this.cancel();
						return;
					}
					if (generating && game.GetState() != GameState.Loading) {
						this.cancel();
						return;
					}

					if (!generating) {
						// === VOTING PHASE ===
						if (ticks == 15 || ticks == 10 || (ticks <= 5 && ticks > 0)) {
							for (Player p : UtilServer.getPlayers()) {
								p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
							}
						}
						
						if (ticks > 0) {
							for (Player p : UtilServer.getPlayers()) {
								String text = com.houzicore.shared.core.lang.LangManager.get().get(p, "arcade.map_vote_ends", (Object) String.valueOf(ticks));
								com.houzicore.shared.common.util.UtilTextTop.displayProgress(text, (double) ticks / 15.0, org.bukkit.boss.BarColor.YELLOW, p);
							}
						}

						if (ticks <= 0) {
							winnerMap = closeVotingAndGetWinner();
							String winnerAuthor = getMapAuthor(game.GetType(), winnerMap);
							String title = C.cGold + C.Bold + "MAP SELECTED!";
							String subtitle = C.cYellow + winnerMap + C.cGray + " by " + C.cAqua + winnerAuthor;

							for (Player p : UtilServer.getPlayers()) {
								p.sendMessage(F.main("Voting", com.houzicore.shared.core.lang.LangManager.get().get(p, "arcade.map_vote_winner", (Object) winnerMap)));
								p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
								com.houzicore.shared.common.util.UtilTextMiddle.display(title, subtitle, 10, 60, 20, p);
							}
							
							// Switch to Loading Phase internally
							generating = true;
							game.SetState(GameState.Loading);
							game.SetCountdown(10);
							ticks = 11; // 10 seconds for Loading phase (+1 because ticks--)
						}
					} else {
						// === GENERATING PHASE ===
						game.SetCountdown(ticks - 1);
						
						if (ticks == 10 || (ticks <= 5 && ticks > 0)) {
							for (Player p : UtilServer.getPlayers()) {
								p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
							}
						}
						
						if (ticks > 0) {
							for (Player p : UtilServer.getPlayers()) {
								String text = com.houzicore.shared.core.lang.LangManager.get().get(p, "arcade.map_generate", (Object) String.valueOf(ticks - 1));
								com.houzicore.shared.common.util.UtilTextTop.displayProgress(text, Math.max(0.0, (double) (ticks - 1) / 10.0), org.bukkit.boss.BarColor.GREEN, p);
							}
						}

						if (ticks <= 1) {
							game.SetCountdown(-1);
							// Refresh state time so GameManager doesn't forcefully timeout too early
							game.SetStateTime(System.currentTimeMillis());
							// Proceed with the intensive operation
							game.loadWorld(winnerMap);
							this.cancel();
						}
					}
					
					ticks--;
				}
			}.runTaskTimer(Manager.getPlugin(), 0L, 20L);
		}
	}
	
	@EventHandler
	public void onGameStateChange(GameStateChangeEvent event)
	{
		if (event.GetState() == GameState.Prepare || event.GetState() == GameState.Dead)
		{
			_votingActive = false;
			_votes.clear();
		}
	}
	
	@EventHandler(priority = org.bukkit.event.EventPriority.MONITOR)
	public void onPlayerJoin(PlayerJoinEvent event)
	{
		if (_votingActive)
		{
			giveVoteItem(event.getPlayer());
		}
	}
	
	private void giveVoteItemToAll()
	{
		for (Player player : UtilServer.getPlayers())
		{
			giveVoteItem(player);
		}
	}
	
	private void giveVoteItem(final Player player)
	{
		// Delay 40 ticks (2 seconds) to execute AFTER all PlayerJoin Manager.Clear() calls
		Bukkit.getScheduler().runTaskLater(Manager.getPlugin(), new Runnable() {
			@Override
			public void run() {
				if (!_votingActive || !player.isOnline()) return;
				ItemStack paper = new ItemBuilder(Material.PAPER)
						.setTitle(C.cGreen + C.Bold + "Vote for Map")
						.addLore(C.cGray + "Right-click to vote for the next map!")
						.build();
				// Slot 4 (center hotbar) - avoids conflict with HubClock at slot 8
				player.getInventory().setItem(4, paper);
			}
		}, 40L);
	}

	@EventHandler
	public void onInteract(PlayerInteractEvent event)
	{
		if (!_votingActive) return;
		
		Player player = event.getPlayer();
		if (event.getAction().name().contains("RIGHT"))
		{
			// Check center hotbar slot where we place the vote paper
			ItemStack item = player.getInventory().getItem(4);
			if (item != null && item.getType() == Material.PAPER && item.hasItemMeta() && 
				item.getItemMeta().getDisplayName().contains("Vote for Map"))
			{
				event.setCancelled(true);
				openVoteGUI(player);
			}
		}
	}
	
	private int getMapNumber(String mapName)
	{
		return _mapOptions.indexOf(mapName) + 1;
	}

	private void setTablistVote(Player player, int num)
	{
		String gameName = "Waiting...";
		String mapName = "Unknown";
		if (Manager.GetGame() != null)
		{
			gameName = Manager.GetGame().GetName();
			if (Manager.GetGame().WorldData != null && Manager.GetGame().WorldData.MapName != null) {
				mapName = Manager.GetGame().WorldData.MapName;
			}
		}

		if (num > 0)
		{
			net.kyori.adventure.text.Component suffix = net.kyori.adventure.text.Component.text(" [", net.kyori.adventure.text.format.NamedTextColor.GRAY)
				.append(net.kyori.adventure.text.Component.text(num, net.kyori.adventure.text.format.NamedTextColor.YELLOW))
				.append(net.kyori.adventure.text.Component.text("]", net.kyori.adventure.text.format.NamedTextColor.GRAY));
			
			com.houzicore.shared.TablistFix.updateTablist(player, Manager.GetClients(), null, suffix, gameName, mapName);
		}
		else
		{
			com.houzicore.shared.TablistFix.updateTablist(player, Manager.GetClients(), null, null, gameName, mapName);
		}
	}
	
	private void openVoteGUI(Player player)
	{
		_shop.attemptShopOpen(player);
	}
	
	public int getVotesForMap(String mapName)
	{
		int count = 0;
		for (String vote : _votes.values())
		{
			if (vote.equals(mapName)) count++;
		}
		return count;
	}

	public List<String> getMapOptions() {
		return _mapOptions;
	}

	public boolean hasVotedFor(Player player, String mapName) {
		return _votes.containsKey(player) && _votes.get(player).equals(mapName);
	}

	public void voteFor(Player player, String mapName)
	{
		if (!_votingActive) return;
		if (_mapOptions.contains(mapName))
		{
			_votes.put(player, mapName);
			player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
			UtilPlayer.message(player, F.main("Voting", "You voted for " + F.elem(mapName) + "!"));
			setTablistVote(player, getMapNumber(mapName));
		}
	}
	
	public String closeVotingAndGetWinner()
	{
		_votingActive = false;
		
		for (Player player : UtilServer.getPlayers())
		{
			if (player.getOpenInventory() != null && player.getOpenInventory().getTitle().equals("Poll: What Theme?"))
			{
				player.closeInventory();
			}
			player.getInventory().setItem(4, null);
			setTablistVote(player, 0); // Clear vote suffix
		}
		
		HashMap<String, Integer> tallies = new HashMap<>();
		for (String opt : _mapOptions) tallies.put(opt, 0);
		for (String vote : _votes.values())
		{
			tallies.put(vote, tallies.getOrDefault(vote, 0) + 1);
		}
		
		// BUG FIX: old logic started maxVotes at -1 meaning any 0-vote map could "win"
		// and the random tie-break fired on every single entry. Now require >= 1 vote to win.
		int maxVotes = 0;
		ArrayList<String> tied = new ArrayList<>();
		
		for (Map.Entry<String, Integer> entry : tallies.entrySet())
		{
			if (entry.getValue() > maxVotes)
			{
				maxVotes = entry.getValue();
				tied.clear();
				tied.add(entry.getKey());
			}
			else if (entry.getValue() == maxVotes && maxVotes > 0)
			{
				// True tie — collect for fair random selection
				tied.add(entry.getKey());
			}
		}
		
		String winnerMap = null;
		if (!tied.isEmpty())
		{
			winnerMap = tied.get(UtilMath.r(tied.size()));
		}
		
		// No votes cast at all → random from all options
		if (winnerMap == null && !_mapOptions.isEmpty())
		{
			winnerMap = _mapOptions.get(UtilMath.r(_mapOptions.size()));
		}
		
		return winnerMap;
	}

	public GameType getGameType()
	{
		return _gameType;
	}

	public static String getMapAuthor(com.houzicore.arcade.GameType gameType, String mapName)
	{
		if (gameType == null || mapName == null)
		{
			return "Unknown";
		}
		String mapFolder = gameType.GetMapFolderName();
		java.io.File zipFile = new java.io.File("Maps/" + mapFolder + "/" + mapName + ".zip");
		if (!zipFile.exists())
		{
			return "Unknown";
		}

		try (java.util.zip.ZipInputStream zip = new java.util.zip.ZipInputStream(new java.io.FileInputStream(zipFile)))
		{
			java.util.zip.ZipEntry entry;
			while ((entry = zip.getNextEntry()) != null)
			{
				if (entry.getName().equalsIgnoreCase("schema.json"))
				{
					java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(zip));
					StringBuilder sb = new StringBuilder();
					String line;
					while ((line = reader.readLine()) != null)
					{
						sb.append(line);
					}
					java.util.regex.Matcher m = java.util.regex.Pattern.compile("\"author\"\\s*:\\s*\"([^\"]+)\"").matcher(sb.toString());
					if (m.find())
					{
						return m.group(1);
					}
				}
				else if (entry.getName().equalsIgnoreCase("WorldConfig.dat"))
				{
					java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(zip));
					String line;
					while ((line = reader.readLine()) != null)
					{
						if (line.startsWith("MAP_AUTHOR:"))
						{
							return line.substring("MAP_AUTHOR:".length()).trim();
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			// Ignore
		}
		return "Unknown";
	}
}
