using System;
using System.Collections;
using System.ComponentModel;
using System.Data;
using System.Diagnostics;
using System.Web;
using System.Web.Services;
using System.Xml;
using System.Xml.Serialization;
using System.IO;
using System.Text;
using System.Data.SqlClient;
using oai;
using oai_dc;
using net.ivoa;
using ivoa.net.ri1_0.server;

namespace registry
{
	/// <summary>
	/// OAI Services for STScI/JHU NVO Registry
	/// </summary>
 	///Current version
	///ID:		$Id: STOAI.asmx.cs,v 1.9 2006/02/28 17:09:49 grgreene Exp $
	///Revision:	$Revision: 1.9 $
	///Date:	$Date: 2006/02/28 17:09:49 $
	///
	[System.Web.Services.WebService(Namespace="http://www.openarchives.org/OAI/2.0/")]
	public class STOAI : System.Web.Services.WebService
	{
        private static string baseURL = Properties.Settings.Default.baseURL;

        private static int resumptionTokenLimit = Properties.Settings.Default.resumptionTokenLimit;

        private static string rqtValue = appendUrl(baseURL,"oai.aspx");
        private static string managedSet = "ivo_managed";

        private static long tokenCounter = 1;
        private static Object counterLock = new Object();

        public static string earliestDatestamp = "2000-01-01T00:00:00Z";

		public STOAI()
		{
			//CODEGEN: This call is required by the ASP.NET Web Services Designer
			InitializeComponent();
			// make sure registry is inited
			new Registry();
		}

        private static string appendUrl(string urlbase, string last)
        {
            if (urlbase.EndsWith("/"))
                return urlbase + last;
            else
                return urlbase + '/' + last;
        }

		#region Component Designer generated code
		
		//Required by the Web Services Designer 
		private IContainer components = null;
				
		/// <summary>
		/// Required method for Designer support - do not modify
		/// the contents of this method with the code editor.
		/// </summary>
		private void InitializeComponent()
		{
		}

		/// <summary>
		/// Clean up any resources being used.
		/// </summary>
		protected override void Dispose( bool disposing )
		{
			if(disposing && components != null)
			{
				components.Dispose();
			}
			base.Dispose(disposing);		
		}
		
		#endregion

		// WEB SERVICE OAI METHOD:  Identify
		// Returns identification attributes describing repository
		
		[WebMethod]
		public OAIPMHtype Identify()
		{

			OAIPMHtype oaiT  = new OAIPMHtype();

			requestType rqT = new requestType();
			rqT.verb = verbType.Identify;
			rqT.verbSpecified = true;
			rqT.Value = rqtValue;
			oaiT.request = rqT;	

			IdentifyType idT = new IdentifyType();
			idT.repositoryName	="STScI VAO Repository";
			idT.baseURL			= rqtValue;
			idT.protocolVersion	=IdentifyTypeProtocolVersion.Item20;
			idT.adminEmail		= new string[1];
			idT.adminEmail[0]	="greene@stsci.edu";
			idT.earliestDatestamp= earliestDatestamp;
            idT.deletedRecord = deletedRecordType.persistent;
			idT.granularity		=granularityType.YYYYMMDDThhmmssZ;



            Registry reg = new Registry();
            XmlDocument[] docs = reg.QueryRIResourceXMLDoc("xsi_type like '%Registry' and (harvestedFrom is null or harvestedFrom = '') and [@status] = 1");
            if (docs.Length > 0)
            {
                idT.description = new XmlElement[1];
                idT.description[0] = docs[0].DocumentElement;
            }

			oaiT.Items = new object[1];
			oaiT.Items[0] = idT;
			return oaiT;
		}

		// WEB SERVICE OAI METHOD:  RecordType
		// Returns detailed record structure
		
		[WebMethod]
		public OAIPMHtype GetRecord(string identifier, string metadataPrefix)
		{
			OAIPMHtype oaiT  = new OAIPMHtype();

			requestType rqT = new requestType();
			rqT.verb = verbType.GetRecord;
			rqT.verbSpecified = true;
			//rqT.Value = "http://nvo.stsci.edu/vor10/oai.aspx";
            rqT.Value = baseURL + "oai.aspx";
            if (metadataPrefix.Length > 0)
                rqT.metadataPrefix = metadataPrefix;
            if (identifier.Length > 0)
                rqT.identifier = identifier;
			oaiT.request = rqT;	

			recordType recT = new recordType();
			GetRecordType grecT = new GetRecordType();

			Registry reg = new Registry();
            System.Xml.XmlDocument[] reses = reg.QueryRIResourceXMLDocAllResources("identifier='" + identifier + "'", true, true);
            if (reses.Length == 0 || reses[0] == null)
                return makeError(OAIPMHerrorcodeType.idDoesNotExist, identifier);

            recT.header = new headerType();
            recT.header.identifier = identifier;

            //since we're not validating these by serialisation or deserialisation anymore, let's not assume anything
            //about the data.
            try
            {
                recT.header.datestamp = reses[0].DocumentElement.Attributes["updated"].Value;
                if( recT.header.datestamp.Contains(".") )
                    recT.header.datestamp = recT.header.datestamp.Remove(recT.header.datestamp.LastIndexOf('.'));
            }
            catch(Exception)
            {
                return makeError(OAIPMHerrorcodeType.idDoesNotExist, identifier);
            }

            System.Xml.XmlDocument[] resesOurs = reg.QueryRIResourceXMLDoc("identifier='" + identifier + "'" +
                                                                                    " and (harvestedFrom is null or harvestedFrom='' or harvestedFrom like 'STScI%') ");
            //ivoa.net.ri1_0.server.Resource[] resesOurs = reg.QueryFullVOR10Resource("identifier='" + identifier + "'" +
            //                                                                        " and (harvestedFrom is null or harvestedFrom='') ");
            if( resesOurs.Length > 0 )
                recT.header.setSpec = new string[] { managedSet };

            //recT.metadata = GetElementFromXMLResource(reses[0]);
            recT.metadata = reses[0].DocumentElement;
            oaiT.Items = new object[1];
            grecT.record = recT;
            oaiT.Items[0] = grecT;
 
			return oaiT;
		}

		public XmlElement GetElementFromResource(net.ivoa.vr0_10.Resource vor)
		{			
			XmlSerializer ser = new XmlSerializer(typeof(net.ivoa.vr0_10.Resource));
			StringBuilder sb = new StringBuilder();
			StringWriter sw = new StringWriter(sb);
		
			//Resource res = new Resource();
			//res.Resource = vor;
			XmlSerializerNamespaces ns = new XmlSerializerNamespaces();
			ns.Add("","http://www.ivoa.net/xml/VOResource/v0.10");

			ser.Serialize(sw,vor,ns);
//			ser.Serialize(sw,vor,ns);
			sw.Close();

			XmlSerializer ser2 = new XmlSerializer(typeof(XmlElement));
			StringReader sr = new StringReader(sb.ToString());
	
			return (XmlElement)ser2.Deserialize(sr);
		}

        public XmlElement GetElementFromXMLResource(ivoa.net.ri1_0.server.Resource vor)
        {
            // ggreene- STILL WORKING ON THIS... need to find out how OAI to be packaged.
            // Does this use the VOResources outer wrapper with Resources inside like the RI
            // or just a set of Resources like before???
            //
            // tdower- looks like just a set of resources as before. trying that.
            XmlSerializer ser = new XmlSerializer(typeof(ivoa.net.ri1_0.server.Resource));
            StringBuilder sb = new StringBuilder();
            StringWriter sw = new StringWriter(sb);

            //Resource res = new Resource();
            //res.Resource = vor;
            XmlSerializerNamespaces ns = new XmlSerializerNamespaces();
            ns.Add("", "http://www.ivoa.net/xml/RegistryInterface/v1.0");

            ser.Serialize(sw, vor, ns);
            //			ser.Serialize(sw,vor,ns);
            sw.Close();

            XmlSerializer ser2 = new XmlSerializer(typeof(XmlElement));
            StringReader sr = new StringReader(sb.ToString());

            return (XmlElement)ser2.Deserialize(sr);
        }

		public XmlElement GetElementFromOAIDC(oai_dc.oai_dcType odc)
		{
			
			XmlSerializer ser = new XmlSerializer(typeof(oai_dc.oai_dcType));
			StringBuilder sb = new StringBuilder();
			StringWriter sw = new StringWriter(sb);

            XmlSerializerNamespaces xsn = new XmlSerializerNamespaces();
            xsn.Add("dc", "http://www.openarchives.org/OAI/2.0/oai_dc/");
            ser.Serialize(sw,odc, xsn);

			sw.Close();

			XmlSerializer ser2 = new XmlSerializer(typeof(XmlElement));
			StringReader sr = new StringReader(sb.ToString());
	
			return (XmlElement)ser2.Deserialize(sr);
		}

		// WEB SERVICE OAI METHOD:  ListIdentifiers
		// Returns detailed record structure		
		[WebMethod(Description="Returns list of identifying headers - OAI")]
		public OAIPMHtype ListIdentifiers(string metadataPrefix, string from, string until, string set, string resumptionToken)
		{
            //resumption tokens -- if we have one, what subset of this list do we return?
            resumptionTokenType incomingToken = null;
            resumptionTokenType outgoingToken = new resumptionTokenType();
            int start = 0;
            int end = 0;

            //do we already have a token?
            DateTime tokenCreated = DateTime.MinValue;
            if (resumptionToken != null && resumptionToken.Length > 0)
            {
                incomingToken = nvo.oai.oai.RetrieveValidResumptionToken(resumptionToken);
                if (incomingToken != null)
                {
                    start = Convert.ToInt32(incomingToken.cursor);
                    string[] tokenParams = incomingToken.Value.Split('!');
                    set = tokenParams[0];
                    from = tokenParams[1];
                    until = tokenParams[2];
                    metadataPrefix = tokenParams[3];

                    if( incomingToken.expirationDateSpecified )
                        tokenCreated = incomingToken.expirationDate.AddHours(-6).ToLocalTime();
                }
            }

            if (from.Length == 0)
                from = "1990-01-01";

			ArrayList arr = new ArrayList();
			ArrayList dateArr = new ArrayList();
            //ArrayList updateArr = new ArrayList();
            SqlConnection conn = null;
			try
			{
                conn = new SqlConnection(Registry.sConnect);
                conn.Open();
                
                String strCmd = "select identifier, [@created]";
                    
                if( tokenCreated > DateTime.MinValue )
                    strCmd += ", [@updated]";
                strCmd += "from Resource where [@updated] > '" + ConvertOAIDateToSQL(from) + "'";
                if (until.Length > 0)
                    strCmd += " and [@updated] < '" + ConvertOAIDateToSQL(until) + "'";
                if (set == managedSet)
                    strCmd += " and (harvestedFrom is null or harvestedFrom='' or harvestedFrom like 'STScI%') ";
                strCmd += " and [@status] = 1";

		        SqlCommand cmd = new SqlCommand(strCmd, conn);
                cmd.Prepare();
				SqlDataReader sdr = cmd.ExecuteReader();

				while (sdr.Read())
				{
					arr.Add(sdr.GetString(0));
					dateArr.Add(sdr.GetDateTime(1));

                    //if( tokenCreated > DateTime.MinValue )
                    //    updateArr.Add(sdr.GetDateTime(2));
				}
			}
			finally 
			{
				conn.Close();
			}


            if (arr.Count == 0)
            {
                return makeError(OAIPMHerrorcodeType.noRecordsMatch, String.Empty);
            }
            if (incomingToken != null && Convert.ToInt32(incomingToken.completeListSize) != arr.Count) 
                return makeError(OAIPMHerrorcodeType.badResumptionToken, "Change in query data.");

            //build OAI header
			OAIPMHtype oaiT  = new OAIPMHtype();
			requestType rqT = new requestType();
			rqT.verb = verbType.ListIdentifiers;
			rqT.verbSpecified = true;
			rqT.Value = rqtValue;
            rqT.from = from;
            if (metadataPrefix != null && metadataPrefix.Length > 0)
                rqT.metadataPrefix = metadataPrefix;
            if (until != null && until.Length > 0)
                rqT.until = until;
            if (set != null && set == managedSet) //only one we're using
                rqT.set = set;
            oaiT.request = rqT;	


			ListIdentifiersType lisT = new ListIdentifiersType();
			lisT.header = new headerType[arr.Count];

			int iECount=0;
			int itest=0;


            end = Math.Min(start + resumptionTokenLimit, arr.Count);

            if ( (arr.Count > resumptionTokenLimit) && ( end < arr.Count) )
            {
                outgoingToken = GenerateResumptionToken(from, until, set, metadataPrefix, start);
            }
			for (int ii = start; ii < end; ++ii)
			{
                //we MAY contain these, according to the OAI spec, and it's cheaper to calculate.
                /*if (tokenCreated > DateTime.MinValue)
                {
                    if( ((DateTime)updateArr[ii]) > tokenCreated )
                        return makeError(OAIPMHerrorcodeType.badResumptionToken, "Change in query data.");
                }*/

				itest++;
				lisT.header[ii] = new headerType();	
				
				try 
				{				
					lisT.header[ii].identifier = (string)arr[ii];					
                    lisT.header[ii].datestamp = GetOAIDatestamp((DateTime)dateArr[ii], granularityType.YYYYMMDDThhmmssZ);
				}
				catch(Exception e)
				{
					iECount++;
					lisT.header[ii].identifier = e.ToString();			
				}
			}

            outgoingToken.completeListSize = arr.Count.ToString();
            if (outgoingToken.Value != null)
            {
                resumptionTokenType nextToken = new resumptionTokenType(outgoingToken);
                nextToken.cursor = Convert.ToString(Convert.ToInt32(outgoingToken.cursor) + resumptionTokenLimit);
                nvo.oai.oai.SaveResumptionToken(nextToken);
            }

            lisT.resumptionToken = outgoingToken;
            oaiT.Items = new object[1];
			oaiT.Items[0] = lisT;
			return oaiT;
		}

		// WEB SERVICE OAI METHOD:  ListIdentifiers
		// Returns detailed record structure
		
	    [WebMethod(Description="Returns list of records - OAI")]
		public OAIPMHtype ListRecords(string from, string metadataPrefix, string until, string set, string resumptionToken)
		{
            //resumption tokens -- if we have one, what subset of this list do we return?
            resumptionTokenType incomingToken = null;
            resumptionTokenType outgoingToken = new resumptionTokenType();
            int start = 0;
            int end = 0;

            //do we already have a token?
            if (resumptionToken != null && resumptionToken.Length > 0)
            {
                incomingToken = nvo.oai.oai.RetrieveValidResumptionToken(resumptionToken);
                if (incomingToken != null)
                {
                    start = Convert.ToInt32(incomingToken.cursor);
                    string[] tokenParams = incomingToken.Value.Split('!');
                    set = tokenParams[0];
                    from = tokenParams[1];
                    until = tokenParams[2];
                    metadataPrefix = tokenParams[3];
                }
            }
            
            if (from.Length == 0 ) 
                from = "1990-01-01";

			OAIPMHtype oaiT  = new OAIPMHtype();

            //build OAI header
			requestType rqT = new requestType();
			rqT.verb = verbType.ListRecords;
			rqT.verbSpecified = true;
            rqT.Value = rqtValue;
            rqT.from = from;
            if( metadataPrefix != null && metadataPrefix.Length > 0 )
                rqT.metadataPrefix = metadataPrefix;
            if (until != null && until.Length > 0)
                rqT.until = until;
            if (set != null && set == managedSet) //the only one we're using.
                rqT.set = set;
			oaiT.request = rqT;

            String querystring = "[@updated] >= '" + ConvertOAIDateToSQL(from) + "'";
            if (until.Length > 0)
                querystring += " and [@updated] <= '" + ConvertOAIDateToSQL(until) + "'";
            if( set == managedSet )
                querystring += " and (harvestedFrom is null or harvestedFrom='' or harvestedFrom like 'STScI%')";

			ListRecordsType lisT = new ListRecordsType();
			Registry reg = new Registry();

			if (metadataPrefix=="ivo_vor")
			{
                System.Xml.XmlDocument[] vod = reg.QueryRIResourceXMLDocAllResources(querystring, true, true);
                if (vod.Length == 0)
                {
                    return makeError(OAIPMHerrorcodeType.noRecordsMatch, String.Empty);
                }

                if (incomingToken != null && Convert.ToInt32(incomingToken.completeListSize) != vod.Length)
                {
                    return makeError(OAIPMHerrorcodeType.badResumptionToken, "Change in query data.");
                }

				lisT.record = new recordType[vod.Length];
				
                end = Math.Min(start + resumptionTokenLimit, vod.Length);
                if ((vod.Length > resumptionTokenLimit) && (end < vod.Length))
                {
                    outgoingToken = GenerateResumptionToken(from, until, set, metadataPrefix, start);
                }

                //revamped to use XML docs only, no serialization/deserialization.
				for (int ii=start; ii< end; ++ii)
				{
					lisT.record[ii] = new recordType();
					
					try 
					{
                        lisT.record[ii].metadata = vod[ii].DocumentElement;

						headerType ht = new headerType();
                        ht.identifier = vod[ii].GetElementsByTagName("identifier")[0].InnerXml;

                        ht.datestamp = vod[ii].DocumentElement.Attributes["updated"].Value;
						lisT.record[ii].header = ht;
		
                        if (vod[ii].DocumentElement.Attributes["status"].Value.ToLower().CompareTo("deleted") == 0 )
						{
							ht.status = statusType.deleted;
							ht.statusSpecified = true;
							lisT.record[ii].header.status = ht.status;
						}
				    }
					catch(Exception e)
					{
						Console.Write(e);
					}
				}

                outgoingToken.completeListSize = vod.Length.ToString();
                if (outgoingToken.Value != null)
                {
                    resumptionTokenType nextToken = new resumptionTokenType(outgoingToken);
                    nextToken.cursor = Convert.ToString(Convert.ToInt32(outgoingToken.cursor) + resumptionTokenLimit);
                    nvo.oai.oai.SaveResumptionToken(nextToken);
                }
                lisT.resumptionToken = outgoingToken;
			}
			else if (metadataPrefix=="oai_dc")
			{
                if (incomingToken != null)
                    return makeError(OAIPMHerrorcodeType.badResumptionToken, String.Empty);

                oai_dc.oai_dcType[] odc = reg.QueryOAIDC(querystring);
                if (odc.Length == 0)
                    return makeError(OAIPMHerrorcodeType.noRecordsMatch, String.Empty);

				lisT.record = new recordType[odc.Length];
			
				for (int ii=0; ii< odc.Length;ii++)
				{
					lisT.record[ii] = new recordType();
					try 
					{
						lisT.record[ii].metadata = GetElementFromOAIDC(odc[ii]);

                        headerType ht = new headerType();
                        ht.identifier = lisT.record[ii].metadata.GetElementsByTagName("identifier")[0].InnerXml;
                        ht.datestamp = lisT.record[ii].metadata.GetElementsByTagName("date")[0].InnerXml;
                        ht.setSpec = new string[1] { "ivo_vor" };
                        //ht.statusSpecified = true;
                        //ht.status = lisT.record[ii].metadata.GetElementsByTagName("

                        lisT.record[ii].header = ht;

                        lisT.record[ii].metadata = null;

					}
					catch(Exception e)
					{
						Console.Write(e);
					}
				}
                outgoingToken.completeListSize = odc.Length.ToString();
                lisT.resumptionToken = outgoingToken;
            }
				
			oaiT.Items = new object[1];
			oaiT.Items[0] = lisT;
			return oaiT;
		}

		// WEB SERVICE OAI METHOD:  ListMetaDataFormats
		// Returns format for VOService metadata
		[WebMethod(Description="Returns format for VOService metadata - OAI")]
		public OAIPMHtype ListMetadataFormats(string identifier)
		{
			OAIPMHtype oaiT  = new OAIPMHtype();

			requestType rqT = new requestType();
			rqT.verb = verbType.ListMetadataFormats;
			rqT.verbSpecified = true;
            rqT.Value = rqtValue;
            if (identifier.Length > 0)
                rqT.identifier = identifier;
			oaiT.request = rqT;

            //if no identifier given, then respond that we know these formats.
            int recordIVO_VOR = 1;
            int recordOAI_DC = 1;

            //if identification given, query for each format we know.
            //Since we're pulling this all from the same database, we're really only
            //checking to make sure we can correctly convert the data to the desired
            //format.
            if (identifier.Length > 0) 
            {
                Registry reg = new Registry();
                //ivoa.net.ri1_0.server.Resource[] reses = reg.QueryFullVOR10Resource("identifier='" + identifier + "'");
                System.Xml.XmlDocument[] reses = reg.QueryRIResourceXMLDoc("identifier='" + identifier + "'");
                if (reses.Length == 0)
                    recordIVO_VOR = 0;

                oai_dc.oai_dcType[] odc = reg.QueryOAIDC("identifier='" + identifier + "'");
                if (odc.Length == 0)
                    recordOAI_DC = 0;
            }

            if (recordIVO_VOR == 0 && recordOAI_DC == 0)
            {
                return makeError(OAIPMHerrorcodeType.idDoesNotExist, identifier);
            }

            metadataFormatType[] metaF = new metadataFormatType[recordOAI_DC + recordIVO_VOR];
            if (recordIVO_VOR > 0)
            {
                metaF[0] = new metadataFormatType();
                //metaF[0].metadataNamespace = "http://www.ivoa.net/xml/VOResource/v0.10";
                metaF[0].metadataNamespace = "http://www.ivoa.net/xml/VOResource/v1.0";
                metaF[0].metadataPrefix = "ivo_vor";
                //metaF[0].schema = "http://www.ivoa.net/xml/VOResource/v0.10/VOResource-v0.10.xsd";
                metaF[0].schema = "http://www.ivoa.net/xml/VOResource/v1.0";
            }
            if (recordOAI_DC > 0)
            {
                metaF[recordIVO_VOR] = new metadataFormatType();
                metaF[recordIVO_VOR].metadataNamespace = "http://www.openarchives.org/OAI/2.0/oai_dc/";
                metaF[recordIVO_VOR].metadataPrefix = "oai_dc";
                metaF[recordIVO_VOR].schema = "http://www.openarchives.org/OAI/2.0/oai_dc.xsd";
            }
            oaiT.Items = new object[1];
            oaiT.Items[0] = metaF;

			return oaiT;
		}

		[WebMethod(Description="Returns format for VOService metadata - OAI")]
		public OAIPMHtype ListSets()
		{

			OAIPMHtype oaiT  = new OAIPMHtype();

			requestType rqT = new requestType();
			rqT.verb = verbType.ListSets;
			rqT.verbSpecified = true;
            rqT.Value = rqtValue;
			oaiT.request = rqT;	

			ListSetsType lsT = new ListSetsType();
			lsT.set = new setType[1];
            lsT.set[0] = new setType();
			lsT.set[0].setName = managedSet;
            lsT.set[0].setSpec = managedSet;
			
			oaiT.Items = new object[1];
			oaiT.Items[0] = lsT;


			return oaiT;
		}

        private OAIPMHtype makeError(OAIPMHerrorcodeType type, string str)
        {
            OAIPMHtype oaiT = new OAIPMHtype();

            requestType rqT = new requestType();
            rqT.Value = rqtValue;
            oaiT.request = rqT;

            OAIPMHerrorType oaiET = new OAIPMHerrorType();
            oaiET.Value = str;
            oaiET.code = type;

            oaiT.Items = new object[1];
            oaiT.Items[0] = oaiET;

            return oaiT;
        }

        [WebMethod(Description = "Returns error - OAI")]
        public OAIPMHtype makeBadVerb(string str)
        {
            return makeError(OAIPMHerrorcodeType.badVerb, str);
        }

        [WebMethod(Description = "Returns error - OAI")]
        public OAIPMHtype makeBadArg(string str)
        {
            return makeError(OAIPMHerrorcodeType.badArgument, str);
        }

        [WebMethod(Description = "Returns errors - OAI")]
        public OAIPMHtype makeMultipleErrors(string[] errorTypes, string[] errorValues)
        {
            OAIPMHtype oaiT = new OAIPMHtype();
            oaiT.Items = new object[errorValues.Length];

            for(int i = 0; i < errorValues.Length; ++i )
            {
                requestType rqT = new requestType();
                rqT.Value = rqtValue;
                oaiT.request = rqT;

                OAIPMHerrorType oaiET = new OAIPMHerrorType();
                oaiET.Value = errorValues[i];

                oaiET.code = (OAIPMHerrorcodeType)Enum.Parse(typeof(OAIPMHerrorcodeType), (String)errorTypes[i]);

                oaiT.Items[i] = oaiET;
            }

            return oaiT;
        }

        private static string buf2(int inpn)
        {
            string inp = "" + inpn;
            if (inp.Length < 2)
                return "0" + inp;
            else return inp;
        }

        public static String GetOAIDatestamp(DateTime date, oai.granularityType granularity)
        {
            string datestring = date.Year.ToString() + "-" + buf2(date.Month) + "-" + buf2(date.Day);
            if( granularity == granularityType.YYYYMMDDThhmmssZ )
                datestring += "T" + buf2(date.Hour) + ":" + buf2(date.Minute) + ":" + buf2(date.Second) + "Z";

            return datestring;
        }

        public static string ConvertOAIDateToSQL(string oai)
        {
            try
            {
                String datetime = oai.Substring(5, 2) + '/' + oai.Substring(8, 2) + '/' + oai.Substring(0, 4);
                if (oai.IndexOf('T') > 0)
                {
                    datetime += ' ' + oai.Substring(11, 2) + ':' + oai.Substring(14, 2) + ':' + oai.Substring(17, 2);
                }
                return datetime;
            }
            catch (Exception) { return String.Empty;  }
        }

        resumptionTokenType GenerateResumptionToken(String from, String until, String set, String prefix, int cursor)
        {
            resumptionTokenType token = new resumptionTokenType();

            //set!from!until!metadataPrefix!counter
            StringBuilder tokenName = new StringBuilder();
            if (set != null)
                tokenName.Append(set);
            tokenName.Append('!');
            if (from != null && from.CompareTo("1990-01-01") != 0)
                tokenName.Append(from);
            tokenName.Append('!');
            if (until != null)
                tokenName.Append(until);
            tokenName.Append('!');
            tokenName.Append(prefix);
            tokenName.Append('!');

            lock (counterLock)
            {
                tokenName.Append(tokenCounter);
                if (tokenCounter < Int64.MaxValue)
                    ++tokenCounter;
                else
                    tokenCounter = 1;
            }


            token.Value = tokenName.ToString();
            token.cursor = cursor.ToString();
            token.expirationDate = System.DateTime.Now.AddHours(6).ToUniversalTime();
            token.expirationDateSpecified = true;

            return token;
        }
	}
}
/* Log of changes
 * $Log: STOAI.asmx.cs,v $
 * Revision 1.9  2006/02/28 17:09:49  grgreene
 * delete for oai pub
 *
 * Revision 1.8  2006/02/22 16:26:46  grgreene
 * added the OAI header status attrib
 *
 * Revision 1.7  2005/06/07 16:52:57  grgreene
 * added default namespace to Resource
 *
 * Revision 1.5  2005/06/03 15:13:28  grgreene
 * fixed from date
 *
 * Revision 1.4  2005/06/03 14:35:41  grgreene
 * resumptiontoken counter in STOAI
 *
 * Revision 1.3  2005/05/09 20:00:18  grgreene
 * oai completelistsize added
 *
 * Revision 1.2  2005/05/06 16:29:53  grgreene
 * fixed oai oaiParams list
 *
 * Revision 1.1.1.1  2005/05/05 15:17:05  grgreene
 * import
 *
 * Revision 1.8  2005/05/05 14:59:01  womullan
 * adding oai files
 *
 * Revision 1.7  2005/03/22 20:11:00  womullan
 * update to parser for descrip + OAI fixes
 *
 * Revision 1.6  2005/03/17 20:54:07  womullan
 * oai fixing
 *
 * Revision 1.5  2004/12/07 15:32:28  womullan
 * readme.txt
 *
 * Revision 1.4  2004/11/01 18:30:16  womullan
 * v0.10 upgrade
 *
 * Revision 1.3  2004/04/15 16:23:43  womullan
 *  voresource for OAI interface
 *
 * Revision 1.2  2004/03/12 19:15:49  womullan
 * added keyword search to form
 *
 * Revision 1.1  2004/02/26 20:16:00  womullan
 * Added OAI interface
 *
 *
 * 
 * */
