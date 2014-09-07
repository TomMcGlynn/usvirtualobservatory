package net.ivoa.registry;

import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.Date;
import java.io.File;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import net.ivoa.datascope.DS;
import net.ivoa.util.Settings;


import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.StringReader;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.net.URL;
import org.xml.sax.InputSource;
import java.lang.StringBuilder;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;


public class RegistrySearch {

   String         wsdl;

   /** 
   * Connect to the STScI NVO registry.
   */
   public RegistrySearch(String wsdlNode)  throws Exception {
      wsdl = wsdlNode;
   }

   /** Submit a query to the registry.
   *  Returns a hash of hashes using the IDs to identify the
   *  nodes and then for each ID using the elements of the returned
   *  query and the values.  The results are represented as arrays of strings.
   */

   public HashMap<String, HashMap<String, String[]>> query(String queryString) throws Exception {
      boolean debug = false;
      if ( debug ) System.out.println("Begin query:" + queryString);

      if ( debug ) System.out.println("getting votable");

      URL votableURL = new URL(wsdl+queryString);
      String votable = "";
      BufferedReader in = new BufferedReader(
         new InputStreamReader(
            votableURL.openStream()));
      String inputLine;
      StringBuilder sb = new StringBuilder();
      while ((inputLine = in.readLine()) != null) {
//         votable += inputLine; // REALLY, REALLY, REALLY SLOW!
         sb.append(inputLine);
         if ( debug ) System.out.println("LINE:"+inputLine);
      }
      in.close();
      votable = sb.toString();

      if ( debug ) System.out.println(votable);


      if ( debug ) System.out.println("done getting votable");

      StreamSource xsl  = XSLTrans.getSource(Settings.get("RegistryXSL"));
      StringWriter sw   = new StringWriter();
      StreamResult out  = new StreamResult(sw);
      new XSLTrans().transform(xsl, new StreamSource(new StringReader(votable)), out);
      sw.close();
      String catalogs = sw.toString();

      if ( debug ) System.out.println("Done converting to straight XML");

//FROM : http://www.java-tips.org/java-se-tips/javax.xml.parsers/how-to-read-xml-file-in-java.html

      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      Document doc = db.parse(new InputSource(new StringReader(catalogs)));
      doc.getDocumentElement().normalize();
      System.out.println("Root element " + doc.getDocumentElement().getNodeName());
      NodeList nl = doc.getElementsByTagName("catalog");

/*
   <ArrayOfSimpleResource>
   <SimpleResource>
<Title>Hubble Space Telescope Preview Images</Title>
<ShortName>HST Previews</ShortName>
<Publisher>MAST</Publisher>
<Creator>CADC</Creator>
   <Subject>
<string>UV</string>
<string> Optical</string>
<string> and Infrared Astronomy</string>
</Subject>
   <Description>
Quick-look preview images produced and processed by CADC.
Data from the following HST instruments are included: WFPC, WFPC2,
STIS, NICMOS, FOC, and ACS.
</Description>
<Contributor>CADC</Contributor>
<Date>2008-06-09T10:18:43.9546747-04:00</Date>
<Version>1.0</Version>
<Identifier>ivo://mast.stsci/siap/hst.previews</Identifier>
<ReferenceURL>http://archive.stsci.edu/</ReferenceURL>
<ServiceURL>http://archive.stsci.edu/siap/search.php?id=HST</ServiceURL>
<ContactName>Archive Branch, STScI</ContactName>
<ContactEmail>archive@stsci.edu</ContactEmail>
<Type>Archive</Type>
<CoverageSpatial/>
   <CoverageSpectral>
<string>Optical</string>
</CoverageSpectral>
<CoverageTemporal>1990-04 to present</CoverageTemporal>
<EntrySize>0.0500000007450581</EntrySize>
<MaxSR>0</MaxSR>
<MaxRecords>0</MaxRecords>
   <ContentLevel>
<string>Research</string>
</ContentLevel>
<Facility>STScI</Facility>
   <Instrument>
<string>WFPC</string>
<string>  WFPC2</string>
<string> STIS </string>
<string>NICMOS</string>
<string> FOC</string>
<string> ACS</string>
</Instrument>
<ServiceType>SIAP/ARCHIVE</ServiceType>
<xml/>
<validationLevel>2</validationLevel>
</SimpleResource>

<catalog><tags>#Images#</tags><shortName>hdap_siap</shortName><title>HDAP -- Heidelberg Digitized Astronomical Plates</title><description>Scans of plates obtained at Landessternwarte Heidelberg-Koenigstuhl and German-Spanish Astronomical Center (Calar Alto Observatory), Spain, 1900 through 1999.</description><publisher>The GAVO DC team</publisher><waveband/><identifier>ivo://org.gavo.dc/lswscans/res/positions/siap</identifier><updated>2008-05-09T00:00:00Z</updated><subject>#astrophotography#photographic plate#pointed observations#</subject><type/><contentLevel/><regionOfRegard/><version/><capabilityClass>SimpleImageAccess</capabilityClass><capabilityID>ivo://ivoa.net/std/SIA</capabilityID><capabilityValidationLevel/><interfaceClass>ParamHTTP</interfaceClass><interfaceVersion/><interfaceRole>std</interfaceRole><accessURL>http://vo.uni-hd.de/lswscans/res/positions/siap/siap.xml?</accessURL><supportedInputParam>#POS#SIZE#INTERSECT#FORMAT#dateObs#</supportedInputParam><maxRadius/><maxRecords>200000000</maxRecords><publisherID/><referenceURL>http://vo.uni-hd.de/lswscans/res/positions/siap/form</referenceURL></catalog>

<catalog><tags>#Images#</tags><shortName>2MASS ASKY AT</shortName><title>2MASS All-Sky Atlas Image Service</title><description>This service provides access to and information about the 2MASS All- Sky Atlas Images. Atlas Images delivered by this service are in FITS format and contain full WCS information in their headers. Additionally, the image headers contain photometric zero point information. 2MASS Atlas Images are suitable for quantitative photometric measurements.</description><publisher>NASA/IPAC Infrared Science Archive</publisher><waveband>#Infrared#</waveband><identifier>ivo://irsa.ipac/2MASS-ASKY-AT</identifier><updated>2008-05-01T13:25:58Z</updated><subject>#NOT PROVIDED#</subject><type>#Archive#</type><contentLevel>#Research#</contentLevel><regionOfRegard/><version/><capabilityClass>SimpleImageAccess</capabilityClass><capabilityID>ivo://ivoa.net/std/SIA</capabilityID><capabilityValidationLevel>2</capabilityValidationLevel><interfaceClass>ParamHTTP</interfaceClass><interfaceVersion/><interfaceRole>std</interfaceRole><accessURL>http://irsa.ipac.caltech.edu/cgi-bin/2MASS/IM/nph-im_sia?type=at&amp;ds=asky</accessURL><supportedInputParam/><maxRadius/><maxRecords>12000</maxRecords><publisherID/><referenceURL>http://irsa.ipac.caltech.edu/applications/2MASS/IM</referenceURL></catalog>

*/
      if (nl == null) {
         System.err.println("No results returned from registry query.");
         return null;
      }

      HashMap<String, HashMap<String, String[]>> res = new HashMap<String, HashMap<String, String[]>>();
      if ( debug ) System.out.println("Nodes:"+nl.getLength());
      for(int i=0; i<nl.getLength(); i += 1) {
         Node n = nl.item(i);
         HashMap<String, String[]> curr = new HashMap<String, String[]>();

         NodeList ml  = n.getChildNodes();
         String   id = null;
         if ( debug ) System.out.println("mNodes:"+ml.getLength());
         for (int j=0; j<ml.getLength(); j += 1) {
            Node m = ml.item(j);

            String key = m.getNodeName();
            if ( debug ) System.out.println("key:"+key);
            if (key.indexOf(':') > 0) {
               key = key.substring(key.indexOf(':')+1);
            }

            if (key.equals("identifier")) {
               try {
                  id = m.getFirstChild().getNodeValue();
               } catch (Exception e) {
                  System.err.println("No ID for entry:"+i);
                  break;
               }
            }

            NodeList kl = m.getChildNodes();
            if ( debug ) System.out.println("kNodes:"+kl.getLength());

            String[] vals = new String[1];
            if ( kl.getLength() > 0 ) {
               Node kk = kl.item(0);
               String kkval = kk.getNodeValue().trim();
               if ( debug ) System.out.println("kk value: "+kkval);
               if ( ( kkval.charAt(0) == '#' ) && ( kkval.charAt(kkval.length()-1)  == '#') ) {
                  String[] values = kkval.split("#");
                  if ( values.length <= 0 ) {
                     vals[0] = null;
                  } else {
                     vals = new String[values.length-1];
                     // ignore the first item in this array
                     for (int v=1;v<values.length;v++){
                        if ( debug ) System.out.println("value:"+v+":"+values[v]);
                        vals[v-1] = values[v];
                     }
                  }
               } else {
                  vals[0] = kkval;
               }
            } else {
               if ( debug ) System.out.println("k1 length <= 0");
               vals[0] = null;
            }

            if ( debug ) System.out.println("Storing :"+key);
            if ( debug ) for (int x=0; x<vals.length; x++ ) System.out.println("Value :"+vals[x]);

            curr.put(key, vals);
         }
         if (id != null) {
            if ( debug ) System.out.println("Putting id:"+id);
            res.put(id, curr);
         }
      }
      return res;
   }

   public static void main(String[] args) throws Exception {

      boolean debug = false;
      String RegistryURL = DS.getRegistryURL();
      RegistrySearch rs = new RegistrySearch(RegistryURL);
      if ( debug ) System.out.println("Beginning:"+RegistryURL);

//      for (String query: args) {
      for (String query: DS.getRegistryQuery()) {
         if ( debug ) System.out.println("calling rs.query("+query+")");
         HashMap<String, HashMap<String, String[]>> hm = rs.query(query);
         if ( debug ) System.out.println("returned from query:"+query);
         for (String id: hm.keySet()) {
            HashMap<String, String[]> obj = hm.get(id);
            System.out.println("Processing ID: "+id);
//            String[] sn  =  obj.get("ShortName");
            String[] sn  =  obj.get("shortName");
            if (sn == null || sn.length ==0 ) {
               System.out.println("  No short name found");
            } else {
               System.out.println("  ShortName="+sn[0]);
            }
//            String[] url =  obj.get("ServiceURL");
            String[] url =  obj.get("accessURL");
            if (url == null  || url.length == 0) {
               System.out.println("  No URL found");
            } else {
               System.out.println("  URL="+url[0]);
            }
            String subj = "";
            String div  = "";
            String[] sarr = obj.get("subject");
//            String[] sarr = obj.get("Subject");
            if (sarr != null) {
               for (String join: sarr) {
                  subj += div + join;
                  div = ",";
               }
            }
            System.out.println("  Subject="+subj);
         }
      }
   }
}
