
using System;
using System.Collections;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Web;
using System.Web.SessionState;
using System.Web.UI;
using System.Web.UI.WebControls;
using System.Web.UI.HtmlControls;
using System.Text;
using System.IO;
using System.Net;
using System.Xml;
using System.Xml.Serialization;

using tapLib.Config;

namespace TAPService.VOSI
{
    public class VOSIRequestHandler
    {
        internal static System.DateTime startTime = System.DateTime.UtcNow;
        public static string baseVOSIURL = (string)System.Configuration.ConfigurationSettings.AppSettings["baseVOSIURL"];
        public static string baseURL = (string)System.Configuration.ConfigurationSettings.AppSettings["baseURL"];
        public static string vDir = (string)System.Configuration.ConfigurationSettings.AppSettings["vDir"];
        internal static string fileName = (string)System.Configuration.ConfigurationSettings.AppSettings["tableConfigFile"];

        public Object HandleRequest(System.Type request)
        {
            TapConfiguration.setConfigFilePath(vDir + "\\" + fileName);
            
            //tdower todo we can do a lot in the web config for availability, planned downtime, etc.

            if (request == typeof(TAPService.VOSI.availability))
            {
                Availability avail = new Availability();
                avail.downAtSpecified = false;
                avail.backAtSpecified = false;

                if (TapConfiguration.Instance.TestDBConnections())
                {
                    avail.available = true;
                    avail.upSince = startTime;
                    avail.upSinceSpecified = true;
                }
                else
                {
                    avail.available = false;
                    avail.upSinceSpecified = false;
                }

                return avail;
            }
            else if (request == typeof(TAPService.VOSI.capabilities))
            {
                //tdower todo - once this is registered, get it from the resource.
                //tdower todo - we also have vr resource classes now.

                StringBuilder response = new StringBuilder();
                response.Append(@"<?xml version='1.0' encoding='utf-8'?>
    <vosi:capabilities
        xmlns:vosi='http://www.ivoa.net/xml/VOSICapabilities/v1.0'
        xmlns:vr='http://www.ivoa.net/xml/VOResource/v1.0'
        xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'
        xsi:schemaLocation= 'http://www.ivoa.net/xml/VOResource/v1.0'>");

                //TAP capability
                response.Append(@"<vr:capability standardID='ivo://ivoa.net/std/TAP'>
                                  <interface xsi:type='vs:ParamHTTP' role='std'>
                                    <accessURL use='full'>");
                response.Append(baseURL);
                response.Append(@"</accessURL>
                                  </interface>
                                </vr:capability>");

                //VOSI capabilities
                response.Append(@"<vr:capability standardID='ivo://ivoa.net/std/VOSI#capabilities'>
                                  <interface xsi:type='vs:ParamHTTP' role='std'>
                                    <accessURL use='full'>");
                response.Append(baseVOSIURL + "capabilities.aspx");
                response.Append(@"</accessURL>
                                  </interface>
                                </vr:capability>");
                response.Append(@"<vr:capability standardID='ivo://ivoa.net/std/VOSI#availability'>
                                  <interface xsi:type='vs:ParamHTTP' role='std'>
                                    <accessURL use='full'>");
                response.Append(baseVOSIURL + "availability.aspx");
                response.Append(@"</accessURL>
                                  </interface>
                                </vr:capability>");
                response.Append(@"<vr:capability standardID='ivo://ivoa.net/std/VOSI#tables'>
                                  <interface xsi:type='vs:ParamHTTP' role='std'>
                                    <accessURL use='full'>");
                response.Append(baseVOSIURL + "tables.aspx");
                response.Append(@"</accessURL>
                                  </interface>
                                </vr:capability>");
                response.Append("</vosi:capabilities>");
                return response.ToString();
            }
            else if (request == typeof(TAPService.VOSI.tables))
            {
                DataSet tables = new DataSet();
                DataSet columns = new DataSet();
                TapConfiguration.Instance.ExecuteVOSIConfigQuery(ref tables, ref columns);
                TableSet ts = TAPService.VOSI.tables.ConvertDataSetsToTableSet(tables, columns);

                return ts;
            }
            return null;
        }
    }
}