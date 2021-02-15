package de.l3s.simpleml.tab2kg.catalog.model;

public class AttributeValue<T> {

	private T value;
	private String stringValue;

	private boolean isNull = false;
	private boolean isInvalid = false;

	public AttributeValue(boolean isNull, boolean isInvalid) {
		super();
		this.isNull = isNull;
		this.isInvalid = isInvalid;
	}

	public AttributeValue(T value) {
		super();
		this.value = value;
	}

	public String getStringValue() {
		if (isNull)
			return "NULL";
		if (isInvalid)
			return "INVALID";
		return stringValue;
	}

	public void setStringValue(String stringValue) {
		this.stringValue = stringValue;
	}

	public T getValue() {
		return value;
	}

	public void setValue(T value) {
		this.value = value;
	}

	public boolean isNull() {
		return isNull;
	}

	public boolean isNullOrInvalid() {
		return isNull || isInvalid;
	}

	public void setNull(boolean isNull) {
		this.isNull = isNull;
	}

	public boolean isInvalid() {
		return isInvalid;
	}

	public void setInvalid(boolean isInvalid) {
		this.isInvalid = isInvalid;
	}

	public boolean isValidNonNull() {
		return !isNull && !isInvalid;
	}

}
