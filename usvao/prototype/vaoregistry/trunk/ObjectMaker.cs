using System;
using System.Data;
using System.Data.SqlClient;
using System.IO;
using System.Reflection;
using System.Text;
using System.Xml;
using System.Xml.Serialization;

using ivoa.net.vr1_0;
using net.ivoa.vr0_10;
using ivoa.net.ri1_0;
using oai_dc;

namespace registry
{
	/// <summary>
	/// Mkae correct ibject for a row - get auxiliary data
 	///ID:		$Id: ObjectMaker.cs,v 1.6 2006/02/28 17:09:49 grgreene Exp $
	///Revision:	$Revision: 1.6 $
	///Date:	$Date: 2006/02/28 17:09:49 $
	/// </summary>
	public class ObjectMaker
	{

		public static void GetData(ServiceSkyNode res, DataRow dr ) 
		{
            try
            {
                int ind = 1; // 0 is dbid
                res.Compliance = (string)dr[ServiceSkyNode.Cols[ind++]];
                res.Latitude = (double)dr[ServiceSkyNode.Cols[ind++]];
                res.Longitude = (double)dr[ServiceSkyNode.Cols[ind++]];
                res.PrimaryTable = (string)dr[ServiceSkyNode.Cols[ind++]];
                res.PrimaryKey = (string)dr[ServiceSkyNode.Cols[ind++]];
                res.MaxRecords = (long)dr[ServiceSkyNode.Cols[ind++]];
            }
            catch (Exception) { }
		}

		public static void GetData(ServiceSimpleImageAccess res, DataRow  dr) 
		{
			int ind=1; // dbid is 0
										
			try
			{
				res.VOTableColumns	=	(string)dr[ServiceSimpleImageAccess.Cols[ind++]];
			}
			catch(Exception){}
			try
			{
				res.ImageServiceType	=	(string)dr[ServiceSimpleImageAccess.Cols[ind++]];
			}
			catch(Exception){}

            try
            {
                if (dr[ServiceSimpleImageAccess.Cols[ind++]] != null)
                    res.MaxQueryRegionSizeLat = (float)dr[ServiceSimpleImageAccess.Cols[ind - 1]];
            }
            catch (Exception) { }
            try
            {
                if (dr[ServiceSimpleImageAccess.Cols[ind++]] != null)
                    res.MaxQueryRegionSizeLong = (float)dr[ServiceSimpleImageAccess.Cols[ind - 1]];
            }
            catch (Exception) { }
            try
            {
                if (dr[ServiceSimpleImageAccess.Cols[ind++]] != null)
                    res.MaxImageExtentLat = (float)dr[ServiceSimpleImageAccess.Cols[ind - 1]];
            }
            catch (Exception) { }
            try
            {
                if (dr[ServiceSimpleImageAccess.Cols[ind++]] != null)
                    res.MaxImageExtentLong = (float)dr[ServiceSimpleImageAccess.Cols[ind - 1]];
            }
            catch (Exception) { }
            try
            {
                if (dr[ServiceSimpleImageAccess.Cols[ind++]] != null)
                    res.MaxImageSizeLat = (int)((double)dr[ServiceSimpleImageAccess.Cols[ind - 1]]);
            }
            catch (Exception) { }
			try
			{			
				if (dr[ServiceSimpleImageAccess.Cols[ind++]] != null) 
				res.MaxImageSizeLong			=	(int)((double)dr[ServiceSimpleImageAccess.Cols[ind-1]]);
			}
			catch(Exception ) {}
			try
			{			
				if (dr[ServiceSimpleImageAccess.Cols[ind++]] != null) 
				res.MaxFileSize					=	(long)dr[ServiceSimpleImageAccess.Cols[ind-1]];
			}
			catch(Exception ) {}
			try
			{			
				if (dr[ServiceSimpleImageAccess.Cols[ind++]] != null) 				
				res.MaxRecords					=	(long)dr[ServiceSimpleImageAccess.Cols[ind-1]];
			}
			catch(Exception ) {}

		}

		public static void GetData(ServiceCone res, DataRow dr) 
		{
			int ind = 1;// 0 is dbid 
			try
			{
				res.MaxSearchRadius		=	(double)dr[ServiceCone.Cols[ind++]];
			}
			catch(Exception ) {}
			try{
				res.MaxRecords			=	(long)dr[ServiceCone.Cols[ind++]];
			} 
			catch (Exception ) {}		
			try
			{
				res.VOTableColumns	=	(string)dr[ServiceCone.Cols[ind++]];
			}
			catch(Exception ) {}
		}

		public static void GetData(ResourceRelation rr, DataRow dr) 
		{
			int ind = 1;// 0 is dbid 
			try
			{
				rr.relatedResourceIvoId		=	(string)dr[ResourceRelation.Cols[ind++]];
			}
			catch(Exception ) {}
			try
			{
				rr.relatedResourceName		=	(string)dr[ResourceRelation.Cols[ind++]];
			} 
			catch (Exception ) {}		
			try
			{
				rr.relationshipType		=	(string)dr[ResourceRelation.Cols[ind++]];
			}
			catch(Exception ) {}
		}
		public static void GetData(ResourceInterface ri, DataRow dr) 
		{
			int ind = 1;// 0 is dbid 
			try
			{
				ri.interfaceNum		=	(int)dr[ResourceInterface.Cols[ind++]];
			}
			catch(Exception ) {}
			try
			{
				ri.type	=	(string)dr[ResourceInterface.Cols[ind++]];
			} 
			catch (Exception ) {}		
			try
			{
				ri.qtype	=	(string)dr[ResourceInterface.Cols[ind++]];
			} 
			catch (Exception ) {}	
			try
			{
				ri.accessURL =	(string)dr[ResourceInterface.Cols[ind++]];
			} 
			catch (Exception ) {}	
			try
			{
				ri.resultType	=	(string)dr[ResourceInterface.Cols[ind++]];
			} 
			catch (Exception ) {}	
		}


		/// <summary>
		/// for voresource.10
		/// </summary>
		/// <param name="rr"></param>
		/// <param name="dr"></param>
		public static net.ivoa.vr0_10.Interface CreateInterface( DataRow dr) 
		{
			net.ivoa.vr0_10.Interface theIf = null;
			int ind = 3;// for qtype, 0 is dbid , 1 is interface num, 2 was type			
			try
			{
				string ift = (string)dr[ResourceInterface.Cols[2]];//type
				Assembly myass = Assembly.GetExecutingAssembly();
				System.Type myType = myass.GetType("net.ivoa.vr0_10."+ift,false);
				theIf = (net.ivoa.vr0_10.Interface)Activator.CreateInstance(myType);
			}
			catch (Exception ) {}
			try
			{
				if (theIf is net.ivoa.vr0_10.ParamHTTP)
                    ((net.ivoa.vr0_10.ParamHTTP)theIf).qtype = (string)dr[ResourceInterface.Cols[ind]] == "GET" ? net.ivoa.vr0_10.HTTPQueryType.GET : net.ivoa.vr0_10.HTTPQueryType.POST;
			} 
			catch (Exception ) {}	
			ind++;
			try
			{
				theIf.accessURL = new net.ivoa.vr0_10.AccessURL();
				theIf.accessURL.Value = (string)dr[ResourceInterface.Cols[ind++]];
			} 
			catch (Exception ) {}	
			try
			{
				if (theIf is net.ivoa.vr0_10.ParamHTTP) 
				((net.ivoa.vr0_10.ParamHTTP)theIf).resultType	=	(string)dr[ResourceInterface.Cols[ind]];
			} 
			catch (Exception ) {}	
			ind++;

			return theIf;
		}

		public static void GetData(InterfaceParam ip, DataRow dr) 
		{
			int ind = 1;// 0 is dbid 
			try
			{
				ip.interfaceNum		=	(int)dr[InterfaceParam.Cols[ind++]];
			}
			catch(Exception ) {}
			try
			{
				ip.name		=	(string)dr[InterfaceParam.Cols[ind++]];
			}
			catch(Exception ) {}
			try
			{
				ip.description	=	(string)dr[InterfaceParam.Cols[ind++]];
			}
			catch(Exception ) {}
			try
			{
				ip.datatype		=	(string)dr[InterfaceParam.Cols[ind++]];
			}
			catch(Exception ) {}
			try
			{
				ip.unit		=	(string)dr[InterfaceParam.Cols[ind++]];
			}
			catch(Exception ) {}
			try
			{
				ip.ucd		=	(string)dr[InterfaceParam.Cols[ind++]];
			}
			catch(Exception ) {}
		}

		public static void GetVOData(OSNCapability res, DataRow dr ) 
		{
			try
			{
				res.Compliance		=	(string)dr["Compliance"];
				res.Longitude		=	(double)dr["Longitude"];
				res.Latitude		=	(double)dr["Latitude"];
				res.PrimaryTable	=	(string)dr["PrimaryTable"];
				res.PrimaryKey		=	(string)dr["PrimaryKey"];
				res.MaxRecords		=	(long)dr["MaxRecords"];

			}
			catch(Exception ) {}
		}

		public static void GetVOData(SIACapability res, DataRow  dr) 
		{
			int ind=1; // dbid is 0
										
			try
			{
				ind++;  // temporary until we figure what to do with VOTableCol
						// for voresource schema
				//res.VOTableColumns	=	dr[ServiceSimpleImageAccess.Cols[ind]];
			}
			catch(Exception ) {}
			try
			{
				
				string type = (string)dr[ServiceSimpleImageAccess.Cols[ind++]];
				if (type.StartsWith("Cut"))
				{
					res.imageServiceType = net.ivoa.vr0_10.ImageServiceType.Cutout;	
				}
				if (type.StartsWith("Atlas"))
				{
                    res.imageServiceType = net.ivoa.vr0_10.ImageServiceType.Atlas;	
				}
				if (type.StartsWith("Mosaic"))
				{
                    res.imageServiceType = net.ivoa.vr0_10.ImageServiceType.Mosaic;	
				}
				if (type.StartsWith("Point"))
				{
                    res.imageServiceType = net.ivoa.vr0_10.ImageServiceType.Pointed;	
				}
			}
			catch(Exception ) {}
		
			try
			{
				// DO NOT CHANGE THE ORDER COL NAMES come from the CLASS
                res.maxQueryRegionSize = new net.ivoa.vr0_10.SkySize();
				res.maxQueryRegionSize.lat		=	(float)dr[ServiceSimpleImageAccess.Cols[ind++]];
				res.maxQueryRegionSize.@long	=	(float)dr[ServiceSimpleImageAccess.Cols[ind++]];
			}
			catch (Exception ) {};
            res.maxImageExtent = new net.ivoa.vr0_10.SkySize();
			try
			{
				res.maxImageExtent.lat			=	(float)dr[ServiceSimpleImageAccess.Cols[ind++]];
				res.maxImageExtent.@long			=	(float)dr[ServiceSimpleImageAccess.Cols[ind++]];
			}
			catch (Exception ) {};

            res.maxImageSize = new net.ivoa.vr0_10.ImageSize();
			try
			{
				res.maxImageSize.lat			=	(int)dr[ServiceSimpleImageAccess.Cols[ind++]];
				res.maxImageSize.@long			=	(int)dr[ServiceSimpleImageAccess.Cols[ind++]];
			}
			catch (Exception ) {};

	
			try
			{
				res.maxFileSize					=	(int)dr[ServiceSimpleImageAccess.Cols[ind++]];
			}
			catch (Exception ) {};

			try
			{
				res.maxRecords					=	(int)(long)dr[ServiceSimpleImageAccess.Cols[ind++]];
			}
			catch (Exception ) {};
			

		}

		public static void GetVOData(ConeSearchCapability res, DataRow dr) 
		{
			int ind = 1;// 0 is dbid 
			try
			{		
				res.maxSR		=	float.Parse(dr[ServiceCone.Cols[ind++]].ToString());
			}			
			catch{}
			try
			{
				res.maxRecords	=	int.Parse(dr[ServiceCone.Cols[ind++]].ToString());
			}
			catch{}
			try
			{
				ind++;
				// ... Until resolve VOResource schema including votable columns
                // res.VOTableColumns	=	dr.GetString(ind++);
			}
			catch{}


		}

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

        public static ivoa.net.ri1_0.server.Resource CreateRI10ResourceFromXML( String xml)
        {
            ivoa.net.ri1_0.server.Resource vor = null;
            if (xml != null)
            {
                StringReader srdr = new StringReader(xml);
                XmlTextReader rdr = new XmlTextReader(srdr);
                XmlSerializer ser = new XmlSerializer(typeof(ivoa.net.ri1_0.server.Resource));

                object o = ser.Deserialize(rdr);
                vor = o as ivoa.net.ri1_0.server.Resource;
            }
            else
            {
                vor = new ivoa.net.ri1_0.server.Resource();
            }
            return vor;
        }

        // Taking the ntext from the xml Datarow and converting to VOR10 xml
        public static ivoa.net.vr1_0.Resource CreateVOR10Resource(DataRow dr)
        {
            String strVOR = (string)dr["xml"];
            StringReader srdr = new StringReader(strVOR);
            XmlTextReader rdr = new XmlTextReader(srdr);
            XmlSerializer ser = new XmlSerializer(typeof(ivoa.net.vr1_0.Resource));

            object o = ser.Deserialize(rdr);
            ivoa.net.vr1_0.Resource vor = o as ivoa.net.vr1_0.Resource;
            return vor;

/*            ivoa.net.vr1_0.Resource res = new ivoa.net.vr1_0.Resource();
          
            String strVOR = (string)dr["xml"];

            XmlSerializer ser1 = new XmlSerializer(typeof(ivoa.net.vr1_0.Resource));
            StringBuilder sb = new StringBuilder();
            StringWriter sw = new StringWriter(sb);
            ser1.Serialize(sw, strVOR);
            sw.Close();           

            XmlSerializer ser = new XmlSerializer(typeof(ivoa.net.vr1_0.Resource));
            StringReader sr = new StringReader(sb.ToString());

            res = (ivoa.net.vr1_0.Resource)ser.Deserialize(sr);

            return res;
 */
        }

        // Taking the ntext from the xml Datarow and converting to VOR10 xml
        public static ivoa.net.ri1_0.server.Resource ConvertVOR10ToRIRes(ivoa.net.vr1_0.Resource vor)
        {
            XmlSerializer ser = new XmlSerializer(typeof(ivoa.net.vr1_0.Resource));
            StringBuilder sb = new StringBuilder();
            StringWriter sw = new StringWriter(sb);
            ser.Serialize(sw, vor);
            sw.Close();

            XmlSerializer ser2 = new XmlSerializer(typeof(ivoa.net.ri1_0.server.Resource));
            StringReader sr = new StringReader(sb.ToString());

            return (ivoa.net.ri1_0.server.Resource)ser2.Deserialize(sr);
        }

        // Creates the XML VOResource v 0.10
		public static net.ivoa.vr0_10.Resource CreateVOResource(DataRow dr) 
		{
			net.ivoa.vr0_10.Resource res = new net.ivoa.vr0_10.Resource();
			String resourceType = (string)dr["ResourceType"];
			resourceType = resourceType.ToUpper();

			if (resourceType.StartsWith("CONE")) 
			{
                res = new net.ivoa.vr0_10.ConeSearch();
				ConeSearchCapability cap = new ConeSearchCapability();
                ((net.ivoa.vr0_10.ConeSearch)res).capability = cap;
			
			}
			if ( (resourceType.StartsWith("SIAP")) || (resourceType.StartsWith("SSAP")) )
//			if (resourceType.StartsWith("SIAP"))
			{
                res = new net.ivoa.vr0_10.SimpleImageAccess();
				SIACapability cap = new SIACapability();
                ((net.ivoa.vr0_10.SimpleImageAccess)res).capability = cap;
				
			}
			if (resourceType.StartsWith("SKYNODE")) 
			{
                res = new net.ivoa.vr0_10.OpenSkyNode();
				OSNCapability cap = new OSNCapability();
                ((net.ivoa.vr0_10.OpenSkyNode)res).capability = cap;
			}
			if (resourceType.StartsWith("AUTHORITY")) 
			{
                res = new net.ivoa.vr0_10.Authority();
			}



			res.title		= 	(string)dr["Title"];
			try
			{
                net.ivoa.vr0_10.Content cont = new net.ivoa.vr0_10.Content();
				res.content = cont;
				cont.description = (string)dr["Description"];
				cont.referenceURL = (string)dr["ReferenceURL"];
				try 
				{
					cont.subject =	((string)dr["Subject"]).Split(',');
				} 
				catch (Exception ) {}
			}
			catch (Exception){}
	
			try
			{
				res.identifier = (string)dr["Identifier"];
			}
			catch (Exception){}
			
			res.shortName = (string)dr["ShortName"];
			

//////////
            res.curation = new net.ivoa.vr0_10.Curation();
            res.curation.publisher = new net.ivoa.vr0_10.ResourceName();
            res.curation.creator = new net.ivoa.vr0_10.Creator();
			
			
			try {
				res.curation.publisher.Value	=	(string)dr["CurationPublisherName"];
			} 
			catch (Exception ) {}
//			try 
//			{
//				res.Curation.Publisher.Description	=	(string)dr["CurationPublisherDescription"];
//			}
//			catch (Exception ) {}
			try 
			{
				res.curation.publisher.ivoid	=	 (string)dr["CurationPublisherIdentifier"];
			}
			catch (Exception ) {}
			
			try
			{
				res.shortName		=	(string)dr["ShortName"];
			}
			catch(Exception ) {}

			try
			{
				res.curation.creator.name.Value		=	(string)dr["CurationCreatorName"];
			}
			catch(Exception ) {}
			try
			{
				res.content.subject		=   ((string)dr["Subject"]).Split(',');
			}
			catch(Exception ) {}

			try
			{
                res.curation.contributor = new net.ivoa.vr0_10.ResourceName[1];
                res.curation.contributor[0] = new net.ivoa.vr0_10.ResourceName();
				res.curation.contributor[0].Value	=	(string)dr["CurationContributor"];
			}
			catch(Exception ) {}
			try
			{
                res.curation.date = new net.ivoa.vr0_10.Date[1];
				res.curation.date[0].Value	=	DateTime.Parse((string)dr["CurationDate"]);
			}
			catch(Exception ) {}
			try
			{
				res.curation.version		=	(string)dr["CurationVersion"];
			}
			catch(Exception ) {}

			try
			{
				res.curation.contact.name.Value		=	(string)dr["CurationContactName"];
			}
			catch(Exception ) {}
			try
			{
				res.curation.contact.email		=	(string)dr["CurationContactEmail"];
			}
			catch(Exception ) {}

/*			try
			{
				res.CoverageSpatial	=	(string)dr["CoverageSpatial"];
			}
			catch(Exception ) {}
			try
			{
				res.CoverageSpectral =	((string)dr["CoverageSpectral"]).Split(',');
			}
			catch(Exception ) {}
			try
			{
				res.CoverageTemporal	=	(string)dr["CoverageTemporal"];
			}
			catch(Exception ) {}
			try
			{
				res.CoverageRegionOfRegard	=	(double)dr["CoverageRegionOfRegard"];
			}
			catch(Exception ) {}
*/
			try
			{
				string[] levs = ((string)dr["ContentLevel"]).Split(',');
				if (levs.Length > 0) 
				{
                    res.content.contentLevel = new net.ivoa.vr0_10.ContentLevel[levs.Length];
					for (int ll=0; ll<levs.Length; ll++)
					{
						switch (levs[ll].ToUpper())
						{
							case "RESEARCH":
                                res.content.contentLevel[ll] = net.ivoa.vr0_10.ContentLevel.Research;
							break;
							case "UNIVERSITY":
                                res.content.contentLevel[ll] = net.ivoa.vr0_10.ContentLevel.University;
								break;
							case "INFORMALEDUCATION":
                                res.content.contentLevel[ll] = net.ivoa.vr0_10.ContentLevel.InformalEducation;
								break;
							case "ELEMENTARYEDUCATION":
                                res.content.contentLevel[ll] = net.ivoa.vr0_10.ContentLevel.ElementaryEducation;
								break;
							case "MIDDLESCHOOLEDUCATION":
                                res.content.contentLevel[ll] = net.ivoa.vr0_10.ContentLevel.MiddleSchoolEducation;
								break;
							case "SECONDARYEDUCATION":
                                res.content.contentLevel[ll] = net.ivoa.vr0_10.ContentLevel.SecondaryEducation;
								break;
							case "COMMUNITYCOLLEGE":
                                res.content.contentLevel[ll] = net.ivoa.vr0_10.ContentLevel.CommunityCollege;
								break;
							case "AMATEUR":
                                res.content.contentLevel[ll] = net.ivoa.vr0_10.ContentLevel.Amateur;
								break;
							default:
                                res.content.contentLevel[ll] = net.ivoa.vr0_10.ContentLevel.General;
								break;

							}
					}
				}
			}
			catch(Exception ) {}
			try
			{
				int stat = (int)dr["status"];
				if (stat == 3)
				{
                    net.ivoa.vr0_10.ResourceStatus status = net.ivoa.vr0_10.ResourceStatus.deleted;
					res.status = status;
				}
				else
				{
					res.updated	=	 (DateTime)dr["modificationDate"];
					res.updatedSpecified = true;
				}
			}
			catch(Exception ) {}

/*			try
			{
				res.Facility	=	(string)dr["Facility"];
			}
			catch(Exception ) {}
			try
			{
				res.Instrument	=	((string)dr["Instrument"]).Split(',');
			}
			catch(Exception ) {}
*/

			return res;
		}

		public static oai_dc.oai_dcType CreateOAIDC(DataRow dr) 
		{
			
			oai_dc.oai_dcType odt = new oai_dc.oai_dcType();

			odt.ItemsElementName = new ItemsChoiceType[6];
			
			odt.ItemsElementName[0] = ItemsChoiceType.title;
			odt.ItemsElementName[1] = ItemsChoiceType.description;
			odt.ItemsElementName[2] = ItemsChoiceType.identifier;
			odt.ItemsElementName[3] = ItemsChoiceType.publisher;
			odt.ItemsElementName[4] = ItemsChoiceType.subject;
            odt.ItemsElementName[5] = ItemsChoiceType.date;

			odt.Items = new elementType[6];

			int ind=0;
			
			odt.Items[ind++] = new elementType();	
			odt.Items[0].Value = 	(string)dr["Title"];
			odt.Items[ind++] = new elementType();
			odt.Items[1].Value = 	(string)dr["Content/Description"];
			odt.Items[ind++] = new elementType();
			odt.Items[2].Value = 	(string)dr["Identifier"];
			odt.Items[ind++] = new elementType();
			odt.Items[3].Value = 	(string)dr["Curation/Publisher"];
			odt.Items[ind++] = new elementType();
			odt.Items[4].Value = 	(string)dr["Content/Subject"];
            odt.Items[ind++] = new elementType();
            odt.Items[5].Value = STOAI.GetOAIDatestamp((DateTime)dr["@updated"], oai.granularityType.YYYYMMDDThhmmssZ);

			return odt;
		}


		public static DBResource CreateResource(DataRow dr) 
		{

            DBResource res = new DBResource();
            long dbid = (long)dr["pkey"];
            res.pkey = (long)dr["pkey"];
            res.title = (string)dr["Title"];
            try
            {
                res.identifier = (string)dr["identifier"];
            }
            catch (Exception) { }

            try
            {
                res.shortName = (string)dr["ShortName"];
            }
            catch (Exception) { }

            try
            {
                res.contentDescription = (string)dr["Description"];
            }
            catch (Exception) {}

/*			
			DBResource res = new DBResource();
			String resourceType = (string)dr["ResourceType"];
			resourceType = resourceType.ToUpper();
			long dbid =(long)dr["dbid"];
			if (resourceType.StartsWith("CONE")) 
			{
				res = new ServiceCone();
			}
			if ( (resourceType.StartsWith("SIAP")) || (resourceType.StartsWith("SSAP")) )
//			if (resourceType.StartsWith("SIAP"))
			{
				res = new ServiceSimpleImageAccess();
			}
			if (resourceType.StartsWith("SKYNODE")) 
			{
				res = new ServiceSkyNode();
			}


			res.dbid		=	(long)dr["dbid"];
			res.Title		= 	(string)dr["Title"];
			try 
			{
				res.CurationPublisherName	=	(string)dr["CurationPublisherName"];
			} 
			catch (Exception ) {}
			try 
			{
				res.CurationPublisherDescription	=	(string)dr["CurationPublisherDescription"];
			}
			catch (Exception ) {}
			try 
			{
				res.CurationPublisherIdentifier	=	(string)dr["CurationPublisherIdentifier"];
			}
			catch (Exception ) {}
			try 
			{
				res.CurationPublisherReferenceUrl	=	(string)dr["CurationPublisherReferenceUrl"];
			}
			catch (Exception ) {}
			
			try
			{
				res.ShortName		=	(string)dr["ShortName"];
			}
			catch(Exception ) {}

			try
			{
				res.CurationCreatorName		=	(string)dr["CurationCreatorName"];
			}
			catch(Exception ) {}
			try
			{
				res.Subject		=   ((string)dr["Subject"]).Split(',');
			}
			catch(Exception ) {}
			try
			{
				res.Description	=	(string)dr["Description"];
			}
			catch(Exception ) {}
			try
			{
				res.CurationContributor	=	(string)dr["CurationContributor"];
			}
			catch(Exception ) {}
			try
			{
				res.CurationDate		=	 (DateTime)dr["CurationDate"];
			}
			catch(Exception ) {}
			try
			{
				res.CurationVersion		=	(string)dr["CurationVersion"];
			}
			catch(Exception ) {}
			try
			{
				res.Identifier	=	(string)dr["Identifier"];
			}
			catch(Exception ) {}
			try
			{
				res.ReferenceURL=	(string)dr["ReferenceURL"];
			}
			catch(Exception ) {}
			try
			{
				res.ServiceURL	=	(string)dr["ServiceURL"];
			}
			catch(Exception ) {}
			try
			{
				res.CurationContactName		=	(string)dr["CurationContactName"];
			}
			catch(Exception ) {}
			try
			{
				res.CurationContactEmail		=	(string)dr["CurationContactEmail"];
			}
			catch(Exception ) {}
			try
			{
				res.Type=	(string)dr["Type"];
			}
			catch(Exception ) {}
			try
			{
				res.CoverageSpatial	=	(string)dr["CoverageSpatial"];
			}
			catch(Exception ) {}
			try
			{
				res.CoverageSpectral =	((string)dr["CoverageSpectral"]).Split(',');
			}
			catch(Exception ) {}
			try
			{
				res.CoverageTemporal	=	(string)dr["CoverageTemporal"];
			}
			catch(Exception ) {}
			try
			{
				res.CoverageRegionOfRegard	=	(double)dr["CoverageRegionOfRegard"];
			}
			catch(Exception ) {}

			try
			{
				res.ContentLevel=	((string)dr["ContentLevel"]).Split(',');
			}
			catch(Exception ) {}
			try
			{
				res.ModificationDate	=	 (DateTime)dr["modificationDate"];
			}
			catch(Exception ) {}

			try
			{
				res.Facility	=	(string)dr["Facility"];
			}
			catch(Exception ) {}
			try
			{
				res.Instrument	=	((string)dr["Instrument"]).Split(',');
			}
			catch(Exception ) {}
			try
			{
				res.ResourceType	=	(string)dr["ResourceType"];
			}
			catch (Exception ) {}

			
			try
			{
				res.xml		=	 (string)dr["xml"];
			}
			catch(Exception ) {}
			try
			{
				res.harvestedfrom		=	 (string)dr["harvestedfrom"];
			}
			catch(Exception ) {}
			try
			{
				res.harvestedfromDate	=	 DateTime.Parse((string)dr["harvestedfromDate"]);
			}
			catch(Exception ) {}
			try
			{
				res.footprint	=	 (string)dr["footprint"];
			}
			catch(Exception ) {}
			try
			{
				res.validationLevel	=	 (int)dr["validationLevel"];
			}
			catch(Exception ) {}

*/
			return res;
		}

		public ObjectMaker()
		{
			//
			// TODO: Add constructor logic here
			//
		}
	}
}
/* Log of changes
$Log: ObjectMaker.cs,v $
Revision 1.6  2006/02/28 17:09:49  grgreene
delete for oai pub

Revision 1.5  2006/02/22 16:26:46  grgreene
added the OAI header status attrib

Revision 1.4  2005/12/19 18:08:57  grgreene
validationLEvel can edit now

Revision 1.3  2005/06/07 16:52:57  grgreene
added default namespace to Resource

Revision 1.2  2005/05/06 14:55:19  grgreene
fixed oai link

Revision 1.1.1.1  2005/05/05 15:17:01  grgreene
import

Revision 1.27  2005/05/05 14:59:01  womullan
adding oai files

Revision 1.26  2004/12/09 16:25:06  womullan
try/catch for interface type

Revision 1.25  2004/12/07 17:30:12  womullan
 interface code

Revision 1.24  2004/11/29 18:26:12  womullan
ssap accessurl with SIAP

Revision 1.23  2004/11/09 21:11:01  womullan
added relation get

Revision 1.22  2004/11/08 20:20:35  womullan
updated relationship insert

Revision 1.21  2004/11/05 18:45:28  womullan
relations added

Revision 1.20  2004/11/02 20:12:53  womullan
date fields fixed

Revision 1.19  2004/11/01 18:30:16  womullan
v0.10 upgrade

Revision 1.18  2004/08/26 21:13:59  womullan
 fix for QueryResource was getting a null

Revision 1.17  2004/08/12 15:18:33  womullan
fixed replicator

Revision 1.16  2004/07/08 18:08:38  womullan
skynode lat/lon added

Revision 1.15  2004/04/05 18:17:35  womullan
 fixed type casts for MaxSearchRadius and MaxRecords

Revision 1.14  2004/04/01 19:28:34  womullan
 fix for nulls in sia

Revision 1.13  2004/04/01 17:24:29  womullan
 insert/update fixed

Revision 1.12  2004/03/31 17:28:26  womullan
changes for new schema

Revision 1.11  2004/03/19 17:30:06  womullan
updated search page for and/or search and column listing

 * */