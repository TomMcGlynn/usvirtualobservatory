using System;
using System.Data;
using System.Xml;
using System.Xml.Serialization;
using System.IO;

namespace registry
{

    //splitting out objectMaker, this will change dramatically with new schema and may be deleted
    public class ResourceMaker
    {
        public static ivoa.net.ri1_0.server.Resource CreateRI10Resource(DataRow dr)
        {
            ivoa.net.ri1_0.server.Resource vor = null;
            String strVOR = (string)dr["xml"];
            if (strVOR != null)
            {
                try
                {
                    StringReader srdr = new StringReader(strVOR);
                    XmlTextReader rdr = new XmlTextReader(srdr);
                    XmlSerializer ser = new XmlSerializer(typeof(ivoa.net.ri1_0.server.Resource), "http://www.ivoa.net/xml/RegistryInterface/v1.0");

                    object o = ser.Deserialize(rdr);
                    vor = o as ivoa.net.ri1_0.server.Resource;
                }
                catch (System.InvalidOperationException) //alter XML -- add namespaces that are probably missing.
                {
                    strVOR = AddNamespaces(strVOR);
                    StringReader srdr = new StringReader(strVOR);
                    XmlTextReader rdr = new XmlTextReader(srdr);
                    XmlSerializer ser = new XmlSerializer(typeof(ivoa.net.ri1_0.server.Resource), "http://www.ivoa.net/xml/RegistryInterface/v1.0");

                    object o = ser.Deserialize(rdr);
                    vor = o as ivoa.net.ri1_0.server.Resource;
                }
            }
            else
            {
                vor = new ivoa.net.ri1_0.server.Resource();
            }
            return vor;
        }

        private static string AddNamespaces(string strVOR)
        {
            int nsindex = strVOR.IndexOf('>');
            if (strVOR.IndexOf("vg", 0, nsindex) > -1 && strVOR.IndexOf("xmlns:vg", 0, nsindex) == -1)
            {
                strVOR = strVOR.Insert(nsindex, " xmlns:vg=\"http://www.ivoa.net/xml/VORegistry/v1.0\" ");
                nsindex = strVOR.IndexOf('>');
            }
            if (strVOR.IndexOf("vs", 0, nsindex) > -1 && strVOR.IndexOf("xmlns:vs", 0, nsindex) == -1)
            {
                strVOR = strVOR.Insert(nsindex, " xmlns:vs=\"http://www.ivoa.net/xml/VODataService/v1.0\" ");
                nsindex = strVOR.IndexOf('>');
            }

            //vr - may be found in capabilities as well
            if (strVOR.IndexOf("vr") > -1 && strVOR.IndexOf("xmlns:vr", 0, nsindex) == -1)
            {
                strVOR = strVOR.Insert(nsindex, " xmlns:vr=\"http://www.ivoa.net/xml/VOResource/v1.0\" ");
                nsindex = strVOR.IndexOf('>');
            }
            //cs, ssa, sia, etc as needed.
            if (strVOR.IndexOf("cs:") > -1 && strVOR.IndexOf(":cs", 0, nsindex) == -1)
            {
                strVOR = strVOR.Insert(nsindex, " xmlns:cs=\"http://www.ivoa.net/xml/ConeSearch/v1.0\" ");
                nsindex = strVOR.IndexOf('>');
            }
            if (strVOR.IndexOf("sia:") > -1 && strVOR.IndexOf(":sia", 0, nsindex) == -1)
            {
                strVOR = strVOR.Insert(nsindex, " xmlns:sia=\"http://www.ivoa.net/xml/SIA/v1.0\" ");
                nsindex = strVOR.IndexOf('>');
            }
             if (strVOR.IndexOf("tr:") > -1 && strVOR.IndexOf(":tr", 0, nsindex) == -1)
            {
                strVOR = strVOR.Insert(nsindex, " xmlns:tr=\"http://www.ivoa.net/xml/TAPRegExt/v1.0");
                nsindex = strVOR.IndexOf('>');
            }
            if (strVOR.IndexOf("ssa:") > -1 && strVOR.IndexOf(":ssa", 0, nsindex) == -1)
            {
                strVOR = strVOR.Insert(nsindex, " xmlns:ssa=\"http://www.ivoa.net/xml/SSA/v0.4\" ");
                nsindex = strVOR.IndexOf('>');
            }
            if (strVOR.Contains("http://www.ivoa.net/xml/SSA/v1.02"))
            {
                strVOR = strVOR.Replace("http://www.ivoa.net/xml/SSA/v1.02", "http://www.ivoa.net/xml/SSA/v0.4");
                nsindex = strVOR.IndexOf('>');
            }

            return strVOR;
        }
    }
}
