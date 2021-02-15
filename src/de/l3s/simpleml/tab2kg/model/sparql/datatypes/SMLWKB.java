package de.l3s.simpleml.tab2kg.model.sparql.datatypes;

import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;

public class SMLWKB implements XSDataType {

	private WKBReader wkbReader = new WKBReader();

	@Override
	public boolean matches(String value) {
		try {

			// The WKBReader throws a Heap Space error in case of malformed,
			// short byte strings. Therefore, do a manual check before if they
			// exceed the minimum size (21 bytes).

			if (!value.startsWith("00") && !value.startsWith("01"))
				return false;

			byte[] bytes = WKBReader.hexToBytes(value);
			if (bytes.length < 21)
				return false;

			wkbReader.read(WKBReader.hexToBytes(value));
		} catch (ParseException | IllegalArgumentException e) {
			return false;
		}
		return true;
	}

	@Override
	public DataTypeClass getRDFClass() {
		return DataTypeClass.SML_WKB;
	}

	@Override
	public boolean isTemporal() {
		return false;
	}

}
