using System;
using registry;
using System.Data;
using System.Text;

namespace Replicate
{
	/// <summary>
	/// Summary description for Harvest.
	/// </summary>
	public class Harvest
	{
        private static string dbAdmin = Properties.Settings.Default.dbAdmin;
        private static string logFileName = Properties.Settings.Default.log_location + "\\replicatelog.txt";

 		public Harvest()
		{
		}

		public static void harvest()
		{
			RegistryAdmin reg = new RegistryAdmin();

			// This is using the dll directly, picks up connection string from
			// the replicate.exe.config
			string rq = "select distinct ServiceUrl from Harvester where (Harvest = 1)";
			DataSet ds = reg.DSQuery(rq, dbAdmin);
			StringBuilder sb = new StringBuilder();
			int stat=0;
			Console.Out.WriteLine("Harvesting ....\n");

            foreach (DataRow dr in ds.Tables[0].Rows)
            {
                stat = 0;
                sb.Remove(0, sb.Length);
                string url = (string)dr["ServiceUrl"];
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
                        string res = reg.HarvestOAI(url, last, true, dbAdmin);
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
                        logfile errlog = new logfile(logFileName);
                        if( errlog.Log("Failed to write end log DB entry for " + url)  == false)
                            Console.Out.WriteLine("Failed to log DB write error for " + url);
                     }
                }
                else
                {
                    logfile errlog = new logfile(logFileName);
                    if (errlog.Log("Failed to write start log DB entry for " + url) == false)
                        Console.Out.WriteLine("Failed to log DB write error for " + url);
                }
            }
			Console.Out.WriteLine("Finished Harvest.\n");
		}

	}
}
