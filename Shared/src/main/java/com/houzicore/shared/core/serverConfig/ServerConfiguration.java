package com.houzicore.shared.core.serverConfig;





import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.plugin.java.JavaPlugin;

import com.houzicore.shared.MiniPlugin;
import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.serverdata.Region;
import com.houzicore.shared.serverdata.data.ServerGroup;
import com.houzicore.shared.serverdata.servers.ServerManager;

public class ServerConfiguration extends MiniPlugin {
	private CoreClientManager _clientManager;

	// private Field _playerListMaxPlayers; // NMS PlayerList removed in Paper 1.21
	private ServerGroup _serverGroup;

	public ServerConfiguration(JavaPlugin plugin, CoreClientManager clientManager) {
		super("Server Configuration", plugin);

		_clientManager = clientManager;
		final Region region = plugin.getConfig().getBoolean("serverstatus.asia") ? Region.ASIA : Region.TH;
		final String groupName = plugin.getConfig().getString("serverstatus.group");

		_serverGroup = ServerManager.getServerRepository(region).getServerGroup(groupName);

		if (_serverGroup == null)
			return;

		// NMS PlayerList removed - max players overriding no longer supported
		// try {
		// 	_playerListMaxPlayers = PlayerList.class.getDeclaredField("maxPlayers");
		// 	_playerListMaxPlayers.setAccessible(true);
		// } catch (final Exception e) {
		// 	org.bukkit.Bukkit.getServer().getLogger().log(java.util.logging.Level.SEVERE, e.getMessage(), e);
		// }

		_plugin.getServer().setWhitelist(_serverGroup.getWhitelist());
		//pvp overriding removed
		// ((CraftServer)_plugin.getServer()).getServer().setTexturePack(_serverGroup.getResourcePack());
	}

	public ServerGroup getServerGroup() {
		return _serverGroup;
	}

	@EventHandler
	public void onPlayerLogin(PlayerLoginEvent event) {
		if (_serverGroup == null) return;

		if (_serverGroup.getStaffOnly() && !_clientManager.Get(event.getPlayer().getName()).GetRank()
				.Has(event.getPlayer(), Rank.HELPER, false)) {
			event.disallow(Result.KICK_OTHER, "This is a staff only server.");
		}
	}
}
