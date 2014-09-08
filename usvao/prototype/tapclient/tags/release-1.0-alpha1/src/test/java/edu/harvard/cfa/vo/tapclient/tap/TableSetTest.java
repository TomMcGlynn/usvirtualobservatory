package edu.harvard.cfa.vo.tapclient.tap;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

import edu.harvard.cfa.vo.tapclient.util.HttpException;
import edu.harvard.cfa.vo.tapclient.util.ResponseFormatException;
import edu.harvard.cfa.vo.tapclient.util.JettyTestServer;
import edu.harvard.cfa.vo.tapclient.util.TestServer;
import edu.harvard.cfa.vo.tapclient.vosi.Column;
import edu.harvard.cfa.vo.tapclient.vosi.ForeignKey;
import edu.harvard.cfa.vo.tapclient.vosi.Schema;
import edu.harvard.cfa.vo.tapclient.vosi.Table;
import edu.harvard.cfa.vo.tapclient.vosi.TableSet;

public class TableSetTest {
    private static TestServer testServer;

    @BeforeClass public static void setUpClass() throws Exception {
	testServer = new JettyTestServer("/tap/tables", 7060);
	testServer.start();
    }

    @Before public void setUp() {
	testServer.setResponseBody("<?xml version='1.0' encoding='UTF-8' ?><tableset xmlns:xml='http://www.w3.org/XML/1998/namespace' xmlns:xlink='http://www.w3.org/1999/xlink' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xmlns:vr='http://www.ivoa.net/xml/VOResource/v1.0' xmlns:vs='http://www.ivoa.net/xml/VODataSource/v1.0' xsi:schemaLocation='http://www.ivoa.net/xml/VOResource/v1.0 http://www.ivoa.net/xml/VOResource/v1.0 http://www.ivoa.net/xml/VODataSource/v1.0 http://www.ivoa.net/xml/VODataSource/v1.0'><schema><name>schemanamevalue</name><title>schematitlevalue</title><description>schemadescriptionvalue</description><utype>schemautypevalue</utype><table type='tabletypevalue'><name>tablenamevalue</name><title>tabletitlevalue</title><description>tabledescriptionvalue</description><utype>tableutypevalue</utype><column std='true'><name>columnnamevalue</name><title>columntitlevalue</title><description>columndescriptionvalue</description><ucd>columnucdvalue</ucd><utype>columnutypevalue</utype><unit>columnunitvalue</unit><dataType delim='columndelimvalue' arraysize='2x3*' extendedSchema='columnextendedschemavalue' extendedType='columnextendedtypevalue'>columndatatypevalue</dataType><flag>primary</flag><flag>indexed</flag><flag>another</flag></column><foreignKey><targetTable>foreignkeytargettablevalue</targetTable><fkColumn><fromColumn>fromcolumnvalue</fromColumn><targetColumn>targetcolumnvalue</targetColumn></fkColumn><description>foreignkeydescriptionvalue</description><utype>foreignkeyutypevalue</utype></foreignKey></table></schema></tableset>".getBytes());
    }

    @AfterClass public static void tearDownClass() throws Exception {
	testServer.stop();
    }

    @Test public void getTableSetTest() throws HttpException, ResponseFormatException, IOException {
	TableSet tableSet = new TapService("http://localhost:7060/tap").getTableSet();
	assertFalse(tableSet.getSchemas().isEmpty());
    } 
    
    @Test public void getSchemaTest() throws HttpException, ResponseFormatException, IOException {
	TableSet tableSet = new TapService("http://localhost:7060/tap").getTableSet();
	
	tableSet.update();
	
	List<Schema> schemas = tableSet.getSchemas();
	assertFalse(schemas.isEmpty());
	
	Schema schema  = schemas.get(0);
	assertEquals("schemanamevalue", schema.getName());
	assertEquals("schematitlevalue", schema.getTitle());
	assertEquals("schemadescriptionvalue", schema.getDescription());
	assertEquals("schemautypevalue", schema.getUtype());
	
	List<Table> tables = schema.getTables();
	assertFalse(tables.isEmpty());
	
	Table table = tables.get(0);
	assertEquals("tablenamevalue", table.getName());
	assertEquals("tabletitlevalue", table.getTitle());
	assertEquals("tabledescriptionvalue", table.getDescription());
 	assertEquals("tableutypevalue", table.getUtype());
	assertEquals("tabletypevalue", table.getType());

	List<Column> columns = table.getColumns();
	assertFalse(columns.isEmpty());

	Column column = columns.get(0);
	assertEquals("columnnamevalue", column.getName());
	assertEquals("columndescriptionvalue", column.getDescription());
	assertEquals("columnunitvalue", column.getUnit());
	assertEquals("columnucdvalue", column.getUcd());
	assertEquals("columnutypevalue", column.getUtype());
	assertEquals("columndatatypevalue", column.getDataType());
	assertEquals("2x3*", column.getArraySize());
	assertEquals("columndelimvalue", column.getDelim());
	assertEquals("columnextendedtypevalue", column.getExtendedType());
	assertEquals("columnextendedschemavalue", column.getExtendedSchema());
	assertTrue(column.isStd());
	assertTrue(column.isPrimary());
	assertTrue(column.isIndexed());
	assertEquals(3, column.getFlags().size());

	List<ForeignKey> foreignKeys = table.getForeignKeys();
	assertFalse(foreignKeys.isEmpty());

	ForeignKey foreignKey = foreignKeys.get(0);
	assertEquals("foreignkeytargettablevalue", foreignKey.getTargetTable());
 	assertEquals("foreignkeydescriptionvalue", foreignKey.getDescription());
	assertEquals("foreignkeyutypevalue", foreignKey.getUtype());

	assertFalse(foreignKey.getFKColumns().isEmpty());
   } 

    @Test(expected=HttpException.class) public void notFoundTest() throws HttpException, ResponseFormatException, IOException {
	TableSet tableSet = new TapService("http://localhost:7060/").getTableSet();

	tableSet.update();
    } 
}