package de.l3s.simpleml.tab2kg.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.WKBReader;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateFilter;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTWriter;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

public class GeoUtil {

	private Map<Integer, Map<Integer, MathTransform>> transformFunctions = new HashMap<Integer, Map<Integer, MathTransform>>();

	public static void main(String[] args) throws ParseException, MismatchedDimensionException,
			NoSuchAuthorityCodeException, FactoryException, TransformException {

		String value = "0102000020E61000001B0000005E32E94A5FF3214070CE88D2DE844A406ABC749318F4214030815B77F3844A40151DC9E53FF421404D327216F6844A406A87BF266BF421408CB96B09F9844A4030F0DC7BB8F42140BEF6CC9200854A401349F4328AF52140B0C91AF510854A406744696FF0F521407784D38217854A40D99942E735F621409335EA211A854A404AEF1B5F7BF62140228E75711B854A40D8F50B76C3F62140228E75711B854A4065FCFB8C0BF72140DAE1AFC91A854A402B137EA99FF72140BE30992A18854A40F2D24D6210F82140E92B483316854A409C8A54185BF821405BD3BCE314854A40B8E9CF7EA4F821403E22A64412854A40B7973446EBF82140931804560E854A400C3CF71E2EF921405BB6D61709854A40EF7211DF89F921400CC85EEFFE844A407DB3CD8DE9F92140295C8FC2F5844A4043E21E4B1FFA21401BF5108DEE844A40B554DE8E70FA2140865AD3BCE3844A40D13FC1C58AFA2140FE261422E0844A4026AAB706B6FA2140541D7233DC844A40EDF5EE8FF7FA214070B1A206D3844A400820B58993FB214069520ABABD844A40EBC5504EB4FB2140789CA223B9844A40A59828E730FC2140AA7125F1A8844A40";

		GeoUtil geoUtil = new GeoUtil();
		geoUtil.convertWKB(value, 3857);
	}

	public Geometry convertWKB(String wkb, int targetSRID) throws ParseException, NoSuchAuthorityCodeException,
			FactoryException, MismatchedDimensionException, TransformException {

		WKBReader wkbReader = new WKBReader();
		WKTWriter wktWriter = new WKTWriter();

		Geometry geometry = wkbReader.read(WKBReader.hexToBytes(wkb));
		System.out.println("Length: " + geometry.getLength());
		System.out.println("SRID: " + geometry.getSRID());
		System.out.println("WKT: " + wktWriter.writeFormatted(geometry));

		System.out.println("------");

		// String query = "SELECT ST_Transform(" + wktWriter.writeFormatted(geometry) +
		// ",3857)";

		MathTransform transform = getOrCreateTransformFunction(geometry.getSRID(), targetSRID);

		System.out.println("transform");
		geometry.apply(new InvertCoordinateFilter());
		geometry = JTS.transform(geometry, transform);

		System.out.println("Length: " + geometry.getLength());
		System.out.println("SRID: " + geometry.getSRID());
		System.out.println("WKT: " + wktWriter.writeFormatted(geometry));

		return geometry;
	}

	public Geometry convertGeometry(Geometry geometry, int targetSRID) throws ParseException,
			NoSuchAuthorityCodeException, FactoryException, MismatchedDimensionException, TransformException {
		MathTransform transform = getOrCreateTransformFunction(geometry.getSRID(), targetSRID);
		return JTS.transform(geometry, transform);
	}

	private MathTransform getOrCreateTransformFunction(int sourceSRID, int targetSRID) throws FactoryException {
		if (transformFunctions.containsKey(sourceSRID) && transformFunctions.get(sourceSRID).containsKey(targetSRID))
			return transformFunctions.get(sourceSRID).get(targetSRID);

		CoordinateReferenceSystem sourceCRS = CRS.decode("EPSG:" + sourceSRID);
		CoordinateReferenceSystem targetCRS = CRS.decode("EPSG:" + targetSRID);
		MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS, false);

		transformFunctions.put(sourceSRID, new HashMap<Integer, MathTransform>());
		transformFunctions.get(sourceSRID).put(targetSRID, transform);
		return transform;
	}

	public static void invertCoordinates(Geometry geometry) {
		geometry.apply(new InvertCoordinateFilter());
	}

	private static class InvertCoordinateFilter implements CoordinateFilter {
		@Override
		public void filter(Coordinate coord) {
			double oldX = coord.x;
			coord.x = coord.y;
			coord.y = oldX;
		}
	}

	public static String createCoordinateArrayString(Geometry geometry) {

		List<String> subSequences = new ArrayList<String>();
		for (int i = 0; i < geometry.getNumGeometries(); i++) {
			List<String> coordinates = new ArrayList<String>();
			for (Coordinate coordinate : geometry.getGeometryN(i).getCoordinates()) {
				coordinates.add("[" + coordinate.getY() + "," + coordinate.getX() + "]");
			}
			subSequences.add("[" + StringUtils.join(coordinates, ",") + "]");
		}

		if (geometry.getNumGeometries() == 1)
			return subSequences.get(0);
		else
			return "[" + StringUtils.join(subSequences, ",") + "]";
	}

}
