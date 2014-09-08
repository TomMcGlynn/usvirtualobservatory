using System;
using System.Text;
using System.Data.SqlClient;
using System.Data;

namespace registry
{
	/// </summary>
	public class SQLHelper
	{
        private static string dbAdmin = Properties.Settings.Default.dbAdmin;

        //can delete active or inactive records.
		public  static string deleteResourceStatement = @"update Resource set [@status] = 3, [@updated]= getdate() where Identifier=@Identifier and ([@status] =1 or [@status] = 0)";
	
		public static string InsertResourceStatement = null;
		public static string InsertConeStatement = null;
		public static string InsertSiapStatement = null;
		public static string InsertSkyNodeStatement = null;
		public static string InsertRelationShipStatement =null;
		public static string InsertInterfaceStatement = null;
		public static string InsertParamStatement = null;

        //we can also deprecate deleted and inctive records - they can be undeleted and reactivated, after all.
		//public static string updateStatus = " update resource set status = 2 where identifier=@Identifier and status=1 ";
        public static string updateStatus = "update Resource set status = 2 where pkey in ( select pkey from Resource where identifier=@Identifier and status != 2 order by updated, harvestedFromDate desc) ";
 
		public static string getPassPhrase = " select passPhrase,modificationDate from Resource where identifier=@Identifier and status in (1,3)";

		public static string getNextIdStatement = " select max(dbid)+1 from resource";

		public static string getResRelationsStatement = " select * from resourceRelations rr where rr.PrimaryResourcedbid = @dbid ";

		public static string getResInterfacesStatement = " select * from Interfaces i where i.dbID = @dbid ";
		public static string getInterfaceParamsStatement = " select * from Params p where p.interfaceNum = @interfaceNum and p.dbid=@dbid ";

        public static string getRKeyStatement = " select top 1 pkey from resource where identifier=@Identifier and [@status]!= 2 order by [@updated], harvestedFromDate desc ";

		/// <summary>
		/// Utility to create prepared update statement for table and given cols
		/// assumes cols[0] is the key - it goes in the where clause.
		/// </summary>
		/// <param name="tableName"></param>
		/// <param name="cols"></param>
		/// <returns></returns>
		public static string createUpdateStatement(string tableName,string[] cols) 
		{
			StringBuilder sb = new StringBuilder("UPDATE ");
			sb.Append(tableName);
			sb.Append(" SET ");

			StringBuilder vals = new StringBuilder();

			for (int c =1; c < cols.Length; c++)// 0 is dbid 
			{
				sb.Append(cols[c]);// statemnt
				sb.Append(" = @");
				sb.Append(cols[c]); 
				if (c < cols.Length-1)// no commas on the end 
				{
					sb.Append(',');
				}
			}
		
			sb.Append(" where " );
			sb.Append(cols[0]);
			sb.Append(" = @");
			sb.Append(cols[0]);

			return sb.ToString();
		}
		
		/// <summary>
		/// Utility to create prepared insert into statement for table and given cols
		/// </summary>
		/// <param name="tableName"></param>
		/// <param name="cols"></param>
		/// <returns></returns>
		public static string createInsertStatement(string tableName,string[] cols) 
		{
			StringBuilder sb = new StringBuilder("INSERT INTO ");
			sb.Append(tableName);
			sb.Append(" (");

			StringBuilder vals = new StringBuilder();

			for (int c =0; c < cols.Length; c++) 
			{
				sb.Append(cols[c]);// statemnt
				vals.Append('@');
				vals.Append(cols[c]); // values part append later
				if (c < cols.Length-1)// commas only on the end 
				{
					sb.Append(',');
					vals.Append(',');
				}
			}
		
			sb.Append(") values (" );
			sb.Append(vals);
			sb.Append(')');

			return sb.ToString();
		}

		/// <summary>
		/// get the statement for insert SimpleImageAccess
		/// </summary>
		/// <returns></returns>
		public static string getInsertSkyNodeStatement() 
		{
			if (null == InsertSkyNodeStatement) 
			{
				InsertSkyNodeStatement = createUpdateStatement(DBResource.Table,
					ServiceSkyNode.Cols);
			}
			return InsertSkyNodeStatement;
		}

		/// <summary>
		/// get the statement for insert SimpleImageAccess
		/// </summary>
		/// <returns></returns>
		public static string getInsertSiapStatement() 
		{
			if (null == InsertSiapStatement) 
			{
				InsertSiapStatement = createUpdateStatement(DBResource.Table,
					ServiceSimpleImageAccess.Cols);
			}
			return InsertSiapStatement;
		}

		/// <summary>
		/// get the statement for insert cone
		/// </summary>
		/// <returns></returns>
		public static string getInsertConeStatement() 
		{
			if (null == InsertConeStatement) 
			{
				InsertConeStatement = createUpdateStatement(DBResource.Table,ServiceCone.Cols);
			}
			return InsertConeStatement;
		}

		/// <summary>
		/// run query to find the next available id ..
		/// what happens with multithreading ..
		/// </summary>
		/// <param name="conn"></param>
		/// <returns></returns>
		
		public static long getNextId(SqlConnection conn) 
		{
			SqlCommand cmd = conn.CreateCommand();
			cmd.CommandText = getNextIdStatement;
			cmd.Prepare() ;  // Calling Prepare after having setup commandtext and params.
			object obj = cmd.ExecuteScalar();
			long id = 1000;
			if (typeof(System.DBNull) != obj.GetType()) // case when no recs in db
			{
				id = (long)obj;
			}
			return id;

		}

		public static SqlCommand getInsertConeCmd(SqlConnection conn) 
		{
			SqlCommand cmd = conn.CreateCommand();
			cmd.CommandText = getInsertConeStatement();
			addParameters(cmd,ServiceCone.Cols,ServiceCone.Types,ServiceCone.Sizes);
			cmd.Prepare() ;  // Calling Prepare after having setup commandtext and params.
			return cmd;
		}
		public static SqlCommand getInsertSkyNodeCmd(SqlConnection conn) 
		{
			SqlCommand cmd = conn.CreateCommand();
			cmd.CommandText = getInsertSkyNodeStatement();
			addParameters(cmd,ServiceSkyNode.Cols,ServiceSkyNode.Types,
				ServiceSkyNode.Sizes);
			cmd.Prepare() ;  // Calling Prepare after having setup commandtext and params.
			return cmd;
		}


		public static SqlCommand getInsertSiapCmd(SqlConnection conn) 
		{
			SqlCommand cmd = conn.CreateCommand();
			cmd.CommandText = getInsertSiapStatement();
			addParameters(cmd,ServiceSimpleImageAccess.Cols,ServiceSimpleImageAccess.Types,
				ServiceSimpleImageAccess.Sizes);
			cmd.Prepare() ;  // Calling Prepare after having setup commandtext and params.
			return cmd;
		}

		public static string getInsertRelationShipStatement()
		{
			if (null == InsertRelationShipStatement) 
			{
				 InsertRelationShipStatement = createInsertStatement(ResourceRelation.Table,
													ResourceRelation.Cols);
			}
			return InsertRelationShipStatement;
		}

		public static SqlCommand getInsertRelationshipCmd(SqlConnection conn) 
		{
			SqlCommand cmd = conn.CreateCommand();
			cmd.CommandText = getInsertRelationShipStatement();
			addParameters(cmd,ResourceRelation.Cols,ResourceRelation.Types,ResourceRelation.Sizes);
			cmd.Prepare() ;  // Calling Prepare after having setup commandtext and params.
			return cmd;

		}

		public static string getInsertInterfaceStatement()
		{
			if (null == InsertInterfaceStatement) 
			{
				InsertInterfaceStatement = createInsertStatement(ResourceInterface.Table,
					ResourceInterface.Cols);
			}
			return InsertInterfaceStatement;
		}

		public static SqlCommand getInsertInterfaceCmd(SqlConnection conn) 
		{
			SqlCommand cmd = conn.CreateCommand();
			cmd.CommandText = getInsertInterfaceStatement();
			addParameters(cmd,ResourceInterface.Cols,ResourceInterface.Types,ResourceInterface.Sizes);
			cmd.Prepare() ;  // Calling Prepare after having setup commandtext and params.
			return cmd;

		}

		public static string getInsertParamStatement()
		{
			if (null == InsertParamStatement) 
			{
				InsertParamStatement = createInsertStatement(InterfaceParam.Table,
					InterfaceParam.Cols);
			}
			return InsertParamStatement;
		}

		public static SqlCommand getInsertParamCmd(SqlConnection conn) 
		{
			SqlCommand cmd = conn.CreateCommand();
			cmd.CommandText = getInsertParamStatement();
			addParameters(cmd,InterfaceParam.Cols,InterfaceParam.Types,InterfaceParam.Sizes);
			cmd.Prepare() ;  // Calling Prepare after having setup commandtext and params.
			return cmd;

		}

        public static string createInactiveResourceSelect(string identifier)
        {
            StringBuilder sb = new StringBuilder("Select top 1 [@status] from resource where identifier = '");
            sb.Append(identifier);
            sb.Append("' order by harvestedFromDate desc");

            return sb.ToString();
        }

		public static string createResourceSelect(string predicate) 
		{
			string pubCols = String.Join(",",DBResource.Cols);
			string snCols = String.Join(",",ServiceSkyNode.Cols);
			string siaCols = String.Join(",",ServiceSimpleImageAccess.Cols);
			//string coneCols = " MaxSearchRadius ";//String.Join(",",ServiceCone.Cols);

			StringBuilder sb = new StringBuilder("Select ");
			sb.Append(pubCols);
//			sb.Append(',');
//			sb.Append(snCols);
//			sb.Append(',');
//			sb.Append(siaCols);
//			sb.Append(',');
//			sb.Append(coneCols); 
			sb.Append(" FROM RESOURCE WHERE  [@status]=1 and ") ;
			sb.Append( predicate );

	
			return sb.ToString();
		}

        public static string createInterfacesSelect(string identifier)
        {
            StringBuilder sb = new StringBuilder();
            sb.Append("select Resource.identifier, Capability.validationLevel, Capability.xsi_type, Capability.[@standardID]," + 
                        "Interface.accessURL, Interface.[@version], Interface.[accessURL/@use] " +
                        "from Resource, Capability, interface " +
                        "where capability.rkey = Resource.pkey " +
                        "and Interface.rkey = Resource.pkey and Interface.container_key = Capability.pkey " +
                        "and Resource.identifier = '");
            sb.Append(identifier);
            sb.Append("' and [@status] = 1");


            return sb.ToString();
        }

        public static string createFullXMLResourceSelect(string predicate)
        {
            // To collect both latest and deleted resources ALSO 
            StringBuilder sb = new StringBuilder("Select xml ");
            sb.Append(" FROM RESOURCE WHERE  ([@status]=1 or [@status]=3) and ");
            sb.Append(predicate);

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
                sb.Append(" where [@status] = 1 and (");

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
                        keys[k].IndexOf('&') >= 0)
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
                    //todo - weight first term higher?
                    sb.Append("') AS search ON resource.pkey = search.[KEY] where resource.[@status] = 1 and resource.validationLevel > 1 order by RANK DESC");
                }
                else
                {
                    sb.Append(" where [@status] = 1 and validationLevel > 1 ");

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
            string[] keys = RemoveNoiseWords(keywords.Trim().Split(' '));

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
                sb.Append(" [@status] = 1 and (");

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
                        keys[k].IndexOf('&') >= 0)
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
                    //todo - weight first term higher?
                    sb.Append("') AS search ON resource.pkey = search.[KEY] where resource.[@status] = 1 and validationLevel > 1 ");
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
                    sb.Append(" where [@status] = 1 and validationLevel > 1 ");

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

        public static string createXMLResourceSelect(string predicate, bool includeDeleted)
        {
            StringBuilder sb = new StringBuilder("Select xml ");
            sb.Append(" FROM RESOURCE WHERE ");
            if( includeDeleted ) {
                sb.Append(" ([@status]=1 or [@status]=3) and ");
            }
            else {
                sb.Append(" [@status]=1 and ");
            }
            sb.Append(predicate);

            //This should at least ensure that deleted/undeleted 'duplicate' records will 
            //have their last state reported last. Harvesters processing the data serially
            //which I assume to be most, if not all, of them, will wind up with the correct
            //record state. This needs to be handled better later, removing the duplicates entirely
            //either through SQL magic or upstream in the OAI processing. --tdower
            if (includeDeleted)
                sb.Append(" order by [@updated] " );

            return sb.ToString();
        }

        public static string createXMLAdvancedResourceSelect(string predicate, string capability, string title, string shortname, string identifier, string publisher, string subject, string waveband)
        {
            StringBuilder sb = new StringBuilder("Select RESOURCE.xml ");
            if (capability == null || capability == string.Empty)
            {
                sb.Append(" FROM RESOURCE WHERE [@status]=1 and validationLevel > 1 ");
            }
            else
            {
                sb.Append(" FROM RESOURCE, CAPABILITY WHERE [@status]=1 and  RESOURCE.pkey = CAPABILITY.rkey");
            }

            if (capability.Length > 0)
                sb.Append(" and CAPABILITY.XSI_TYPE LIKE '%" + capability + "%'");

            if (title.Length > 0)
                sb.Append(" and title like '%" + title + "%'");

            if (shortname.Length > 0)
                sb.Append(" and shortname like '%" + title + "%'");

            if (identifier.Length > 0)
                sb.Append(" and identifier like '%" + identifier + "%' ");

            if (publisher.Length > 0)
                sb.Append(" and [curation/publisher] like '%" + publisher + "%' ");

            if (subject.Length > 0)
                sb.Append(" and [curation/subject] like '%" + subject + "%' ");

            if (waveband.Length > 0)
                sb.Append(" and [coverage/waveband] like '%" + waveband + "%' ");

            if (predicate.Length > 0)
                sb.Append(" and " + predicate);

            return sb.ToString();
        }

        public static string createXMLCapabilityResourceSelect(string predicate, string capability)
        {
            if (capability == null || capability == string.Empty)
                return createXMLResourceSelect(predicate, false);

            StringBuilder sb = new StringBuilder("Select RESOURCE.xml ");
            sb.Append(" FROM RESOURCE, CAPABILITY WHERE [@status]=1 and  RESOURCE.pkey = CAPABILITY.rkey");
            if (capability.Length > 0)
                sb.Append(" and CAPABILITY.XSI_TYPE LIKE '%" + capability + "%'");
            if (predicate.Length > 0)
                sb.Append(" and " + predicate);

            return sb.ToString();
        }

        public static string createXMLCapBandResourceSelect(string predicate, string capability, string band)
        {
            StringBuilder sb = new StringBuilder("Select RESOURCE.xml ");
            sb.Append(" FROM RESOURCE, CAPABILITY WHERE [@status]=1 and ");
            sb.Append("RESOURCE.[coverage/waveband] like '%" + band + "%' and RESOURCE.pkey = CAPABILITY.rkey and ");
            sb.Append(" CAPABILITY.XSI_TYPE LIKE '%" + capability + "%'");

            if (predicate.Length > 0)
            {
                sb.Append(" and ");
                sb.Append(predicate);
            }

            return sb.ToString();
        }

  
        
        public static string createFullResourceSelect(string predicate) 
		{
			// To collect deleted resources ALSO 
			string pubCols = String.Join(",",DBResource.Cols);
			string snCols = String.Join(",",ServiceSkyNode.Cols);
			string siaCols = String.Join(",",ServiceSimpleImageAccess.Cols);
			string coneCols = " MaxSearchRadius ";//String.Join(",",ServiceCone.Cols);

			StringBuilder sb = new StringBuilder("Select ");
			sb.Append(pubCols);
			sb.Append(',');
			sb.Append(snCols);
			sb.Append(',');
			sb.Append(siaCols);
			sb.Append(',');
			sb.Append(coneCols); 
			// select status of deleted sources also
			//			sb.Append(" FROM RESOURCE WHERE  status=1 and ") ;
			sb.Append(" FROM RESOURCE WHERE  (status=1 or status=3) and ") ;
			sb.Append( predicate );
	
			return sb.ToString();
		}

		public static string getInsertResourceStatement() 
		{
			if (null == InsertResourceStatement) 
			{
				StringBuilder sb = new StringBuilder("INSERT INTO Resource (");
				StringBuilder vals = new StringBuilder();

				for (int c =0; c < DBResource.Cols.Length; c++) 
				{
					sb.Append(DBResource.Cols[c]);// statemnt
					vals.Append('@');
					vals.Append(DBResource.Cols[c]); // values part append later
					sb.Append(',');
					vals.Append(',');
				}
				vals.Append("@PassPhrase");
				sb.Append("PassPhrase ) values (" );
				sb.Append(vals);
				sb.Append(')');
				InsertResourceStatement = sb.ToString();
			}
			return InsertResourceStatement;
		}


		public  static SqlCommand getInsertResourceCmd(SqlConnection conn) 
		{
			SqlCommand ins = conn.CreateCommand();
			ins.CommandText = getInsertResourceStatement();
			addParameters(ins,DBResource.Cols,DBResource.Types,DBResource.Sizes);
			ins.Parameters.Add("@PassPhrase",SqlDbType.VarChar,10);
			ins.Prepare() ;  // Calling Prepare after having setup commandtext and params.
			return ins;
		}

        public static SqlCommand getRkeyLookupCmd(SqlConnection conn)
        {
            SqlCommand cmd = conn.CreateCommand();
            cmd.CommandText = getRKeyStatement;
            cmd.Parameters.Add("@Identifier", SqlDbType.VarChar, 500);
            cmd.Prepare();  // Calling Prepare after having setup commandtext and params.
            return cmd;
        }
		public static SqlCommand getUpdateStatusCmd(SqlConnection conn) 
		{
			SqlCommand cmd = conn.CreateCommand();
			cmd.CommandText = updateStatus;
			cmd.Parameters.Add("@Identifier",SqlDbType.VarChar,500);
			cmd.Prepare() ;  // Calling Prepare after having setup commandtext and params.
			return cmd;
		}
		public static SqlCommand getGetPassPhraseCmd(SqlConnection conn) 
		{
			SqlCommand cmd = conn.CreateCommand();
			cmd.CommandText = getPassPhrase;
			cmd.Parameters.Add("@Identifier",SqlDbType.VarChar,500);
			cmd.Prepare() ;  // Calling Prepare after having setup commandtext and params.
			return cmd;
		}

		public static SqlCommand getResRelationsCmd(SqlConnection conn) 
		{
			SqlCommand cmd = conn.CreateCommand();
			cmd.CommandText = getResRelationsStatement;
			cmd.Parameters.Add("@dbid",SqlDbType.BigInt,8);
			cmd.Prepare() ;  // Calling Prepare after having setup commandtext and params.
			return cmd;
		}

		public static SqlCommand getResInterfacesCmd(SqlConnection conn) 
		{
			SqlCommand cmd = conn.CreateCommand();
			cmd.CommandText = getResInterfacesStatement;
			cmd.Parameters.Add("@dbid",SqlDbType.BigInt,8);
			cmd.Prepare() ;  // Calling Prepare after having setup commandtext and params.
			return cmd;
		}

		public static SqlCommand getInterfaceParamsCmd(SqlConnection conn) 
		{
			SqlCommand cmd = conn.CreateCommand();
			cmd.CommandText = getInterfaceParamsStatement;
			cmd.Parameters.Add("@dbid",SqlDbType.BigInt,8);
			cmd.Parameters.Add("@interfaceNum",SqlDbType.BigInt,8);
			cmd.Prepare() ;  // Calling Prepare after having setup commandtext and params.
			return cmd;
		}

		/// <summary>
		/// does not actually delet just flags it deleted
		/// </summary>
		/// <param name="conn"></param>
		/// <returns></returns>
		public static SqlCommand getGetDeleteResourceCmd(SqlConnection conn) 
		{
			SqlCommand cmd = conn.CreateCommand();
			cmd.CommandText = deleteResourceStatement + " and (@PassPhrase = '" + dbAdmin + "')";
			cmd.Parameters.Add("@Identifier",SqlDbType.VarChar,500);
			cmd.Parameters.Add("@PassPhrase",SqlDbType.VarChar,10);
			cmd.Prepare() ;  // Calling Prepare after having setup commandtext and params.
			return cmd;
		}

        public static string getGetResourceCacheCmd()
        {
            return "select ResourceAsRow from ResourceVOTableCache inner join resource on resource.pkey = ResourceVOTableCache.rkey ";
        }
        public static string getResourceCacheNotNullCmd()
        {
            return " and ResourceAsRow is not null";
        }

        public static string getGetInterfacesCacheCmd()
        {
            return "select InterfaceAsRow from ResourceVOTableCache inner join resource on resource.pkey = ResourceVOTableCache.rkey ";
        }
        public static string getInterfaceCacheNotNullCmd()
        {
            return " and InterfaceAsRow is not null";
        }

        public static string createAdvancedResourceCacheSelect(string predicate, string capability, string title, string shortname, string identifier, 
                                                               string publisher, string subject, string waveband)
        {
            StringBuilder sb = new StringBuilder();
            sb.Append(SQLHelper.getGetResourceCacheCmd());
            sb.Append(" WHERE [@status]=1 and validationLevel > 1 ");

            if (capability.Length > 0)
                sb.Append(" and ResourceAsRow like '%" + capability + "%' ");

            if (title.Length > 0)
                sb.Append(" and title like '%" + title + "%' ");

            if (shortname.Length > 0)
                sb.Append(" and shortname like '%" + shortname + "%' ");

            if (identifier.Length > 0)
                sb.Append(" and identifier like '%" + identifier + "%' ");

            if (publisher.Length > 0)
                sb.Append(" and [curation/publisher] like '%" + publisher + "%' ");

            if (subject.Length > 0)
                sb.Append(" and [curation/subject] like '%" + subject + "%' ");

            if (waveband.Length > 0)
                sb.Append(" and [coverage/waveband] like '%" + waveband + "%' ");
            
            if (predicate.Length > 0)
                sb.Append(" and " + predicate);

            sb.Append(SQLHelper.getResourceCacheNotNullCmd());

            return sb.ToString();
        }

        public static string createCapabilityPredicateSelectUsingCache(string predicate, string capability, int option)
        {
            StringBuilder sb = new StringBuilder();
            if (option == 1)
                sb.Append(SQLHelper.getGetResourceCacheCmd());
            else
                sb.Append(SQLHelper.getGetInterfacesCacheCmd());

            sb.Append(" WHERE [@status]=1 ");

            if (capability.Length > 0)
            {
                //sb.Append(" JOIN CAPABILITY ON RESOURCE.pkey = CAPABILITY.rkey WHERE [@status]=1 ");
                //sb.Append(" and CAPABILITY.XSI_TYPE LIKE '%" + capability + "%'");

                if( option == 1)
                    sb.Append(" and ResourceAsRow like '%" + capability + "%' ");
                else
                    sb.Append(" and InterfaceAsRow like '%" + capability + "%' ");

            }
            if (predicate.Length > 0)
                sb.Append(" and " + predicate);
            //sb.Append(" and [@status] = 1");
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

		// add params to prepared statement
		public static void addParameters(SqlCommand cmd, string[] pars, SqlDbType[] types, int[] sizes) 
		{
			if (pars.Length > types.Length || pars.Length > sizes.Length) 
			{
				throw new Exception("Arrays are not same lenght in addParemters "+
					pars.Length+" "+types.Length+" "+sizes.Length);
			}
			for (int p=0; p < pars.Length; p++) 
			{
				cmd.Parameters.Add ('@'+pars[p],types[p],sizes[p]);
			}
		}
	
        /*
         * 
		/// <summary>
		/// remember to set the db id before calling this !
		/// </summary>
		/// <param name="pars"></param>
		/// <param name="res"></param>
		/// <param name="PassPhrase"></param>
		public static void grabResourceParameters(SqlParameterCollection pars, DBResource res) 
		{
			int ind = 0;	  
												  
			pars[ind++].Value = res.dbid;	//	dbid,
			pars[ind++].Value = res.status; 
			pars[ind++].Value = res.Identifier;			//	@Identifier,
			pars[ind++].Value = res.Title;				//	@Title,
			pars[ind++].Value = res.ShortName ;
			pars[ind++].Value = res.CurationPublisherName;			//	@Publisher,
			pars[ind++].Value = res.CurationPublisherIdentifier;			//	@Publisher,
			pars[ind++].Value = res.CurationPublisherDescription;			//	@Publisher,
			pars[ind++].Value = res.CurationPublisherReferenceUrl;			//	@Publisher,
			pars[ind++].Value = res.CurationCreatorName;			//	@Creator,
			pars[ind++].Value = res.CurationCreatorLogo;			//	@Creator,
			pars[ind++].Value = res.CurationContributor;		//	@Contributor,
			pars[ind++].Value = res.CurationDate;				//	@Date,
			pars[ind++].Value = res.CurationVersion;			//	@Version,
			pars[ind++].Value = res.CurationContactName;				//	@ContactName,
			pars[ind++].Value = res.CurationContactEmail;				//	@ContactEmail,
			pars[ind++].Value = res.CurationContactAddress;				//	@ContactEmail,
			pars[ind++].Value = res.CurationContactPhone;				//	@ContactEmail,
			try 
			{
				pars[ind++].Value = String.Join(",",res.Subject);//		@Subject,
			}
			catch (Exception ) {} ;
			pars[ind++].Value = res.Description;		//	@Description,
			pars[ind++].Value = res.ReferenceURL;		//	@ReferenceURL,
			pars[ind++].Value = res.Type;		//	@Type,
			pars[ind++].Value = res.Facility;			//	@Facility,
			try 
			{
				pars[ind++].Value = String.Join(",",res.Instrument);	//	@Instrument,
			}
			catch (Exception ) {} ;
			try 
			{
				pars[ind++].Value = String.Join(",",res.ContentLevel);//		@ContentLevel,
			}
			catch (Exception ) {} ;
			pars[ind++].Value = res.ModificationDate;						
			pars[ind++].Value = res.ServiceURL;			//	@ServiceURL,

			pars[ind++].Value = res.CoverageSpatial;	//	@Coverage,
			try 
			{
				pars[ind++].Value = String.Join(",",res.CoverageSpectral);	//	@Coverage,
			}
			catch (Exception ) {} ;
			pars[ind++].Value = res.CoverageTemporal;	//	@Coverage,
			pars[ind++].Value = res.CoverageRegionOfRegard;	//	@Coverage,


			pars[ind++].Value = res.ResourceType;		//	@ResourceType
			pars[ind++].Value = res.xml;	// xml document
			pars[ind++].Value = res.harvestedfrom;	// url this came from 
			pars[ind++].Value = res.harvestedfromDate;			
			pars[ind++].Value = res.footprint;
			pars[ind++].Value = res.validationLevel;

			if (ind < DBResource.Cols.Length ) 
			{
				throw new Exception("Not all parmeters set in resource SQLHelper.grabParams set "+ind+
					"  have "+DBResource.Cols.Length);
			}
			for (int i = 0 ; i < pars.Count; i++) 
			{
				if (pars[i].Value == null) pars[i].Value="NOT PROVIDED";
			}
			
		}

		public static void grabConeParameters(SqlParameterCollection pars, ServiceCone res) 
		{
			int ind = 0;	  
			pars[ind++].Value = res.dbid;	//	dbid,												  
			pars[ind++].Value = res.MaxSearchRadius; 
			pars[ind++].Value = res.MaxRecords; 
			pars[ind++].Value = res.VOTableColumns==null ? "" : res.VOTableColumns; 					 

		}

		public static void grabSkyNodeParameters(SqlParameterCollection pars, ServiceSkyNode res) 
		{
			int ind = 0;	  
			pars[ind++].Value = res.dbid;	//	dbid,												  
			pars[ind++].Value = res.Compliance; 					 
			pars[ind++].Value = res.Latitude; 					 
			pars[ind++].Value = res.Longitude; 					 
			pars[ind++].Value = res.PrimaryTable==null ? "" :res.PrimaryTable; 
			pars[ind++].Value = res.PrimaryKey ==null ? "" :res.PrimaryKey; 
			pars[ind++].Value = res.MaxRecords; 

		}

		public static void grabSiapParameters(SqlParameterCollection pars, ServiceSimpleImageAccess res) 
		{
			int ind = 0;	  										  
			pars[ind++].Value = res.dbid;	//	dbid,
			pars[ind++].Value = res.VOTableColumns==null ? "" : res.VOTableColumns;
			pars[ind++].Value = res.ImageServiceType==null ?"" : res.ImageServiceType; 
			pars[ind++].Value = res.MaxQueryRegionSizeLat;
			pars[ind++].Value = res.MaxQueryRegionSizeLong;
			pars[ind++].Value = res.MaxImageExtentLat;
			pars[ind++].Value = res.MaxImageExtentLong;
			pars[ind++].Value = res.MaxImageSizeLat;
			pars[ind++].Value = res.MaxImageSizeLong;
			pars[ind++].Value = res.MaxFileSize;
			pars[ind++].Value = res.MaxRecords;
				 
		}
		public static void grabRelationshipParameters(SqlParameterCollection pars, DBResource res, int relInd) 
		{
			int ind = 0;	  
			ResourceRelation rel = res.resourceRelations[relInd];
												  
			pars[ind++].Value = res.dbid;	//	dbid,
			pars[ind++].Value = rel.relatedResourceIvoId;
			pars[ind++].Value = rel.relatedResourceName;
 			pars[ind++].Value = rel.relationshipType;
			
		}
		public static void grabInterfaceParameters(SqlParameterCollection pars, DBResource res, int intInd) 
		{
			int ind = 0;	  
			ResourceInterface inf = res.resourceInterfaces[intInd];
												  
			pars[ind++].Value = res.dbid;					// dbid,
			pars[ind++].Value = inf.interfaceNum;			// interfaceNum
			pars[ind++].Value = inf.type;					// type
			pars[ind++].Value = inf.qtype;					// qtype
			pars[ind++].Value = inf.accessURL;				// accessURL
			pars[ind++].Value = inf.resultType;				// resultType
		}
		public static void grabParamParameters(SqlParameterCollection pars, DBResource res, int interfaceInd, int parInd) 
		{
			int ind = 0;	  
			InterfaceParam par = res.resourceInterfaces[interfaceInd].interfaceParams[parInd];
												  
			pars[ind++].Value = res.dbid;				// dbid,
			pars[ind++].Value = interfaceInd;			// interfaceNum
			pars[ind++].Value = par.name;					
			pars[ind++].Value = par.description;
			pars[ind++].Value = par.datatype;
			pars[ind++].Value = par.unit;
			pars[ind++].Value = par.ucd;
		}
         * 
         * 
         */ 
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
                        sb.Append(" contains (xml,'*" + keys[k] + "* ')");
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
                        sb.Append(" contains (xml,'\"*" + keys[k] + "*\" ')");
                    }
		
				}		
			}

			return sb.ToString();
		}

		public static string Revision
		{
			get
			{
				return "$Revision: 1.7 $";
			}
		}

        
	}
}


		

		

/* Log of changes
 * $Log: SQLHelper.cs,v $
 * Revision 1.7  2006/02/28 17:09:49  grgreene
 * delete for oai pub
 *
 * Revision 1.6  2006/02/22 16:26:46  grgreene
 * added the OAI header status attrib
 *
 * Revision 1.5  2005/12/19 18:08:57  grgreene
 * validationLEvel can edit now
 *
 * Revision 1.4  2005/06/09 18:28:29  grgreene
 * fixed keyword multi white space
 *
 * Revision 1.3  2005/06/09 17:21:50  grgreene
 * fixed multiple space keyword search
 *
 * Revision 1.2  2005/05/27 18:53:47  grgreene
 * fixed identifier trim
 *
 * Revision 1.1.1.1  2005/05/05 15:17:05  grgreene
 * import
 *
 * Revision 1.20  2004/11/09 21:11:01  womullan
 * added relation get
 *
 * Revision 1.19  2004/11/05 18:45:28  womullan
 * relations added
 *
 * Revision 1.18  2004/11/01 18:30:16  womullan
 * v0.10 upgrade
 *
 * Revision 1.17  2004/08/12 17:23:14  womullan
 *  added new cls for SkyNode PrimaryTabel PrimaryKey max records, fixed it on the forms also fixed repluicator to deal with new fiedls
 *
 * Revision 1.16  2004/08/12 15:18:33  womullan
 * fixed replicator
 *
 * Revision 1.15  2004/07/22 14:54:06  womullan
 * trim space from keywords
 *
 * Revision 1.14  2004/07/08 18:08:38  womullan
 * skynode lat/lon added
 *
 * Revision 1.13  2004/04/23 22:32:15  womullan
 *  delte updates moddate , do not accept rec if delted and mod date higher than incomming
 *
 * Revision 1.12  2004/04/05 18:17:35  womullan
 *  fixed type casts for MaxSearchRadius and MaxRecords
 *
 * Revision 1.11  2004/04/01 19:28:34  womullan
 *  fix for nulls in sia
 *
 * Revision 1.10  2004/04/01 17:24:29  womullan
 *  insert/update fixed
 *
 * Revision 1.9  2004/03/31 17:28:26  womullan
 * changes for new schema
 *
 * Revision 1.8  2004/03/25 16:29:21  womullan
 *  contains for inverted index
 *
 * Revision 1.7  2004/02/05 18:48:47  womullan
 * added sqlquery and harvestedfromDate
 *
 * Revision 1.6  2003/12/18 19:45:18  womullan
 * updated harvester
 *
 * Revision 1.5  2003/12/15 21:00:39  womullan
 * relations and Harvested from added
 *
 * Revision 1.4  2003/12/06 19:29:07  womullan
 * all working insert update
 *
 * Revision 1.3  2003/12/05 13:41:41  womullan
 *  cone siap skynode insert working
 *
 * Revision 1.2  2003/12/04 19:46:39  womullan
 *  now working for Resource
 *
 * Revision 1.1  2003/12/03 23:00:15  womullan
 *  many mods to get SQL working
 *
 * 
 * */
