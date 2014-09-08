package edu.harvard.cfa.vo.tapclient.vosi;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import edu.harvard.cfa.vo.tapclient.vosi.Column;

public class ColumnTest {
    private net.ivoa.xml.voDataService.v11.TableParam xcolumn;

    @Before public void setUp() throws Exception {
	xcolumn = net.ivoa.xml.voDataService.v11.TableParam.Factory.parse("<xml-fragment std='true'><name>columnnamevalue</name><title>columntitlevalue</title><description>columndescriptionvalue</description><ucd>columnucdvalue</ucd><utype>columnutypevalue</utype><unit>columnunitvalue</unit><dataType arraysize='2x3*' delim='columndelimvalue' extendedSchema='columnextendedschemavalue' extendedType='columnextendedtypevalue'>columndatatypevalue</dataType><flag>primary</flag><flag>indexed</flag><flag>another</flag></xml-fragment>");
   }

    @After public void tearDown() {
	xcolumn = null;
    }

    @Test public void getArraySizeTest() throws Exception {
	Column column = new Column(xcolumn);													    
	assertEquals("2x3*", column.getArraySize());
    }

    @Test public void getDelimTest() throws Exception {
	Column column = new Column(xcolumn);													    
	assertEquals("columndelimvalue", column.getDelim());
    }

    @Test public void getExtendedTypeTest() throws Exception {
	Column column = new Column(xcolumn);													    
	assertEquals("columnextendedtypevalue", column.getExtendedType());
    }

    @Test public void getExtendedSchemaTest() throws Exception {
	Column column = new Column(xcolumn);													    
	assertEquals("columnextendedschemavalue", column.getExtendedSchema());
    }
}