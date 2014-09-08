using System;
using System.Data;
using System.Xml;
using System.Xml.Serialization;

namespace registry
{
	/// <summary>
	/// Representaion of the Resource table.
	///Current version
	///ID:		$Id: Resource.cs,v 1.2 2005/12/19 18:08:57 grgreene Exp $
	///Revision:	$Revision: 1.2 $
	///Date:	$Date: 2005/12/19 18:08:57 $
	/// </summary>
	/// 

	[XmlInclude(typeof(ServiceCone))]
	[XmlInclude(typeof(ServiceSimpleImageAccess))]
	[XmlInclude(typeof(ServiceSkyNode))]

	public class DBResource
	{
		public static string Table = "Resource";

        public static string[] Cols = {
                                        "pkey",
                                        "xsi_type",
                                        "[@created]",
                                        "[@updated]",
                                        "[@status]",
                                        "validationLevel",
                                        "title",
                                        "shortName",
                                        "identifier",
                                        "[curation/publisher/@ivo-id]",
                                        "[curation/publisher]",
                                        "[curation/version]",
                                        "[content/subject]",
                                        "[content/description]",
                                        "[content/source/@format]",
                                        "[content/source]",
                                        "[content/referenceURL]",
                                        "[content/type]",
                                        "[content/contentLevel]",
                                        "[rights]",
                                        "[coverage/footprint/@ivo-id]",
                                        "[coverage/footprint]",
                                        "[coverage/waveband]",
                                        "[coverage/regionOfRegard]",
                                        "harvestedFrom",
                                        "harvestedFromDate",
                                        "tag",
                                        "xml"};

		public static SqlDbType[] Types = {
											  SqlDbType.BigInt,
											  SqlDbType.VarChar,
											  SqlDbType.VarChar,
											  SqlDbType.VarChar,
											  SqlDbType.BigInt,
											  SqlDbType.BigInt,
											  SqlDbType.VarChar,
											  SqlDbType.VarChar,
											  SqlDbType.VarChar,
											  SqlDbType.VarChar,
											  SqlDbType.VarChar,
											  SqlDbType.VarChar,
											  SqlDbType.VarChar,
											  SqlDbType.VarChar,
											  SqlDbType.VarChar,
											  SqlDbType.VarChar,
											  SqlDbType.VarChar,
											  SqlDbType.VarChar,
											  SqlDbType.VarChar,
											  SqlDbType.VarChar,
											  SqlDbType.VarChar,
											  SqlDbType.VarChar,
											  SqlDbType.VarChar,
											  SqlDbType.VarChar,
											  SqlDbType.Float,
											  SqlDbType.VarChar,
											  SqlDbType.VarChar,
											  SqlDbType.VarChar,
											  SqlDbType.VarChar
										  };

		public static int[] Sizes = {
												8,
												1000,
												1000,
												1000,
												8,
												8,
												1000,
												1000,
												1000,
												1000,
												1000,
												1000,
												1000,
												1000,
												1000,
												1000,
												1000,
												1000,
												1000,
												1000,
												1000,
												1000,
												1000,
												1000,
												16,
												1000,
												1000,
												1000,
												100000
											};

        public long pkey;
        public string xsi_type;
        public string created;
        public DateTime updated = DateTime.Now;
        public string status;
        public long validationLevel=2;
        public string title;
        public string shortName;
        public string identifier;
        public string curationPublisherIvoid;
        public string curationPublisher;
        public string curationVersion;
        public string contentSubject;
        public string contentDescription;
        public string contentSourceFormat;
        public string contentSource;
        public string contentReferenceURL;
        public string contentType;
        public string contentContentLevel;
        public string rights;
        public string coverageFootprintIvoid;
        public string coverageFootprint = "";
        public string coverageWaveband;
        public string coverageRegionOfRegard;
        public string harvestedFrom = "";
        public DateTime harvestedFromDate = DateTime.Now;
        public string tag;
        public string xml = "";



/*        public long   dbid ; // internal database identifier
		public int	  status=1;
		public string Identifier;//		@Identifier,
		public string Title;//	@Title,
		public string ShortName; // ticker
		public string CurationPublisherName;//	@Publisher,
		public string CurationPublisherIdentifier;//	,
		public string CurationPublisherDescription;//
		public string CurationPublisherReferenceUrl;//	
		public string CurationCreatorName;//		@Creator,
		public string CurationCreatorLogo;//		@Creator,
		public string CurationContributor;//		@Contributor,
		public  DateTime CurationDate = DateTime.Now;//		@Date,
		public string  CurationVersion;//		@Version,
		public string CurationContactName;//		@ContactName,
		public string CurationContactEmail;//		@ContactEmail,
		public string CurationContactAddress;//		@ContactName,
		public string CurationContactPhone;//		@ContactEmail,
		public string[] Subject;//		@Subject,
		public string Description;//		@Description,
		public string ReferenceURL;//		@ReferenceURL,
		public string Type;//		@Type,
		public string Facility;//		@Facility,
		public string[] Instrument;//		@Instrument,
		public string[] ContentLevel;//		@ContentLevel,
		public  DateTime ModificationDate = DateTime.Now;//		@Date,
	
		public string ServiceURL;//		@ServiceURL,
		public string CoverageSpatial;//		@Coverage,
		public string[] CoverageSpectral;
		public string CoverageTemporal;
		public double CoverageRegionOfRegard;
		public string ResourceType;//		@ResourceType
		public ResourceRelation[] resourceRelations;
		public ResourceInterface[] resourceInterfaces;
		public string xml="";// the original document - if provided i.e. from harvest
		public string harvestedfrom ="";
		public DateTime harvestedfromDate = DateTime.Now;
		public string footprint="";
		public int validationLevel=2;		//starting point
*/

		public DBResource()
		{
			
		}
		/// <summary>
		/// Revision from CVS
		/// </summary>
		public static string Revision
		{
			get
			{
				return "$Revision: 1.2 $";
			}
		}
	}	

	public class ResourceRelation
	{
		public static string Table = "ResourceRelations";
		public static string[] Cols = {"PrimaryResourceDbID","RelatedResourceIvoId","RelatedResourceName","RelationShipType"};
		public static SqlDbType[] Types = {SqlDbType.BigInt,SqlDbType.VarChar,SqlDbType.VarChar, SqlDbType.VarChar};
		public static int[] Sizes = {8,500,200,50};

		public string relatedResourceIvoId = "";
		public string relationshipType;
		public string relatedResourceName = "";
	}

	public class ResourceInterface
	{
		public static string Table = "Interfaces";
		
		public static string[] Cols = {"dbID","interfaceNum","type","qtype","accessURL","resultType"};
		public static SqlDbType[] Types = {SqlDbType.BigInt,SqlDbType.BigInt,SqlDbType.VarChar,SqlDbType.VarChar,SqlDbType.VarChar,SqlDbType.VarChar};
		public static int[] Sizes = {8,8,100,100,200,200};

		public int interfaceNum;
		public string type = "";
		public string qtype = "";
		public string accessURL = "";
		public string resultType = "";
		public InterfaceParam[] interfaceParams;
	}
	public class InterfaceParam
	{
		public static string Table = "Params";
		public static string[] Cols = {"dbID","interfaceNum","name","description","datatype","unit","ucd"};
		public static SqlDbType[] Types = {SqlDbType.BigInt,SqlDbType.BigInt,SqlDbType.VarChar,SqlDbType.VarChar,SqlDbType.VarChar,SqlDbType.VarChar,SqlDbType.VarChar};
		public static int[] Sizes = {8,8,100,200,50,50,50};

		public int interfaceNum;
		public string name = "";
		public string description = "";
		public string datatype = "";
		public string unit = "";
		public string ucd = "";
	}
}

/* Log of changes
 * $Log: Resource.cs,v $
 * Revision 1.2  2005/12/19 18:08:57  grgreene
 * validationLEvel can edit now
 *
 * Revision 1.1.1.1  2005/05/05 15:17:03  grgreene
 * import
 *
 * Revision 1.17  2004/11/10 19:10:48  womullan
 * interfaces
 *
 * Revision 1.16  2004/11/09 21:11:01  womullan
 * added relation get
 *
 * Revision 1.15  2004/11/05 18:45:28  womullan
 * relations added
 *
 * Revision 1.14  2004/11/01 18:30:16  womullan
 * v0.10 upgrade
 *
 * Revision 1.13  2004/05/18 19:04:52  womullan
 *  fix for title truncation
 *
 * Revision 1.12  2004/04/01 20:21:56  womullan
 *  footprint added
 *
 * Revision 1.11  2004/04/01 19:28:34  womullan
 *  fix for nulls in sia
 *
 * Revision 1.10  2004/02/05 18:48:47  womullan
 * added sqlquery and harvestedfromDate
 *
 * Revision 1.9  2003/12/16 21:17:50  womullan
 * now returning voresource
 *
 * Revision 1.8  2003/12/15 21:00:39  womullan
 * relations and Harvested from added
 *
 * Revision 1.7  2003/12/06 01:46:26  womullan
 *  insert working update on the way
 *
 * Revision 1.6  2003/12/05 13:41:41  womullan
 *  cone siap skynode insert working
 *
 * Revision 1.5  2003/12/04 20:36:50  womullan
 * updated voresource parser
 *
 * Revision 1.4  2003/12/04 19:46:39  womullan
 *  now working for Resource
 *
 * Revision 1.3  2003/12/03 23:00:15  womullan
 *  many mods to get SQL working
 *
 * Revision 1.2  2003/12/03 15:47:30  womullan
 * Cone added
 *
 * Revision 1.1  2003/12/02 21:57:01  womullan
 *  start of new schema
 *
 
 * 
 * */
