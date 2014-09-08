package edu.jhu.pha.vospace.process.database;

public interface DatabaseFormat {
	
	public String getSingleType();
	public String getDoubleType();
	public String getUInt8Type();
	public String getInt16Type();
	public String getInt32Type();
	public String getInt64Type();
	public String getCharFixedType(int n);
	public String getCharVariableType();
	public String escapeChars(String s);
	public String formatObjectName(String s);
	public String formatCharString(String s);
	public String formatDateTime(String s);
	public String getDatabaseType(String type);	

}
