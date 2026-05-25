package com.houzicore.shared.core.party;

import java.util.Iterator;
import java.util.UUID;

import com.houzicore.shared.MiniPlugin;
import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.common.util.NautHashMap;
import com.houzicore.shared.core.party.commands.PartyCommand;
import com.houzicore.shared.core.party.redis.RedisPartyData;
import com.houzicore.shared.core.party.redis.RedisPartyHandler;
import com.houzicore.shared.core.portal.Portal;
import com.houzicore.shared.core.portal.ServerTransferEvent;
import com.houzicore.shared.core.preferences.PreferencesManager;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.shared.serverdata.commands.ServerCommandManager;
import com.houzicore.shared.core.party.ui.PartyShop;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class PartyManager extends MiniPlugin {
	private final CoreClientManager _clientManager;
	private final PreferencesManager _preferenceManager;
	private final com.houzicore.shared.core.ignore.IgnoreManager _ignoreManager;
	private final Portal _portal;
	private final String _serverName;
	private final PartyShop _shop;

	public NautHashMap<UUID, Party> _partyLeaderMap = new NautHashMap<>();

	public PartyManager(JavaPlugin plugin, Portal portal, CoreClientManager clientManager,
			PreferencesManager preferenceManager, com.houzicore.shared.core.ignore.IgnoreManager ignoreManager) {
		super("Party Manager", plugin);

		_portal = portal;
		_clientManager = clientManager;
		_preferenceManager = preferenceManager;
		_ignoreManager = ignoreManager;
		_serverName = getPlugin().getConfig().getString("serverstatus.name");
		_shop = new PartyShop(this, clientManager);

		ServerCommandManager.getInstance().registerCommandType("RedisPartyData", RedisPartyData.class,
				new RedisPartyHandler(this));
	}

	public com.houzicore.shared.core.ignore.IgnoreManager getIgnoreManager() {
		return _ignoreManager;
	}

	@Override
	public void addCommands() {
		addCommand(new PartyCommand(this));
	}

	public void openShop(Player player) {
		_shop.attemptShopOpen(player);
	}

	public void addParty(Party party) {
		if (_partyLeaderMap.containsKey(party.getLeaderUuid())) {
			_partyLeaderMap.get(party.getLeaderUuid()).resetWaitingTime();
		} else {
			_partyLeaderMap.put(party.getLeaderUuid(), party);
		}
	}

	public Party CreateParty(Player player) {
		final Party party = new Party(this);
		party.JoinParty(player);
		_partyLeaderMap.put(player.getUniqueId(), party);

		return party;
	}

	public void ExpireParties() {
		final Iterator<Party> partyIterator = _partyLeaderMap.values().iterator();

		while (partyIterator.hasNext()) {
			final Party party = partyIterator.next();

			if (party.IsDead()) {
				party.Announce("ปาร์ตี้ของคุณถูกปิดแล้ว", "Your Party has been closed.");
				partyIterator.remove();
			}
		}
	}

	public CoreClientManager GetClients() {
		return _clientManager;
	}

	public Party getPartyByPlayer(Player player) {
		for (final Party party : _partyLeaderMap.values()) {
			if (party.getPlayerUuids().contains(player.getUniqueId()))
				return party;
		}

		return null;
	}

	public PreferencesManager getPreferenceManager() {
		return _preferenceManager;
	}

	public String getServerName() {
		return _serverName;
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void PlayerJoin(PlayerJoinEvent event) {
		try {
			for (final Party party : _partyLeaderMap.values()) {
				party.PlayerJoin(event.getPlayer());
			}
		} catch (final Exception ex) {
			throw ex;
		}
	}

	@EventHandler
	public void PlayerQuit(PlayerQuitEvent event) {
		for (final Party party : _partyLeaderMap.values()) {
			party.PlayerQuit(event.getPlayer());
		}
	}

	@EventHandler
	public void serverTransfer(ServerTransferEvent event) {
		final Party party = getPartyByPlayer(event.getPlayer());

		if (party != null && party.getLeaderUuid().equals(event.getPlayer().getUniqueId())) {
			party.switchedServer();

			final RedisPartyData data = new RedisPartyData(party, _serverName);
			data.setTargetServers(event.getServer());
			data.publish();

			for (final Player player : party.GetPlayersOnline()) {
				if (player != event.getPlayer()) {
					_portal.sendPlayerToServer(player, event.getServer(), false);
				}
			}
		}
	}

	@EventHandler
	public void Update(UpdateEvent event) {
		if (event.getType() != UpdateType.FAST)
			return;

		ExpireParties();

		for (final Party party : _partyLeaderMap.values()) {
			party.ExpireInvitees();
			party.UpdateScoreboard();
		}
	}
}

