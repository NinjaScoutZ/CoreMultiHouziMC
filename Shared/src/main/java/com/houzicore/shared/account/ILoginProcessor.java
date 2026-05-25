package com.houzicore.shared.account;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface ILoginProcessor {
	String getName();

	String getQuery(int accountId, String uuid, String name);

	void processLoginResultSet(String playerName, int accountId, ResultSet resultSet) throws SQLException;
}
