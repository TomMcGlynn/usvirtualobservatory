using System.Xml.Xsl;
using System.Xml;
using System.Xml.XPath;
using System;
using System.IO;
using System.Text;

namespace registry
{
	/// <summary>
	/// Summary description for votxslt.
	/// </summary>
	public class votxslt
	{
		static string regionXsl =System.Configuration.ConfigurationSettings.AppSettings["regionXsl"];

		public votxslt()
		{
			//
			// TODO: Add constructor logic here
			//
		}

		public static string transformRegion(string region){
			if (regionXsl ==null) regionXsl="region.xsl";
			try 
			{
				StringReader reader = new StringReader(region);
				XPathDocument myXPathDoc = new XPathDocument(reader) ;
				XslTransform myXslTrans = new XslTransform() ;
            
				//load the Xsl 
				StringBuilder sb = new StringBuilder();
				StringWriter sw = new StringWriter(sb);
				myXslTrans.Load(regionXsl) ;
				XmlTextWriter writer = new XmlTextWriter(sw);
				myXslTrans.Transform(myXPathDoc,null,writer);
				return sb.ToString();
			}
			catch (Exception) 
			{
				try 
				{
					StringReader reader = new StringReader("<root>"+region+"</root>");
					XPathDocument myXPathDoc = new XPathDocument(reader) ;
					XslTransform myXslTrans = new XslTransform() ;
            
					//load the Xsl 
					StringBuilder sb = new StringBuilder();
					StringWriter sw = new StringWriter(sb);
					myXslTrans.Load(regionXsl) ;
					XmlTextWriter writer = new XmlTextWriter(sw);
					myXslTrans.Transform(myXPathDoc,null,writer);
					return sb.ToString();

				} 
				catch (Exception ex)
				{
					return region + " " + ex.Message;
				}
			}
		} 

		public static void transform(string url,TextWriter oloc,string xslFile) 
		{
			string defXslFile =System.Configuration.ConfigurationSettings.AppSettings["votableXsl"];
			if (xslFile == null) 
			{
				if (defXslFile == null) 
					xslFile = "votable.xsl";
				else 
					xslFile= defXslFile;
			}
			//load the Xml doc
			
			XPathDocument myXPathDoc = new XPathDocument(url) ;
			XslTransform myXslTrans = new XslTransform() ;
            
			//load the Xsl 
			myXslTrans.Load(xslFile) ;
			XmlTextWriter writer = new XmlTextWriter(oloc);
			myXslTrans.Transform(myXPathDoc,null,writer);
            

		}
	}
}
