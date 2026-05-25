package com.houzicore.shared.core.database.column;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class Column<Type> {
	public String Name;
	public Type Value;

	public Column(String name) {
		Name = name;
	}

	public Column(String name, Type value) {
		Name = name;
		Value = value;
	}

	@Override
	public abstract Column<Type> clone();

	public abstract String getCreateString();

	public abstract Type getValue(ResultSet resultSet) throws SQLException;

	public abstract void setValue(PreparedStatement preparedStatement, int columnNumber) throws SQLException;
}
