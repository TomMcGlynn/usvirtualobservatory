using System;
using System.Data;

namespace registry
{
	/// <summary>
	///Class for Cone Services - maps to ConeService to 
	///Current version
	///ID:		$Id: ServiceSkyNode.cs,v 1.1.1.1 2005/05/05 15:17:03 grgreene Exp $
	///Revision:	$Revision: 1.1.1.1 $
	///Date:	$Date: 2005/05/05 15:17:03 $
	/// </summary>
	public class ServiceSkyNode : DBResource
	{
		
		new public static string Table = "ServiceSkyNode";

		new public static string[] Cols = {
										  "dbid",
										  "Compliance",
										  "Latitude",
										  "Longitude",
											  "PrimaryTable",
											  "PrimaryKey",
											  "MaxRecords"

									  };
		new public static SqlDbType[] Types ={ 
											 SqlDbType.BigInt,
											 SqlDbType.VarChar,
											 SqlDbType.Float,
											 SqlDbType.Float,
											SqlDbType.VarChar,
											SqlDbType.VarChar,
											SqlDbType.VarChar
		};
		new public static int[] Sizes = {
										8,
										50,
										8,
										8,
										100,
										50,
										9
									};
		 public string Compliance;
		 public double Latitude;
		 public double Longitude;
		 public long MaxRecords;
		 public string PrimaryTable;// used for the default query when clicked in OSQ
		 public string PrimaryKey;
	

		/// <summary>
		/// Revision from CVS
		/// </summary>
		new public static string Revision
		{
			get
			{
				return "$Revision: 1.1.1.1 $";
			}
		}
	}

}
/* Log of changes
 * $Log: ServiceSkyNode.cs,v $
 * Revision 1.1.1.1  2005/05/05 15:17:03  grgreene
 * import
 *
 * Revision 1.6  2004/11/01 18:30:16  womullan
 * v0.10 upgrade
 *
 * Revision 1.5  2004/08/12 15:18:33  womullan
 * fixed replicator
 *
 * Revision 1.4  2004/07/08 18:08:38  womullan
 * skynode lat/lon added
 *
 * Revision 1.3  2003/12/05 13:41:41  womullan
 *  cone siap skynode insert working
 *
 * Revision 1.2  2003/12/03 23:00:15  womullan
 *  many mods to get SQL working
 *
 * Revision 1.1  2003/12/03 16:01:33  womullan
 *  removed reg.suo
 *
 * Revision 1.1  2003/12/03 15:47:30  womullan
 * Cone added
 *
 * Revision 1.1  2003/12/02 21:57:01  womullan
 *  start of new schema
 *
 
 * 
 * */