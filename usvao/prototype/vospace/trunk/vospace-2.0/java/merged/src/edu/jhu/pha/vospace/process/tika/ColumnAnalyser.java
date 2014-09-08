package edu.jhu.pha.vospace.process.tika;

import java.util.ArrayList;
import java.util.List;

import no.geosoft.cc.util.SmartTokenizer;

public class ColumnAnalyser {
	private char delimiter;
	private List<Integer> columns;
	private List<String> names;
	
	public ColumnAnalyser(char delimiter, int n) {
		this.columns = new ArrayList<Integer>();
		this.names = new ArrayList<String>();
		this.delimiter = delimiter;
		for (int i=0; i<n; i++) {
			columns.add(new Integer(DataTypes.UNKNOWN));
			names.add("column"+i);
		}
	}
	
	public void addRow(String row) {
		SmartTokenizer tokenizer = new SmartTokenizer(row,String.valueOf(delimiter));
		int i = 0;
		while (tokenizer.hasMoreTokens()) {
			String s = tokenizer.nextToken();
			int type = DataTypes.getDataType(s);
			columns.set(i, Math.min(type, columns.get(i)));
			i++;
		}
	}
	
	public void addHeader(String header) {
		SmartTokenizer tokenizer = new SmartTokenizer(header,String.valueOf(delimiter));
		int i = 0;
		while (tokenizer.hasMoreTokens()) {
			String s = tokenizer.nextToken();
			names.set(i, s);
			i++;
		} 
	}
	
	public int getColumnType(int index) {
		return columns.get(index);
	}
	
	public List<String> getColumnNames() {
		return names;
	}
	
	public List<Integer> getColumnTypes() {
		return columns;
	}

}
