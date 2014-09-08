using System;
using System.Data;

namespace registry
{
	/// <summary>
	/// Summary description for managedAuthority.
	/// </summary>
	public class ManagedAuthority : DBResource
	{
		new public static string Table = "ManagedAuthority";

		new public static string[] Cols = {
											"dbId",
											"managingOrg",
											"authorityID"
										  };
		new public static SqlDbType[] Types ={ 
												 SqlDbType.BigInt,
												 SqlDbType.VarChar,
												 SqlDbType.VarChar
											 };
		new public static int[] Sizes = {
											8,
											1000,
											1000
										};
		public string managingOrg;
		public string authorityID;

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
