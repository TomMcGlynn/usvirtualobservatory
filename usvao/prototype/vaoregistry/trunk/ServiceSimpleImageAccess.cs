using System;
using System.Data;

namespace registry
{
	/// <summary>
	///Class for Cone Services - maps to ConeService to 
	///Current version
	///ID:		$Id: ServiceSimpleImageAccess.cs,v 1.1.1.1 2005/05/05 15:17:03 grgreene Exp $
	///Revision:	$Revision: 1.1.1.1 $
	///Date:	$Date: 2005/05/05 15:17:03 $
	/// </summary>
	public class ServiceSimpleImageAccess : DBResource
	{
		new public static string Table = "ServiceSimpleImageAccess";
		new public static string[] Cols = {
											  "dbid",
											  "VOTableColumns",
											  "ImageServiceType",
											  "MaxqueryRegionSizeLat",
											  "MaxqueryRegionSizeLong",
											  "MaxImageExtentLat",
											  "MaxImageExtentLong",
											  "MaxImageSizeLat",
											  "MaxImageSizeLong",
											  "MaxFileSize",			
											  "MaxRecords"
										  };
		new public static SqlDbType[] Types ={ 
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
												 SqlDbType.VarChar
											 };
		new public static int[] Sizes = {
											8,
											10000,
											500,
											20,
											20,
											20,
											20,
											20,
											20,
											20,
											20
											
										};
		public string	ImageServiceType;		//Cutout, Mosaic, Atlas, Pointed
		public double	MaxQueryRegionSizeLong;
		public double	MaxQueryRegionSizeLat;
		public double	MaxImageExtentLong;
		public double	MaxImageExtentLat;
		public int		MaxImageSizeLong;
		public int		MaxImageSizeLat;
		public long		MaxFileSize;  //bytes
		public long		MaxRecords;
		public string[]	Format;
		public string	VOTableColumns;
		
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
 * $Log: ServiceSimpleImageAccess.cs,v $
 * Revision 1.1.1.1  2005/05/05 15:17:03  grgreene
 * import
 *
 * Revision 1.7  2004/11/01 18:30:16  womullan
 * v0.10 upgrade
 *
 * Revision 1.6  2003/12/06 19:29:07  womullan
 * all working insert update
 *
 * Revision 1.5  2003/12/06 01:46:26  womullan
 *  insert working update on the way
 *
 * Revision 1.4  2003/12/05 13:41:41  womullan
 *  cone siap skynode insert working
 *
 * Revision 1.3  2003/12/04 20:36:50  womullan
 * updated voresource parser
 *
 * Revision 1.2  2003/12/03 23:00:15  womullan
 *  many mods to get SQL working
 *
 * Revision 1.1  2003/12/03 16:50:44  womullan
 * added new siap class
 *
 * Revision 1.1  2003/12/03 15:47:30  womullan
 * Cone added
 *
 * Revision 1.1  2003/12/02 21:57:01  womullan
 *  start of new schema
 *
 
 * 
 * */