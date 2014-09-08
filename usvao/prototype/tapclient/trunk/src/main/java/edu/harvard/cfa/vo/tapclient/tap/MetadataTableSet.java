package edu.harvard.cfa.vo.tapclient.tap;

import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.harvard.cfa.vo.tapclient.vosi.Column;
import edu.harvard.cfa.vo.tapclient.vosi.ForeignKey;
import edu.harvard.cfa.vo.tapclient.vosi.Schema;
import edu.harvard.cfa.vo.tapclient.vosi.Table;
import edu.harvard.cfa.vo.tapclient.vosi.TableSet;

import edu.harvard.cfa.vo.tapclient.util.HttpException;
import edu.harvard.cfa.vo.tapclient.util.ResponseFormatException;

import uk.ac.starlink.table.ColumnInfo;
import uk.ac.starlink.table.StarTable;
import uk.ac.starlink.table.TableFormatException;
import uk.ac.starlink.table.TableSink;
import uk.ac.starlink.votable.VOTableBuilder;

/**
 * A TableSet implementation that updates using TAP metadata queries against the TAP_SCHEMA tables.
 */
class MetadataTableSet extends TableSet {
    private static final Logger logger = Logger.getLogger("edu.harvard.cfa.vo.tapclient.MetadataTableSet");
    
    class MetadataKeyColumnsQuery implements TableSink {
	private Map<String,Map<String,String>> keyColumns;
	
	private TapService tapService;
	
	private String keyIdColumnName;
	private String fromColumnColumnName;
	private String targetColumnColumnName;

	private int keyIdIndex;
	private int fromColumnIndex;
	private int targetColumnIndex;

	private long rowCount;

	public MetadataKeyColumnsQuery(TapService tapService) {
	    this(tapService, "key_id", "from_column", "target_column");
	}

	public MetadataKeyColumnsQuery(TapService tapService, String keyIdColumnName, String fromColumnColumnName, String targetColumnColumnName) {
	    this.keyColumns = new HashMap<String,Map<String,String>>();

	    this.tapService = tapService;

	    this.keyIdColumnName = keyIdColumnName;
	    this.fromColumnColumnName = fromColumnColumnName;
	    this.targetColumnColumnName = targetColumnColumnName;
	    
	    this.keyIdIndex = -1;
	    this.fromColumnIndex = -1;
	    this.targetColumnIndex = -1;
	}

	public void acceptMetadata(StarTable meta) throws TableFormatException {
	    rowCount = 0;
	    keyColumns.clear();

	    keyIdIndex = -1;
	    fromColumnIndex = -1;
	    targetColumnIndex = -1;

	    for (int index = 0; index < meta.getColumnCount(); index++) {
		ColumnInfo columnInfo = meta.getColumnInfo(index);
		String name = columnInfo.getName();

		if (keyIdColumnName.equals(name)) 
		    keyIdIndex = index;
		else if (fromColumnColumnName.equals(name)) 
		    fromColumnIndex = index;
		else if (targetColumnColumnName.equals(name)) 
		    targetColumnIndex = index;
	    }

	    if (keyIdIndex < 0 || fromColumnIndex < 0 || targetColumnIndex < 0) {
		StringBuilder sb = new StringBuilder("missing required column: ");
		String separator = " ";
		if (keyIdIndex < 0) {
		    sb.append(separator);
		    sb.append(keyIdColumnName);
		    separator = ",";
		}
		if (fromColumnIndex < 0) {
		    sb.append(separator);
		    sb.append(fromColumnColumnName);
		    separator = ",";
		}
		if (targetColumnIndex < 0) {
		    sb.append(separator);
		    sb.append(targetColumnColumnName);
		    separator = ",";
		}
		
		if (logger.isLoggable(Level.WARNING)) 
		    logger.log(Level.WARNING, sb.toString());
		
		//		throw new TableFormatException(sb.toString());
	    }
	}

	public void acceptRow(Object[] row) throws IOException {
	    rowCount++;
	    String keyId = (keyIdIndex >= 0 && row[keyIdIndex] != null) ? row[keyIdIndex].toString() : null;
	    String fromColumn = (fromColumnIndex >= 0 && row[fromColumnIndex] != null) ? row[fromColumnIndex].toString() : null;
	    String targetColumn = (targetColumnIndex >= 0 && row[targetColumnIndex] != null) ? row[targetColumnIndex].toString() : null;
	    
	    Map<String,String> value = keyColumns.get(keyId);
	    if (value == null) {
		value = new HashMap<String,String>();
		if (logger.isLoggable(Level.INFO))
		    logger.log(Level.INFO, keyId);
		keyColumns.put(keyId,value);
	    }

	    if (logger.isLoggable(Level.INFO))
		logger.log(Level.INFO, fromColumn+" = "+targetColumn);

	    value.put(fromColumn, targetColumn);
	}

	public void endRows() throws IOException {
	    if (logger.isLoggable(Level.INFO)) 
		logger.log(Level.INFO, "Rows "+rowCount);
	}

	public Map<String,Map<String,String>> getKeyColumns() {
	    return keyColumns;
	}

	public void update() throws HttpException, ResponseFormatException, IOException {
	    SyncJob syncJob = new SyncJob(tapService);
	    syncJob.setParameter("QUERY", "SELECT * FROM TAP_SCHEMA.key_columns");
	    syncJob.setParameter("LANG", "ADQL");
	    syncJob.setParameter("FORMAT", "votable");
	    
	    InputStream inputStream = syncJob.run();
	    try { 
		new VOTableBuilder().streamStarTable(inputStream, this, "0");
	    } finally {
		try { 
		    inputStream.close();
		} catch (Exception ex) {
		    logger.log(Level.SEVERE, "error closing TAP_SCHEMA.key_columns results", ex);
		}
	    }
	}
    }

    class MetadataKeysQuery implements TableSink {
	private Map<String,MetadataForeignKey> keys;
	
	private TapService tapService;
	
	private String keyIdColumnName;
	private String fromTableColumnName;
	private String targetTableColumnName;
	private String descriptionColumnName;
	private String utypeColumnName;

	private int keyIdIndex;
	private int fromTableIndex;
	private int targetTableIndex;
	private int descriptionIndex;
	private int utypeIndex;

	public MetadataKeysQuery(TapService tapService) {
	    this(tapService, "key_id", "from_table", "target_table", "description", "utype");
	}

	public MetadataKeysQuery(TapService tapService, String keyIdColumnName, String fromTableColumnName, String targetTableColumnName, String descriptionColumnName, String utypeColumnName) {
	    this.keys = new HashMap<String,MetadataForeignKey>();

	    this.tapService = tapService;

	    this.keyIdColumnName = keyIdColumnName;
	    this.fromTableColumnName = fromTableColumnName;
	    this.targetTableColumnName = targetTableColumnName;
	    this.descriptionColumnName = descriptionColumnName;
	    this.utypeColumnName = utypeColumnName;
	    
	    this.keyIdIndex = -1;
	    this.fromTableIndex = -1;
	    this.targetTableIndex = -1;
	    this.descriptionIndex = -1;
	    this.utypeIndex = -1;
	}

	public void acceptMetadata(StarTable meta) throws TableFormatException {
	    keys.clear();

	    keyIdIndex = -1;
	    fromTableIndex = -1;
	    targetTableIndex = -1;
	    descriptionIndex = -1;
	    utypeIndex = -1;

	    for (int index = 0; index < meta.getColumnCount(); index++) {
		ColumnInfo columnInfo = meta.getColumnInfo(index);
		String name = columnInfo.getName();

		if (keyIdColumnName.equals(name)) 
		    keyIdIndex = index;
		else if (fromTableColumnName.equals(name)) 
		    fromTableIndex = index;
		else if (targetTableColumnName.equals(name)) 
		    targetTableIndex = index;
		else if (descriptionColumnName.equals(name)) 
		    descriptionIndex = index;
		else if (utypeColumnName.equals(name)) 
		    utypeIndex = index;
	    }

	    if (keyIdIndex < 0 || fromTableIndex < 0 || targetTableIndex < 0 || descriptionIndex < 0 || utypeIndex < 0) {
		StringBuilder sb = new StringBuilder("missing required column: ");
		String separator = " ";
		if (keyIdIndex < 0) {
		    sb.append(separator);
		    sb.append(keyIdColumnName);
		    separator = ",";
		}
		if (fromTableIndex < 0) {
		    sb.append(separator);
		    sb.append(fromTableColumnName);
		    separator = ",";
		}
		if (descriptionIndex < 0) {
		    sb.append(separator);
		    sb.append(descriptionColumnName);
		    separator = ",";
		}
		if (utypeIndex < 0) {
		    sb.append(separator);
		    sb.append(utypeColumnName);
		    separator = ",";
		}
		
		if (logger.isLoggable(Level.WARNING)) 
		    logger.log(Level.WARNING, sb.toString());
		
		//		throw new TableFormatException(sb.toString());
	    }
	}

	public void acceptRow(Object[] row) throws IOException {
	    String keyId = (keyIdIndex >= 0 && row[keyIdIndex] != null) ? row[keyIdIndex].toString() : null;
	    String fromTable = (fromTableIndex >= 0 && row[fromTableIndex] != null) ? row[fromTableIndex].toString() : null;
	    String targetTable = (targetTableIndex >= 0 && row[targetTableIndex] != null) ? row[targetTableIndex].toString() : null;
	    String description = (descriptionIndex >= 0 && row[descriptionIndex] != null) ? row[descriptionIndex].toString() : null;
	    String utype = (utypeIndex >= 0 && row[utypeIndex] != null) ? row[utypeIndex].toString() : null;
	    
	    MetadataForeignKey key = new MetadataForeignKey(keyId, fromTable, targetTable, description, utype);
	    keys.put(keyId, key);
	}

	public void endRows() throws IOException {
	}

	public Map<String,MetadataForeignKey> getKeys() {
	    return keys;
	}

	public void update() throws HttpException, ResponseFormatException, IOException {
	    SyncJob syncJob = new SyncJob(tapService);
	    syncJob.setParameter("QUERY", "SELECT * FROM TAP_SCHEMA.keys");
	    syncJob.setParameter("LANG", "ADQL");
	    syncJob.setParameter("FORMAT", "votable");
	    
	    InputStream inputStream = syncJob.run();
	    try { 
		new VOTableBuilder().streamStarTable(inputStream, this, "0");
	    } finally {
		try { 
		    inputStream.close();
		} catch (Exception ex) {
		    logger.log(Level.SEVERE, "error closing TAP_SCHEMA.keys results", ex);
		}
	    }
	}
    }

    class MetadataColumnsQuery implements TableSink {
	private List<MetadataColumn> columns;
	
	private TapService tapService;
	
	private String tableNameColumnName;
	private String columnNameColumnName;
	private String descriptionColumnName;
	private String unitColumnName;
	private String ucdColumnName;
	private String utypeColumnName;
	private String datatypeColumnName;
	private String sizeColumnName;
	private String principalColumnName;
	private String indexedColumnName;
	private String stdColumnName;

	private int tableNameIndex;
	private int columnNameIndex;
	private int descriptionIndex;
	private int unitIndex;
	private int ucdIndex;
	private int utypeIndex;
	private int datatypeIndex;
	private int sizeIndex;
	private int principalIndex;
	private int indexedIndex;
	private int stdIndex;

	public MetadataColumnsQuery(TapService tapService) {
	    this(tapService, "table_name", "column_name", "description", "unit", "ucd", "utype", "datatype", "size", "principal", "indexed", "std");
	}

	public MetadataColumnsQuery(TapService tapService, String tableNameColumnName, String columnNameColumnName, String descriptionColumnName, String unitColumnName, String ucdColumnName, String utypeColumnName, String datatypeColumnName, String sizeColumnName, String principalColumnName, String indexedColumnName, String stdColumnName) {
	    this.columns = new ArrayList<MetadataColumn>();

	    this.tapService = tapService;

	    this.tableNameColumnName = tableNameColumnName;
	    this.columnNameColumnName = columnNameColumnName;
	    this.descriptionColumnName = descriptionColumnName;
	    this.unitColumnName = unitColumnName;
	    this.ucdColumnName = ucdColumnName;
	    this.utypeColumnName = utypeColumnName;
	    this.datatypeColumnName = datatypeColumnName;
	    this.sizeColumnName = sizeColumnName;
	    this.principalColumnName = principalColumnName;
	    this.indexedColumnName = indexedColumnName;
	    this.stdColumnName = stdColumnName;
	    
	    this.tableNameIndex = -1;
	    this.columnNameIndex = -1;
	    this.descriptionIndex = -1;
	    this.unitIndex = -1;
	    this.ucdIndex = -1;
	    this.utypeIndex = -1;
	    this.datatypeIndex = -1;
	    this.sizeIndex = -1;
	    this.principalIndex = -1;
	    this.indexedIndex = -1;
	    this.stdIndex = -1;
	}

	public void acceptMetadata(StarTable meta) throws TableFormatException {
	    columns.clear();

	    tableNameIndex = -1;
	    columnNameIndex = -1;
	    descriptionIndex = -1;
	    unitIndex = -1;
	    ucdIndex = -1;
	    utypeIndex = -1;
	    datatypeIndex = -1;
	    sizeIndex = -1;
	    principalIndex = -1;
	    indexedIndex = -1;
	    stdIndex = -1;

	    for (int index = 0; index < meta.getColumnCount(); index++) {
		ColumnInfo columnInfo = meta.getColumnInfo(index);
		String name = columnInfo.getName();

		if (tableNameColumnName.equals(name)) 
		    tableNameIndex = index;
		else if (columnNameColumnName.equals(name)) 
		    columnNameIndex = index;
		else if (descriptionColumnName.equals(name)) 
		    descriptionIndex = index;
		else if (unitColumnName.equals(name)) 
		    unitIndex = index;
		else if (ucdColumnName.equals(name)) 
		    ucdIndex = index;
		else if (utypeColumnName.equals(name)) 
		    utypeIndex = index;
		else if (datatypeColumnName.equals(name)) 
		    datatypeIndex = index;
		else if (sizeColumnName.equals(name)) 
		    sizeIndex = index;
		else if (principalColumnName.equals(name)) 
		    principalIndex = index;
		else if (indexedColumnName.equals(name)) 
		    indexedIndex = index;
		else if (stdColumnName.equals(name)) 
		    stdIndex = index;
	    }

	    if (tableNameIndex < 0 || columnNameIndex < 0 || descriptionIndex < 0 || unitIndex < 0 || ucdIndex < 0 || utypeIndex < 0 || datatypeIndex < 0 || sizeIndex < 0 || principalIndex < 0 || indexedIndex < 0 || stdIndex < 0) {
		StringBuilder sb = new StringBuilder("missing required column: ");
		String separator = " ";
		if (tableNameIndex < 0) {
		    sb.append(separator);
		    sb.append(tableNameColumnName);
		    separator = ",";
		}
		if (columnNameIndex < 0) {
		    sb.append(separator);
		    sb.append(columnNameColumnName);
		    separator = ",";
		}
		if (descriptionIndex < 0) {
		    sb.append(separator);
		    sb.append(descriptionColumnName);
		    separator = ",";
		}
		if (unitIndex < 0) {
		    sb.append(separator);
		    sb.append(unitColumnName);
		    separator = ",";
		}
		if (ucdIndex < 0) {
		    sb.append(separator);
		    sb.append(ucdColumnName);
		    separator = ",";
		}
		if (utypeIndex < 0) {
		    sb.append(separator);
		    sb.append(utypeColumnName);
		    separator = ",";
		}
		if (datatypeIndex < 0) {
		    sb.append(separator);
		    sb.append(datatypeColumnName);
		    separator = ",";
		}
		if (sizeIndex < 0) {
		    sb.append(separator);
		    sb.append(sizeColumnName);
		    separator = ",";
		}
		if (principalIndex < 0) {
		    sb.append(separator);
		    sb.append(principalColumnName);
		    separator = ",";
		}
		if (indexedIndex < 0) {
		    sb.append(separator);
		    sb.append(indexedColumnName);
		    separator = ",";
		}
		if (stdIndex < 0) {
		    sb.append(separator);
		    sb.append(stdColumnName);
		    separator = ",";
		}
		
		if (logger.isLoggable(Level.WARNING)) 
		    logger.log(Level.WARNING, sb.toString());

		//		throw new TableFormatException(sb.toString());
	    }
	}

	public void acceptRow(Object[] row) throws IOException {
	    String tableName = (tableNameIndex >= 0 && row[tableNameIndex] != null) ? row[tableNameIndex].toString() : null;
	    String columnName = (columnNameIndex >= 0 && row[columnNameIndex] != null) ? row[columnNameIndex].toString() : null;
	    String description = (descriptionIndex >= 0 && row[descriptionIndex] != null) ? row[descriptionIndex].toString() : null;
	    String unit = (unitIndex >= 0 && row[unitIndex] != null) ? row[unitIndex].toString() : null;
	    String ucd = (ucdIndex >= 0 && row[ucdIndex] != null) ? row[ucdIndex].toString() : null;
	    String utype = (utypeIndex >= 0 && row[utypeIndex] != null) ? row[utypeIndex].toString() : null;
	    String datatype = (datatypeIndex >= 0 && row[datatypeIndex] != null) ? row[datatypeIndex].toString() : null;
	    String size = (sizeIndex >= 0 && row[sizeIndex] != null) ? row[sizeIndex].toString() : null;
	    String principal = (principalIndex >= 0 && row[principalIndex] != null) ? row[principalIndex].toString() : null;
	    String indexed = (indexedIndex >= 0 && row[indexedIndex] != null) ? row[indexedIndex].toString() : null;
	    String std = (stdIndex >= 0 && row[stdIndex] != null) ? row[stdIndex].toString() : null;

	    String arraySize = null;
	    if ("CHAR".equals(datatype) || "BINARY".equals(datatype))  {
		arraySize = size != null ? size : "*";
	    } else if ("VARCHAR".equals(datatype) || "VARBINARY".equals(datatype)) {
		arraySize = size != null ? size+"*" : "*";
	    }
	    List<String> flags = new ArrayList<String>();
//	    if ("1".equals(principal)) 
//		flags.add("principal");
	    if ("1".equals(indexed)) 
		flags.add("indexed");
	    
	    MetadataColumn column = new MetadataColumn(tableName, columnName, description, unit, ucd, utype, datatype, arraySize, flags, "1".equals(std));
	    columns.add(column);
	}

	public void endRows() throws IOException {
	}

	public List<MetadataColumn> getColumns() {
	    return columns;
	}

	public void update() throws HttpException, ResponseFormatException, IOException {
	    SyncJob syncJob = new SyncJob(tapService);
	    syncJob.setParameter("QUERY", "SELECT * FROM TAP_SCHEMA.columns");
	    syncJob.setParameter("LANG", "ADQL");
	    syncJob.setParameter("FORMAT", "votable");
	    
	    InputStream inputStream = syncJob.run();
	    try { 
		new VOTableBuilder().streamStarTable(inputStream, this, "0");
	    } finally {
		try { 
		    inputStream.close();
		} catch (Exception ex) {
		    logger.log(Level.SEVERE, "error closing TAP_SCHEMA.columns results", ex);
		}
	    }
	}
    }

    class MetadataTablesQuery implements TableSink {
	private Map<String,MetadataTable> tables;

	private TapService tapService;

	private String schemaNameColumnName;
	private String tableNameColumnName;
	private String tableTypeColumnName;
	private String descriptionColumnName;
	private String utypeColumnName;

	private int schemaNameIndex;
	private int tableNameIndex;
	private int tableTypeIndex;
	private int descriptionIndex;
	private int utypeIndex;

	public MetadataTablesQuery(TapService tapService) {
	    this(tapService, "schema_name", "table_name", "table_type", "description", "utype"); 
	}

	public MetadataTablesQuery(TapService tapService, String schemaNameColumnName, String tableNameColumnName, String tableTypeColumnName, String descriptionColumnName, String utypeColumnName) {
	    this.tables = new HashMap<String,MetadataTable>();

	    this.tapService = tapService;

	    this.schemaNameColumnName = schemaNameColumnName;
	    this.tableNameColumnName = tableNameColumnName;
	    this.tableTypeColumnName = tableTypeColumnName;
	    this.descriptionColumnName = descriptionColumnName;
	    this.utypeColumnName = utypeColumnName;

	    this.schemaNameIndex = -1;
	    this.tableNameIndex = -1;
	    this.tableTypeIndex = -1;
	    this.descriptionIndex = -1;
	    this.utypeIndex = -1;
	}

	public void acceptMetadata(StarTable meta) throws TableFormatException {
	    tables.clear();

	    schemaNameIndex = -1;
	    tableNameIndex = -1;
	    tableTypeIndex = -1;
	    descriptionIndex = -1;
	    utypeIndex = -1;

	    for (int index = 0; index < meta.getColumnCount(); index++) {
		ColumnInfo columnInfo = meta.getColumnInfo(index);
		String name = columnInfo.getName();

		if (schemaNameColumnName.equals(name)) 
		    schemaNameIndex = index;
		else if (tableNameColumnName.equals(name)) 
		    tableNameIndex = index;
		else if (tableTypeColumnName.equals(name)) 
		    tableTypeIndex = index;
		else if (descriptionColumnName.equals(name)) 
		    descriptionIndex = index;
		else if (utypeColumnName.equals(name))
		    utypeIndex = index;
	    }

	    if (schemaNameIndex < 0 || tableNameIndex < 0 || tableTypeIndex < 0 || descriptionIndex < 0 || utypeIndex < 0) {
		StringBuilder sb = new StringBuilder("missing required column: ");
		String separator = " ";
		if (schemaNameIndex < 0) {
		    sb.append(separator);
		    sb.append(schemaNameColumnName);
		    separator = ",";
		}
		if (tableNameIndex < 0) {
		    sb.append(separator);
		    sb.append(tableNameColumnName);
		    separator = ",";
		}
		if (tableTypeIndex < 0) {
		    sb.append(separator);
		    sb.append(tableTypeColumnName);
		    separator = ",";
		}
		if (descriptionIndex < 0) {
		    sb.append(separator);
		    sb.append(descriptionColumnName);
		    separator = ", ";
		}
		if (utypeIndex < 0) {
		    sb.append(separator);
		    sb.append(utypeColumnName);
		}
		
		if (logger.isLoggable(Level.WARNING)) 
		    logger.log(Level.WARNING, sb.toString());

		//		throw new TableFormatException(sb.toString());
	    }
	}

	public void acceptRow(Object[] row) throws IOException {
	    String schemaName = (schemaNameIndex >= 0 && row[schemaNameIndex] != null) ? row[schemaNameIndex].toString() : null;
	    String tableName = (tableNameIndex >= 0 && row[tableNameIndex] != null) ? row[tableNameIndex].toString() : null;
	    String tableType = (tableTypeIndex >= 0 && row[tableTypeIndex] != null) ? row[tableTypeIndex].toString() : null;
	    String  description = (descriptionIndex >= 0 && row[descriptionIndex] != null) ? row[descriptionIndex].toString() : null;
	    String utype = (utypeIndex >= 0 && row[utypeIndex] != null) ? row[utypeIndex].toString() : null;

	    MetadataTable table = new MetadataTable(schemaName, tableName, description, utype, tableType);
	    tables.put(tableName, table);
	}

	public void endRows() throws IOException {
	}

	public Map<String,MetadataTable> getTables() {
	    return tables;
	}

	public void update() throws HttpException, ResponseFormatException, IOException {
	    SyncJob syncJob = new SyncJob(tapService);
	    syncJob.setParameter("QUERY", "SELECT * FROM TAP_SCHEMA.tables");
	    syncJob.setParameter("LANG", "ADQL");
	    syncJob.setParameter("FORMAT", "votable");
	    
	    InputStream inputStream = syncJob.run();
	    try { 
		new VOTableBuilder().streamStarTable(inputStream, this, "0");
	    } finally {
		try { 
		    inputStream.close();
		} catch (Exception ex) {
		    logger.log(Level.SEVERE, "error closing TAP_SCHEMA.tables results", ex);
		}
	    }
	}
    }

    class MetadataSchemasQuery implements TableSink {
	private Map<String,MetadataSchema> schemas;

	private TapService tapService;

	private String schemaNameColumnName;
	private String descriptionColumnName;
	private String utypeColumnName;

	private int schemaNameIndex;
	private int descriptionIndex;
	private int utypeIndex;

	public MetadataSchemasQuery(TapService tapService) {
	    this(tapService, "schema_name", "description", "utype");
	}

	public MetadataSchemasQuery(TapService tapService, String schemaNameColumnName, String descriptionColumnName, String utypeColumnName) {
	    this.schemas = new HashMap<String,MetadataSchema>();

	    this.tapService = tapService;

	    this.schemaNameColumnName = schemaNameColumnName;
	    this.descriptionColumnName = descriptionColumnName;
	    this.utypeColumnName = utypeColumnName;

	    this.schemaNameIndex = -1;
	    this.descriptionIndex = -1;
	    this.utypeIndex = -1;
	}

	public void acceptMetadata(StarTable meta) throws TableFormatException {
	    schemas.clear();

	    schemaNameIndex = -1;
	    descriptionIndex = -1;
	    utypeIndex = -1;

	    for (int index = 0; index < meta.getColumnCount(); index++) {
		ColumnInfo columnInfo = meta.getColumnInfo(index);
		String name = columnInfo.getName();

		if (schemaNameColumnName.equals(name)) 
		    schemaNameIndex = index;
		if (descriptionColumnName.equals(name)) 
		    descriptionIndex = index;
		if (utypeColumnName.equals(name))
		    utypeIndex = index;
		
	    }

	    if (schemaNameIndex < 0 || descriptionIndex < 0 || utypeIndex < 0) {
		StringBuilder sb = new StringBuilder("missing required column: ");
		String separator = " ";
		if (schemaNameIndex < 0) {
		    sb.append(separator);
		    sb.append(schemaNameColumnName);
		    separator = ",";
		}
		if (descriptionIndex < 0) {
		    sb.append(separator);
		    sb.append(descriptionColumnName);
		    separator = ", ";
		}
		if (utypeIndex < 0) {
		    sb.append(separator);
		    sb.append(utypeColumnName);
		}
		
		if (logger.isLoggable(Level.WARNING)) 
		    logger.log(Level.WARNING, sb.toString());

		//		throw new TableFormatException(sb.toString());
	    }
	}
	
	public void acceptRow(Object[] row) throws IOException {
	    String schemaName = (schemaNameIndex >= 0 && row[schemaNameIndex] != null) ? row[schemaNameIndex].toString() : null;
	    String  description = (descriptionIndex >= 0 && row[descriptionIndex] != null) ? row[descriptionIndex].toString() : null;
	    String utype = (utypeIndex >= 0 && row[utypeIndex] != null) ? row[utypeIndex].toString() : null;

	    schemas.put(schemaName, new MetadataSchema(schemaName, description, utype));
	}

	public void endRows() throws IOException {
	}
	
	public Map<String,MetadataSchema> getSchemas() {
	    return schemas;
 	}
	
	public void update() throws HttpException, ResponseFormatException, IOException {
	    SyncJob syncJob = new SyncJob(tapService);
	    syncJob.setParameter("QUERY", "SELECT * FROM TAP_SCHEMA.schemas");
	    syncJob.setParameter("LANG", "ADQL");
	    syncJob.setParameter("FORMAT", "votable");
	    
	    InputStream inputStream = syncJob.run();
	    try { 
		new VOTableBuilder().streamStarTable(inputStream, this, "0");
	    } finally {
		try { 
		    inputStream.close();
		} catch (Exception ex) {
		    logger.log(Level.SEVERE, "error closing TAP_SCHEMA.schemas results", ex);
		}
	    }
	}
    }

    class MetadataForeignKey extends ForeignKey {
	private String keyId;
	private String fromTable;
	public MetadataForeignKey(String keyId, String fromTable, String targetTable, String description, String utype) {
	    super(targetTable, new HashMap<String,String>(), description, utype);
	    this.keyId = keyId;
	    this.fromTable = fromTable;
	}
	
	public String getKeyId() {
	    return keyId;
	}

	public String getFromTable() {
	    return fromTable;
	}

	public void add(Map<String,String> fkColumns) {
	    this.fkColumns.putAll(fkColumns);
	}
    }
    
    class MetadataColumn extends Column {
	private String tableName;
	
	public MetadataColumn(String tableName, String columnName, String description, String unit, String ucd, String utype, String dataType, String arraySize, List<String> flags, boolean std) {
	    super(columnName, description, unit, ucd, utype, dataType, arraySize, (String) null, (String) null, (String) null, flags, std);
	    this.tableName = tableName;
	}

	public String getTableName() {
	    return tableName;
	}
    }

    class MetadataTable extends Table {
	private String schemaName;
	public MetadataTable(String schemaName, String tableName, String description, String utype, String type) {
	    super(tableName, (String) null, description, utype, new ArrayList<Column>(), new ArrayList<ForeignKey>(), type);
	    this.schemaName = schemaName;
	}
	
	public String getSchemaName() {
	    return schemaName;
	}

	public void add(MetadataColumn column) {
	    super.add(column);
	}

	public void add(MetadataForeignKey key) {
	    super.add(key);
	}
    }

    class MetadataSchema extends Schema {
	public MetadataSchema(String name, String description, String utype) {
	    super(name, (String) null, description, utype, new ArrayList<Table>());
	}

	public void add(MetadataTable table) {
	    super.add(table);
	}
    }

    private TapService tapService;

    /**
     * Constructs a MetadataTableSet object from the given service.
     * @param tapService the TAP service associated with this MetadataTableSet object
     */
    public MetadataTableSet(TapService tapService) {
	super(tapService.getBaseURL()+"/tables");
	this.tapService = tapService;
    }

    /**
     * Updates this MetadataTableSet object with TAP metadata query responses from the TAP service.  Multiple requests are made to the TAP service associated with this TableSet object.
     * @throws Exception if an error occurs connecting to the TAP service or parsing the response
     * @throws HttpException if the service responses to the metadata queries has an unexpected HTTP status.
     * @throws ResponseFormatException if an error occurs parsing the service response into an VOSI Tables document.
     * @throws IOException if an error occurs creating an input stream.
     */   
    public void update() throws HttpException, ResponseFormatException, IOException {
	MetadataSchemasQuery schemasQuery = new MetadataSchemasQuery(tapService);
	schemasQuery.update();
	Map<String,MetadataSchema> schemas = schemasQuery.getSchemas();
	Iterator<MetadataSchema> schemasIterator = schemas.values().iterator();
	while (schemasIterator.hasNext()) {
	    MetadataSchema schema = schemasIterator.next();
	    this.add(schema);
	}
	
	MetadataTablesQuery tablesQuery = new MetadataTablesQuery(tapService);
	tablesQuery.update();
	Map<String,MetadataTable> tables = tablesQuery.getTables();
	Iterator<MetadataTable> tablesIterator = tables.values().iterator();
	while (tablesIterator.hasNext()) {
	    MetadataTable table = tablesIterator.next();
	    String schemaName = table.getSchemaName();
	    MetadataSchema schema = schemas.get(schemaName);
	    if (schema == null) {
		schema = new MetadataSchema(schemaName, (String) null, (String) null);
		schemas.put(schemaName, schema);
	    }
	    schema.add(table);
	}
	
	MetadataColumnsQuery columnsQuery = new MetadataColumnsQuery(tapService);
	columnsQuery.update();
	List<MetadataColumn> columns = columnsQuery.getColumns();
	for (MetadataColumn column: columns) {
	    String tableName = column.getTableName();
	    MetadataTable table = tables.get(tableName);
	    if (table == null) {
		table = new MetadataTable((String) null, tableName, (String) null, (String) null, (String) null);
		tables.put(tableName, table);
	    }
	    table.add(column);
	}
	
	MetadataKeysQuery keysQuery = new MetadataKeysQuery(tapService);
	keysQuery.update();
	Map<String,MetadataForeignKey> foreignKeys = keysQuery.getKeys();
	Iterator<MetadataForeignKey> foreignKeysIterator = foreignKeys.values().iterator();
	while (foreignKeysIterator.hasNext()) {
	    MetadataForeignKey foreignKey = foreignKeysIterator.next();
	    String fromTable = foreignKey.getFromTable();
	    MetadataTable table = tables.get(fromTable);
	    if (table == null) {
		table = new MetadataTable((String) null, fromTable, (String) null, (String) null, (String) null);
		tables.put(fromTable, table);
	    }
	    table.add(foreignKey);
	}

	MetadataKeyColumnsQuery keyColumnsQuery = new MetadataKeyColumnsQuery(tapService);
	keyColumnsQuery.update();
	Map<String,Map<String,String>> keyColumns = keyColumnsQuery.getKeyColumns();
	Set<Map.Entry<String,Map<String,String>>> entrySet = keyColumns.entrySet();
	Iterator<Map.Entry<String,Map<String,String>>> iterator = entrySet.iterator();
	while (iterator.hasNext()) {
	    Map.Entry<String,Map<String,String>> entry = iterator.next();
	    String keyId = entry.getKey();
	    if (logger.isLoggable(Level.INFO))
		logger.log(Level.INFO, "looking for key "+keyId);
	    MetadataForeignKey foreignKey = foreignKeys.get(keyId);
	    if (foreignKey == null) {
		if (logger.isLoggable(Level.INFO))
		    logger.log(Level.INFO, "key not found"+keyId);
		foreignKey = new MetadataForeignKey(keyId, (String) null, (String) null, (String) null, (String) null);
		foreignKeys.put(keyId, foreignKey);
	    }
	    foreignKey.add(entry.getValue());
	}
     }

    public static void main(String[] args) {
	try { 
	    TapService tapService = new TapService(args[0]);
	    MetadataTableSet tableset = new MetadataTableSet(tapService);
	    tableset.update();
	    tableset.getSchemas();

	    for (Schema schema: tableset.getSchemas()) {
		System.out.println(schema);
		for (Table table: schema.getTables()) {
		    System.out.println("\t"+table);
//		    for (Column column: table.getColumns()) {
//			System.out.println("\t\t"+column);
//		    }
		    for (ForeignKey key: table.getForeignKeys()) {
			System.out.println("\t\t"+key);
//			Map<String,String> fkColumns = key.getFKColumns();
//			Set<Map.Entry<String,String>> entrySet = fkColumns.entrySet();
//			Iterator<Map.Entry<String,String>> iterator = entrySet.iterator();
//			while (iterator.hasNext()) {
//			    Map.Entry<String,String> entry = iterator.next();
//			    System.out.println("\t\t\t"+entry.getKey()+" = "+entry.getValue());
//			}
		    }
		}
	    }

	    /*
	    MetadataKeyColumnsQuery keyColumnsQuery = new MetadataKeyColumnsQuery(tapService);
	    keyColumnsQuery.update();
	    Map<String,Map<String,String>> keyColumns = keyColumnsQuery.getKeyColumns();
	    Iterator<Map.Entry<String,Map<String,String>>> iterator = keyColumns.entrySet().iterator;
	    while (iterator.hasNext()) {
		Map.Entry<String,Map<String,String>> entry = iterator.next();
		String keyId = entry.getKey();
		ForeignKey key = keys.get(keyId);
		if (key == null) {
		    key = new ForeignKey(fromTable, (String) null, (String) null, (String) null, new ArrayList<Column>(), new ArrayList<ForeignKey>(), (String) null);
		    tables.put(fromTable, table);
		}
		table.add(foreignKey);
	    }
	    */
	} catch (Throwable throwable) {
	    throwable.printStackTrace();
	}
    }
}
