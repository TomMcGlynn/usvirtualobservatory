
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
using oai;

namespace nvo.oai
{
	/// <summary>
	/// for oai we need verb not op - this just swizles it 
	/// </summary>
	///Current version
	///ID:		$Id: oai.aspx.cs,v 1.3 2005/05/06 16:29:53 grgreene Exp $
	///Revision:	$Revision: 1.3 $
	///Date:	$Date: 2005/05/06 16:29:53 $
	/// 

	public class oai : System.Web.UI.Page
	{
        private enum Verbs : int
        {
            GetRecord = 0,
            Identify,
            ListIdentifiers,
            ListMetadataFormats,
            ListRecords,
            ListSets,

            NumVerbs
        };
        private static ArrayList verbs = new ArrayList();
        private static ArrayList requiredArgs = new ArrayList();
        private static ArrayList optArgs = new ArrayList();

        private static Hashtable resumptionTokens = new Hashtable();

		private void Page_Load(object sender, System.EventArgs e)
        {
            #region Load Parameter Checking Information (once)
            lock (verbs.SyncRoot)
            {
                if (verbs.Count == 0)
                {
                    // Generate tables of allowed and optional arguments for each verb.
                    // Since we are calling a web service based on these parameters, we
                    // want to check them here where we can return proper OAI errors and not
                    // waste the expensive WS call.
                    verbs.AddRange( new String[(int)Verbs.NumVerbs] );
                    verbs[(int)Verbs.GetRecord] = "GetRecord";
                    verbs[(int)Verbs.Identify] = "Identify";
                    verbs[(int)Verbs.ListIdentifiers] = "ListIdentifiers";
                    verbs[(int)Verbs.ListMetadataFormats] = "ListMetadataFormats";
                    verbs[(int)Verbs.ListRecords] = "ListRecords";
                    verbs[(int)Verbs.ListSets] = "ListSets";

                    requiredArgs.AddRange( new String[verbs.Count] );
                    requiredArgs[(int)Verbs.GetRecord] = new ArrayList();
                    ((ArrayList)requiredArgs[(int)Verbs.GetRecord]).Add("identifier");
                    ((ArrayList)requiredArgs[(int)Verbs.GetRecord]).Add("metadataPrefix");

                    requiredArgs[(int)Verbs.ListIdentifiers] = new ArrayList();
                    ((ArrayList)requiredArgs[(int)Verbs.ListIdentifiers]).Add("metadataPrefix");

                    requiredArgs[(int)Verbs.ListRecords] = new ArrayList();
                    ((ArrayList)requiredArgs[(int)Verbs.ListRecords]).Add("metadataPrefix");


                    optArgs.AddRange(new String[verbs.Count]);

                    //Theoretically we should handle resumptionTokens on ListSets if we have many sets.
                    //However, with only one set in the DB, there is no reason that we should
                    //hand out a resumptionToken for ListSets. Therefore, any resumptionToken
                    //passed into ListSets must be invalid.

                    optArgs[(int)Verbs.ListIdentifiers] = new ArrayList();
                    ((ArrayList)optArgs[(int)Verbs.ListIdentifiers]).Add("from");
                    ((ArrayList)optArgs[(int)Verbs.ListIdentifiers]).Add("until");
                    ((ArrayList)optArgs[(int)Verbs.ListIdentifiers]).Add("set");
                    ((ArrayList)optArgs[(int)Verbs.ListIdentifiers]).Add("resumptionToken");

                    optArgs[(int)Verbs.ListMetadataFormats] = new ArrayList();
                    ((ArrayList)optArgs[(int)Verbs.ListMetadataFormats]).Add("identifier");

                    optArgs[(int)Verbs.ListRecords] = new ArrayList();
                    ((ArrayList)optArgs[(int)Verbs.ListRecords]).Add("from");
                    ((ArrayList)optArgs[(int)Verbs.ListRecords]).Add("until");
                    ((ArrayList)optArgs[(int)Verbs.ListRecords]).Add("set");
                    ((ArrayList)optArgs[(int)Verbs.ListRecords]).Add("resumptionToken");
                }
            };
            #endregion

            #region Check Parameters

            System.Collections.Specialized.NameValueCollection input = Request.QueryString;
            if (Request.RequestType == "POST")
            {
                input = Request.Form;
            }
            string verb = input["verb"];
            string resumptionToken = input["resumptionToken"];

			// Check for verb parameter and its validity
            if (!checkRequired("verb", input) || !verbs.Contains(verb))
            {
                filterXML("stoai.asmx/makeBadVerb?&str=" + verb);
                return;
            }

            //check for required parameters by verb.
            if (!checkParams(verb, resumptionToken, input)) return;
            #endregion

            #region Build Web Service Query
            StringBuilder sb = new StringBuilder("stoai.asmx/");
			sb.Append(verb);
			sb.Append("?");

            bool bAddedParams = AppendEmptyParameters(ref sb, verb, input);
            for (int i = 0; i < input.Count; i++)
            {
                String thisParam = input.AllKeys[i];
                
                if (thisParam.CompareTo("verb") != 0)
                {
                    if (bAddedParams) //...is this not the first var
                        sb.Append("&");
                    else
                        bAddedParams = true; 
                    sb.Append(thisParam);
                    sb.Append("=");
                    try
                    {
                        //There is at least one special character in identifiers that .Net thinks mean s
                        //something else in request parameters.
                        if( thisParam == "identifier" )
                        {
                            string wholeParam = input.ToString();
                            wholeParam = wholeParam.Substring(wholeParam.IndexOf('=', wholeParam.IndexOf("identifier")) + 1);
                            if (wholeParam.Contains("&")) 
                                wholeParam = wholeParam.Substring(0, wholeParam.IndexOf('&'));
                            wholeParam = wholeParam.Replace("+", "%2B");
                            sb.Append(wholeParam);
                        }
                        else
                        {
                            sb.Append(input[thisParam]);
                        }
                    }
                    catch (Exception ex)
                    {
                        filterXML("stoai.asmx/makeBadArg?str=" + thisParam + ex.Message);
                        return;
                    }
                }
            }

            Response.ContentType="text/xml";
			filterXML(sb.ToString());        
            #endregion
		}

		// Checking required OAI input parameters exist
        // tdower - changed so that parameters don't need to be in any order and
        //          we will get better error responses for some verbs on malformed query.
		bool checkRequired(String value, System.Collections.Specialized.NameValueCollection collection)
		{
            foreach (String param in collection.AllKeys)
            {
                if (param == null)
                    return false;

                if (param.CompareTo(value) == 0)
                    return true;
            }

            filterXML("stoai.asmx/makeBadArg?str=Missing " + value);
			return false;
		}

        bool checkParams(String verb, String resumptionToken, System.Collections.Specialized.NameValueCollection collection)
        {
            ArrayList errors = new ArrayList();

            //If from and until values exist, make sure they are properly formatted,
            //consistent in granularity, and within a valid date range
            #region from / until values consistent if present
            string from = collection["from"];
            string until = collection["until"];

            if (from != null)
            {
                try
                {
                    Convert.ToInt32(from.Substring(0,4));
                    int month = Convert.ToInt16(from.Substring(5,2));
                    int day = Convert.ToInt16(from.Substring(8, 2));
                    if( month > 12 || day > 31 )
                    {
                        errors.Add(OAIPMHerrorcodeType.badArgument);
                        errors.Add("from");
                    }
                }
                catch (Exception)
                {
                    errors.Add(OAIPMHerrorcodeType.badArgument);
                    errors.Add("from");
                }

                if (until != null)
                {
                    try
                    {
                        Convert.ToInt32(until.Substring(0, 4));
                        int month = Convert.ToInt16(until.Substring(5, 2));
                        int day = Convert.ToInt16(until.Substring(8, 2));
                        if (month > 12 || day > 31)
                        {
                            errors.Add(OAIPMHerrorcodeType.badArgument);
                            errors.Add("until");
                        }
                    }
                    catch (Exception)
                    {
                        errors.Add(OAIPMHerrorcodeType.badArgument);
                        errors.Add("until");
                    }
                    
                    //granularity and order, if we've gotten this far.
                    if (errors.Count == 0)
                    {
                        int ft = from.IndexOf('T');
                        int ut = collection["until"].IndexOf('T');
                        if (from.CompareTo(until) > 0 || (ft >= 0 && ut < 0) || (ft < 0 && ut >= 0))
                        {
                            errors.Add(OAIPMHerrorcodeType.badArgument);
                            errors.Add("until");
                        }
                    }
                }

                if (errors.Count == 0 && from.CompareTo(registry.STOAI.earliestDatestamp) < 0)
                {
                    errors.Add(OAIPMHerrorcodeType.badArgument);
                    errors.Add("from");
                }
            }
            if (errors.Count == 0 && until != null && until.CompareTo(registry.STOAI.earliestDatestamp) < 0)
            {
                errors.Add(OAIPMHerrorcodeType.badArgument);
                errors.Add("until");
            }
            #endregion

            //Check that all required parameters have been entered (or resumptionToken)
            #region all required params present (or resumptionToken)
            if (resumptionToken == null)
            {
                if (requiredArgs[verbs.IndexOf(verb)] != null)
                {
                    foreach (String required in (ArrayList)requiredArgs[verbs.IndexOf(verb)])
                    {
                        bool bFound = false;
                        foreach (string param in collection.AllKeys)
                        {
                            if (param != null && param.CompareTo(required) == 0)
                            {
                                bFound = true;
                                break;
                            }
                        }
                        if (bFound == false)
                        {
                            errors.Add(OAIPMHerrorcodeType.badArgument);
                            errors.Add(required);
                        }
                    }
                }
            }
            #endregion

            // Reverse: check that all params entered are either in required or optional lists for this verb.
            // Also check that if there is a resumptionToken, all other parameters are empty.
            #region all present are optional or required, resumptionToken exclusive
            bool bResumptionTokenError = false;
            foreach (string param in collection.AllKeys)
            {
                if (param == "verb")
                    continue;

                //only show this error once.
                if (resumptionToken != null && !bResumptionTokenError && param.CompareTo("resumptionToken") != 0)
                {
                    errors.Add(OAIPMHerrorcodeType.badArgument);
                    errors.Add("resumptionToken cannot be combined with other parameters");
                    bResumptionTokenError = true;
                }

                if (requiredArgs[verbs.IndexOf(verb)] == null || !((ArrayList)requiredArgs[verbs.IndexOf(verb)]).Contains(param))
                {
                    if (optArgs[verbs.IndexOf(verb)] == null || !((ArrayList)optArgs[verbs.IndexOf(verb)]).Contains(param))
                    {
                        errors.Add(OAIPMHerrorcodeType.badArgument);
                        errors.Add(param);
                    }
                }
            }
            #endregion

            //check values and consistency of various params.
            #region look for malformed identifiers and metadata prefixes, invalid sets, bad resumptionTokens
            if (collection["identifier"] != null)
            {
                if (!collection["identifier"].StartsWith("ivo://"))
                {
                    errors.Add(OAIPMHerrorcodeType.badArgument);
                    errors.Add("identifier");
                }
            }
            if (collection["metadataPrefix"] != null)
            {
                String prefix = collection["metadataPrefix"];
                if (prefix != "ivo_vor" && prefix != "oai_dc")
                {
                    errors.Add(OAIPMHerrorcodeType.cannotDisseminateFormat);
                    errors.Add("metadataPrefix");

                    char [] reserved = new char[] { ';' , '/' , '?' , ':' , '@' , '&' , '=' , '+' , '$' , ',' };
                    if( prefix.IndexOfAny( reserved ) != -1 )
                    {
                        errors.Add(OAIPMHerrorcodeType.badArgument);
                        errors.Add("metadataPrefix");
                    }
                }
            }
            if (collection["set"] != null)
            {
                if (!collection["set"].StartsWith("ivo_"))
                {
                    errors.Add(OAIPMHerrorcodeType.badArgument);
                    errors.Add("set");
                }
            }
            if (resumptionToken != null)
            {
                resumptionTokenType token = RetrieveValidResumptionToken(resumptionToken);
                if ( token == null )
                {
                    errors.Add(OAIPMHerrorcodeType.badResumptionToken);
                    errors.Add("resumptionToken");
                }
            }
            #endregion

            #region return with or without errors
            if (errors.Count == 0)
                return true;
            else
            {
                string errorArgs = "stoai.asmx/makeMultipleErrors?";
                bool firstArg = true;
                for( int i = 0; i < errors.Count; ++i )
                {
                    if (firstArg)
                        firstArg = false;
                    else
                        errorArgs += "&";
                    errorArgs += "errorTypes=" + ((OAIPMHerrorcodeType)errors[i++]).ToString();
                    errorArgs += "&errorvalues=" + errors[i];
                }
                filterXML(errorArgs);
                return false;
            }
            #endregion
        }

        public static resumptionTokenType RetrieveValidResumptionToken(String value)
        {
            resumptionTokenType token = (resumptionTokenType)resumptionTokens[value];

            //check date validity, etc.
            if (token != null)
            {
                if (token.expirationDateSpecified == true && token.expirationDate < DateTime.Now.ToUniversalTime())
                {
                    resumptionTokens.Remove(token.Value); //token "value" is the string we're using as a key.
                    token = null;
                }
            }
            return token;
        }

        public static void SaveResumptionToken( resumptionTokenType token )
        {
            //Todo -- eventually we ought to have a cleanup thread for this and temp files.
            //For now, delete old ones when creating new one since it's a rarely occuring and relevant time
            //to afford the slowdown.
            foreach (DictionaryEntry myEntry in resumptionTokens)
            {
                if (((resumptionTokenType)myEntry.Value).expirationDateSpecified &&
                    ((resumptionTokenType)myEntry.Value).expirationDate > DateTime.Now.ToUniversalTime())
                {
                    resumptionTokens.Remove(myEntry);
                }
            }

            resumptionTokens.Add(token.Value, token);
        }

        //Appends empty params to query string before sending off to web service.
        //the web service will require all optional parameters because function
        //overloading for web services is non-compliant and unpleasant.
        bool AppendEmptyParameters(ref StringBuilder sb, string verb, System.Collections.Specialized.NameValueCollection collection)
        {
            bool bAddedParams = false;

            if (optArgs[verbs.IndexOf(verb)] == null)
                return bAddedParams;

            foreach (String arg in (ArrayList)optArgs[verbs.IndexOf(verb)])
            {
                if (collection[arg] == null)
                {
                    if( bAddedParams == true ) //is this not the first var?
                        sb.Append("&");
                    sb.Append( arg + "=");
                    bAddedParams = true;
                }
            }

            if (requiredArgs[verbs.IndexOf(verb)] == null)
                return bAddedParams;

            //if we have a ResumptionToken, otherwise-required args may be missing.
            foreach (String arg in (ArrayList)requiredArgs[verbs.IndexOf(verb)])
            {
                if (collection[arg] == null)
                {
                    if (bAddedParams == true) //is this not the first var?
                        sb.Append("&");
                    sb.Append(arg + "=");
                    bAddedParams = true;
                }
            } return bAddedParams;
        }

		// This allows schemaLocation output to be handled for OAI
		void filterXML(string partURL)
		{
			string fullURL = Request.Url.GetLeftPart(System.UriPartial.Authority)+Request.ApplicationPath + "/"+ partURL;

			HttpWebRequest wr = (HttpWebRequest)WebRequest.Create(fullURL); 
			// Sends the HttpWebRequest and waits for the response. 
			HttpWebResponse resp = null;

			try
			{
				resp = (HttpWebResponse)wr.GetResponse(); 
				// Gets the stream associated with the response.
				Stream receiveStream = resp.GetResponseStream();
				Encoding encode = System.Text.Encoding.GetEncoding("utf-8");
				StreamReader stream = new StreamReader( receiveStream, encode);
				string line = null;
				while ((line = stream.ReadLine())!=null) 
				{
					line = line.Replace(" schemaLocation", " xsi:schemaLocation");
                    //line = line.Replace("http://www.ivoa.net/xml/SkyNode/v0.2", "http://www.ivoa.net/xml/OpenSkyNode/OpenSkyNode-v0.2.xsd");
					Response.Output.WriteLine(line);
				}
			}
			catch (Exception ex)
			{
				Response.Output.WriteLine(" Failed "+fullURL+" " +ex.Message+" : "+ ex.StackTrace);
			}

		}

		#region Web Form Designer generated code
		override protected void OnInit(EventArgs e)
		{
			//
			// CODEGEN: This call is required by the ASP.NET Web Form Designer.
			//
			InitializeComponent();
			base.OnInit(e);
		}
		
		/// <summary>
		/// Required method for Designer support - do not modify
		/// the contents of this method with the code editor.
		/// </summary>
		private void InitializeComponent()
		{    
			this.Load += new System.EventHandler(this.Page_Load);
		}
		#endregion

		// Checking for Bad input parameters or argument for OAI
		/*private bool checkParam(string str)
		{
			string lstr = str.ToLower();

			// Added by .NET so okay, ignore these
			if (lstr.StartsWith("asp")|| lstr.StartsWith("all") || lstr.StartsWith("app") 
				|| lstr.StartsWith("auth") || lstr.StartsWith("log") || lstr.StartsWith("rem")  
				|| lstr.StartsWith("cert") || lstr.StartsWith("cont")
				|| lstr.StartsWith("gate") || lstr.StartsWith("http")  
				|| lstr.StartsWith("ins")|| lstr.StartsWith("loc")  
				|| lstr.StartsWith("path") || lstr.StartsWith("quer")  
				|| lstr.StartsWith("req") || lstr.StartsWith("scri") 
                || lstr.StartsWith("__utm") //google analytics.
				|| lstr.StartsWith("serv") || lstr.StartsWith("url") ) return true;

			for (int i=0;i<validParams.Length;i++)
			{
				if (validParams[i]==lstr) return true;	
			}

			return false;
		}*/
		
	}
}
/* Log of changes
 * $Log: oai.aspx.cs,v $
 * Revision 1.3  2005/05/06 16:29:53  grgreene
 * fixed oai oaiParams list
 *
 * Revision 1.2  2005/05/06 15:32:13  grgreene
 * fixed oai validparams list
 *
 * Revision 1.1.1.1  2005/05/05 15:17:01  grgreene
 * import
 *
 * Revision 1.5  2005/05/05 14:59:01  womullan
 * adding oai files
 *
 * Revision 1.4  2005/03/17 20:54:07  womullan
 * oai fixing
 *
 * Revision 1.3  2004/04/15 18:01:39  womullan
 * updated index form
 *
 * Revision 1.2  2004/03/12 19:15:49  womullan
 * added keyword search to form
 *
 *
 * 
 * */