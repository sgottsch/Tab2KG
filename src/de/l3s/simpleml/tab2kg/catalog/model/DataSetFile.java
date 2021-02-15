package de.l3s.simpleml.tab2kg.catalog.model;

/**
 * Created by iosifidis on 29.03.19.
 */
public class DataSetFile {

	private long byteSize;
	private String fileType;
	private String fileLocation;

	private String nullValue;
	private String format;
	private boolean hasHeader;

	private String delimiter;
	private boolean hasRowNumberColumn;
	private boolean hasColumnTitles;

	public DataSetFile() {
	}

	public DataSetFile(String fileLocation, String fileType, String delimiter, String nullValue, String format,
			boolean hasHeader) {
		this.fileLocation = fileLocation;
		this.fileType = fileType;
		this.delimiter = delimiter;
		this.format = format;
		this.hasHeader = hasHeader;
	}

	public DataSetFile(long byteSize, String fileLocation, String fileType, String delimiter, String nullValue,
			String format, boolean hasHeader) {
		this.byteSize = byteSize;
		this.fileLocation = fileLocation;
		this.fileType = fileType;
		this.delimiter = delimiter;
		this.nullValue = nullValue;
		this.format = format;
		this.hasHeader = hasHeader;
	}

	public long getByteSize() {
		return byteSize;
	}

	public void setByteSize(Long byteSize) {
		this.byteSize = byteSize;
	}

	public String getFileLocation() {
		return fileLocation;
	}

	public void setFileLocation(String fileLocation) {
		this.fileLocation = fileLocation;
	}

	public String getSeparator() {
		return delimiter;
	}

	public void setSeparator(String delimiter) {
		this.delimiter = delimiter;
	}

	public String getNullValue() {
		return nullValue;
	}

	public void setNullValue(String nullValue) {
		this.nullValue = nullValue;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public boolean hasHeader() {
		return hasHeader;
	}

	public void setHasHeader(boolean hasHeader) {
		this.hasHeader = hasHeader;
	}

	public String getFileType() {
		return fileType;
	}

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

	public DataSetFile copy() {
		DataSetFile fileCopy = new DataSetFile(byteSize, fileLocation, fileType, delimiter, nullValue, format,
				hasHeader);
		return fileCopy;
	}

	public String getDelimiter() {
		return delimiter;
	}

	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}

	public boolean hasRowNumberColumn() {
		return hasRowNumberColumn;
	}

	public void setHasRowNumberColumn(boolean hasRowNumberColumn) {
		this.hasRowNumberColumn = hasRowNumberColumn;
	}

	public boolean hasColumnTitles() {
		return hasColumnTitles;
	}

	public void setHasColumnTitles(boolean hasColumnTitles) {
		this.hasColumnTitles = hasColumnTitles;
	}

}
