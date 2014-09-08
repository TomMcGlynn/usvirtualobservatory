using System;


namespace registry
{
	/// <summary>
	/// Summary description for SimpleResource.
	///Current version
	///ID:		$Id: SimpleResource.cs,v 1.2 2005/12/19 18:08:57 grgreene Exp $
	///Revision:	$Revision: 1.2 $
	///Date:	$Date: 2005/12/19 18:08:57 $
	/// </summary>
	public class SimpleResource
	{
		public string Title;//	@Title,
		public string ShortName; // ticker
		public string Publisher;//	@Publisher,
		public string Creator;//		@Creator,
		public string[] Subject;//		@Subject,
		public string Description;//		@Description,
		public string Contributor;//		@Contributor,
		public  DateTime Date = DateTime.Now;//		@Date,
		public string  Version;//		@Version,
		public string Identifier;//		@Identifier,
		public string ReferenceURL;//		@ReferenceURL,
		public string ServiceURL;//		@ServiceURL,
		public string ContactName;//		@ContactName,
		public string ContactEmail;//		@ContactEmail,
		public string Type;//		@Type,
		public string CoverageSpatial;//		@Coverage,
		public string[] CoverageSpectral;
		public string CoverageTemporal;
		public double EntrySize; 
		public double MaxSR;//		@Coverage,
		public int MaxRecords;//		@Coverage,
		public string[] ContentLevel;//		@ContentLevel,
		public string Facility;//		@Facility,
		public string[] Instrument;//		@Instrument,
		public string[] Format;//		@Format,
		public string ServiceType;//		@ServiceType
		public string xml="";// the original document - if provided i.e. from harvest
		public int validationLevel;
		public SimpleResource()
		{
			//
			//
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
}
/* Log of changes
 * $Log: SimpleResource.cs,v $
 * Revision 1.2  2005/12/19 18:08:57  grgreene
 * validationLEvel can edit now
 *
 * Revision 1.1.1.1  2005/05/05 15:17:04  grgreene
 * import
 *
 * Revision 1.10  2003/12/02 21:57:01  womullan
 *  start of new schema
 *
 * Revision 1.9  2003/07/01 13:47:40  womullan
 * modified string enum lists to arrays
 *
 * Revision 1.8  2003/06/16 19:47:49  womullan
 *  many changes as discussed in meeting- new pages
 *
 * Revision 1.7  2003/06/13 15:01:44  womullan
 * added aspx page for updating registry
 * added style sheet and fixed a few bugs
 *
 * Revision 1.6  2003/06/02 15:28:07  womullan
 *  fixed format content level
 *
 * Revision 1.5  2003/05/08 21:19:51  womullan
 *  harvester now working - new update service
 *
 * 
 * */
