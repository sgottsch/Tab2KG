package de.l3s.simpleml.tab2kg.model.sparql.datatypes;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

public class SMLWKT implements XSDataType {

	private WKTReader wktReader = new WKTReader();

	@Override
	public boolean matches(String value) {

		try {
			Geometry geometry = wktReader.read(value);
			return geometry != null;
		} catch (ParseException e) {
			return false;
		}

	}

	@Override
	public DataTypeClass getRDFClass() {
		return DataTypeClass.SML_WKT;
	}
	@Override
	public boolean isTemporal() {
		return false;
	}
}
