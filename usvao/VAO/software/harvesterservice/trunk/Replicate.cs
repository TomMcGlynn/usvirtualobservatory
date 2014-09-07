using System;
using System.Configuration;
using System.Data.SqlClient;
using System.Data;
using System.Text;


using Replicate.registry;
using registry;

namespace Replicate
{
	class Replicate
	{
		const int MLEN = 4000;

        //because we need to write to the log table, this is an admin connection
        //public static string connStr = (string)System.Configuration.ConfigurationManager.AppSettings["SqlAdminConnection"];
        //private static string dbAdmin = (string)System.Configuration.ConfigurationManager.AppSettings["dbAdmin"];

        private static string connStr = Properties.Settings.Default.SqlAdminConnection;
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
			DateTime ret = DateTime.Parse("2000-JAN-01");

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
                    ret = DateTime.Parse("2000-JAN-01");
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
                message = message.Replace('<', '(').Replace('>', ')'); //raw record creating html in reporting errors: we don't really need it anyway.

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

        public static void harvestDatabaseRegistryList()
        {
            RegistryDBQuery reg = new RegistryDBQuery();
            HarvestAdmin harvest = new HarvestAdmin();

            // This is using the dll directly, picks up connection string from
            // the replicate.exe.config
            string rq = "select distinct ServiceUrl, RegistryID from Harvester where (DoHarvest = 1)";
            DataSet ds = reg.DSQuery(rq, dbAdmin);
            StringBuilder sb = new StringBuilder();
            int stat = 0;
            Console.Out.WriteLine("Harvesting ....\n");

            foreach (DataRow dr in ds.Tables[0].Rows)
            {
                stat = 0;
                sb.Remove(0, sb.Length);
                string url = (string)dr["ServiceUrl"];
                string regid = (string)dr["RegistryID"];
                DateTime last = Replicate.lookupLastRep(url);

                //oai is on UTC
                //last = last.ToUniversalTime(); //is already in UTC
                last = new DateTime(last.Ticks - (last.Ticks % TimeSpan.TicksPerSecond), last.Kind);

                //let's add some leeway in here for gateways that have less fine-grained time resolution than w
                DateTime startTime = DateTime.Now.ToUniversalTime();
                startTime = new DateTime(startTime.Ticks - (startTime.Ticks % TimeSpan.TicksPerSecond), startTime.Kind);

                bool wroteStartLog = Replicate.writeStartLog(url, startTime, "harvest");
                if (wroteStartLog)
                {
                    sb.Append(last);
                    sb.Append(" ");
                    try
                    {
                        Console.Out.WriteLine("trying :" + url + " last harvest " + last);
                        string res = harvest.HarvestOAI(url, regid, last, true, dbAdmin);
                        sb.Append(res);
                    }
                    catch (Exception e)
                    {
                        sb.Append(e);
                        stat = 1;
                    }
                    //hack to make sense of status from logging.
                    string mes = sb.ToString();
                    if (mes.Contains("No Records to Harvest")) //test: in case of hidden timeout errors.
                        stat = 3;
                    else if (mes.Contains("Loaded 0 RESOURCES"))
                        stat = 2;
                    else if (mes.Contains("Loaded") && mes.Contains("Got"))
                    {
                        string loaded = mes.Substring(mes.IndexOf("Loaded") + 7, mes.IndexOf("RESOURCES", mes.IndexOf("Loaded")) - (mes.IndexOf("Loaded") + 7)).Trim();
                        string got = mes.Substring(mes.IndexOf("Got") + 4, mes.IndexOf("recs") - (mes.IndexOf("Got") + 4)).Trim();

                        string skipped = "0";
                        if (mes.Contains("Skipped"))
                        {
                            int skip = mes.IndexOf("Skipped");
                            skipped = mes.Substring(skip + 8, mes.IndexOf("RESOURCES", skip) - (skip + 8)).Trim();
                        }
                        if ((Convert.ToInt32(skipped) + Convert.ToInt32(loaded)) != Convert.ToInt32(got))
                            stat = 2;
                    }
                    if (sb.Length > 1000)
                    {
                        mes = mes.Substring(mes.Length - 1000, 999);
                    }
                    else
                    {
                        Console.Out.WriteLine(sb.ToString() + "\n");
                    }
                    bool wroteEnd = Replicate.writeEndLog(startTime, sb.ToString(), stat, url);
                    if (wroteEnd == false)
                    {
                        logfile errlog = new logfile(Harvester.logFileName);
                        if (errlog.Log("Failed to write end log DB entry for " + url) == false)
                            Console.Out.WriteLine("Failed to log DB write error for " + url);
                    }
                }
                else
                {
                    logfile errlog = new logfile(Harvester.logFileName);
                    if (errlog.Log("Failed to write start log DB entry for " + url) == false)
                        Console.Out.WriteLine("Failed to log DB write error for " + url);
                }
            }
            Console.Out.WriteLine("Finished Harvest.\n");
        }

	}
}
