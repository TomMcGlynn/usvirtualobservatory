using System;
using System.Configuration;
using System.Data.SqlClient;
using System.Data;
using System.Text;
using Replicate.registry;



namespace Replicate
{
	/// <summary>
	/// Summary description for Class1.
	/// </summary>
	class Replicate
	{
		const int MLEN = 4000;

        //because we need to write to the log table, this is an admin connection
        public static string connStr = Properties.Settings.Default.SqlAdminConnection;
        private static string dbAdmin = Properties.Settings.Default.dbAdmin;

		/// <summary>
		/// The main entry point for the application.
		/// </summary>
		//[STAThread]
		/*public static void replicate()
		{
			int status = 0;
			Console.Out.WriteLine("Replicate "+fromReg+ " to "+toReg );
			DateTime lastRep = lookupLastRep(fromReg);
			DateTime entry = writeStartLog(fromReg);
			StringBuilder message = new StringBuilder();
			message.Append("Last rep ");
			message.Append(lastRep);
			message.Append("\nToRegistry:");
			message.Append(toReg);
			message.Append("\n");
			status += replicateResourceTypes(message);
			status += replicateDeletedResources(message,lastRep,toReg,fromReg);
			status += replicateResources(message,lastRep,toReg,fromReg);
			writeEndLog(entry,message.ToString(),status);
			Console.Out.Write("Done.\n");


		}*/

		public static DateTime lookupLastRep(string fromReg)
		{
			SqlConnection conn = null;
			DateTime ret = DateTime.Parse("1970-JAN-01");

			try 
			{
				conn = new SqlConnection(connStr);
				conn.Open();

				string s = " select top 1 date from HarvesterLog where ServiceURL ='";
				s += fromReg+"' and status = 0 order by date desc";
				SqlCommand cmd =conn.CreateCommand();
				cmd.CommandText=s;
				try 
				{
					ret = (DateTime)cmd.ExecuteScalar();
				} 
				catch (Exception) 
				{
					// there is no log entry !
                    ret = DateTime.Parse("1970-JAN-01");
				}
			}
			finally
			{
				conn.Close();
			}

			return ret;
		}
		

		public static bool writeStartLog(string from, DateTime start, string type )
		{
			SqlConnection conn = null;

            try
            {
                conn = new SqlConnection(connStr);
                conn.Open();

                string s = " insert into HarvesterLog (date, type, ServiceURL) values ('";
                s += start.ToString() + "',";
                s += "'" + type + "',";
                s += "'" + from + "') ";
                SqlCommand cmd = conn.CreateCommand();
                cmd.CommandText = s;
                cmd.ExecuteNonQuery();
            }
            catch
            {
                return false;
            }
			finally
			{
				conn.Close();
			}

            return true;
		}

		public static bool writeEndLog(DateTime date,  string message, int status, string url )
		{

			SqlConnection conn = null;
            try
            {
                conn = new SqlConnection(connStr);
                conn.Open();
                if (message.Length > MLEN)
                {
                    message = message.Substring(0, MLEN);
                }
                message = message.Replace('\'', ' ');

                string s = " update HarvesterLog set message=";
                s += "'" + message + "',";
                s += "status=" + status;
                s += " where [date]='" + date.ToString() + "' and ServiceURL = '" + url + "' ";
                SqlCommand cmd = conn.CreateCommand();
                cmd.CommandText = s;
                //				Console.Out.Write(s);
                cmd.ExecuteNonQuery();
            }
            catch (Exception)
            {
                return false;
            }
			finally
			{
				conn.Close();
			}
            return true;
		}

		/*static public int  replicateDeletedResources(StringBuilder message,DateTime last,string toReg,string fromReg)
		{
			int probs =0;
			message.Append("\nDelete Resources:\n");

			RegistryAdmin	raTo		= new RegistryAdmin();
			RegistryAdmin	raFrom		= new RegistryAdmin();

			raTo.Url = toReg + "RegistryAdmin.asmx";
			raFrom.Url = fromReg + "RegistryAdmin.asmx";

			string qs = "select identifier,passphrase from Resource where status=3 and modificationDate > '";
			qs +=  last.ToString() + "'";
			Console.Out.WriteLine("Delete query is:"+qs);
			DataSet ds = raFrom.DSQuery(qs,PASS);
			foreach (DataRow dr in ds.Tables[0].Rows )
			{

				string identifier= (string)dr["identifier"];
				string pass= (string)dr["passphrase"];
				message.Append("deleting ");
				message.Append(identifier);
				String ret = " nothing done ";
				try 
				{
					ret=raTo.DeleteEntry(identifier,pass);
				}
				catch (Exception e) 
				{
					probs++;
					message.Append(e);
					ret =e.Message;
				}
				message.Append(ret);
				message.Append("\n");
				Console.Out.WriteLine("Delete "+identifier+" "+ret);
			}
			message.Append("\nDelete Resources:\n");
			return probs;
		}			

		static public int  replicateResources(StringBuilder message,DateTime last,string toReg,string fromReg)
		{
			int probs =0;
			message.Append("\nResources:\n");

			RegistryAdmin	raTo		= new RegistryAdmin();
			RegistryAdmin	raFrom		= new RegistryAdmin();

			raTo.Url = toReg + "RegistryAdmin.asmx";
			raFrom.Url = fromReg + "RegistryAdmin.asmx";
			String pred = "modificationDate > '" + last.ToString() + "'";
			Console.Out.WriteLine(pred);
			
			DBResource[] res =
				raFrom.QueryResource(pred);
			for (int r=0; r<res.Length; r++) 
			{
				string qs = "select passphrase from Resource where status=1 and identifier = '";
				qs += res[r].Identifier + "'";
				DataSet ds = raFrom.DSQuery(qs,PASS);
				string passw = (string)(ds.Tables[0].Rows[0][0]);
				DBResource[] resl = new DBResource[1];
				resl[0] = res[r];
				string m = raTo.Load(resl,passw);
				m = res[r].Identifier + m.Replace("'","\"") +"\n";
				Console.Out.WriteLine(m);
				message.Append( m );
			}
			return probs;
		}

		static public int  replicateResourceTypes(StringBuilder message)
		{
		    int probs =0;
			
			SqlConnection conn = null;

			try 
			{
				message.Append("ResourceTypes:");
				RegistryAdmin from = new RegistryAdmin();
				from.Url = fromReg + "RegistryAdmin.asmx";
				
				String qs = "select * from resourcetype";
				Console.Out.WriteLine("Query is:"+qs);
				DataSet ds = from.DSQuery(qs,PASS);
				DataTable dt = ds.Tables[0];
		
				conn = new SqlConnection(connStr);
				conn.Open();
				SqlCommand cmd =conn.CreateCommand();
				string cs = "select resourcetype from resourceType where resourcetype=@st";
				cmd.CommandText = cs;
				cmd.Parameters.Add("@st", SqlDbType.VarChar, 50);
				cmd.Prepare();
				SqlCommand ins = conn.CreateCommand();
				ins.CommandText = "insert into resourceType values (@st,@desc)";
				ins.Parameters.Add("@st", SqlDbType.VarChar, 50);
				ins.Parameters.Add("@desc", SqlDbType.VarChar, 500);
				ins.Prepare();

				foreach (DataRow dr in dt.Rows) 
				{
					cmd.Parameters["@st"].Value = dr[0];
					string servt = (string)(cmd.ExecuteScalar());
					if (servt == null || servt.Length == 0) 
					{
						int ind =0;
						ins.Parameters["@st"].Value = dr[ind++];
						ins.Parameters["@desc"].Value = dr[ind++];
						ins.ExecuteNonQuery();
						message.Append(dr[0]+" ");
						Console.Out.WriteLine("Got ResourceType: "+dr[0]);
					}
				}
			} 
			catch (Exception e) 
			{
				Console.Error.WriteLine(e.Message+":"+e.StackTrace);
				message.Append(e.Message);
				probs++;
			}
		    return probs;
		}*/
	}
}
