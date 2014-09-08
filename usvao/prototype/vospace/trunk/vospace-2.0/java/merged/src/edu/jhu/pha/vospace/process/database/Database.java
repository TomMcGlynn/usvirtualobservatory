package edu.jhu.pha.vospace.process.database;

import java.util.ArrayList;
import org.apache.tika.metadata.Metadata;

import edu.jhu.pha.vospace.process.sax.AsciiTable;

public interface Database {
	
	/**
	 * Performs initial database setup. Creates tables, views, etc.
	 * @throws DatabaseException
	 */
	public void setup() throws DatabaseException;
	
	/**
	 * Populates metadata tables. Creates and populates data tables.
	 * @param metadata		metadata obtained from Tika parser
	 * @param dataTables 	data tables
	 * @throws DatabaseException
	 */
	public void update(Metadata metadata, ArrayList<AsciiTable> dataTables) throws DatabaseException;
	
	/**
	 * Closes the database connection
	 * @throws DatabaseException
	 */
	public void close() throws DatabaseException;

}
