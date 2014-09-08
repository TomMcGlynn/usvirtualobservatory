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
    public class RIWebService : IRegistrySearchSOAP
    {
        public RIWebService()
        {
        }

        #region IRegistrySearchSOAP Members

        [WebMethod(Description = "Not implemented.")]
        public ResolveResponse GetIdentity()
        {
            throw new Exception("The method or operation is not implemented.");
        }

        [WebMethod(Description = "Not implemented.")]
        public ResolveResponse GetResource(GetResource GetResource1)
        {
            throw new Exception("The method or operation is not implemented.");
        }

        [WebMethod]
        public SearchResponse KeywordSearch(KeywordSearch keywords)
        {
            KeywordSearch k = new KeywordSearch();
            k.keywords = keywords.keywords;

            string q = SQLHelper.createKeyWordStatement(k.keywords, true);

            // First query the resources from the registry 
            registry.Registry reg = new registry.Registry();
            ivoa.net.ri1_0.server.Resource[] resArr = null;
            resArr = reg.QueryXMLRIResource(q);

            object[] vorReses = new Object[resArr.Length] as ivoa.net.ri1_0.server.Resource[];
            vorReses = resArr;

            VOResources vres = new VOResources();
            vres.Items = vorReses;

            SearchResponse sr = new SearchResponse();
            sr.VOResources = vres;

            return sr;
        }

        [WebMethod(Description = "Standard RI1.0 keyword String search")]
        public SearchResponse KeywordStringSearch(string keywords)
        {

            string q = SQLHelper.createKeyWordStatement(keywords, true);

            // First query the resources from the registry 
            registry.Registry reg = new registry.Registry();
            ivoa.net.ri1_0.server.Resource[] resArr = null;
            resArr = reg.QueryXMLRIResource(q);

            object[] vorReses = new Object[resArr.Length] as ivoa.net.ri1_0.server.Resource[];
            vorReses = resArr;

            VOResources vres = new VOResources();
            vres.Items = vorReses;

            SearchResponse sr = new SearchResponse();
            sr.VOResources = vres;

            return sr;
        }

        [WebMethod(Description = "Custom simple predicate search (placeholder for ADQL imp) Returns standard SearchResponse RI1.0")]
        public SearchResponse VORPredicate(string predicate)
        {
            // We have 2 namespaces that both contain resource objects,  
            // need to think through for only 1 namespace

            // First query the resources from the registry 
            registry.Registry reg = new registry.Registry();
            ivoa.net.ri1_0.server.Resource[] resArr = null;
            resArr = reg.QueryXMLRIResource(predicate);      

            object[] vorReses = new Object[resArr.Length] as ivoa.net.ri1_0.server.Resource[];
            vorReses = resArr;

            VOResources vres = new VOResources();
            vres.Items = vorReses;

            SearchResponse sr = new SearchResponse();
            sr.VOResources = vres;

            return sr;

        }

        [WebMethod(Description = "Not implemented.")]
        public SearchResponse Search(Search Search1)
        {
            throw new Exception("The method or operation is not implemented.");
        }


        [WebMethod(Description = "Not implemented.")]
        public System.Xml.XmlElement XQuerySearch(XQuerySearch XQuerySearch1)
        {
            throw new Exception("The method or operation is not implemented.");
        }

        #endregion
    }
}