using System;
using System.Collections;
using System.ComponentModel;
using System.Data;
using System.Diagnostics;
using System.ServiceProcess;
using System.Configuration;
using System.Threading;

namespace Replicate
{
	public class Runner : System.ServiceProcess.ServiceBase
	{
		/// <summary> 
		/// Required designer variable.
		/// </summary>
		private System.ComponentModel.Container components = null;
		static int delay = 60*10000;
		
		private Thread theThread =null;
		public const string NAME = "NVORegistryReplicator";

		public bool harvest = true;
		public bool replicate=true;
        public bool repeat = true;

		public Runner()
		{
			// This call is required by the Windows.Forms Component Designer.
			InitializeComponent();
			ServiceName=NAME;
            try
            {
                delay = Properties.Settings.Default.repeatDelay;
            }
            catch (Exception) { }
            try
            {
                repeat = Properties.Settings.Default.repeat;
            }
            catch (Exception) { }

			try 
			{
                harvest = Properties.Settings.Default.harvest;
			} 
			catch (Exception e) 
			{
				Console.WriteLine(e.StackTrace);
			}
			try 
			{
                replicate = Properties.Settings.Default.replicate;
			} 
			catch (Exception e) 
			{
				Console.WriteLine(e.StackTrace);
			}
			Console.Out.WriteLine("Delay:"+delay+" harvest:"+harvest+ " replicate:"+replicate+ " process repeat:"+repeat);

		}

		// The main entry point for the process
		static void Main(string[] args)
		{
			if (args.Length > 0) 
			{
				RunAsProcess();
			} 
			else 
			{
				RunAsService();
			}
		}

		static void RunAsProcess() 
		{
			Console.Out.WriteLine("Will run as process not service ..");
			Runner r = new Runner();
			r.OnStart(null);
		}
		static void RunAsService()
		{
			System.ServiceProcess.ServiceBase[] ServicesToRun;
			ServicesToRun = new System.ServiceProcess.ServiceBase[] { new Runner() };
			System.ServiceProcess.ServiceBase.Run(ServicesToRun);
		}

		/// <summary> 
		/// Required method for Designer support - do not modify 
		/// the contents of this method with the code editor.
		/// </summary>
		private void InitializeComponent()
		{
			components = new System.ComponentModel.Container();
			this.ServiceName = "Service1";
		}

		/// <summary>
		/// Clean up any resources being used.
		/// </summary>
		protected override void Dispose( bool disposing )
		{
			if( disposing )
			{
				if (components != null) 
				{
					components.Dispose();
				}
			}
			base.Dispose( disposing );
		}

		/// <summary>
		/// Set things in motion so your service can do its work.
		/// </summary>
		protected override void OnStart(string[] args)
		{
			going =true;
			theThread = new Thread(new ThreadStart(work));
		    theThread.Start();
		}

		public void work() 
		{
			while (going) 
			{
				try 
				{
					if (replicate)
					{
					    //	Replicate.replicate();
                        Console.Error.WriteLine("Replication not ported for new database schema.");
					}
				} 
				catch (Exception e) 
				{
					Console.Error.WriteLine(e.StackTrace);
				}
				try 
				{
					if (harvest) 
					{
						Harvest.harvest();
					}
				} 
				catch (Exception e) 
				{
					Console.Error.WriteLine(e.Message+":"+e.StackTrace);
				}
                if (repeat)
                {
                    Thread.Sleep(delay);
                }
                else //note: it makes little sense to have 'repeat' false when running as a service, but the code will let you.
                {
                    going = false;
                }
			}
		}
		public bool going = false;
		/// <summary>
		/// Stop this service.
		/// </summary>
		protected override void OnStop()
		{
			going=false;
		}
	}
}
