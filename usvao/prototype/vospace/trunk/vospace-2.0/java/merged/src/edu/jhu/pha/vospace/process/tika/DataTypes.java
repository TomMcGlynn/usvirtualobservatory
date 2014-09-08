package edu.jhu.pha.vospace.process.tika;

import org.apache.tika.exception.TikaException;

public class DataTypes {
	public static final int STRING = 0;
	public static final int DOUBLE = 1;
	public static final int SINGLE = 2;
	public static final int INT64 = 3;
	public static final int INT32 = 4;
	public static final int INT16 = 5;
	public static final int UINT8 = 6;
	public static final int UNKNOWN = 7;
	
	public static String getCharCode(int type) throws TikaException {
		switch (type) {
			case STRING:
				return "A";
			case DOUBLE:
				return "D";
			case SINGLE:
				return "E";
			case INT64:
				return "K";
			case INT32:
				return "J";
			case INT16:
				return "I";
			case UINT8:
				return "B";
			default:
				return "U";
		}
	}
	
	public static int getDataType(String value) {
		if (value.trim().isEmpty()) return (DataTypes.UNKNOWN);
		try {
			byte v = Byte.parseByte(value);
			if (v >= 0) return DataTypes.UINT8;
			else return DataTypes.INT16;
		}
		catch (Exception e) {}
		
		try {
			short v = Short.parseShort(value);
			return DataTypes.INT16;
		}
		catch (Exception e) {}
		
		try {
			int v = Integer.parseInt(value);
			return DataTypes.INT32;
		}
		catch (Exception e) {}
		
		try {
			long v = Long.parseLong(value);
			return DataTypes.INT64;
		}
		catch (Exception e) {}
		
		try {
			float v = Float.parseFloat(value);
			return DataTypes.SINGLE;
		}
		catch (Exception e) {}
		
		try {
			double v = Double.parseDouble(value);
			return DataTypes.DOUBLE;
		}
		catch (Exception e) {}
		
		return DataTypes.STRING;
	}
}
