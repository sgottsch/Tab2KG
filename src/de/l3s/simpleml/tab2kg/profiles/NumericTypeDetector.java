package de.l3s.simpleml.tab2kg.profiles;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.l3s.simpleml.tab2kg.catalog.model.statistics.AttributeStatisticsTypeL3;
import de.l3s.simpleml.tab2kg.datareader.AttributeAnalyzer;

/**
 * Source: "Typology-based Semantic Labeling of Numeric Tabular Data", SWJ
 *
 */

public class NumericTypeDetector {

	private List<Long> values;

	private List<Long> sortedValues;

	private Set<Long> uniqueValues;

	private Boolean hasSameNumberOfDigits;

	private Boolean isSequential;

	public static void main(String[] args) {

		Map<List<Long>, String> examples = new LinkedHashMap<List<Long>, String>();

		examples.put(Arrays.asList(9008l, 9001l, 9005l, 9004l, 9002l, 9003l, 9006l, 9009l, 9007l), "sequential");
		examples.put(Arrays.asList(185l, 188l, 171l, 160l, 210l, 191l, 154l, 187l, 178l), "other");
		examples.put(Arrays.asList(1l, 2l, 2l, 2l, 1l, 2l, 1l, 1l, 2l), "categorical");
		examples.put(Arrays.asList(1l, 2l, 3l, 4l, 5l, 6l, 7l, 8l, 9l), "ordinal");
		examples.put(Arrays.asList(0l, 2l, 3l, 2l, 43l, 52l, 5l, 18l, 1l), "counts");
		examples.put(Arrays.asList(12034l, 34842l, 43833l, 83732l, 29243l, 30152l, 18513l, 50418l, 13312l), "random");
		examples.put(Arrays.asList(18l, 7l, 12l, 15l, 6l, 19l, 17l, 10l, 9l), "other");
		examples.put(Arrays.asList(2900201134l, 3890415293l, 2881214201l, 7841128284l, 3920820131l, 5940423221l,
				1850404118l, 4850327178l, 4911223213l), "hierarchical");

		for (List<Long> example : examples.keySet()) {
			String type = examples.get(example);

			NumericTypeDetector ntd = new NumericTypeDetector(example);
			System.out.println(type + " -> " + ntd.detectType());
			System.out.println("");
		}

	}

	public NumericTypeDetector(List<Long> values) {
		super();
		this.values = values;
	}

	AttributeStatisticsTypeL3 detectType() {

		this.sortedValues = new ArrayList<Long>();
		this.sortedValues.addAll(values);
		Collections.sort(sortedValues);

//		if (isOrdinal())
//			return AttributeStatisticsTypeL3.ORDINAL;
		if (isCategorical())
			return AttributeStatisticsTypeL3.CATEGORICAL_NUMBER;
		else if (isSequential())
			return AttributeStatisticsTypeL3.SEQUENTIAL;
//		else if (isHierarchical())
//			return AttributeStatisticsTypeL3.HIERARCHICAL;
		else
			return AttributeStatisticsTypeL3.ANY_NUMBER;

	}

	private boolean isSequential() {

		if (this.isSequential != null)
			return this.isSequential;

		if (!hasSameNumberOfDigits())
			return false;

		long min = this.sortedValues.get(0);
		long max = this.sortedValues.get(this.sortedValues.size() - 1);

		int common = uniqueValues.size();
		long total = max - min + 1;

		this.isSequential = common > 0.8 * total && this.uniqueValues.size() == values.size();

		return this.isSequential;
	}

//	private boolean isHierarchical() {
//
//		if (!hasSameNumberOfDigits())
//			return false;
//
//		// no duplicates
//		for (int i = 0; i <= this.sortedValues.size() - 2; i++) {
//			if (this.sortedValues.get(i) == this.sortedValues.get(i + 1)) {
//				return false;
//			}
//		}
//		return !isSequential;
//	}

	private boolean isCategorical() {
		this.uniqueValues = new HashSet<Long>();
		uniqueValues.addAll(this.sortedValues);

		// return Math.sqrt(this.values.size()) > this.uniqueValues.size() &&
		// this.uniqueValues.size() > 1;

//		return this.uniqueValues.size() <= AttributeAnalyzer.MAX_VALUES_IF_CATEGORICAL
//				&& Math.sqrt(this.values.size()) > this.uniqueValues.size() && this.uniqueValues.size() > 1;

		return AttributeAnalyzer.isCategorical(this.values.size(), this.uniqueValues.size());
	}

//	private boolean isOrdinal() {
//
//		long min = this.sortedValues.get(0);
//		long max = this.sortedValues.get(this.sortedValues.size() - 1);
//
//		this.uniqueValues = new HashSet<Long>();
//		uniqueValues.addAll(this.sortedValues);
//		for (long v : uniqueValues)
//			System.out.println(v);
//		long total = max - min + 1;
//
//		// X and Y are equal, if uniqueValues.size = total and values.size=total
//
//		System.out.println("U: " + this.uniqueValues.size() + ", " + values.size());
//		return this.uniqueValues.size() == total && values.size() == total;
//	}

	public boolean hasSameNumberOfDigits() {

		if (this.hasSameNumberOfDigits == null) {
			int previousNumberOfDigits = String.valueOf(sortedValues.get(0)).length();
			for (int i = 1; i <= sortedValues.size() - 2; i++) {
				int numberOfDigits = String.valueOf(sortedValues.get(0)).length();
				if (previousNumberOfDigits != numberOfDigits) {
					this.hasSameNumberOfDigits = false;
					return false;
				}
			}
			this.hasSameNumberOfDigits = true;
			return true;
		} else
			return this.hasSameNumberOfDigits;
	}

}
