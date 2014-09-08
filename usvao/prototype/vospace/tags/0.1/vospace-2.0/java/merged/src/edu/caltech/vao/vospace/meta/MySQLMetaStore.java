/**
 * MySQLMetaStore.java
 * Author: Matthew Graham (Caltech)
 * Author: Dmitry Mishin (JHU)
 * Version: (0.2)
 */

package edu.caltech.vao.vospace.meta;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Vector;

import org.apache.log4j.Logger;

import edu.caltech.vao.vospace.NodeType;
import edu.caltech.vao.vospace.xml.Node;
import edu.caltech.vao.vospace.xml.NodeFactory;
import edu.jhu.pha.vospace.api.exceptions.InternalServerErrorException;
import edu.jhu.pha.vospace.api.exceptions.NotFoundException;

/**
 * This class represents a metadata store for VOSpace based on the MySQL
 * open source database
 */
public class MySQLMetaStore implements MetaStore{

	public static final int MIN_DETAIL = 1;
	public static final int PROPERTY_DETAIL = 2;
	public static final int MAX_DETAIL = 3;
	private static Logger logger = Logger.getLogger(MySQLMetaStore.class);


	/*
	 * (non-Javadoc)
	 * @see edu.caltech.vao.vospace.meta.MetaStore#isStored(java.lang.String)
	 */
	@Override
	public boolean isStored(String identifier, String owner) {
		String query = "select identifier from nodes where identifier = '" + identifier + "' and owner = '"+owner+"'";
		return null != queryString(query);
	}

	/*
	 * (non-Javadoc)
	 * @see edu.caltech.vao.vospace.meta.MetaStore#getType(java.lang.String)
	 */
	@Override
	public NodeType getType(String identifier, String owner)  {
		logger.debug("Checking type for uri:" +identifier);
		String query = "select type from nodes where identifier = '" + identifier + "' and owner = '"+owner+"'";
		
		String result = queryString(query);
		if(null == result){
			logger.error("Not found node: "+identifier+" "+owner);
			throw new NotFoundException("A Node does not exist with the requested URI.");
		}

		return NodeType.valueOf(NodeType.class, result);
	}

	/*
	 * (non-Javadoc)
	 * @see edu.caltech.vao.vospace.meta.MetaStore#isKnownProperty(java.lang.String)
	 */
	@Override
	public boolean isKnownProperty(String identifier) {
		String query = "select * from metaproperties where identifier = '" + identifier + "'";
		return null != queryString(query);
	}

	/*
	 * (non-Javadoc)
	 * @see edu.caltech.vao.vospace.meta.MetaStore#registerProperty(java.lang.String, int, boolean)
	 */
	@Override
	public void registerProperty(String property, int type, boolean readOnly)  {
		int wp = 0;
		if (readOnly) wp = 1;
		String query = "insert into metaproperties (identifier, type, readOnly) values ('" + property + "', '" + type + "', " + wp + ")";
		executeUpdate(query);
	}

	/*
	 * (non-Javadoc)
	 * @see edu.caltech.vao.vospace.meta.MetaStore#storeData(java.lang.String, edu.caltech.vao.vospace.NodeType, java.lang.String, java.lang.Object)
	 */
	@Override
	public void storeData(String identifier, NodeType type, String owner, Object metadata)  {
		if (metadata instanceof String) {
			String query = "insert into nodes (identifier, type, owner, creationDate, node) values ('" + identifier + "', '" + type + "', '" + owner + "', cast(now() as datetime), '" + (String) metadata + "')"; 
			executeUpdate(query);
			storeProperties((String) metadata);
		}
	}


	/*
	 * (non-Javadoc)
	 * @see edu.caltech.vao.vospace.meta.MetaStore#checkData(java.lang.String[], int)
	 */
	@Override
	public String checkData(String[] identifiers, int limit)  {
		String whereQuery = null, token = null;
		// Get count
		for (int i = 0; i < identifiers.length; i++) {
			if (i == 0) whereQuery = "where";
			if (identifiers[i].contains("*")) {
				whereQuery += " identifier like '" + identifiers[i].replace("*", "%") + "'";
			} else {
				whereQuery += " identifier = '" + identifiers[i] + "'";
			}
			if (i != identifiers.length - 1) whereQuery += " or ";
		}
		String query = "select count(identifier) from nodes " + whereQuery;
		String result = queryString(query);
		int count = 0;
		if(null != result)
			count = Integer.parseInt(result);
		
		if (limit < count) {
			token = UUID.randomUUID().toString();
			String createToken = "insert into listings (token, offset, count, whereQuery) values ('" + token + "', " + 0 + ", " + count + ", '" + whereQuery.replace("'", "\\'") + "')";
			executeUpdate(createToken);
		}
		return token;
	}


	/*
	 * (non-Javadoc)
	 * @see edu.caltech.vao.vospace.meta.MetaStore#removeData(java.lang.String)
	 */
	@Override
	public void removeData(String identifier, String owner) {
		String query = "delete from nodes where identifier like '" + identifier + "%' and owner = '"+owner+"'";
		executeUpdate(query);
	}

	/*
	 * (non-Javadoc)
	 * @see edu.caltech.vao.vospace.meta.MetaStore#updateData(java.lang.String, java.lang.Object)
	 */
	@Override
	public void updateData(String identifier, String owner, Object metadata)  {
		if (metadata instanceof String) {
			String query = "update nodes set node = '" + (String) metadata + "' where identifier = '" + identifier + "' and owner = '"+owner+"'"; 
			executeUpdate(query);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see edu.caltech.vao.vospace.meta.MetaStore#updateData(java.lang.String, java.lang.String, java.lang.Object)
	 */
	@Override
	public void updateData(String identifier, String newIdentifier, String owner, Object metadata)  {
		if (metadata instanceof String) {
			String query = "update nodes set identifier = '" + newIdentifier + "', node = '" + (String) metadata + "' where identifier = '" + identifier + "' and owner = '"+owner+"'"; 
			executeUpdate(query);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see edu.caltech.vao.vospace.meta.MetaStore#getToken(java.lang.String[])
	 */
	@Override
	public String getToken(String[] identifiers)  {
		String whereQuery = null, query = null;
		for (int i = 0; i < identifiers.length; i++) {
			if (i == 0) whereQuery += "where";
			if (identifiers[i].contains("*")) {
				whereQuery += " identifier like '" + identifiers[i].replace("*", "%") + "'";
			} else {
				whereQuery += " identifier = '" + identifiers[i] + "'";
			}
			if (i != identifiers.length - 1) query += " or ";
		}
		query = "select token from listings where whereQuery = '" + whereQuery.replace("'", "\\'") + "'";
		return queryString(query);
	}

	/*
	 *    (non-Javadoc)
	 * @see edu.caltech.vao.vospace.meta.MetaStore#updateProperty(java.lang.String, edu.caltech.vao.vospace.NodeType)
	 */
	@Override
	public void updateProperty(String property, NodeType type)  {
		String query = "update metaproperties set type = '" + type + "' where identifier = '" + property + "'";
		executeUpdate(query);
	}

	/*
	 * (non-Javadoc)
	 * @see edu.caltech.vao.vospace.meta.MetaStore#getPropertyType(java.lang.String)
	 */
	@Override
	public String getPropertyType(String identifier)  {
		String query = "select type from metaproperties where identifier = '" + identifier + "'";
		return queryString(query);
	}


	/**
	 * 
	 * @param query
	 * @return
	 */
	private static String queryString(String query) {
		Connection con = null;
		Statement st = null;
		ResultSet resSet = null;
		try {
			con = DriverManager.getConnection("jdbc:apache:commons:dbcp:dbPool");
			st = con.createStatement();
			resSet = st.executeQuery(query);
			if(resSet.next()){
				return resSet.getString(1);
			}
		} catch(SQLException ex) {
			throw new InternalServerErrorException(ex);
		} finally {
			try { resSet.close(); } catch(Exception e) { }
			try { st.close(); } catch(Exception e) { }
			try { con.close(); } catch(Exception e) { }
		}

		return null;
	}

	/**
	 * Execute a query on the store
	 * @param query
	 * @return
	 */
	private static int executeUpdate(String query) {
		Connection con = null;
		Statement st = null;
		try {
			con = DriverManager.getConnection("jdbc:apache:commons:dbcp:dbPool");
			st = con.createStatement();
			return st.executeUpdate(query);
		} catch(SQLException ex) {
			throw new InternalServerErrorException(ex);
		} finally {
			try { st.close(); } catch(Exception e) { }
			try { con.close(); } catch(Exception e) { }
		}
	}

	/**
	 * Extract and store the properties from the specified node description
	 * @param nodeAsString
	 */
	private void storeProperties(String nodeAsString)  {
		try {
			/*	    NodeType node = NodeType.Factory.parse(nodeAsString);
	    String identifier = node.getUri();
	    PropertyListType properties = node.getProperties();
	    for (PropertyType property : properties.getPropertyArray()) {
	        String query = "insert into properties (identifier, property, value) values ('" + identifier + "', '" + property.getUri() + "', '" + property.getStringValue() + "')"; 
	        statement.executeUpdate(query);
		} */
		} catch (Exception e) {}
	}

	/*
	 * (non-Javadoc)
	 * @see edu.caltech.vao.vospace.meta.MetaStore#getNode(java.lang.String)
	 */
	@Override
	public Node getNode(String identifier, String owner) {
		String nodeStr = queryString("select node from nodes where identifier = '" + identifier + "' and owner = '"+owner+"'");
		if(null == nodeStr)
			throw new NotFoundException("A Node does not exist with the requested URI.");
		Node node = NodeFactory.getInstance().getNode(nodeStr.getBytes());
		return node;
	}

	/*
	 * (non-Javadoc)
	 * @see edu.caltech.vao.vospace.meta.MetaStore#getNodeChildren(java.lang.String)
	 */
	@Override
	public List<String> getNodeChildren(String identifier, String owner) {
		Connection con = null;
		Statement st = null;
		ResultSet resSet = null;
		
		Vector<String> result = new Vector();
		
		try {
			con = DriverManager.getConnection("jdbc:apache:commons:dbcp:dbPool");
			st = con.createStatement();
			resSet = st.executeQuery("select identifier from nodes where identifier like '" + identifier + "%' and owner = '"+owner+"'");
			while (resSet.next()) {
			    String child = resSet.getString(1);
			    if (!child.equals(identifier) && !child.substring(identifier.length() + 1).contains("/")) {
			    	result.add(child);
			    }
			}
		} catch(SQLException ex) {
			throw new InternalServerErrorException(ex);
		} finally {
			try { resSet.close(); } catch(Exception e) { }
			try { st.close(); } catch(Exception e) { }
			try { con.close(); } catch(Exception e) { }
		}
		return result;
	}
	
    public List<String> getAllChildren(String identifier, String owner) {
    	ArrayList<String> children = new ArrayList<String>();
		Connection con = null;
		Statement st = null;
		ResultSet resSet = null;
		
		try {
			con = DriverManager.getConnection("jdbc:apache:commons:dbcp:dbPool");
			st = con.createStatement();
			resSet = st.executeQuery("select identifier from nodes where identifier like '" + identifier + "%' and owner = '"+owner+"'");
			while (resSet.next()) {
			    String child = resSet.getString(1);
			    if (!child.equals(identifier)) {
			    	children.add(child);
			    }
			}
		} catch(SQLException ex) {
			throw new InternalServerErrorException(ex);
		} finally {
			try { resSet.close(); } catch(Exception e) { }
			try { st.close(); } catch(Exception e) { }
			try { con.close(); } catch(Exception e) { }
		}
		return children;
    }

}
