package com.houzicore.shared.core.party;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.UUID;

import com.houzicore.shared.common.Rank;
import com.houzicore.shared.common.jsonchat.ChildJsonMessage;
import com.houzicore.shared.common.jsonchat.ClickEvent;
import com.houzicore.shared.common.jsonchat.JsonMessage;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.NautHashMap;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilTime;
import com.houzicore.shared.core.party.redis.RedisPartyData;
import com.houzicore.shared.serverdata.Region;
import com.houzicore.shared.serverdata.commands.ServerTransfer;
import com.houzicore.shared.serverdata.commands.TransferCommand;
import com.houzicore.shared.serverdata.data.ServerGroup;
import com.houzicore.shared.serverdata.servers.ServerManager;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

public class Party {
	private PartyManager _manager;
	private boolean _isHub;

	private UUID _creatorUuid;
	private String _previousServer;
	private String _password = null;

	private ArrayList<UUID> _players = new ArrayList<>();
	private final NautHashMap<UUID, Long> _invitee = new NautHashMap<>();
	private final NautHashMap<UUID, String> _playerNames = new NautHashMap<>();

	private Scoreboard _scoreboard;
	private Objective _scoreboardObj;
	private final ArrayList<String> _scoreboardLast = new ArrayList<>();

	private long _partyOfflineTimer = -1;
	private long _informNewLeaderTimer = -1;
	
	public String getPassword() {
	    return _password;
	}
	
	public void setPassword(String password) {
	    _password = password;
	}
	
	public boolean isLocked() {
	    return _password != null && !_password.isEmpty();
	}

	public Party(PartyManager manager) {
		_manager = manager;
		final Region region = manager.getPlugin().getConfig().getBoolean("serverstatus.asia") ? Region.ASIA : Region.TH;
		final String groupName = manager.getPlugin().getConfig().getString("serverstatus.group");

		final ServerGroup serverGroup = ServerManager.getServerRepository(region).getServerGroup(groupName);

		if (serverGroup == null)
			return;

		_isHub = !serverGroup.getArcadeGroup();

		if (_isHub) {
			_scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
			_scoreboardObj = _scoreboard.registerNewObjective("Party", "dummy");
			_scoreboardObj.setDisplaySlot(DisplaySlot.SIDEBAR);

			_scoreboard.registerNewTeam(ChatColor.GREEN + "Members");

			for (final Rank rank : Rank.values()) {
				if (rank != Rank.ALL) {
					_scoreboard.registerNewTeam(rank.Name).setPrefix(rank.GetTag(true, false) + ChatColor.RESET + " ");
				} else {
					_scoreboard.registerNewTeam(rank.Name).setPrefix("");
				}
			}

			_scoreboard.registerNewTeam("Party")
					.setPrefix(ChatColor.LIGHT_PURPLE + C.Bold + "Party" + ChatColor.RESET + " ");

			for (final Player player : Bukkit.getOnlinePlayers()) {
				_scoreboard.getTeam(_manager.GetClients().Get(player).GetRank().Name).addPlayer(player);
			}
		}
	}

	public Party(PartyManager manager, RedisPartyData partyData) {
		this(manager);
        _players = new ArrayList<>(java.util.Arrays.asList(partyData.getPlayerUuids()));
        for (int i = 0; i < partyData.getPlayerUuids().length; i++) {
            _playerNames.put(partyData.getPlayerUuids()[i], partyData.getPlayers()[i]);
        }
		_creatorUuid = partyData.getLeaderUuid();
		_previousServer = partyData.getPreviousServer();
		_password = partyData.getPassword();
	}

    public String getName(UUID uuid) {
        if (_playerNames.containsKey(uuid)) return _playerNames.get(uuid);
        return "Unknown";
    }

	public void Announce(String translationKey, String... args) {
		for (final UUID uuid : _players) {
			final Player player = Bukkit.getPlayer(uuid);

			if (player != null && player.isOnline()) {
				String localized = com.houzicore.shared.core.lang.LangManager.get().get(player, translationKey);
				if (args != null && args.length > 0) {
					for (int i = 0; i < args.length; i++) {
						localized = localized.replace("{" + i + "}", args[i]);
					}
				}
				UtilPlayer.message(player, F.main("Party", localized));
				player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1.5f);
			}
		}
	}

	public void ExpireInvitees() {
		final Iterator<UUID> inviteeIterator = _invitee.keySet().iterator();

		while (inviteeIterator.hasNext()) {
			final UUID uuid = inviteeIterator.next();

			if (UtilTime.elapsed(_invitee.get(uuid), 60000)) {
				Announce("party.invite_offline", F.name(getName(uuid)));
				inviteeIterator.remove();
			}
		}
	}

	public Collection<UUID> GetInvitees() {
		return _invitee.keySet();
	}

	public String getLeaderName() {
		if (_players.isEmpty())
			return getName(_creatorUuid);

		return getName(_players.get(0));
	}

    public UUID getLeaderUuid() {
        if (_players.isEmpty()) return _creatorUuid;
        return _players.get(0);
    }

	public Collection<String> GetPlayers() {
		ArrayList<String> list = new ArrayList<>();
        for (UUID u : _players) list.add(getName(u));
        return list;
	}

    public Collection<UUID> getPlayerUuids() {
        return _players;
    }

	public Collection<Player> GetPlayersOnline() {
		final ArrayList<Player> players = new ArrayList<>();

		for (final UUID uuid : _players) {
			final Player player = Bukkit.getPlayer(uuid);
			if (player != null) {
				players.add(player);
			}
		}

		return players;
	}

	public void InviteParty(Player player, boolean inviteeInParty) {
		_invitee.put(player.getUniqueId(), System.currentTimeMillis());
        _playerNames.put(player.getUniqueId(), player.getName());

		if (_players.contains(player.getUniqueId())) {
			UtilPlayer.message(player, F.main("Party", com.houzicore.shared.core.lang.LangManager.get().get(player, "party.invite_declined").replace("{0}", F.name(player.getName()))));
			player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1.5f);
		}

		Announce("party.invited", F.name(player.getName()));
		UtilPlayer.message(player, F.main("Party", com.houzicore.shared.core.lang.LangManager.get().get(player, "party.invited_you").replace("{0}", F.name(getLeaderName()))));

		if (inviteeInParty) {
			final ChildJsonMessage message = new JsonMessage("").extra(C.mHead + "Party> " + C.mBody + com.houzicore.shared.core.lang.LangManager.get().get(player, "party.invited_instruction_1").replace("{0}", F.link(getLeaderName())));
			message.click(ClickEvent.RUN_COMMAND, "/party " + getLeaderName());
			message.sendToPlayer(player);
		} else {
			final ChildJsonMessage message = new JsonMessage("").extra(C.mHead + "Party> " + C.mBody + com.houzicore.shared.core.lang.LangManager.get().get(player, "party.invited_instruction_2").replace("{0}", F.link(getLeaderName())));
			message.click(ClickEvent.RUN_COMMAND, "/party " + getLeaderName());
			message.sendToPlayer(player);
		}

		player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1.5f);
	}

	public boolean IsDead() {
		if (_players.size() == 0)
			return true;

		if (_players.size() == 1 && _invitee.size() == 0)
			return true;

		int online = 0;
		for (final UUID uuid : _players) {
			final Player player = Bukkit.getPlayer(uuid);
			if (player != null) {
				online++;
			}
		}

		if (online <= 1) {
			if (_partyOfflineTimer == -1) {
				_partyOfflineTimer = System.currentTimeMillis();
			} else {
				if (UtilTime.elapsed(_partyOfflineTimer, online == 0 ? 5000 : 120000))
					return true;
			}
		} else if (_partyOfflineTimer > 0) {
			_partyOfflineTimer = -1;
		}

		return false;
	}

	public void JoinParty(Player player) {
        _playerNames.put(player.getUniqueId(), player.getName());

		if (_players.isEmpty()) {
			_players.add(player.getUniqueId());
			UtilPlayer.message(player, F.main("Party", com.houzicore.shared.core.lang.LangManager.get().get(player, "party.created")));
			_creatorUuid = player.getUniqueId();
		} else {
			_players.add(player.getUniqueId());
			_invitee.remove(player.getUniqueId());
			Announce("party.joined", F.elem(player.getName()));
		}

		if (_isHub) {
			_scoreboard.getTeam("Party").addPlayer(player);
		}
	}

	public void KickParty(String playerName) {
        UUID toRemove = null;
        for (UUID u : _players) {
            if (getName(u).equalsIgnoreCase(playerName)) {
                toRemove = u;
                break;
            }
        }
        if (toRemove != null) {
		    Announce("party.kicked", F.name(getName(toRemove)));
		    _players.remove(toRemove);
        }
	}

	public void PromoteParty(UUID targetUuid) {
        if (_players.contains(targetUuid)) {
            _players.remove(targetUuid);
            _players.add(0, targetUuid);
            _creatorUuid = targetUuid;
            Announce("party.leadership_passed", F.name(getName(targetUuid)));
        }
	}

	public void LeaveParty(Player player) {
		Announce("party.left", F.name(player.getName()));

		final boolean leader = player.getUniqueId().equals(getLeaderUuid());

		_players.remove(player.getUniqueId());

		if (_isHub) {
			_scoreboard.getTeam(_manager.GetClients().Get(player).GetRank().Name).addPlayer(player);
		}

		if (leader && _players.size() > 0) {
			Announce("party.leadership_passed", F.name(getLeaderName()));
		}
	}

	public void PlayerJoin(Player player) {
        _playerNames.put(player.getUniqueId(), player.getName());

		if (_isHub) {
			if (_players.contains(player.getUniqueId())) {
				_scoreboard.getTeam("Party").addPlayer(player);
			} else if (_manager.GetClients().Get(player) != null) {
				_scoreboard.getTeam(_manager.GetClients().Get(player).GetRank().Name).addPlayer(player);
			}
		}

		if (_creatorUuid != null && _creatorUuid.equals(player.getUniqueId())) {
			_players.remove(player.getUniqueId());
			_players.add(0, player.getUniqueId());

			if (_informNewLeaderTimer < System.currentTimeMillis()) {
				Announce("party.leadership_returned", F.name(getLeaderName()));
			}

			if (_previousServer != null) {
				for (final UUID puuid : _players) {
					final Player p = Bukkit.getPlayer(puuid);

					if (p != null) {
						continue;
					}

					final TransferCommand transferCommand = new TransferCommand(
							new ServerTransfer(getName(puuid), _manager.getServerName()));

					transferCommand.setTargetServers(_previousServer);

					transferCommand.publish();
				}

				_previousServer = null;
			}
		}
	}

	public void PlayerQuit(Player player) {
		if (player.getUniqueId().equals(getLeaderUuid())) {
			_players.remove(player.getUniqueId());
			_players.add(player.getUniqueId());

			if (_informNewLeaderTimer < System.currentTimeMillis()) {
				Announce("party.leadership_passed", F.name(getLeaderName()));
			}
		}
	}

	public void resetWaitingTime() {
		_partyOfflineTimer = -1;
	}

	public void switchedServer() {
		_informNewLeaderTimer = System.currentTimeMillis() + 5000;
	}

	public void UpdateScoreboard() {
		if (_isHub) {
			_scoreboardObj.setDisplayName(getLeaderName() + "'s Party");

			for (final String pastLine : _scoreboardLast) {
				_scoreboard.resetScores(pastLine);
			}
			_scoreboardLast.clear();

			int i = 16;

			for (int j = 0; j < _players.size(); j++) {
				final UUID puuid = _players.get(j);
				final Player player = Bukkit.getPlayer(puuid);

				ChatColor col = ChatColor.GREEN;
				if (player == null) {
					col = ChatColor.RED;
				}

				String line = col + getName(puuid);

				if (line.length() > 16) {
					line = line.substring(0, 16);
				}

				_scoreboardObj.getScore(line).setScore(i);

				_scoreboardLast.add(line);

				i--;
			}

			for (final UUID puuid : _invitee.keySet()) {
				final int time = 1 + (int) ((60000 - (System.currentTimeMillis() - _invitee.get(puuid))) / 1000);

				String line = time + " " + ChatColor.GRAY + getName(puuid);

				if (line.length() > 16) {
					line = line.substring(0, 16);
				}

				_scoreboardObj.getScore(line).setScore(i);

				_scoreboardLast.add(line);

				i--;
			}

			for (final UUID puuid : _players) {
				final Player player = Bukkit.getPlayer(puuid);

				if (player != null) {
					if (!player.getScoreboard().equals(_scoreboard)) {
						player.setScoreboard(_scoreboard);
					}
				}
			}
		}
	}
}

