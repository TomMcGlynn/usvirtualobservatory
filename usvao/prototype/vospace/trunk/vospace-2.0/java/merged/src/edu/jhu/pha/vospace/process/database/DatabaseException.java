package edu.jhu.pha.vospace.process.database;

public class DatabaseException extends Exception {

	public DatabaseException(String message) {
		super(message);
	}
	
	public DatabaseException(String message, Exception e) {
		super(message,e);
	}
	
}
