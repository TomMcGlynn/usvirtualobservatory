package net.ivoa.datascope;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.mortbay.log.LogStream.STDOUT;

import uk.ac.starlink.table.ColumnInfo;
import uk.ac.starlink.table.RowListStarTable;
import uk.ac.starlink.table.RowSequence;
import uk.ac.starlink.table.StarTable;
import uk.ac.starlink.table.StarTableFactory;
import uk.ac.starlink.table.StarTableOutput;

import net.ivoa.registry.RegistryQuery;

public class MetaFile {

	private RowListStarTable metaTable;
	private Map<String, Integer> columnKeys = new HashMap<String, Integer>();
	private Map<String, Integer> rowIDs = new HashMap<String, Integer>();
	private int rowCount;
	private String[] identifier;

	public static void main(String[] args) throws Exception {
		make();
		System.out.println("<html><head><title>Meta File Update Successful</title></head><body>Meta File Update Successful</body></html>\n");
	}
	
	public static void make() throws Exception {
		MetaFile makeMeta = new MetaFile();
		makeMeta.makeTable();
		makeMeta.writeTable();
	}

	public void makeTable() throws Exception {
		String registry = DS.getRegistryURL();
		ArrayList<String> urls = new ArrayList<String>();
		int i = 0;
		for(String q : DS.getRegistryQuery()){
			urls.add(i, registry + "?" + q);
			i++;
		}
		
		metaTable = RegistryQuery.queryTable(urls);
		buildIndices();
		
		return;
	}
	
	public void writeTable() throws Exception {
		String metaFile = DS.getDataHome() + DS.getMetadataFile();
		File f = new File(metaFile + ".tmp");
		new StarTableOutput().writeStarTable(metaTable, f.toString(), "votable");

		File fn = new File(metaFile);
		f.renameTo(fn);		
	}
	
	public void readTable() throws Exception {
		String metaFile = DS.getDataHome() + DS.getMetadataFile();
		// System.err.println("Datascope: Reading metatable " + metaFile);
		StarTable voTable = new StarTableFactory().makeStarTable(metaFile);

		metaTable = new RowListStarTable(voTable);
		RowSequence combinedSeq = voTable.getRowSequence();
		while (combinedSeq.next()) {
			metaTable.addRow( combinedSeq.getRow() );
		}
		buildIndices();
		
        return;
	}
	
	private void buildIndices(){
		// Populate column and ID keys
		for (int iColumn = 0; iColumn < metaTable.getColumnCount(); iColumn++) {
			ColumnInfo colInfo = metaTable.getColumnInfo(iColumn);
			columnKeys.put(colInfo.getName(), iColumn);
		}

		rowCount = (int) metaTable.getRowCount();

		identifier = new String[rowCount];
		int IDColumn = columnKeys.get("identifier");
		for (int iRow = 0; iRow < rowCount; iRow++) {
			identifier[iRow] = metaTable.getCell(iRow, IDColumn).toString();
			String idValue = metaTable.getCell(iRow, IDColumn).toString();
			// idValue = idValue.replace("#", "_");
			
			// metaTable.setCell(iRow, IDColumn, idValue);
			rowIDs.put(idValue, iRow);
		}
	}
	
	public int getColumnIndex(String columnName){
		if(columnKeys.containsKey(columnName)){
			return columnKeys.get(columnName);
		}else{
			return -1;
		}
	}
	
	public int getRowIndex(String ivoaID){
		if(rowIDs.containsKey(ivoaID)){
			return rowIDs.get(ivoaID);
		}else{
			return -1;
		}
	}
	
	public int getRowCount(){
		return rowCount;
	}

	public RowListStarTable getMeta() throws Exception {
		try {
			readTable();
		} catch (Exception e) {
			DS.log("Exception reading metadata file:" + e);

			try {
				makeTable();
			} catch (Exception e2) {
				throw new Exception("Error reading metadata from registry.", e2);
			}
		}

		return metaTable;
	}

	public String getScalar(String key, int row) {
		Object[] result = metaTable.getRow(row);
		int colIndex = getColumnIndex(key);
		if ( colIndex >= 0) {
			ColumnInfo colInfo = metaTable.getColumnInfo(colIndex);
			if (colInfo.isArray()) {
				if (colInfo.getShape()[0] > 0) {
					Object[] cellArray = (Object[]) result[colIndex];
					return cellArray.toString();
				}
			} else {
				if (result[colIndex] != null) {
					return result[colIndex].toString();
				}
			}
		}
		return null;
	}
	
	public String getArr(String key, int row) {
		Object[] result = metaTable.getRow(row);
		int colIndex = getColumnIndex(key);
		if (colIndex>=0) {
			ColumnInfo colInfo = metaTable.getColumnInfo(colIndex);

			if (colInfo.isArray()) {
				if (colInfo.getShape()[0] > 0) {
					Object[] cellArray = (Object[]) result[colIndex];
					String arrayString = "";
					String sep = "";
					for (Object z : cellArray) {
						arrayString += sep + z.toString();
						sep = ",";
					}
					return arrayString;
				}
			} else {
				if (result[colIndex] != null) {
					String cellValue = result[colIndex].toString();
					if (cellValue.contains("#")) {
						String arrayString = "";
						String sep = "";
						for (String split_val : cellValue.split("#")) {
							if (split_val.length() > 0) {
								arrayString += sep + split_val;
								sep = ",";
							}
						}
						return arrayString;
					}
					return cellValue;
				}
			}
		}
		return "";
	}
	
	public String[] getColumnNames(){
		return columnKeys.keySet().toArray(new String[0]);
	}
}
