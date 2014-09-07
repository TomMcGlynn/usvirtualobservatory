using System;
using System.Text;
using System.Data.SqlClient;
using System.Data;

namespace registry
{

	/// </summary>
	public class SQLHelper
	{
        private static string dbAdmin = (string)System.Configuration.ConfigurationManager.AppSettings["dbAdmin"];

        public static string getKeysStatement = " select top 1 pkey, ukey from resource where ivoid=@Identifier and [rstat] > 0 order by [updated], harvestedFromDate desc ";

 		/*public static string createBasicResourceSelect(string predicate) 
		{
			StringBuilder sb = new StringBuilder("SELECT TOP 1 ");
            sb.Append(@"RESOURCE.res_type,
                      RESOURCE.created,
                      RESOURCE.updated,
                      RESOURCE.res_title,
                      RESOURCE.short_name,
                      RESOURCE.ivoid,
                      RESOURCE.res_description,
                      RESOURCE.reference_url,
                      RESOURCE.waveband,
                      RESOURCE.rstat,
                      harvestedFromID,
                      harvestedFromDate,
                      tag,
                      xml,
                      res_role.role_name as publisher,
                      [subject] ");
			sb.Append(@" FROM RESOURCE INNER JOIN RES_ROLE ON RES_ROLE.RKEY = RESOURCE.PKEY 
                         INNER JOIN [SUBJECT] ON SUBJECT.RKEY = RESOURCE.PKEY 
                         WHERE  ([rstat] > 0) and base_utype like '%publisher%' AND ") ;
			sb.Append( predicate );
            sb.Append(" order by [updated] DESC");

			return sb.ToString();
		}*/

        public static string createBasicResourceSelect(string predicate) 
        {
            StringBuilder sb = new StringBuilder("SELECT ");
            sb.Append(@"RESOURCE.res_type,
                      RESOURCE.created,
                      RESOURCE.updated,
                      RESOURCE.res_title,
                      RESOURCE.short_name,
                      RESOURCE.ivoid,
                      RESOURCE.res_description,
                      RESOURCE.reference_url,
                      RESOURCE.waveband,
                      RESOURCE.rstat,
                      harvestedFromID,
                      harvestedFromDate,
                      tag,
                      xml");
            sb.Append(@" FROM RESOURCE WHERE  [rstat] > 0 AND (") ;
            sb.Append( predicate );
            sb.Append(") order by [updated] DESC");

            return sb.ToString();
        }

        public static string createInterfacesSelect(string identifier)
        {
            StringBuilder sb = new StringBuilder();
            sb.Append("select Resource.ivoid, Capability.cap_type, Capability.cap_name, Capability.standard_id, " + 
                        "Interface.access_url, Interface.std_version, Interface.url_use " +
                        "from Resource, Capability, interface " +
                        "where capability.rkey = Resource.pkey " +
                        "and Interface.rkey = Resource.pkey and Interface.ckey = Capability.pkey " +
                        "and Resource.identifier = '");
            sb.Append(identifier);
            sb.Append("' and [rstat] = 1");


            return sb.ToString();
        }

        public static string createFullXMLResourceSelect(string predicate)
        {
            // To collect both latest and deleted resources ALSO 
            StringBuilder sb = new StringBuilder("Select xml ");
            sb.Append(" FROM RESOURCE WHERE  ([rstat] > 0) and ");
            sb.Append(predicate);

            return sb.ToString();
        }

        public static string createFullXMLActiveResourceSelect(string predicate)
        {
            // To collect both latest and deleted resources ALSO 
            StringBuilder sb = new StringBuilder("Select top 1 xml ");
            sb.Append(" FROM RESOURCE WHERE  ([rstat]=1) and ");
            sb.Append(predicate);
            sb.Append(" ORDER BY [UPDATED] DESC");

            return sb.ToString();
        }

        public static string createFindHomeRegistrySelect(string[] ids)
        {
            StringBuilder sb = new StringBuilder("Select distinct ivoid, harvestedFrom, harvestedFromID, harvestedFromDate from resource where ([rstat] > 0) and ivoid in(");
            foreach (string str in ids)
                sb.Append("'" + str.Trim() + "',");
            sb.Replace(',', ')', sb.Length - 1, 1);

            return sb.ToString();
        }

        public static string createFindResourcesByHomeRegistrySelect(string source)
        {
            StringBuilder sb = new StringBuilder("Select distinct ivoid, harvestedFromID, harvestedFrom, harvestedFromDate from resource where ([rstat] > 0) and harvestedFromID = '" + source + "' order by ivoid");
            return sb.ToString();
        }

        public static string createXMLRankedResourceSelect(string keywords, bool andKeys)
        {
            StringBuilder sb = new StringBuilder("Select xml FROM RESOURCE ");

            string logical = " AND ";
            string[] keys = keywords.Trim().Split(' ');
            int cmpVal;

            //todo - rank 'or' case. test.
            if (!andKeys)
            {
                sb.Append(" where [rstat] = 1 and (");

                logical = " OR ";
                sb.Append("contains (xml,'");
                for (int k = 0; k < keys.Length; k++)
                {
                    cmpVal = keys[k].CompareTo("");
                    if (cmpVal == 0) // the string is null
                        continue;

                    if (k > 0) sb.Append(logical);
                    sb.Append(" \"" + keys[k] + "\" ");
                }
                sb.Append("')");
            }
            else
            {
                bool canFullTextRank = true;
                for (int k = 0; k < keys.Length; k++)
                {
                    //some special characters break words in fulltext search.
                    if (keys[k].IndexOf('/') >= 0 ||
                        keys[k].IndexOf('&') >= 0 ||
                        keys[k].IndexOf(' ') >= 0 )
                    {
                        canFullTextRank = false;
                    }
                }
                if (canFullTextRank)
                {
                    sb.Append(" INNER JOIN CONTAINSTABLE(resource, xml, '");
                    for (int k = 0; k < keys.Length; k++)
                    {
                        if (k > 0) sb.Append(" AND ");
                        sb.Append(keys[k]);
                    }
                    sb.Append("') AS search ON resource.pkey = search.[KEY] where resource.[rstat] = 1 and resource.validationLevel > 1 order by RANK DESC");
                }
                else
                {
                    sb.Append(" where [rstat] = 1 and validationLevel > 1 ");

                    for (int k = 0; k < keys.Length; k++)
                    {
                        cmpVal = keys[k].CompareTo("");
                        if (cmpVal == 0) // the string is null
                            continue;

                        if (k > 0) sb.Append(logical);


                        //This is a common case where we can trick the fulltext index int
                        //finding the item we need by removing quotes and allowing the word breaker to 
                        //separate things.
                        if (keys[k].Contains("ivo://"))
                        {
                            sb.Append(" contains (xml,'*" + keys[k] + "* ')");
                        }
                        //some special characters break words in fulltext search.
                        //do this the slow way.
                        else if (keys[k].IndexOf('/') >= 0 ||
                                 keys[k].IndexOf('&') >= 0)
                        {
                            //best we can do for &, also throws off 'like'
                            string temp = " xml like '%" + keys[k] + "%'";
                            sb.Append(temp.Replace('&', '%'));
                        }
                        else
                        {
                            sb.Append(" contains (xml,'\"*" + keys[k] + "*\" ')");
                        }
                    }
                }
            }
            return sb.ToString();
        }

        private static string[] RemoveNoiseWords(string[] input)
        {
            System.Collections.ArrayList list = new System.Collections.ArrayList();
            foreach (string str in input)
            {
                //etc
                if (str.ToLower() != "the")
                    list.Add(str);
            }
            return (string[])list.ToArray(typeof(string));
        }

        public static string createRankedResourceConditional(string keywords, bool andKeys, bool cache, int option)
        {
            StringBuilder sb = new StringBuilder();

            string logical = " AND ";
            string[] splitWords = keywords.Replace('\'', '"').Split(new string[] { "\" \"", "\"" }, StringSplitOptions.RemoveEmptyEntries);
            string[] keys = RemoveNoiseWords(splitWords);

            //string[] keys = RemoveNoiseWords(keywords.Trim().Split(' '));

            if (keys.Length == 0)
                return string.Empty;

            int cmpVal;

            //todo - rank 'or' case. test.
            if (!andKeys)
            {
                sb.Append(" where ");
                if (cache)
                {
                    if( option == 1 )
                        sb.Append(" ResourceAsRow is not null ");
                    else
                        sb.Append(" InterfaceAsRow is not null and ");
                }
                sb.Append(" [rstat] = 1 and (");

                logical = " OR ";
                sb.Append("contains (xml,'");
                for (int k = 0; k < keys.Length; k++)
                {
                    cmpVal = keys[k].CompareTo("");
                    if (cmpVal == 0) // the string is null
                        continue;

                    if (k > 0) sb.Append(logical);
                    sb.Append(" \"" + keys[k] + "\" ");
                }
                sb.Append("')");
            }
            else
            {
                bool canFullTextRank = true;
                for (int k = 0; k < keys.Length; k++)
                {
                    if (keys[k].IndexOf('/') >= 0 ||
                        keys[k].IndexOf('&') >= 0 ||
                        keys[k].IndexOf(' ') >= 0)
                    {
                        canFullTextRank = false;
                    }
                }
                if (canFullTextRank)
                {
                    sb.Append(" INNER JOIN CONTAINSTABLE(resource, xml, '");
                    for (int k = 0; k < keys.Length; k++)
                    {
                        if (k > 0) sb.Append(" AND ");
                        sb.Append(keys[k]);
                    }
                    sb.Append("') AS search ON resource.pkey = search.[KEY] where resource.[rstat] = 1 and validationLevel > 1 ");
                    if( cache )
                    {
                        if( option == 1)
                            sb.Append(" and ResourceAsRow is not null ");
                        else
                            sb.Append(" and InterfaceAsRow is not null ");
                    }
                    sb.Append("order by RANK DESC");
                }
                else
                {
                    sb.Append(" where [rstat] = 1 and validationLevel > 1 ");

                    if (option == 1)
                        sb.Append(" and ResourceAsRow is not null and ");
                    else
                        sb.Append(" and InterfaceAsRow is not null and ");

                    for (int k = 0; k < keys.Length; k++)
                    {
                        cmpVal = keys[k].CompareTo("");
                        if (cmpVal == 0) // the string is null
                            continue;

                        if (k > 0) sb.Append(logical);


                        //This is a common case where we can trick the fulltext index int
                        //finding the item we need by removing quotes and allowing the word breaker to 
                        //separate things.
                        if (keys[k].Contains("ivo://"))
                        {
                            sb.Append(" contains (xml,'" + keys[k] + "') ");
                        }
                        //some special characters break words in fulltext search.
                        //do this the slow way.
                        else if (keys[k].IndexOf('/') >= 0 ||
                                 keys[k].IndexOf('&') >= 0)
                        {
                            //best we can do for &, also throws off 'like'
                            string temp = " xml like '%" + keys[k] + "%'";
                            sb.Append(temp.Replace('&', '%'));
                        }
                        else
                        {
                            sb.Append(" contains (xml,'\"" + keys[k] + "\"') ");
                        }
                    }
                }
            }
            return sb.ToString();
        }

        public static string createXMLResourceSelect(string predicate, bool includeDeleted = false, bool includeInactive = false)
        {
            StringBuilder sb = new StringBuilder("Select xml ");
            sb.Append(" FROM RESOURCE WHERE ( [rstat]=1 ");
            if( includeDeleted ) {
                sb.Append("  or [rstat]=3 ");
            }
            if (includeInactive)
            {
                sb.Append(" or [rstat]=1 ");
            }
            sb.Append(" ) and ");
            sb.Append(predicate);

            //This should at least ensure that deleted/undeleted 'duplicate' records will 
            //have their last state reported last. Harvesters processing the data serially
            //which I assume to be most, if not all, of them, will wind up with the correct
            //record state. This needs to be handled better later, removing the duplicates entirely
            //either through SQL magic or upstream in the OAI processing. --tdower
            if (includeDeleted || includeInactive)
                sb.Append(" order by [updated] " );

            return sb.ToString();
        }

        public static string createXMLCapabilityResourceSelect(string predicate, string capability)
        {
            if (capability == null || capability == string.Empty)
                return createXMLResourceSelect(predicate, false);

            StringBuilder sb = new StringBuilder("SELECT RESOURCE.xml FROM RESOURCE WHERE RESOURCE.pkey in (SELECT DISTINCT RESOURCE.pkey ");
            sb.Append(" FROM RESOURCE, CAPABILITY WHERE [rstat]=1 and  RESOURCE.pkey = CAPABILITY.rkey");
            if (capability.Length > 0)
                sb.Append(" and CAPABILITY.cap_type LIKE '%" + capability + "%'");
            if (predicate.Length > 0)
                sb.Append(" and " + predicate);
            sb.Append(')');

            return sb.ToString();
        }

        public static string createXMLCapBandResourceSelect(string predicate, string capability, string band)
        {
            StringBuilder sb = new StringBuilder("SELECT RESOURCE.xml FROM RESOURCE WHERE RESOURCE.pkey in (SELECT DISTINCT RESOURCE.pkey ");
            sb.Append(" FROM RESOURCE inner join CAPABILITY on RESOURCE.pkey = CAPABILITY.rkey WHERE [rstat]=1 and ");
            sb.Append(" RESOURCE.waveband like '%" + band + "%' and ");
            sb.Append(" CAPABILITY.cap_type LIKE '%" + capability + "%'");

            if (predicate.Length > 0)
            {
                sb.Append(" and ");
                sb.Append(predicate);
            }
            sb.Append(')');

            return sb.ToString();
        }

        public static SqlCommand getKeysLookupCmd(SqlConnection conn)
        {
            SqlCommand cmd = conn.CreateCommand();
            cmd.CommandText = getKeysStatement;
            cmd.Parameters.Add("@Identifier", SqlDbType.VarChar, 500);
            cmd.Prepare();  // Calling Prepare after having setup commandtext and params.
            return cmd;
        }

        public static string getGetResourceCacheCmd(bool ordered = false)
        {
            if(ordered)
                return "select ResourceAsRow from ResourceVOTableCache inner join resource on resource.pkey = ResourceVOTableCache.rkey ";
            else
                return "select distinct ResourceAsRow from ResourceVOTableCache inner join resource on resource.pkey = ResourceVOTableCache.rkey ";
        }
        public static string getResourceCacheNotNullCmd()
        {
            return " and ResourceAsRow is not null";
        }

        public static string getGetInterfacesCacheCmd(bool ordered = false)
        {
            if(ordered)
                return "select InterfaceAsRow from ResourceVOTableCache inner join resource on resource.pkey = ResourceVOTableCache.rkey ";
            else
                return "select distinct InterfaceAsRow from ResourceVOTableCache inner join resource on resource.pkey = ResourceVOTableCache.rkey ";

        }
        public static string getInterfaceCacheNotNullCmd()
        {
            return " and InterfaceAsRow is not null";
        }

        public static string createCapabilityWavebandPredicateSelectUsingCache(string predicate, string capability, string waveband, int option)
        {
            StringBuilder sb = new StringBuilder();
            if (option == 1)
                sb.Append(SQLHelper.getGetResourceCacheCmd());
            else
                sb.Append(SQLHelper.getGetInterfacesCacheCmd());

            sb.Append(" WHERE [rstat]=1 ");

            if (capability.Length > 0)
            {
                if( option == 1)
                    sb.Append(" and ResourceAsRow like '%" + capability + "%' ");
                else
                    sb.Append(" and InterfaceAsRow like '%" + capability + "%' ");

            }
            if (waveband.Length > 0)
            {
                sb.Append(" and resource.waveband like '%" + waveband + "%'" );
            }

            if (predicate.Length > 0)
                sb.Append(" and " + predicate);
            //sb.Append(" and [rstat] = 1");
            if (option == 1)
                sb.Append(SQLHelper.getResourceCacheNotNullCmd());
            else
                sb.Append(SQLHelper.getInterfaceCacheNotNullCmd());

            return sb.ToString();
        }

        public static SqlCommand GetInsertVOTableCmd(string resourceKey, string tbl, string ifaces, SqlConnection conn)
        {
            string[] tr = new string[1] { "<TR>" };
            string[] interfaces = ifaces.Split(tr, StringSplitOptions.RemoveEmptyEntries);

            SqlCommand cmd = conn.CreateCommand();
            string cmdtext = string.Empty;
            if (resourceKey == "@rkey")
            {
                cmdtext = "declare @Resource_key bigint;\n declare @rkey bigint;\n";
                cmdtext += "SELECT @Resource_key = MAX([pkey]) FROM [dbo].[Resource];\n SELECT @rkey = @Resource_key;\n";
            }
            cmdtext += " INSERT INTO ResourceVOTableCache (rkey, ResourceAsRow) VALUES (" +
                        resourceKey + ", @row); \n";

            cmd.Parameters.Add("@row", SqlDbType.VarChar, 10000);
            cmd.Parameters["@row"].Value = tbl;

            for (int i = 0; i < interfaces.Length; ++i)
            {
                string nface = "@ifaces" + i.ToString();
                cmdtext += " INSERT INTO ResourceVOTableCache (rkey, InterfaceAsRow) VALUES (" +
                        resourceKey + ", " + nface + "); \n";

                cmd.Parameters.Add(nface, SqlDbType.VarChar, 10000);
                cmd.Parameters[nface].Value =  "<TR> " + interfaces[i];
            }

            cmd.CommandText = cmdtext;
            cmd.Prepare();
            return cmd;
        }

		public static string createKeyWordStatement(string keywords, bool andKeys) 
		{
			string logical = " AND ";
            string[] keys = keywords.Trim().Split(' ');
			StringBuilder sb = new StringBuilder();
			int cmpVal;

			if (!andKeys) 
			{
				logical = " OR "; 
				sb.Append("contains (xml,'");
				for (int k=0;k<keys.Length;k++)
				{
					cmpVal = keys[k].CompareTo("");
					if (cmpVal == 0) // the string is null
						continue; 

					if (k>0 ) sb.Append(logical);
                    //sb.Append(" \"*" + keys[k] + "*\" ");
                    sb.Append(" \"" + keys[k] + "\" ");
                }		
				sb.Append("')");
			}
			else 
			{
				for (int k=0;k<keys.Length;k++)
				{
					cmpVal = keys[k].CompareTo("");
					if (cmpVal == 0) // the string is null
						continue; 

					if (k>0 ) sb.Append(logical);


                    //This is a common case where we can trick the fulltext index int
                    //finding the item we need by removing quotes and allowing the word breaker to 
                    //separate things.
                    if (keys[k].Contains("ivo://") )
                    {
                        sb.Append(" contains (xml,'" + keys[k] + "') ");
                    }
                    //some special characters break words in fulltext search.
                    //do this the slow way.
                    else if (keys[k].IndexOf('-') >= 0 || 
                             keys[k].IndexOf('+') >= 0 || 
                             keys[k].IndexOf('/') >= 0 ||
                             keys[k].IndexOf('&') >= 0 )
                    {
                        //best we can do for &, also throws off 'like'
                        string temp = " xml like '%" + keys[k] + "%'";
                        sb.Append(temp.Replace('&', '%'));
                    }
                    else
                    {
                        sb.Append(" contains (xml,'\"" + keys[k] + "\"') ");
                    }
		
				}		
			}

			return sb.ToString();
		}

        #region temporary parsing for backward-compatibility with the old schema
        private static System.Collections.ArrayList SplitPreservingQuotes(string stringToSplit)
        {
            System.Collections.ArrayList results = new System.Collections.ArrayList();

            bool inQuote = false;
            StringBuilder currentToken = new StringBuilder();
            for (int index = 0; index < stringToSplit.Length; ++index)
            {
                char currentCharacter = stringToSplit[index];
                if (currentCharacter == '\'')
                {
                    inQuote = !inQuote;
                    currentToken.Append(currentCharacter);
                }
                else if (currentCharacter == '[' || currentCharacter == ']')
                {
                    //do nothing?: remove these from new schema queries.
                }
                else if (currentCharacter == '(' || currentCharacter == ')')
                {
                    if(currentToken.Length > 0 )
                        results.Add(currentToken.ToString().Trim());
                    results.Add(currentCharacter.ToString());
                    currentToken = new StringBuilder();
                }
                else if (currentCharacter == ' ' && inQuote == false)
                {
                    string result = currentToken.ToString().Trim();
                    if (result != "") results.Add(result);
                    currentToken = new StringBuilder();
                }
                else
                {
                    currentToken.Append(currentCharacter);
                }
            }
            string lastResult = currentToken.ToString().Trim();
            if (lastResult != "") results.Add(lastResult);
            return results;
        }

        //
        // This will not always work until we: 1. change the main queries to proper joins, not just comma / where 
        // *everywhere* (at least partially done; inspect code for otherwise.)
        //
        // test with cacheing and resource/capability per row options.
        //

        public static string TranslateOldSchemaQuery(string oldQuery)
        {
            System.Collections.ArrayList tokenList = SplitPreservingQuotes(oldQuery);
            StringBuilder substitutedString = new StringBuilder();
            System.Collections.ArrayList newJoins = new System.Collections.ArrayList();
            System.Collections.ArrayList newConditionals = new System.Collections.ArrayList();
 
            string currentToken;
            string currentJoin;
            foreach (string str in tokenList)
            {
                currentToken = str;
                if (!currentToken.StartsWith("\'"))
                {
                    currentToken = currentToken.ToLower();

                    //simple string replacements for renamed columns. this could probably be done faster.
                    if (currentToken == "xsi_type") currentToken = "res_type";
                    else if (currentToken == "@created") currentToken = "created";
                    else if (currentToken == "@updated") currentToken = "updated";
                    else if (currentToken.StartsWith("@status")) currentToken = currentToken.Replace("@status","rstat");
                    else if (currentToken == "title") currentToken = "res_title";
                    else if (currentToken == "shortname") currentToken = "short_name";
                    else if (currentToken == "identifier") currentToken = "resource.ivoid";
                    else if (currentToken == "content/description") currentToken = "res_description";
                    else if (currentToken == "content/source/@format") currentToken = "source_format";
                    else if (currentToken == "content/source") currentToken = "source_value";
                    else if (currentToken == "content/reference_url") currentToken = "reference_url";
                    else if (currentToken == "content/type") currentToken = "content_type";
                    else if (currentToken == "content/contentLevel") currentToken = "content_level";
                    else if (currentToken == "coverage/footprint/footprint/@ivo-id") currentToken = "footprint_ivoid";
                    else if (currentToken == "coverage/footprint") currentToken = "footprint_url";
                    else if (currentToken == "coverage/waveband") currentToken = "waveband";
                    else if (currentToken == "coverage/regionofregard") currentToken = "region_of_regard";
                    else if (currentToken == "curation/version") currentToken = "version";
                    else if (currentToken.StartsWith("*")) currentToken = currentToken.Replace("*","xml");

                    //todo: many more substitutions for refactored columns
                    else if (currentToken == "content/subject")
                    {
                        currentToken = "subject.subject";
                        currentJoin = "left outer join subject on subject.rkey = resource.pkey";
                        if (!newJoins.Contains(currentJoin))
                            newJoins.Add(currentJoin);
                    }
                    else if (currentToken == "curation/publisher")
                    {
                        currentToken = "res_role.role_name";
                        currentJoin = "left outer join res_role on res_role.rkey = resource.pkey";
                        if (!newJoins.Contains(currentJoin))
                        {
                            newJoins.Add(currentJoin);
                            newConditionals.Add("and res_role.base_utype = 'vor:resource.curation.publisher'");
                        }
                    }

                    // may be too much work for something not much used: removal of potential xml namespaces in changed content? 
                    // ie. 'vs:authority' now 'authority' but only after xsi_type?
                }

                substitutedString.Append(currentToken + ' ');
            }

            string substitutedResults = substitutedString.ToString();

            int whereIndex = substitutedResults.ToLower().LastIndexOf("where");  //"Last" to get innermost subquery before potential distinct resource.ivoid in... filtering
            StringBuilder fullResults = new StringBuilder(substitutedResults.Substring(0, whereIndex));
            foreach (string str in newJoins)
            {
                fullResults.Append(str + ' ');
            }
            fullResults.Append(substitutedResults.Substring(whereIndex));
            foreach (string str in newConditionals)
            {
                fullResults.Append(str + ' ');
            }

            return fullResults.ToString().TrimEnd();
        }
        #endregion
    }
}
