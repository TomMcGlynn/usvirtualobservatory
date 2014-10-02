package dalserver.conf;

import dalserver.KeywordFactory;
import dalserver.TableParam;

import java.io.IOException;
import org.xml.sax.SAXException;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

public class XMLv1KeywordConfigTest { 

    XMLv1KeywordConfig config = null;
    KeywordFactory kwf = null;

    @Before 
    public void setup(){
        kwf = new KeywordFactory();
        config = new XMLv1KeywordConfig(kwf);
    }

    @After 
    public void teardown() {
        config = null;
        kwf = null;
    }

    @Test
    public void testLoad() throws IOException, KeywordConfig.FormatException {
        config.load(getClass().getResourceAsStream("testconfig.xml"));

        TableParam kw = (TableParam) kwf.getKeyword("Name");
        assertNotNull("keyword Name not found", kw);
        assertEquals(kw.getName(), "Name");
        assertEquals(kw.getDataType(), "char");
        assertEquals(kw.getUcd(), "ID_MAIN");
        assertEquals(kw.getUtype(), "");
        assertEquals(kw.getArraySize(), "*");
        assertTrue(kw.getDescription().startsWith("The unique name"));
        // assertEquals("OBJECTID", kw.getFitsKeyword());
       
        kw = (TableParam) kwf.getKeyword("title");
        assertNotNull("keyword itle not found", kw);
        assertEquals(kw.getName(), "title");
        assertEquals(kw.getDataType(), "char");
        assertEquals(kw.getUcd(), "em.line");
        assertEquals(kw.getUtype(), "Line.title");
        assertEquals(kw.getArraySize(), "*");
        assertTrue(kw.getDescription().startsWith("an appropriate but "));
        // assertIsNull(kw.getFitsKeyword());
       
        kw = (TableParam) kwf.getKeyword("shorttitle");
        assertNotNull("keyword shorttitle not found", kw);
        assertEquals(kw.getName(), "shorttitle");
        assertEquals(kw.getDataType(), "char");
        assertEquals(kw.getUcd(), "em.line");
        assertEquals(kw.getUtype(), "Line.title");
        assertEquals(kw.getArraySize(), "*");
        assertTrue(kw.getDescription().startsWith("small description "));
        // assertIsNull(kw.getFitsKeyword());
       
    }

}