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

using ivoa.net.ri1_0.server;

using System.Xml.XPath;
using System.Xml.Xsl;


namespace registryInterface
{

    [System.Web.Services.WebService(Namespace = "ivoa.net.riws.v10")]
    [WebServiceBinding(ConformsTo = WsiProfiles.BasicProfile1_1)]
    public class RIStandardService : IRegistrySearchSOAP
    {
        private static string baseURL = registry.Properties.Settings.Default.baseURL;

        public RIStandardService()
        {
        }

        private static string appendUrl(string urlbase, string last)
        {
            if (urlbase.EndsWith("/"))
                return urlbase + last;
            else
                return urlbase + '/' + last;
        }

        #region IRegistrySearchSOAP Members

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
        //Testing - putting this here to use the new xsl file.
        [WebMethod(Description = "Placeholder ADQL function")]
        SearchResponse IRegistrySearchSOAP.Search(Search Search1)
        {
            XmlSerializer ser = new XmlSerializer(typeof(whereType));
            StringBuilder sb = new StringBuilder();
            StringWriter sw = new StringWriter(sb);
            ser.Serialize(sw, Search1.Where);
            sw.Close();
            string xml = sb.ToString().Replace("whereType", "Where");

            //This is hackish code to re-insert definitions that may have been up
            //at the soap level. The xsl translation will need them.
            #region namespace wrangling
            if (xml.IndexOf("xmlns=") < xml.IndexOf('>', xml.IndexOf("Where") ))
                xml = xml.Replace("xmlns=\"http://www.ivoa.net/wsdl/RegistrySearch/v1.0\"", "xmlns=\"http://www.ivoa.net/xml/ADQL/v1.0\" ");
            else
            {
                int index = xml.IndexOf("xmlns:");
                xml = xml.Insert(index, "xmlns=\"http://www.ivoa.net/xml/ADQL/v1.0\" ");
            }
            if (!xml.Contains("http://www.w3.org/2001/XMLSchema-instance"))
            {
                int index = xml.IndexOf("xmlns=");
                xml = xml.Insert(index, "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" ");
            }
            #endregion

            StringReader reader = new StringReader(xml);
            XPathDocument myXPathDoc = new XPathDocument(reader);
            XslCompiledTransform myXslTrans = new XslCompiledTransform();

            //load the Xsl and transform
            //note - this is an edit to the old file
            StringBuilder sbVOT = new StringBuilder();
            StringWriter swVOT = new StringWriter(sbVOT);
            myXslTrans.Load( appendUrl(baseURL, "xsl/ADQLx2SQLServer_Registry_v1.0.xsl"));
            XmlTextWriter writer = new XmlTextWriter(swVOT);
            myXslTrans.Transform(myXPathDoc, null, writer);

            //check for SQL inject, write etc.
            //todo - Brian's stored procedure
            string rawSQL = EnsureSafeSQLStatement(sbVOT.ToString());

            // First query the resources from the registry 
            registry.Registry reg = new registry.Registry();
            ivoa.net.ri1_0.server.Resource[] resArr = null;
            resArr = reg.SQLQueryRI10Resource(rawSQL);
            object[] vorReses = new Object[resArr.Length] as ivoa.net.ri1_0.server.Resource[];

            #region generate results from returned objects
            int max = 0;
            int from = 0;
            bool more = false;
            try //interpret negative and non-numbers as default values.
            {
                if (Search1.max != null && Search1.max.Length > 0)
                    max = Math.Max(Convert.ToInt32(Search1.max), 0);
                if (Search1.from != null && Search1.from.Length > 0)
                    from = Math.Max(Convert.ToInt32(Search1.from), 1) - 1; //1-based, not 0-based.
            }
            catch (System.FormatException)
            {
                max = from = 0;
            }

            if (from > resArr.Length)
            {
                vorReses = new ivoa.net.ri1_0.server.Resource[0];
            }
            else
            {
                 if ((max == 0) || (max + from > resArr.Length))
                     max = resArr.Length - from;

                 if (max + from < resArr.Length)
                     more = true;

                vorReses = new object[max];
                Array.Copy(resArr, from, vorReses, 0, max);
            }

            SearchResponse sr = new SearchResponse();
            VOResources vres = new VOResources();
            if (Search1.identifiersOnlySpecified == true && Search1.identifiersOnly == true)
            {
                string[] identifiers = new string[vorReses.Length];
                for (int i = 0; i < vorReses.Length; ++i)
                {
                    identifiers[i] = resArr[i].identifier;
                }
                vres.Items = identifiers;
            }
            else
            {
                vres.Items = vorReses;
            }
            #endregion

            vres.numberReturned = vorReses.Length.ToString();
            vres.more = more;

            sr.VOResources = vres;

            return sr;
        }


        [WebMethod(Description="Placeholder ADQL function")]
        //SearchResponse IRegistrySearchSOAP.Search(Search Search1)
        SearchResponse SearchOld(Search Search1)
        {
            XmlSerializer ser = new XmlSerializer(typeof(Search));
            StringBuilder sb = new StringBuilder();
            StringWriter sw = new StringWriter(sb);
            ser.Serialize(sw, Search1);
            sw.Close();

            string xml = sb.ToString();

            //This is hackish code to re-insert definitions that may have been up
            //at the soap level. The xsl translation will need them.
            #region namespace wrangling
            int startSearch = xml.IndexOf("<", 1);
            string search = xml.Substring(startSearch, xml.IndexOf(">", startSearch) - startSearch);
            if (!search.Contains("xmlns="))
            {
                int index = xml.IndexOf("xmlns:");
                xml = xml.Insert(index, "xmlns=\"http://www.ivoa.net/wsdl/RegistrySearch/v1.0\" ");
            }
            if( !search.Contains("http://www.w3.org/2001/XMLSchema-instance"))
            {
                int index = xml.IndexOf("xmlns=");
                xml = xml.Insert(index, "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" ");
            }

            //we may not have to deal with the SR thing because of how .NET will interpret
            //incoming SOAP messages. Test after java client fixed.
            if( !search.Contains("xmlns:rs=\"http://www.ivoa.net/wsdl/RegistrySearch/v1.0\"") &&
                !search.Contains("xmlns:sr=\"http://www.ivoa.net/wsdl/RegistrySearch/v1.0\"" ))
            {
                int index = xml.IndexOf("xmlns=");
                xml = xml.Insert(index, "xmlns:rs=\"http://www.ivoa.net/wsdl/RegistrySearch/v1.0\" ");
            }
            //test
            xml = xml.Replace("<Search", "<rs:Search");
            xml = xml.Replace("</Search", "</rs:Search");
            xml = xml.Replace("<Where", "<rs:Where");
            xml = xml.Replace("</Where", "</rs:Where");
            xml = System.Text.RegularExpressions.Regex.Replace(xml, "sr:", "rs:");
            #endregion

            StringReader reader = new StringReader(xml);
            XPathDocument myXPathDoc = new XPathDocument(reader);
            XslCompiledTransform myXslTrans = new XslCompiledTransform();

            //load the Xsl and transform
            //note - this is an edit to the old file
            StringBuilder sbVOT = new StringBuilder();
            StringWriter swVOT = new StringWriter(sbVOT);
            myXslTrans.Load(appendUrl(baseURL,"xsl/ADQLx2s-v1.0_temp.xsl"));
            XmlTextWriter writer = new XmlTextWriter(swVOT);
            myXslTrans.Transform(myXPathDoc, null, writer);
            string rawSQL = sbVOT.ToString();
            if (rawSQL.Contains("WHERE"))
                rawSQL = rawSQL.Substring(rawSQL.IndexOf("WHERE") + 6); //predicate is past the end of "where "
            else
                rawSQL = String.Empty;

            // First query the resources from the registry 
            registry.Registry reg = new registry.Registry();
            ivoa.net.ri1_0.server.Resource[] resArr = null;
            resArr = reg.QueryXMLRIResource(rawSQL);
            object[] vorReses = new Object[resArr.Length] as ivoa.net.ri1_0.server.Resource[];
            vorReses = resArr;
            VOResources vres = new VOResources();
            vres.Items = vorReses;
            vres.numberReturned = resArr.Length.ToString();
            SearchResponse sr = new SearchResponse();
            sr.VOResources = vres;
            return sr;
        }

        [WebMethod(Description="Searches entire resource description for ORed keywords. Only 'keywords' parameter used.")]
        SearchResponse IRegistrySearchSOAP.KeywordSearch(KeywordSearch KeywordSearch1)
        {

            string q = SQLHelper.createKeyWordStatement(KeywordSearch1.keywords, true);

            // First query the resources from the registry 
            registry.Registry reg = new registry.Registry();
            ivoa.net.ri1_0.server.Resource[] resArr = null;
            resArr = reg.QueryXMLRIResource(q);
            object[] vorReses = new Object[resArr.Length] as ivoa.net.ri1_0.server.Resource[];

            int max = 0;
            int from = 0;
            bool more = false;
            try //interpret negative and non-numbers as default values.
            {
                if (KeywordSearch1.max != null && KeywordSearch1.max.Length > 0)
                    max = Math.Max(Convert.ToInt32(KeywordSearch1.max), 0);
                if (KeywordSearch1.from != null && KeywordSearch1.from.Length > 0)
                    from = Math.Max(Convert.ToInt32(KeywordSearch1.from), 1) - 1; //1-based, not 0-based.
            }
            catch (System.FormatException)
            {
                max = from = 0;
            }

            if (from > resArr.Length)
            {
                vorReses = new ivoa.net.ri1_0.server.Resource[0];
            }
            else
            {
                if ((max == 0) || (max + from > resArr.Length))
                    max = resArr.Length - from;

                if (max + from < resArr.Length)
                    more = true;

                vorReses = new object[max];
                Array.Copy(resArr, from, vorReses, 0, max);
            }

            VOResources vres = new VOResources();
            if (KeywordSearch1.identifiersOnlySpecified == true && KeywordSearch1.identifiersOnly == true)
            {
                string[] identifiers = new string[vorReses.Length];
                for (int i = 0; i < vorReses.Length; ++i)
                {
                    identifiers[i] = resArr[i].identifier;
                }
                vres.Items = identifiers;
            }
            else
            {
                vres.Items = vorReses;
            }
            vres.numberReturned = vorReses.Length.ToString();
            vres.more = more;

            SearchResponse sr = new SearchResponse();
            sr.VOResources = vres;

            return sr;
        }

        [WebMethod(Description = "Returns a resource given an IVOA identifier.")]
        ResolveResponse IRegistrySearchSOAP.GetResource(GetResource GetResource1)
        {
            ResolveResponse response = new ResolveResponse();

            registry.Registry reg = new registry.Registry();
            ivoa.net.ri1_0.server.Resource[] reses = reg.QueryFullVOR10Resource("identifier='" + GetResource1.identifier + "'");
            if (reses.Length > 0)
                response.Resource = (Resource)reses[0];

            return response;
        }

        [WebMethod(Description = "Gets the Resource record for the registry itself.")]
        ResolveResponse IRegistrySearchSOAP.GetIdentity()
        {
            ResolveResponse response = new ResolveResponse();

            registry.Registry reg = new registry.Registry();
            ivoa.net.ri1_0.server.Resource[] reses = reg.QueryFullVOR10Resource("xsi_type like '%Registry' and (harvestedFrom is null or harvestedFrom = '' or harvestedFrom like 'STScI%') and [@status] = 1");
            if (reses.Length > 0)
                response.Resource = (Resource)reses[0];

            return response;
        }

        System.Xml.XmlElement IRegistrySearchSOAP.XQuerySearch(XQuerySearch XQuerySearch1)
        {
            throw new Exception("The method or operation is not implemented.");
        }
        #endregion

        #region Test methods for IRegistrySearchSOAP Members
        [WebMethod(Description = "Tests KeywordSearch function. From and Max must be integers or empty")]
        public SearchResponse TestKeywords(string keywords, string from, string max)
        {
            KeywordSearch search = new KeywordSearch();
            search.keywords = keywords;
            search.orValues = false;

            search.from = from;
            search.max = max;

            return ((IRegistrySearchSOAP)this).KeywordSearch(search);
        }

        [WebMethod(Description = "Tests GetResource function.")]
        public ResolveResponse TestGetResource(string identifier)
        {
            GetResource get = new GetResource();
            get.identifier = identifier;

            return ((IRegistrySearchSOAP)this).GetResource(get);
        }

        [WebMethod(Description = "Tests Search function. Assumes From and Max are integers or empty.")]
        public SearchResponse TestSearch(string from, string max)
        {
            Search search = new Search();
            search.Where = new whereType();

            //closedSearchType closed = new closedSearchType();
            likePredType like = new likePredType();
            stringType str = new stringType();
            str.Value = "%simplespectral%";
            like.Pattern = new atomType();
            like.Pattern.Literal = str;
            columnReferenceType col = new columnReferenceType();
            col.Name = "capability/@xsi:type";
            col.xpathName = "capability/@xsi:type";
            like.Arg = col;
            
            //closed.Condition = like;
            //search.Where.Condition = closed;
            search.Where.Condition = like;

            //search.identifiersOnlySpecified = true;
            //search.identifiersOnly = true;

            search.max = max;
            search.from = from;

            return ((IRegistrySearchSOAP)this).Search(search);
        }


        #endregion
    }
}