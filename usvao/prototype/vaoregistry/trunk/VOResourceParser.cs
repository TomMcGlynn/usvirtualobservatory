using System;
using System.Xml;
using System.IO;
using System.Collections;
using registry;
using System.Text;
using System.Data;

namespace registry
{
	/// <summary>
	/// Summary description for VOResourceParser.
	/// </summary>
	public class VOResourceParser
	{
		public static string XMLHEADER = @"<?xml version=""1.0"" encoding=""UTF-8""?>";

		static Hashtable nodes = null; // hold noede we want to deal with 

		public const int TITLE=1;
		public const int IDENTIFIER=2;
		public const int AUTHORITYID=3;
		public const int RESOURCEKEY=4;
		public const int SHORTNAME=5;
		public const int CONTACT=6;
		public const int CURATION=7;
		public const int PUBLISHER=8;
		public const int CREATOR=9;
		public const int REFERENCEURL=10;
		public const int NAME=11;
		public const int EMAIL=12;
		public const int ACCESSURL=13;
		public const int SOURCE=14;
		public const int TYPE=15;
		public const int RELATEDRESOURCE=16;
		public const int RELATIONSHIP=17;
		public const int DESCRIPTION=18;
		public const int SUBJECT=19;
		public const int FACILITY=20;
		public const int INSTRUMENT=21;
		public const int SPATIAL=22;
		public const int TEMPORAL=23;
		public const int SPECTRAL=24;
		public const int ITEM=25;
		public const int FORMAT=26;
		public const int ORGANIZATION=27;
		public const int DATACOLLECTION=28;
		public const int CONESEARCH=29;
		public const int MAXSEARCHRADIUS=30;
		public const int SIMPLEIMAGEACCESS=31;
		public const int MAXQUERYREGIONSIZE=32;
		public const int MAXIMAGEEXTENT=33;
		public const int MAXIMAGESIZE=34;
		public const int MAXFILESIZE=35;
		public const int MAXRECORDS=36;
		public const int SKYNODE=37;
		public const int COMPLIANCE=38;
		public const int REGIONOFREGARD=39;
		public const int MODIFICATIONDATE=40;
		public const int CONTENTLEVEL=41;
		public const int DATE=42;
		public const int WAVEBAND=43;
		public const int STANDARDID=44;
		public const int MAXSR=45;
		public const int RELATIONSHIPTYPE=46;
		public const int INTERFACE=47;
		public const int RESULTTYPE=48;
		public const int PARAM=49;
		public const int DATATYPE=50;
		public const int UCD=51;
		public const int UNIT=52;
		public const int TABLE=53;
		public const int CONTENT=54;
		
		protected void  setupNodes() 
		{
			if (nodes!=null) return;

			nodes = new Hashtable();
			nodes.Add("TITLE", TITLE) ;
			nodes.Add("IDENTIFIER", IDENTIFIER);
			nodes.Add("AUTHORITYID", AUTHORITYID);
			nodes.Add("RESOURCEKEY", RESOURCEKEY);
			nodes.Add("SHORTNAME", SHORTNAME);
			nodes.Add("CONTACT",CONTACT);
			nodes.Add("CURATION",CURATION);
			nodes.Add("PUBLISHER", PUBLISHER);
			nodes.Add("CREATOR", CREATOR) ;
			nodes.Add("REFERENCEURL", REFERENCEURL) ;
			nodes.Add("NAME", NAME) ;
			nodes.Add("EMAIL", EMAIL) ;
			nodes.Add("ACCESSURL", ACCESSURL) ;
			nodes.Add("SOURCE", SOURCE) ;
			nodes.Add("TYPE", TYPE) ;
			nodes.Add("RELATEDRESOURCE", RELATEDRESOURCE) ;
			nodes.Add("RELATIONSHIP", RELATIONSHIP);
			nodes.Add("RELATIONSHIPTYPE",RELATIONSHIPTYPE);
			nodes.Add("INTERFACE",INTERFACE);
			nodes.Add("PARAM",PARAM);
			nodes.Add("DATATYPE",DATATYPE);
			nodes.Add("UCD",UCD);
			nodes.Add("UNIT",UNIT);
			nodes.Add("RESULTTYPE",RESULTTYPE);
			nodes.Add("DESCRIPTION", DESCRIPTION) ;
			nodes.Add("SUBJECT", SUBJECT) ;
			nodes.Add("FACILITY",FACILITY);
			nodes.Add("INSTRUMENT",INSTRUMENT);
			nodes.Add("SPATIAL",SPATIAL);
			nodes.Add("TEMPORAL",TEMPORAL);
			nodes.Add("SPECTRAL",SPECTRAL);
			nodes.Add("ITEM", ITEM) ;
			nodes.Add("FORMAT", FORMAT) ;
			nodes.Add("ORGANIZATION", ORGANIZATION) ;
			nodes.Add("DATACOLLECTION", DATACOLLECTION) ;
			nodes.Add("CONESEARCH", CONESEARCH) ;
			nodes.Add("MAXSEARCHRADIUS", MAXSEARCHRADIUS) ;
			nodes.Add("SIMPLEIMAGEACCESS", SIMPLEIMAGEACCESS) ;
			nodes.Add("MAXQUERYREGIONSIZE", MAXQUERYREGIONSIZE) ;
			nodes.Add("MAXIMAGEEXTENT", MAXIMAGEEXTENT) ;
			nodes.Add("MAXIMAGESIZE", MAXIMAGESIZE) ;
			nodes.Add("MAXFILESIZE", MAXFILESIZE) ;
			nodes.Add("MAXRECORDS", MAXRECORDS) ;
			nodes.Add("SKYNODE", SKYNODE) ;
			nodes.Add("COMPLIANCE", COMPLIANCE) ;
			nodes.Add("REGIONOFREGARD", REGIONOFREGARD) ;
			nodes.Add("MODIFICATIONDATE", MODIFICATIONDATE) ;
			nodes.Add("CONTENTLEVEL",CONTENTLEVEL);
			nodes.Add("DATE",DATE);
			nodes.Add("WAVEBAND",WAVEBAND);
			nodes.Add("STANDARDID",STANDARDID);
			nodes.Add("MAXSR",MAXSR);
			nodes.Add("TABLE",TABLE);
			nodes.Add("CONTENT",CONTENT);
		}

		public VOResourceParser()
		{
			//
			// TODO: Add constructor logic here
			//
			setupNodes();
		}

		public DBResource  parseVOResource(string theXML, StringBuilder sbOut)
		{
			StringReader rd = new StringReader(theXML);
			XmlReader xr = new XmlTextReader(rd);
			DBResource sr = null;
			//string rType="";

			// Check WHICH Resource xsi:type

			// Conesearch, SimpleImageAccess, OpenSkyNode,
			// Registry, Authority, DataCollection, Organisation
/*			try
			{
				string tName="";
				bool done = false;
				while (xr.Read() && !done) 
				{			
					tName = xr.LocalName.ToUpper().Trim();
					int found = 0; // found : after ns dec
 

					if (tName.CompareTo("RESOURCE")==0)
					{
						tName = xr.GetAttribute("xsi:type");
						if (tName != null)
						{
							found = tName.IndexOf(":");
							tName = tName.ToUpper().Substring(found+1).Trim();
							rType = tName;
						}
						else tName = "";
					}
					
					if (tName.CompareTo("SIMPLEIMAGEACCESS")==0) // create SIAP class
					{ 
						sr = new ServiceSimpleImageAccess(); 
						((ServiceSimpleImageAccess)sr).Format = new string[1];
						sr.ResourceType="SIAP";
						done=true;
					}
					else if (tName.CompareTo("CONESEARCH")==0)	// create CONE class
					{ 
						sr = new ServiceCone(); 
						sr.ResourceType="CONE";
						done=true;
					}
					else if (tName.CompareTo("OPENSKYNODE")==0) // create SkyNode
					{ 
						sr = new ServiceSkyNode();
						sr.ResourceType="SKYNODE";
						done=true;
					}
								
				}// End Read
				
			}
			catch (Exception e) { e=e;sbOut.Append("******"+e.Message+":"+e.StackTrace);};
			if ( sr == null)
			{
				sr = new DBResource();
				if (rType.CompareTo("")==0)
					sr.ResourceType = "OTHER";
				else
					sr.ResourceType = rType;
					
			}

			//sbOut.Append("RESOURCETYPE: " + sr.ResourceType);

			// RESET the reader so we can get the rest now we know what type we have
			rd = new StringReader(theXML);
			xr = new XmlTextReader(rd);
			
			sr.ContentLevel = new string[1];
			sr.Subject = new string[1];
			sr.CoverageSpectral = new String[1];

			sr.xml=theXML;

			// Create ArrayList for Relationships, Interfaces
			ArrayList relArr = new ArrayList();
			ArrayList intArr = new ArrayList();
			ArrayList paramArr = new ArrayList();
			ResourceRelation currentRelation = new ResourceRelation();
			ResourceInterface currentInterface = new ResourceInterface();
			InterfaceParam currentParam = new InterfaceParam();

			int interfaceNum=0;

			StringBuilder sb = new StringBuilder();

			bool inContent = false;
			bool inCuration = false;	
			bool inContact = false;
			bool inCreator = false;
			bool inRelation = false;
			bool inInterface = false;
			bool inParam = false;
			bool inStandardID = false;
			bool inFacility	= false;
			bool inInstrument= false;
			bool inTable=false;

			double ror;
			string tmpText	= "";// some time the start tags do not show up
								// SO TRY TO GETTHEM ON THE END TAG

			// this Read Loop picks out Elements from XML file
			while (xr.Read())
			{
				string name = xr.LocalName.ToUpper();

				int num = 0;
				try 
				{
					num= (int)nodes[name];
				} catch (Exception e) { e=e;};

				if (num == 0) 
				{
					tmpText = xr.Value;
				}

		//		Console.Out.WriteLine(xr.NodeType+" "+xr.Name+" "+xr.Value);
				

				if ( xr.NodeType == XmlNodeType.EndElement ) 
				{
					// special case for elements within elements
					switch (num) 
					{
						case CONTENT: inContent=false;break;
						case CURATION:	inCuration=false;break;
						case TABLE:		inTable=false;break;
						case CONTACT:	inContact=false;break;
						case CREATOR:	inCreator=false;break;
						case IDENTIFIER: sb.Remove(0,sb.Length);break;
							// screwed up tags ...
						case RELATIONSHIP : 
							if (currentRelation != null) 
							{
								relArr.Add(currentRelation);
								inRelation=false; 
							}break;		
						case INTERFACE :
							if (currentInterface != null)
							{
								intArr.Add(currentInterface);
								inInterface=false;
							}break;
						case PARAM : 
							if (currentParam != null) 
							{
								paramArr.Add(currentParam);
								inParam=false; 
							}break;		
						case STANDARDID : inStandardID = false;break;
						case FACILITY : inFacility = false; break;
						case INSTRUMENT : inInstrument = false; break;

					}
				} 

				sr.CurationPublisherIdentifier = "";
			
				if (xr.NodeType == XmlNodeType.Element) 
				{
					switch (num) 
					{
						case TITLE : 
							if ( (inCuration==false) && (inFacility==false) && (inInstrument ==false) ) {
								xr.Read() ; sr.Title = xr.Value;
							}
							else if (inCuration==true) {
								xr.Read() ; sr.CurationPublisherName = xr.Value;
							}
							break;
						case IDENTIFIER  : xr.Read(); 
							sb.Append(xr.Value);
							sr.Identifier = sb.ToString().Trim();
							sbOut.Append( "/nIdentifier: " + xr.Value +"/n");
							break;
						case SHORTNAME: xr.Read() ; sr.ShortName = xr.Value; break;
						case CONTENT: inContent = true; break;
						case CURATION: inCuration = true; break;
						case DATE: 
							if (inCuration)
							{
								xr.Read(); 
								sr.CurationDate = DateTime.Parse(xr.Value);
								//sbOut.Append( "CurationDate: " + xr.Value);
							}
							break;
						case PUBLISHER :
							sr.CurationPublisherIdentifier = xr.GetAttribute("ivo-id");
							xr.Read();
							sr.CurationPublisherName = xr.Value;
							break;
						case CONTACT : inContact = true; break;
						case CREATOR : inCreator = true; break;
						case FACILITY : inFacility = true; break;
						case INSTRUMENT : inInstrument = true; break;
						case STANDARDID : inStandardID = true;break;
						case TYPE :	xr.Read(); sr.Type = xr.Value; break;
						case CONTENTLEVEL :
							xr.Read();
							if (sr.ContentLevel[0] != null) sr.ContentLevel[0] = sr.ContentLevel[0] + "," + xr.Value;
							else sr.ContentLevel[0] = xr.Value;
							break;
						case SUBJECT :
							xr.Read();
							if (sr.Subject[0] != null) sr.Subject[0] = sr.Subject[0] + "," + xr.Value;
							else sr.Subject[0] = xr.Value;
							break;
						case FORMAT :
							xr.Read();
							if (sr.GetType() == typeof(ServiceSimpleImageAccess))
							{
								if ( ((ServiceSimpleImageAccess)sr).Format[0]!=null  )
								{
									((ServiceSimpleImageAccess)sr).Format[0] = ((ServiceSimpleImageAccess)sr).Format[0] + "," + xr.Value;
								}
								else ((ServiceSimpleImageAccess)sr).Format[0] = xr.Value;
							}
							break;
						case REFERENCEURL : 
							if (inCuration)
							{
								xr.Read() ; sr.CurationPublisherReferenceUrl= xr.Value;
							}
							else xr.Read() ; sr.ReferenceURL = xr.Value; 
							break; 				
						case NAME : 
							if (inContact==true) 
							{
								xr.Read() ; sr.CurationContactName = xr.Value; 
							}
							else if (inCreator==true)
							{
								xr.Read() ; sr.CurationCreatorName = xr.Value; 
							}	
							else if (inParam==true)
							{
								xr.Read() ; 
								currentParam.name = xr.Value;
							}
							break;
						case EMAIL : xr.Read() ; sr.CurationContactEmail = xr.Value; break;
						case INTERFACE :
							inInterface=true;
							currentInterface = new ResourceInterface();
							currentInterface.interfaceNum = interfaceNum++;
							currentInterface.type = xr.GetAttribute("xsi:type");
							int found = currentInterface.type.IndexOf(":");
							currentInterface.type = currentInterface.type.ToUpper().Substring(found+1).Trim();
							currentInterface.qtype = xr.GetAttribute("qtype");
							if (currentInterface.qtype==null)currentInterface.qtype="";
							break;
						case PARAM :					
							inParam = true;
							currentParam = new InterfaceParam();
							currentParam.interfaceNum = interfaceNum;
							break;
						case TABLE : inTable = true; break;
						case DESCRIPTION :
							if (inContent)
							{
								xr.Read() ; sr.Description = xr.Value;
								break;
							}
							if (inCuration)
							{
								xr.Read() ; sr.CurationPublisherDescription = xr.Value;
								break;
							}
							else if (inTable) 
							{
								xr.Read(); 
								break;
							}
							else if (inParam)
							{
								xr.Read() ; 
								currentParam.description = xr.Value;
								break;
							}
							else
							{
								xr.Read();
								break;
							}
						case ACCESSURL : 
							xr.Read() ; sr.ServiceURL = xr.Value; 
							if (inInterface) 
								currentInterface.accessURL = xr.Value; 
							break;
						case RESULTTYPE : 
							xr.Read() ; 
							if (inInterface) 
								currentInterface.resultType = xr.Value; 
							break;
						case RELATEDRESOURCE :
							currentRelation.relatedResourceIvoId = xr.GetAttribute("ivo-id");
							xr.Read();
							currentRelation.relatedResourceName = xr.Value;
							break;
						case RELATIONSHIP :
							inRelation=true;
							currentRelation = new ResourceRelation();break;
						case RELATIONSHIPTYPE :
							xr.Read();
                            currentRelation.relationshipType = xr.Value.Trim();
							break;		
						case DATATYPE :
							xr.Read();
							currentParam.datatype = xr.Value;
							break;
						case UCD :
							xr.Read();
							currentParam.ucd = xr.Value;
							break;
						case UNIT :
							xr.Read();
							currentParam.unit = xr.Value;
							break;
						//case FACILITY : xr.Read() ; sr.Facility = xr.Value; break;
						//case INSTRUMENT : xr.Read() ; sr.Instrument = xr.Value; break;
						case SPATIAL : 
							sr.CoverageSpatial = xr.ReadInnerXml();
							ror = getRegionOfRegard(sr.CoverageSpatial);
							sr.CoverageRegionOfRegard = ror; 
							break;
						case TEMPORAL : sr.CoverageTemporal = xr.ReadInnerXml(); break;
						case WAVEBAND : 
							xr.Read();
							if (sr.CoverageSpectral[0] != null) sr.CoverageSpectral[0] = sr.CoverageSpectral[0] + "," + xr.Value;
							else sr.CoverageSpectral[0] = xr.Value;
							break;
						case ORGANIZATION : break;
						case DATACOLLECTION : 
							// sr.Type = xr.LocalName; 
							break;
						case REGIONOFREGARD: 
							// Inside the SPATIAL InnerXML (see above)
							break;
						case MAXRECORDS : 
							xr.Read() ; 
							if ( xr.Value != null && xr.Value.Length > 0 )
							{
								try
								{
									if (sr.ResourceType.StartsWith("SIA") )
									{
										((ServiceSimpleImageAccess)sr).MaxRecords = Convert.ToInt64(xr.Value); 
									}
									else 
									{
										((ServiceCone)sr).MaxRecords = Convert.ToInt64(xr.Value); 
									}
								}
								catch (Exception e) {e=e;
									if (sr.ResourceType.StartsWith("SIA") )
									{
										((ServiceSimpleImageAccess)sr).MaxRecords = Convert.ToInt64(xr.Value.Substring(0,(xr.Value.Length-2))); 
									}
									else 
									{
										((ServiceCone)sr).MaxRecords = Convert.ToInt64(xr.Value.Substring(0,(xr.Value.Length-2))); 
									}
								}
							}
							break;
						case MAXSR :
							// this is a fall through it's the same as below;
						case MAXSEARCHRADIUS :

							xr.Read();
							if (xr.Value != null && xr.Value.Length > 0 )
							{
								((ServiceCone)sr).MaxSearchRadius = Convert.ToDouble(xr.Value); 
							}
							break;
						// this date is no longer in schema, keeping for now need to remove 04/18/05
						case MODIFICATIONDATE : xr.Read() ; sr.ModificationDate = DateTime.Parse(xr.Value); break;
					}
				}
			}

			if (relArr.Count > 0 ) 
			{
				sr.resourceRelations = (ResourceRelation[])relArr.ToArray(typeof(ResourceRelation));
			}
			if (intArr.Count > 0)
			{
				sr.resourceInterfaces = (ResourceInterface[])intArr.ToArray(typeof(ResourceInterface));
				if (paramArr.Count > 0)
				{
					for (int ic=0; ic< intArr.Count; ic++)
					{
						sr.resourceInterfaces[ic].interfaceParams = (InterfaceParam[])paramArr.ToArray(typeof(InterfaceParam));
					}	
				}
			}
*/
			return sr;			
		}

		public double  getRegionOfRegard(string theInnerXML)
		{
			string theXML = XMLHEADER +  "<grg>" +theInnerXML +"</grg>";
//			string theXML = theInnerXML;
			StringReader rd = new StringReader(theXML);
			XmlReader xr = new XmlTextReader(rd);
			double rr = 0;

			string tmpText = "";// some time the start tags do not show up
			// this Read Loop picks out RegionOfRegard from InnerXML section of CoverageSpatial
			while (xr.Read())
			{
				string name = xr.LocalName.ToUpper();

				int num = 0;
                try
                {
                    num = (int)nodes[name];
                }
                catch (Exception) { }
				if (num == 0) 
				{
					tmpText = xr.Value;
				}

				if (xr.NodeType == XmlNodeType.Element) 
				{
					switch (num) 
					{
						case REGIONOFREGARD: 
							xr.Read();
                            try
                            {
                                rr = Convert.ToDouble(xr.Value);
                            }
                            catch (Exception) { }
							break;
					}
				}
			}

			return rr;			
		}

	}
}
/* Log of changes
 * $Log: VOResourceParser.cs,v $
 * Revision 1.1.1.1  2005/05/05 15:17:07  grgreene
 * import
 *
 * Revision 1.32  2005/05/05 14:59:01  womullan
 * adding oai files
 *
 * Revision 1.31  2005/03/22 20:11:00  womullan
 * update to parser for descrip + OAI fixes
 *
 * Revision 1.30  2005/03/21 19:58:18  womullan
 * fix to resourcetype
 *
 * Revision 1.29  2004/12/13 20:54:22  womullan
 * fixed description
 *
 * Revision 1.28  2004/12/07 15:45:21  womullan
 *  downfile
 *
 * Revision 1.27  2004/12/07 15:32:28  womullan
 * readme.txt
 *
 * Revision 1.26  2004/11/15 16:23:21  womullan
 * fixed stringbuffer
 *
 * Revision 1.25  2004/11/11 19:40:20  womullan
 * minor updates
 *
 * Revision 1.24  2004/11/10 20:15:00  womullan
 * added interface count
 *
 * Revision 1.23  2004/11/10 19:10:48  womullan
 * interfaces
 *
 * Revision 1.22  2004/11/09 21:11:01  womullan
 * added relation get
 *
 * Revision 1.21  2004/11/08 20:20:35  womullan
 * updated relationship insert
 *
 * Revision 1.20  2004/11/05 18:45:28  womullan
 * relations added
 *
 * Revision 1.19  2004/11/02 20:12:53  womullan
 * date fields fixed
 *
 * Revision 1.18  2004/11/01 18:30:16  womullan
 * v0.10 upgrade
 *
 * Revision 1.17  2004/04/01 18:29:28  womullan
 *  harvest ncsa
 *
 * Revision 1.16  2003/12/19 18:32:10  womullan
 * updated maxsr and regionofregard
 *
 * Revision 1.15  2003/12/18 19:45:18  womullan
 * updated harvester
 *
 * Revision 1.14  2003/12/17 18:48:41  womullan
 * updated contentlevel and type
 *
 * Revision 1.13  2003/12/16 21:17:50  womullan
 * now returning voresource
 *
 * Revision 1.12  2003/12/15 21:00:39  womullan
 * relations and Harvested from added
 *
 * Revision 1.11  2003/12/08 22:35:37  womullan
 * Parser fixed
 *
 * Revision 1.10  2003/12/08 17:32:39  womullan
 * parser almost working
 *
 * Revision 1.9  2003/12/04 21:37:03  womullan
 * updated voresource parser
 *
 * Revision 1.8  2003/12/04 21:27:05  womullan
 * updated voresource parser
 *
 * Revision 1.7  2003/12/04 21:07:24  womullan
 * updated voresource parser
 *
 * Revision 1.6  2003/12/04 20:36:50  womullan
 * updated voresource parser
 *
 * Revision 1.5  2003/12/02 21:57:01  womullan
 *  start of new schema
 *
 * Revision 1.4  2003/06/16 19:47:49  womullan
 *  many changes as discussed in meeting- new pages
 *
 * Revision 1.3  2003/06/02 15:28:07  womullan
 *  fixed format content level
 *
 * Revision 1.2  2003/05/09 21:24:57  womullan
 *  harvest more fields
 *
 * Revision 1.1  2003/05/09 19:18:40  womullan
 * moved harvester into registry service
 *
 * Revision 1.1  2003/05/08 21:19:51  womullan
 *  harvester now working - new update service
 *
 * 
 * */
