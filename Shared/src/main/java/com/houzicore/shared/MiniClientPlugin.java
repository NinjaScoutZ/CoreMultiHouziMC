package com.houzicore.shared;

import java.util.Collection;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.java.JavaPlugin;

import com.houzicore.shared.account.event.ClientUnloadEvent;
import com.houzicore.shared.common.util.NautHashMap;

public abstract class MiniClientPlugin<DataType extends Object> extends MiniPlugin {
	private static Object _clientDataLock = new Object();

	private final NautHashMap<String, DataType> _clientData = new NautHashMap<>();

	public MiniClientPlugin(String moduleName, JavaPlugin plugin) {
		super(moduleName, plugin);
	}

	protected abstract DataType AddPlayer(String player);

	public DataType Get(Player player) {
		return Get(player.getName());
	}

	public DataType Get(String name) {
		synchronized (_clientDataLock) {
			if (!_clientData.containsKey(name)) {
				_clientData.put(name, AddPlayer(name));
			}

			return _clientData.get(name);
		}
	}

	protected Collection<DataType> GetValues() {
		return _clientData.values();
	}

	protected void Set(Player player, DataType data) {
		Set(player.getName(), data);
	}

	protected void Set(String name, DataType data) {
		synchronized (_clientDataLock) {
			_clientData.put(name, data);
		}
	}

	@EventHandler
	public void UnloadPlayer(ClientUnloadEvent event) {
		synchronized (_clientDataLock) {
			_clientData.remove(event.GetName());
		}
	}
}
