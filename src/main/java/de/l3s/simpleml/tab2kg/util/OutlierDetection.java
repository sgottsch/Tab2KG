package de.l3s.simpleml.tab2kg.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public class OutlierDetection {

	public static void main(String[] args) {

		List<Double> values = new ArrayList<Double>();
		values.add(3d);
		values.add(30d);
		values.add(34d);
		values.add(35d);
		values.add(41d);
		values.add(44d);
		values.add(45d);
		values.add(222d);

		System.out.println(isOutlier(values, 40d));
	}

	public static boolean isOutlier(List<Double> rawItems, double value) {
		DescriptiveStatistics descriptiveStatistics = new DescriptiveStatistics();
		double dValue = value;
		for (double rawItem : rawItems) {
			double d = rawItem;
			descriptiveStatistics.addValue(d);
		}
		double Q1 = descriptiveStatistics.getPercentile(25);
		double Q3 = descriptiveStatistics.getPercentile(75);
		double IQR = Q3 - Q1;
		double highRange = Q3 + 1.5 * IQR;
		double lowRange = Q1 - 1.5 * IQR;
		System.out.println(lowRange);
		System.out.println(highRange);
		System.out.println(Q1);
		System.out.println(Q3);

		if (dValue > highRange || dValue < lowRange) {
			return true;
		}
		return true;
	}

}
