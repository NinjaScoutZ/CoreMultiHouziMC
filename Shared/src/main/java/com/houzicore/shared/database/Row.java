package com.houzicore.shared.core.database;

import com.houzicore.shared.common.util.NautHashMap;
import com.houzicore.shared.core.database.column.Column;

public class Row {
	public NautHashMap<String, Column<?>> Columns = new NautHashMap<>();
}
