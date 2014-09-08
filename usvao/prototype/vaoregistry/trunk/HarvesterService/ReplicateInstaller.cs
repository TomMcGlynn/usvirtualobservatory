using System;
using System.Collections;
using System.ComponentModel;
using System.Configuration.Install;

namespace Replicate
{
	/// <summary>
	/// Summary description for ReplicateInstaller.
	/// </summary>
	[RunInstaller(true)]
	public class ReplicateInstaller : System.Configuration.Install.Installer
	{
		/// <summary>
		/// Required designer variable.
		/// </summary>
		private System.ComponentModel.Container components = null;
		private System.ServiceProcess.ServiceProcessInstaller serviceProcessInstaller1;

		private System.ServiceProcess.ServiceInstaller serviceInstaller1;


		public ReplicateInstaller()
		{
			// This call is required by the Designer.
			InitializeComponent();
			components=null;
			this.serviceProcessInstaller1 = new System.ServiceProcess.ServiceProcessInstaller();
			this.serviceInstaller1 = new System.ServiceProcess.ServiceInstaller();	
			this.serviceInstaller1.ServiceName = Runner.NAME;
			this.Installers.AddRange(new System.Configuration.Install.Installer[] {
						  this.serviceProcessInstaller1,
						  this.serviceInstaller1});
		}

		#region Component Designer generated code
		/// <summary>
		/// Required method for Designer support - do not modify
		/// the contents of this method with the code editor.
		/// </summary>
		private void InitializeComponent()
		{
			components = new System.ComponentModel.Container();
		}
		#endregion
	}
}
