package de.l3s.simpleml.tab2kg.datareader;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import de.l3s.simpleml.tab2kg.catalog.model.Attribute;
import de.l3s.simpleml.tab2kg.catalog.model.dataset.DataSet;
import de.l3s.simpleml.tab2kg.catalog.model.dataset.datatable.DataTable;
import de.l3s.simpleml.tab2kg.catalog.model.dataset.datatable.Row;
import de.l3s.simpleml.tab2kg.util.Tab2KGConfiguration;

public class DataTableReader {

	public static DataTable readDataTable(String fileName, String delimiter, boolean hasHeader,
			boolean hasRowNumberColumn, String nullValue) {
		return readDataTable(fileName, delimiter, hasHeader, hasRowNumberColumn, false, false, nullValue);
	}

	public static DataTable readDataTable(String fileName, String delimiter, boolean hasHeader,
			boolean hasRowNumberColumn) {
		return readDataTable(fileName, delimiter, hasHeader, hasRowNumberColumn, false, false,
				Tab2KGConfiguration.NULL_VALUE);
	}

	public static DataTable readDataTable(String fileName, String delimiter, boolean hasHeader,
			boolean hasRowNumberColumn, boolean hasColumnTitles, String nullValue) {
		return readDataTable(fileName, delimiter, hasHeader, hasRowNumberColumn, false, hasColumnTitles, nullValue);
	}

	public static DataTable readDataTable(String fileName, String delimiter, boolean hasHeader,
			boolean hasRowNumberColumn, boolean hasColumnTitles) {
		return readDataTable(fileName, delimiter, hasHeader, hasRowNumberColumn, false, hasColumnTitles,
				Tab2KGConfiguration.NULL_VALUE);
	}

	public static DataTable readDataTable(DataTable dataTable) {

		boolean firstLine = true;
		int rowNumber = 0;
		if (dataTable.hasHeader()) {
			rowNumber = -1; // header has row number -1
		}
		Integer numberOfColumns = null;

		String fileName = dataTable.getFile().getFileLocation();

		Reader in = null;
		try {
			in = new FileReader(fileName.replace("\"", ""));

			CSVFormat format = CSVFormat.RFC4180.withIgnoreEmptyLines();
			if (dataTable.getDelimiter().equals("\t"))
				format = CSVFormat.TDF;

			Iterable<CSVRecord> records = format.parse(in);

			// for (Iterator<CSVRecord> it = records.iterator(); it.hasNext();)
			// {
			for (CSVRecord record : records) {

				// CSVRecord record = it.next();
				// System.out.println(record);

				if (firstLine) {

					// create column titles c1, c2, ..
					for (int columnNumber = 0; columnNumber < record.size(); columnNumber++) {

						if (dataTable.hasRowNumberColumn() && columnNumber == record.size() - 1)
							continue;

						Attribute column = null;
						if (!dataTable.getAllAttributes().isEmpty()
								&& columnNumber < dataTable.getAllAttributes().size())
							column = dataTable.getAllAttributes().get(columnNumber);

						if (column == null) {
							if (dataTable.hasRowNumberColumn() && dataTable.hasColumnTitles())
								column = new Attribute(columnNumber, record.get(columnNumber + 1));
							else if (!dataTable.hasRowNumberColumn() && dataTable.hasColumnTitles()) {
								column = new Attribute(columnNumber, record.get(columnNumber));
							} else
								column = new Attribute(columnNumber + 1);
							column.setDataSet(dataTable);
							column.setActive(true);
							dataTable.addAttribute(column);
						} else {
							if (dataTable.hasRowNumberColumn() && dataTable.hasColumnTitles())
								column.setIdentifier(record.get(columnNumber + 1));
							else if (!dataTable.hasRowNumberColumn() && dataTable.hasColumnTitles())
								column.setIdentifier(record.get(columnNumber));
						}
					}
					if (dataTable.hasRowNumberColumn())
						numberOfColumns = dataTable.getAllAttributes().size() + 1;
					else
						numberOfColumns = dataTable.getAllAttributes().size();
				}

				Row row = new Row(rowNumber);
				rowNumber += 1;
				int columnNumber = 0;

				boolean firstValue = true;
				// String[] parts = line.split(dataTable.getDelimiter());

				if (record.size() != numberOfColumns) {
					System.out.println("ERR: " + numberOfColumns + " in " + fileName + ", row" + rowNumber + ".");
					return null;
				}

				for (int i = 0; i < numberOfColumns; i++) {

					String value = record.get(i).trim();

					if (value.equals(dataTable.getNullValue()))
						value = null;

					if (dataTable.hasRowNumberColumn() && firstValue) {
						firstValue = false;
						continue;
					}

					row.addValue(value);

					if (!(firstLine && dataTable.hasHeader())) {
						dataTable.getAllAttributes().get(columnNumber).addValue(value);
					}

					columnNumber += 1;
				}

				if (firstLine && dataTable.hasHeader()) {
					dataTable.setHeaderRow(row);
					firstLine = false;
				} else {
					dataTable.addRow(row);
				}

			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		dataTable.setNumberOfInstances(rowNumber);

		return dataTable;

	}

	public static DataTable readDataTable(String fileName, String delimiter, boolean hasHeader,
			boolean hasRowNumberColumn, boolean columnsQuoted, boolean hasColumnTitles, String nullValue) {

		DataTable dataTable = new DataTable(fileName, delimiter);
		dataTable.setHasHeader(hasHeader);
		dataTable.setNullValue(nullValue);
		dataTable.setHasRowNumberColumn(hasRowNumberColumn);
		dataTable.setHasColumnTitles(hasColumnTitles);
		dataTable.setDelimiter(delimiter);

		return readDataTable(dataTable);
	}

	public static DataTable readDataTable(DataSet dataSet) {

		DataTable dataTable = new DataTable(dataSet);

		dataTable.setHasHeader(dataSet.getFile().hasHeader());
		dataTable.setNullValue(dataSet.getFile().getNullValue());
		dataTable.setHasRowNumberColumn(dataSet.getFile().hasRowNumberColumn());
// TODO: Why is has column titles false?
		dataTable.setHasColumnTitles(true);
		dataTable.setLatBeforeLon(dataSet.latBeforeLon());

		return readDataTable(dataTable);
	}

}
