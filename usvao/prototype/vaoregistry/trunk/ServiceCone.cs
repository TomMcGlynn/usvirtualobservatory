using System;
using System.Data;

namespace registry
{
	/// <summary>
	///Class for Cone Services - maps to ConeService to 
	///Current version
	///ID:		$Id: ServiceCone.cs,v 1.1.1.1 2005/05/05 15:17:03 grgreene Exp $
	///Revision:	$Revision: 1.1.1.1 $
	///Date:	$Date: 2005/05/05 15:17:03 $
	/// </summary>
	public class ServiceCone : DBResource
	{
		new public static string Table = "ServiceCone";

		new public static string[] Cols = {
											  "dbid",
											  "MaxSearchRadius",
											  "MaxRecords",
											  "VOTableColumns"
										  };
		new public static SqlDbType[] Types ={ 
												 SqlDbType.BigInt,
												 SqlDbType.VarChar,
												 SqlDbType.VarChar,
												 SqlDbType.VarChar
											 };
		new public static int[] Sizes = {
											8,
											9,
											9,
											10000
										};
		public double MaxSearchRadius;
		public long MaxRecords;
		public string VOTableColumns;
		
	

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
 * $Log: ServiceCone.cs,v $
 * Revision 1.1.1.1  2005/05/05 15:17:03  grgreene
 * import
 *
 * Revision 1.4  2004/11/01 18:30:16  womullan
 * v0.10 upgrade
 *
 * Revision 1.3  2003/12/05 13:41:41  womullan
 *  cone siap skynode insert working
 *
 * Revision 1.2  2003/12/03 23:00:15  womullan
 *  many mods to get SQL working
 *
 * Revision 1.1  2003/12/03 15:47:30  womullan
 * Cone added
 *
 * Revision 1.1  2003/12/02 21:57:01  womullan
 *  start of new schema
 *
 
 * 
 * */