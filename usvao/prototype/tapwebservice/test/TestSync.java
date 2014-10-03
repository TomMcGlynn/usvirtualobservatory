
import edu.jhu.pha.helpers.adqlhelper.ADQLParser;
import java.io.FileOutputStream;
import java.io.File;
import edu.jhu.pha.writers.VotableWriter;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.io.InputStream;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import javax.ws.rs.core.MultivaluedMap;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.test.framework.JerseyTest;
import java.sql.DriverManager;
import javax.ws.rs.core.MediaType;
import javax.xml.transform.TransformerException;
import org.astrogrid.adql.AdqlException;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test Web Resources
 * @author deoyani nandrekar-heinis
 */
public class TestSync extends JerseyTest {
    private Properties prop;
    private Connection con;
    
    public TestSync()throws Exception {
        super("edu.jhu.pha.resources.SyncResource");        
        
        prop = new Properties();
        try{        
            InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("tapwebservice.properties");
            prop.load(in);
            in.close();}catch(Exception exp){
                System.out.println("Exception:"+exp.getMessage());
        }
        Class.forName(prop.getProperty("database.Driver"));
        con = DriverManager.getConnection(prop.getProperty("database.URL"),
                                          prop.getProperty("database.UserName"),
                                          prop.getProperty("database.UserPassword"));
    }
    
    @Test
    public void testDatabaseConnection() throws SQLException, ClassNotFoundException{
        Class.forName(prop.getProperty("database.Driver"));
        con = DriverManager.getConnection(prop.getProperty("database.URL"),
                                          prop.getProperty("database.UserName"),
                                          prop.getProperty("database.UserPassword"));
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(prop.getProperty("data.testquery"));
        int i = 0;
        while(rs.next()){i++;}
        assertEquals(true,(i>0));
        System.out.println("Connection to database passed");
    }
    
    @Test
    public void testVOtableWriter(){
        Statement stmt = null;
        try{            
            stmt = con.createStatement();
            //Write any query to test
            ResultSet rs = stmt.executeQuery(prop.getProperty("data.testquery"));
            FileOutputStream foStream = new FileOutputStream(new File("testvotable.xml"));
            long timebefore = System.currentTimeMillis();
            //pass resultset object and max number of rows to be written in a votable
            VotableWriter _votable = new VotableWriter(rs,100);
            _votable.generateFinalVOTable(foStream);            
            rs.close();            
            foStream.close();            
            long timeafter = System.currentTimeMillis();
            //table write should not take more than hour (this can be changed if for big tables more time is needed)
            assertEquals(true, (timeafter-timebefore < 216000));
            
        }catch(Exception exp){
            System.out.println("Exception :"+exp.getMessage());
            
        }
        finally{
            try{stmt.close();}catch(Exception exp){}
        }
    }
    
    @Test
    public void testADQLParser() throws AdqlException, TransformerException{
        ADQLParser parser = new ADQLParser();
        String adqlString ="SELECT o.ra, o.dec FROM photoobjAll o  WHERE CONTAINS( POINT('J2000', o.ra, o.dec), CIRCLE('J2000', 180, 0, 0.3)) = 1";
        String sqlString = parser.getSQL(adqlString,prop.getProperty("adql.styleSheet"));
        System.out.println("Parsed SQL:"+sqlString);
        //String knownsqlEquivalent= "Select o.ra, o.dec From photoobjall as o inner join dbo.fSphGetHtmRanges(dbo.fSphSimplifyString('CIRCLE J2000 180 0 0.3')) h on o.HtmID between h.HtmIdStart and h.HtmIdEnd Where dbo.fSphRegionContainsXYZ(dbo.fSphSimplifyString('CIRCLE J2000 180 0 0.3'), o.cx ,o.cy,o.cz) = 1";
        assertEquals(true, (sqlString != null || sqlString.equals("")));
    }        
    
    
    @Test
    public void testSync() {     
        WebResource webResource = com.sun.jersey.api.client.Client.create().resource("http://localhost:8080/tapwebservice/tap/sync");
        //Response resp = webResource.path("sync").entity(this);
        MultivaluedMap formData = new MultivaluedMapImpl();
        formData.add("REQUEST", "doQuery");
        formData.add("LANG", "ADQL");
        formData.add("QUERY", "SELECT TOP 10 ra FROM PhotoObjAll");
        ClientResponse response = webResource.type("application/x-www-form-urlencoded").post(ClientResponse.class, formData);        
        assertEquals( 200, response.getStatus());
        System.out.println("Sync passed");
    }
     
    @Test
    public void testAsync() {     
        WebResource webResource = com.sun.jersey.api.client.Client.create().resource("http://localhost:8080/tapwebservice/tap/async");
        //Response resp = webResource.path("sync").entity(this);
        MultivaluedMap formData = new MultivaluedMapImpl();
        formData.add("REQUEST", "doQuery");
        formData.add("LANG", "ADQL");
        formData.add("QUERY", "SELECT TOP 10 ra FROM PhotoObjAll");
        ClientResponse response = webResource.type("application/x-www-form-urlencoded").post(ClientResponse.class, formData);        
        assertEquals( 200, response.getStatus());
        System.out.println("Async passed");
    }
    
    @Test
    public void testAsyncList() {     
        WebResource webResource = com.sun.jersey.api.client.Client.create().resource("http://localhost:8080/tapwebservice/tap/async");
        //Response resp = webResource.path("sync").entity(this);
        ClientResponse response = webResource.get(ClientResponse.class);        
        try{
        InputStream ip = response.getEntityInputStream();
        int p = 0;
        while((p=ip.read())!= -1){
            System.out.print((char)p);
            
        }}catch(Exception exp){
            System.out.println("Exception :"+exp.getMessage());
        }
        assertEquals(MediaType.APPLICATION_XML_TYPE,response.getType());
        assertEquals( 200, response.getStatus());
        System.out.println("Async getlist");
    }       
    
    @Test
    public void testallAsync(){
        String jobid = "68f70311-cb9e-46e2-a039-5d678aa1b499";
        testAsyncPhase(jobid);
        testAsyncStarttime(jobid);
        testAsyncEndtime(jobid);
        testAsyncDuration(jobid);
        testAsyncDestruction(jobid);
        testAsyncParameters(jobid);
        testAsyncJob(jobid);
    }
    
    public void testAsyncPhase(String jobid) {   
        WebResource webResource = com.sun.jersey.api.client.Client.create().resource("http://localhost:8080/tapwebservice/tap/async/"+jobid+"/phase");
        //Response resp = webResource.path("sync").entity(this);
        ClientResponse response = webResource.get(ClientResponse.class);  
        assertEquals( 200, response.getStatus());
        System.out.println("Async phase passed");
    }
    
    public void testAsyncStarttime(String jobid) {   
        WebResource webResource = com.sun.jersey.api.client.Client.create().resource("http://localhost:8080/tapwebservice/tap/async/"+jobid+"/starttime");
        //Response resp = webResource.path("sync").entity(this);
        ClientResponse response = webResource.get(ClientResponse.class);  
        assertEquals( 200, response.getStatus());
        System.out.println("Async starttime passed");
    }
    
    
    public void testAsyncEndtime(String jobid) {   
        WebResource webResource = com.sun.jersey.api.client.Client.create().resource("http://localhost:8080/tapwebservice/tap/async/"+jobid+"/endtime");
        //Response resp = webResource.path("sync").entity(this);
        ClientResponse response = webResource.get(ClientResponse.class);  
        assertEquals( 200, response.getStatus());
        System.out.println("Async endtime passed");
    }
    
    
    public void testAsyncDuration(String jobid) {   
        WebResource webResource = com.sun.jersey.api.client.Client.create().resource("http://localhost:8080/tapwebservice/tap/async/"+jobid+"/executionduration");
        //Response resp = webResource.path("sync").entity(this);
        ClientResponse response = webResource.get(ClientResponse.class);  
        assertEquals( 200, response.getStatus());
        System.out.println("Async duration passed");
    }
    
    
    public void testAsyncDestruction(String jobid) {   
        WebResource webResource = com.sun.jersey.api.client.Client.create().resource("http://localhost:8080/tapwebservice/tap/async/"+jobid+"/destruction");
        //Response resp = webResource.path("sync").entity(this);
        ClientResponse response = webResource.get(ClientResponse.class);  
        assertEquals( 200, response.getStatus());
        System.out.println("Async destruction passed");
    }
    
    
    public void testAsyncParameters(String jobid) {   
        WebResource webResource = com.sun.jersey.api.client.Client.create().resource("http://localhost:8080/tapwebservice/tap/async/"+jobid+"/parameters");
        //Response resp = webResource.path("sync").entity(this);
        ClientResponse response = webResource.get(ClientResponse.class);  
        assertEquals( 200, response.getStatus());
        System.out.println("Async parameters passed!!");
    }   
    
    public void testAsyncJob(String jobid) {   
        WebResource webResource = com.sun.jersey.api.client.Client.create().resource("http://localhost:8080/tapwebservice/tap/async/"+jobid);
        //Response resp = webResource.path("sync").entity(this);
        ClientResponse response = webResource.get(ClientResponse.class);  
        assertEquals( 200, response.getStatus());
        System.out.println("Async job passed!!");
    }     
}

