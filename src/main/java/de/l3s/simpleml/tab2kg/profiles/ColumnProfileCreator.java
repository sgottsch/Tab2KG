package de.l3s.simpleml.tab2kg.profiles;

import java.util.List;

import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKTReader;

import de.l3s.simpleml.tab2kg.catalog.model.Attribute;
import de.l3s.simpleml.tab2kg.util.GeoUtil;

public class ColumnProfileCreator {

	private Attribute column;

	public ColumnProfileCreator(Attribute column) {
		this.column = column;
	}

	public boolean createProfile(List<Integer> numberOfQuantiles, List<Integer> numberOfIntervals) {
		boolean valid = collectValues();
		if (!valid)
			return false;
		computeStatistics(numberOfQuantiles, numberOfIntervals);
		return true;
	}

	public void computeStatistics(List<Integer> numberOfQuantiles, List<Integer> numberOfIntervals) {
		StatisticsComputer.computeStatistics(this.column, numberOfQuantiles, numberOfIntervals);
	}

	public boolean collectValues() {

		ProfileCreator.identifyDataTypeAndAddStatistics(column);

		WKTReader wktReader = new WKTReader();
		WKBReader wkbReader = new WKBReader();

		GeoUtil geoUtil = new GeoUtil();

		for (String value : column.getValues()) {
			StatisticsComputer.addToStatistics(column, value, wktReader, wkbReader, geoUtil);
		}

		if (column.getStatistics().getNumberOfNullValues() == column.getValues().size()) {
			System.out.println("Could not parse values in " + column.getIdentifier());
			return false;
		}

		return true;
	}

}
