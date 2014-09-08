package edu.jhu.pha.vospace.process.tika;

import java.util.ArrayList;
import java.util.List;

import no.geosoft.cc.util.SmartTokenizer;

public class RowAnalyser {
	private static final int MAX_HEADER_ROWS = 2;
	private char delimiter;
	
	private List<Integer> rows;

	
	public RowAnalyser(char delimiter) {
		rows = new ArrayList<Integer>();
		this.delimiter = delimiter;
	}
	
	public void addRow(String row) {
		rows.add(getRowType(row));
	}
	
	private int getRowType(String row) {
		SmartTokenizer tokenizer = new SmartTokenizer(row,String.valueOf(delimiter));
		int result = DataTypes.UNKNOWN;
		while (tokenizer.hasMoreTokens()) {
			String s = tokenizer.nextToken();
			int type = DataTypes.getDataType(s);
			
			if (result == DataTypes.UNKNOWN) { 
				result = type;
			}
			else if (result != type) {
				return DataTypes.UNKNOWN;
			}			
		}
		return result;
	}
	
	public int getNumHeaderRows() {
		int headerRows = 0;
		int currentRow = 0;
		while (currentRow < rows.size() && currentRow < MAX_HEADER_ROWS) {
			if (rows.get(currentRow) == DataTypes.STRING) {
				headerRows++;
			}
			currentRow++;
		}
		return headerRows;
	}
	
	public int getNumDataRows() {
		return rows.size() - getNumHeaderRows();
	}
	
	public int getNumRows() {
		return rows.size();
	}

	public int getNumStringRows() {
		int n = 0;
		for (int t: rows) {
			if (t == DataTypes.STRING) n++;
		}
		return n;
	}
	
}
