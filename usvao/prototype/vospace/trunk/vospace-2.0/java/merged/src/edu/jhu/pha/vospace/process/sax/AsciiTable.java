package edu.jhu.pha.vospace.process.sax;

import java.util.ArrayList;

public class AsciiTable {
	private int tableId;
	private int columns;
	private String[] columnTypes;
	private String[] columnNames;
	private ArrayList<String[]> rows;
	
	public AsciiTable(int tableId, int columns) {
		this.tableId = tableId;
		this.columns = columns;
		
		rows = new ArrayList<String[]>();
		columnNames = new String[columns];
		columnTypes = new String[columns];
	}

	public int getTableId() {
		return tableId;
	}

	public String[] getColumnTypes() {
		return columnTypes;
	}

	public String[] getColumnNames() {
		return columnNames;
	}
	
	public ArrayList<String[]> getRows() {
		return rows;
	}	
	
	public int getColumns() {
		return columns;
	}
}
