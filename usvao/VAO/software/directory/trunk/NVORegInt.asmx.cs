using System;
using System.Collections;
using System.ComponentModel;
using System.Data;
using System.Diagnostics;
using System.IO;
using System.Text;
using System.Web;
using System.Web.Services;
using System.Xml;
using System.Xml.Serialization;
using registry;

using System.Net;

using ivoa.net.ri1_0.server;
using ivoa.altVOTable;

using System.Xml.XPath;
using System.Xml.Xsl;

namespace registry
{
    [System.Web.Services.WebService(Namespace = "ivoa.net.riws.v10")]
    [WebServiceBinding(ConformsTo = WsiProfiles.BasicProfile1_1)]
    public class NVORegInt
    {
        private static string appDir = System.Web.Hosting.HostingEnvironment.ApplicationPhysicalPath;
        private static bool useCache = Convert.ToBoolean(System.Configuration.ConfigurationManager.AppSettings["useCache"]);

        public NVORegInt()
        {
        }

        [WebMethod(Description = "<b>Input: predicate </b>Custom simple predicate search (placeholder for ADQL imp)<br><b>Output:</b> VOTable 1.1 Unique Resource per Row")]
        public ivoa.altVOTable.VOTABLE VOTPredicate(string predicate)
        {
            return VOTCapabilityPredOpt(predicate, string.Empty, 1);          
        }


        [WebMethod(Description = "<b>Input: predicate </b>Custom simple predicate search (placeholder for ADQL imp)<br><b>Input: VOTStyleOption </b> 1 = Unique Resource/Row, 2 = Unique Interface/Row<br><b>Output:</b>VOTable 1.1 with Fields set by VOTStyleOption")]
        public ivoa.altVOTable.VOTABLE VOTPredOpt(string predicate, int VOTStyleOption)
        {
            return VOTCapabilityPredOpt(predicate, string.Empty, VOTStyleOption);
        }

        //Clears the query string if it matches any common forms of malicious SQL.
        //I'm sure this could use improvement.
        private string EnsureSafeSQLStatement(string original)
        {
            string up = original.ToUpper();
            if (up.Contains("UPDATE ") || up.Contains("DELETE ") || up.Contains("EXEC("))
                return string.Empty;

            if (System.Text.RegularExpressions.Regex.IsMatch(up, "DROP\\s+TABLE"))
                return string.Empty;

            return original;
        }

        private static string appendUrl(string urlbase, string last)
        {

            if (urlbase.EndsWith("/"))
                return urlbase + last;
            else
                return urlbase + '/' + last;
        }

        [WebMethod(Description = "<b>Input: standard capability </b>conesearch, SimpleImageAccess, SimpleSpectralAccess<br><b>Output:</b> VOTable 1.1 Unique Resource per Row")]
        public ivoa.altVOTable.VOTABLE VOTCapability(string capability)
        {
            return VOTCapBandPredOpt(String.Empty, capability, string.Empty, 1);
        }

        [WebMethod(Description = "<b>Input: predicate </b>Custom simple predicate search (e.g. title like '%galex%')<br><b>Input: standard capability </b>conesearch, SimpleImageAccess, SimpleSpectralAccess<br><b>Output:</b> VOTable 1.1 Unique Resource per Row")]
        public ivoa.altVOTable.VOTABLE VOTCapabilityPredicate(string predicate, string capability)
        {
            return VOTCapBandPredOpt(predicate, capability, string.Empty, 1);
        }

        public ivoa.altVOTable.VOTABLE VOTCapBandPredOptCache(string predicate, string capability, string waveband, int VOTStyleOption)
        {
            registry.Registry reg = new registry.Registry();
            return reg.QueryCapBandPredicateResourceCache(predicate, capability, waveband, VOTStyleOption);
        }

        [WebMethod(Description = "<b>Input: predicate </b>Custom simple predicate search (e.g. title like '%galex%')<br><b>Input: standard capability </b>conesearch, SimpleImageAccess, SimpleSpectralAccess<br><b>Input: VOTStyleOption </b> 1 = Unique Resource/Row, 2 = Unique Interface/Row<br><b>Output:</b>VOTable 1.1 with Fields set by VOTStyleOption")]
        public ivoa.altVOTable.VOTABLE VOTCapabilityPredOpt(string predicate, string capability, int VOTStyleOption)
        {
            return VOTCapBandPredOpt(predicate, capability, string.Empty, VOTStyleOption);
        }

        [WebMethod(Description = "<b>Input: predicate </b>Custom simple predicate search (e.g. title like '%galex%')<br><b>Input: standard capability </b>conesearch, SimpleImageAccess, SimpleSpectralAccess<br><b>Input: waveband </b>Optical,UV,x-ray,EUV,radio,Infrared,Gamma-ray,Millimeter<br><b>Input: VOTStyleOption </b>1 = Unique Resource/Row, 2 = Unique Interface/Row<br><b>Output:</b> VOTable 1.1 with Fields set by VOTStyleOption")]
        public ivoa.altVOTable.VOTABLE VOTCapBandPredOpt(string predicate, string capability, string waveband, int VOTStyleOption)
        {
            // Query the resources from the registry 
            registry.Registry reg = new registry.Registry();
            System.Xml.XmlDocument[] resArr = null;

            if (predicate.Length > 0)
                predicate = EnsureSafeSQLStatement(predicate);

            if (useCache)
                return VOTCapBandPredOptCache(predicate, capability, waveband, VOTStyleOption);


            resArr = reg.QueryCapBandResourceXMLDoc(predicate, capability, waveband);

            StringBuilder vres = new StringBuilder();
            vres.AppendLine("<VOResources more=\"false\" xmlns=\"http://www.ivoa.net/xml/RegistryInterface/v1.0\">");
            foreach (System.Xml.XmlDocument doc in resArr)
            {
                //Some necessary namespace cleanup we were doing in serialisation/deserialisation
                if (doc != null)
                {
                    string inner = doc.InnerXml;
                    int endtag = inner.IndexOf(">", inner.IndexOf("Resource"));

                    if (inner.StartsWith("<?"))
                        inner = inner.Substring(inner.IndexOf(">") + 1);
                    if (inner.IndexOf("xmlns=\"\"") == -1 || inner.IndexOf("xmlns=\"\"") > endtag)
                        inner = inner.Replace("Resource ", "Resource xmlns=\"\" ");

                    vres.Append(inner);
                }
            }
            vres.AppendLine("</VOResources>");

            StringReader srVOT = new StringReader(String.Empty);
            XmlSerializer serVOT = new XmlSerializer(typeof(ivoa.altVOTable.VOTABLE));
            try
            {
                // Create reader for the doc.
                StringReader reader = new StringReader(vres.ToString());
                XPathDocument myXPathDoc = new XPathDocument(reader);
                XslCompiledTransform myXslTrans = new XslCompiledTransform();

                /*StreamWriter sr = new StreamWriter("c:\\temp\\resources.xml");
                sr.Write(vres.ToString());
                sr.Flush();
                sr.Close();*/

                //load the Xsl and transform to VOTABLE
                StringBuilder sbVOT = new StringBuilder();
                StringWriter swVOT = new StringWriter(sbVOT);

                // Select which Output Style you would like with input web Parameter for stylesheet
                String resultsStyle = null;
                if (VOTStyleOption == 1)
                {
                    resultsStyle = "xsl\\VOTableResults_multiple_resources.xsl";
                }
                else
                {
                    resultsStyle = "xsl\\VOTableInterfaceResults_multiple_resources.xsl";
                }
                myXslTrans.Load( appDir + resultsStyle);

                XmlTextWriter writer = new XmlTextWriter(swVOT);
                myXslTrans.Transform(myXPathDoc, null, writer);

                srVOT = new StringReader(sbVOT.ToString());
            }
            catch (Exception e)
            {
                e.ToString();
            }

            return (VOTABLE)serVOT.Deserialize(srVOT);
        }


        //Replacement method for VOTKeyword does not use any direct serialization or deserialization of .NET Resource objects.
        //This allows us to handle non-standard resources as will be produced by AstroGrid.
        [WebMethod(Description = "<b>Input: keywords </b> enter text keywords (e.g. galex, redshift, binary star,...)<br><b>Input: andKeys </b> true = AND, false = OR<br><b>Output:</b> VOTable 1.1 Unique Resource per Row")]
        public ivoa.altVOTable.VOTABLE VOTKeyword(string keywords, bool andKeys)
        {
            keywords = EnsureSafeSQLStatement(keywords);

            if (useCache)
                return VOTKeywordCache(keywords.Trim(), andKeys, 1);

            registry.Registry reg = new registry.Registry();

            System.Xml.XmlDocument[] resArr = null;
            resArr = reg.QueryRIResourceRankedXMLDoc(keywords.Trim(), andKeys);

            StringBuilder vres = new StringBuilder();
            vres.AppendLine("<VOResources more=\"false\" xmlns=\"http://www.ivoa.net/xml/RegistryInterface/v1.0\">");
            foreach (System.Xml.XmlDocument doc in resArr)
            {
                //Some necessary namespace cleanup we were doing in serialisation/deserialisation
                if (doc != null)
                {
                    string inner = doc.InnerXml;
                    int endtag = inner.IndexOf(">", inner.IndexOf("Resource"));

                    if (inner.StartsWith("<?"))
                        inner = inner.Substring(inner.IndexOf(">") + 1);
                    if (inner.IndexOf("xmlns=\"\"") == -1 || inner.IndexOf("xmlns=\"\"") > endtag)
                        inner = inner.Replace("Resource ", "Resource xmlns=\"\" ");

                    vres.Append(inner);
                }
            }
            vres.AppendLine("</VOResources>");

            StringReader srVOT = new StringReader(String.Empty);
            XmlSerializer serVOT = new XmlSerializer(typeof(ivoa.altVOTable.VOTABLE)); 
            try
            {
                // Create reader for the doc.
                StringReader reader = new StringReader(vres.ToString());
                XPathDocument myXPathDoc = new XPathDocument(reader);
                XslCompiledTransform myXslTrans = new XslCompiledTransform();

                //load the Xsl and transform to VOTABLE
                StringBuilder sbVOT = new StringBuilder();
                StringWriter swVOT = new StringWriter(sbVOT);
                myXslTrans.Load( appDir + "xsl\\VOTableResults_multiple_resources.xsl");

                XmlTextWriter writer = new XmlTextWriter(swVOT);
                myXslTrans.Transform(myXPathDoc, null, writer);

                srVOT = new StringReader(sbVOT.ToString());
            }
            catch (Exception e)
            {
                e.ToString();
            }

            return (VOTABLE)serVOT.Deserialize(srVOT);
        }


        private ivoa.altVOTable.VOTABLE VOTKeywordCache(string keywords, bool andKeys, int option)
        {
            registry.Registry reg = new registry.Registry();
            return reg.SqlQueryRI10ResourceCache(keywords, andKeys, option);
        }

        //Replacement method for VOTKeyword does not use any direct serialization or deserialization of .NET Resource objects.
        //This allows us to handle non-standard resources as will be produced by AstroGrid.
        [WebMethod(Description = "<b>Input: keywords </b> enter text keywords (e.g. galex, redshift, binary star,...)<br><b>Input: andKeys </b> true = AND, false = OR<br><b>Input: VOTStyleOption </b>1 = Unique Resource/Row, 2 = Unique Interface/Row<br><b>Output:</b> VOTable 1.1 with Fields set by VOTStyleOption")]
        public ivoa.altVOTable.VOTABLE VOTKeyOpt(string keywords, bool andKeys, int VOTStyleOption)
        {
            if (useCache)
                return VOTKeywordCache(keywords.Trim(), andKeys, VOTStyleOption);

            System.Xml.XmlDocument[] resArr = null;
            registry.Registry reg = new registry.Registry();
            resArr = reg.QueryRIResourceRankedXMLDoc(keywords, andKeys);

            StringBuilder vres = new StringBuilder();
            vres.AppendLine("<VOResources more=\"false\" xmlns=\"http://www.ivoa.net/xml/RegistryInterface/v1.0\">");
            foreach (System.Xml.XmlDocument doc in resArr)
            {
                //Some necessary namespace cleanup we were doing in serialisation/deserialisation
                if (doc != null)
                {
                    string inner = doc.InnerXml;
                    int endtag = inner.IndexOf(">", inner.IndexOf("Resource"));

                    if (inner.StartsWith("<?"))
                        inner = inner.Substring(inner.IndexOf(">") + 1);
                    if (inner.IndexOf("xmlns=\"\"") == -1 || inner.IndexOf("xmlns=\"\"") > endtag)
                        inner = inner.Replace("Resource ", "Resource xmlns=\"\" ");

                    vres.Append(inner);
                }
            }
            vres.AppendLine("</VOResources>");

            StringReader srVOT = new StringReader(String.Empty);
            XmlSerializer serVOT = new XmlSerializer(typeof(ivoa.altVOTable.VOTABLE)); 
            try
            {
                // Create reader for the doc.
                StringReader reader = new StringReader(vres.ToString());
                XPathDocument myXPathDoc = new XPathDocument(reader);
                XslCompiledTransform myXslTrans = new XslCompiledTransform();

                //load the Xsl and transform to VOTABLE
                StringBuilder sbVOT = new StringBuilder();
                StringWriter swVOT = new StringWriter(sbVOT);
                // Select which Output Style you would like with input web Parameter for stylesheet
                String resultsStyle = null;
                if (VOTStyleOption == 1)
                {
                    resultsStyle = "xsl\\VOTableResults_multiple_resources.xsl";
                }
                else
                {
                    resultsStyle = "xsl\\VOTableInterfaceResults_multiple_resources.xsl";
                }
                myXslTrans.Load( appendUrl(appDir + resultsStyle, resultsStyle));
                
                XmlTextWriter writer = new XmlTextWriter(swVOT);
                myXslTrans.Transform(myXPathDoc, null, writer);

                srVOT = new StringReader(sbVOT.ToString());
    }
            catch (Exception e)
            {
                e.ToString();
            }

            return (VOTABLE)serVOT.Deserialize(srVOT);
        }
    }


}