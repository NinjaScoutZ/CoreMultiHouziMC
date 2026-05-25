package com.houzicore.shared;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.plugin.java.JavaPlugin;

import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.account.ILoginProcessor;

public abstract class MiniDbClientPlugin<DataType extends Object> extends MiniClientPlugin<DataType>
		implements ILoginProcessor {
	protected CoreClientManager ClientManager = null;

	public MiniDbClientPlugin(String moduleName, JavaPlugin plugin, CoreClientManager clientManager) {
		super(moduleName, plugin);

		ClientManager = clientManager;

		clientManager.addStoredProcedureLoginProcessor(this);
	}

	public CoreClientManager getClientManager() {
		return ClientManager;
	}

	@Override
	public abstract void processLoginResultSet(String playerName, int accountId, ResultSet resultSet)
			throws SQLException;
}
