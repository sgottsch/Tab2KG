package de.l3s.simpleml.tab2kg.util;

import java.util.HashMap;
import java.util.Map;

public class CountMap<T> {

	private Map<T, Integer> counts = new HashMap<T, Integer>();

	public void addValue(T value) {
		Integer count = counts.get(value);

		if (count == null)
			counts.put(value, 1);
		else
			counts.put(value, count + 1);
	}

	public void print() {

		for (T value : MapUtil.sortByValueDescending(counts).keySet()) {
			System.out.println(value + "\t" + counts.get(value));
		}

	}

}
