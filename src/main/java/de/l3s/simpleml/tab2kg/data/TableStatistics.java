package de.l3s.simpleml.tab2kg.data;

import java.util.HashMap;
import java.util.Map;

public class TableStatistics {

	private Map<TableSkipReason, Integer> skippedTables;

	public TableStatistics() {
		this.skippedTables = new HashMap<TableSkipReason, Integer>();
		for (TableSkipReason reason : TableSkipReason.values())
			skippedTables.put(reason, 0);
	}

	public void printResults() {
		for (TableSkipReason reason : skippedTables.keySet()) {
			System.out.println(reason + " " + skippedTables.get(reason));
		}
	}

	public void increaseSkipCount(TableSkipReason reason) {
		this.skippedTables.put(reason, this.skippedTables.get(reason) + 1);
	}

}
