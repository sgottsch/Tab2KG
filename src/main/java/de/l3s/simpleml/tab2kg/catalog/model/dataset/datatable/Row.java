package de.l3s.simpleml.tab2kg.catalog.model.dataset.datatable;

import java.util.ArrayList;
import java.util.List;

public class Row {

	private int rowNumber;

	private List<String> values = new ArrayList<String>();

	public Row(int rowNumber) {
		super();
		this.rowNumber = rowNumber;
	}

	public int getRowNumber() {
		return rowNumber;
	}

	public void setRowNumber(int rowNumber) {
		this.rowNumber = rowNumber;
	}

	public List<String> getValues() {
		return values;
	}

	public void addValue(String value) {
		this.values.add(value);
	}

}
