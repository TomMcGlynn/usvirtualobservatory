using System;
using System.Web;
using System.Diagnostics;
using System.Text;
using System.Net.Mail;
using System.Text.RegularExpressions;
using System.Configuration;
using System.Net;
using System.Net.Mime;
using System.Threading;
using System.ComponentModel;
using System.Timers;

using log4net;

namespace Utilities
{
	public class Mail
	{
		// Logger
		public static readonly ILog log = LogManager.GetLogger(System.Reflection.MethodBase.GetCurrentMethod().DeclaringType);
		public static string tid { get {return String.Format("{0,6}", "[" + System.Threading.Thread.CurrentThread.ManagedThreadId) + "] ";}  }	
		
		// Default email address and smtp server
		public static readonly string MAIL_TO_KEY = "MailTo";
		public static readonly string MAIL_FROM_KEY = "MailFrom";
		public static readonly string MAIL_SMTP_SERVER_KEY = "MailSmtpServer";
		public static readonly string MAIL_BURST_DISCARD_SECONDS_KEY = "MailBurstDiscardSeconds";

		static string mailTo = System.Configuration.ConfigurationManager.AppSettings.Get(MAIL_TO_KEY);
		static string mailFrom = System.Configuration.ConfigurationManager.AppSettings.Get(MAIL_FROM_KEY);
		static string mailSmtpServer = System.Configuration.ConfigurationManager.AppSettings.Get(MAIL_SMTP_SERVER_KEY);
		static string mailBurstDiscardSeconds = System.Configuration.ConfigurationManager.AppSettings.Get(MAIL_BURST_DISCARD_SECONDS_KEY);
				
		private Mail ()
		{
		}
		
		public static string getStackInfo()
        {
            StackTrace st = new StackTrace(true);
            int nNumFrames = st.FrameCount;

            String sInfo = "";
            if (nNumFrames > 2)
            {
                for (int i = 2; i < nNumFrames; i++)
                {
                    StackFrame sf = st.GetFrame(i);
                    if (sf.GetFileName() != null && sf.GetFileName().Length > 0)
                    {
                        sInfo += ("[Filename] " + sf.GetFileName() + " " +
                                  "[Method] " + sf.GetMethod().Name + " " +
                                  "[Line] " + sf.GetFileLineNumber() + "</br>");
                    }
                    else
                    {
                        break;
                    }
                }
            }

            return sInfo;
        }

        public static void sendException(ref Exception ex)
        {
            sendException(ref ex, null);
        }
		
        public static void sendException (ref Exception ex, string sMessage)
        {   
			try
			{
	            // Grab the time immediately so that it coincides with the Web Log Entry.
	            DateTime dateNow = DateTime.Now;
	
	            // Build exception trace message in HTML
	            StringBuilder sb = new StringBuilder();
	
	            sb.Append("<b>Message:</b></br>");
	            sb.Append((sMessage != null ? sMessage : "null"));
	            sb.Append("<br><br>");
	
	            sb.Append("<b>Caller Stack Info:</b></br>");
	            sb.Append(getStackInfo());
	            sb.Append("<br>");
	
	            sb.Append("<b>Exception:</b></br>");
	            sb.Append(ex.Message);
	            sb.Append("<br><br>");
	
	            sb.Append("<b>Source:</b></br>");
	            sb.Append(ex.Source);
	            sb.Append("<br><br>");
	
	            sb.Append("<b>StackTrace:</b></br>");
	            sb.Append(ex.StackTrace);
	            sb.Append("<br><br>");
	
	            Mail.sendError("Exception Caught", dateNow, sb.ToString(), true);
			} 
			catch (Exception exx)
			{
				//
				// IMPORTANT NOTE:
				// Big Assumption Here: is that caller has a problem and caught an Exception to
				// email out to a developer.  So if the Mail client causes an Exception, 
				// we just log the problem rather then let this Secondary Explosion cause further 
				// problems for the Applicaiton.
				//
				log.Error (tid + "Exception Caught and Ignored while attempting to Email Developers.", exx);
			}		
        }
			
        public static void sendError(string sReason, DateTime date, string sBody, bool bHtml)
        {
			// Create Subject String
			String sHostName = System.Environment.MachineName;
	        String sSiteName = System.Web.VirtualPathUtility.ToAbsolute("~");
	        String sHostSiteName = sHostName + sSiteName;
			string sSubject = sReason + ":" + sHostSiteName + " - " + date.ToShortDateString() + " at " + date.ToLongTimeString();
				
			// Send out email
            sendEmail(mailTo, mailFrom, "", sSubject, sBody, bHtml);
    	}
		
		public static void sendEmail(string To, string From, string Cc, string Subject, string Body, bool isBodyHtml)
		{
			if (To != null && To.Length > 0 && From != null && From.Length > 0 &&
				mailSmtpServer != null && mailSmtpServer.Length > 0)
			{			
	            //create the mail message
	            MailMessage mail = new MailMessage(From, To, Subject, Body);
	            if (Cc != null && Cc.Length > 0)
	            {
	                mail.CC.Add(Cc);
	            }
	            mail.IsBodyHtml = isBodyHtml;
				
				// Add email to the Send Queue
				queueEmailMessage(mail);
			}
		}
		
		static double nMailBurstDiscardSeconds = (mailBurstDiscardSeconds != null ? Convert.ToInt32(mailBurstDiscardSeconds) : 60);	
		public static System.Collections.ArrayList discardQueue = new System.Collections.ArrayList();
		public static System.Timers.Timer emailTimer = new System.Timers.Timer(nMailBurstDiscardSeconds*1000);
		
		public static void queueEmailMessage(MailMessage mail)
		{	
			lock (discardQueue)
			{			
				if (!emailTimer.Enabled)
				{
					// Send out this email now...
					sendEmailAsync(mail);
					
					 // ..and enable the timer to start discarding emails until the timer expires
			        emailTimer.Elapsed += new ElapsedEventHandler(onEmailTimer);
					emailTimer.Start();			
				}
				else
				{
					discardQueue.Add(mail);
				}
			} // End Lock() of SendQueue
		}

	    private static void onEmailTimer(object source, ElapsedEventArgs e)
	    {
			lock (discardQueue)
			{
				// Disable the timer
				emailTimer.Close();
				
				//
				// Bundle up messages on the Discard Queue into a single message
				//
				MailMessage mail = null;
				if (discardQueue.Count > 0)
				{
					String body = "Discarded Email Subjects:\n\n";
					foreach ( MailMessage mm in discardQueue )
					{
						body += mm.Subject + "\n";
					}
					body += "\n See Web Application Log for Details";
					
					String sHostName = System.Environment.MachineName;
			        String sSiteName = System.Web.VirtualPathUtility.ToAbsolute("~");
			        String sHostSiteName = sHostName + sSiteName;
					DateTime date = DateTime.Now;
					string subject = "Discarded Emails [" + discardQueue.Count + "]:" + sHostSiteName + " - " + date.ToShortDateString() + " at " + date.ToLongTimeString();
					
					// Create email message of batched subjects
					mail = new MailMessage(mailTo, mailFrom, subject, body);
				}
				
				// Finally, send out the Discarded Emails Notice 
				if (mail != null)
				{
					sendEmailAsync(mail);
				}
				
				// Clear out the Discard Queue
				discardQueue.Clear();
				
			} // End Lock() of discardQueue
	    }
		
		public static void sendEmailAsync(MailMessage mail) 
		{
			if (mail != null && mailSmtpServer != null && mailSmtpServer.Length > 0)
			{
	            SmtpClient smtp = new SmtpClient(mailSmtpServer);
	
	            //to authenticate we set the username and password properites on the SmtpClient
	            // smtp.Credentials = new NetworkCredential("username", "secret");
				
				//
				// IMPORTTANT NOTE: 
				// Send Mail Asynchronously so that this call does not block.
				// If it blocks, it can cause timeing problems for the caller.
				// We wire up the onSendCompleted() Callback below.
				//
				smtp.SendCompleted += new SendCompletedEventHandler(onSendCompleted);
	            smtp.SendAsync(mail, mail.Subject);	
			}
		}

		private static void onSendCompleted(object sender, AsyncCompletedEventArgs e)
        {
            // Get the token passed along (see above SendAsync()) for this Asynchronous Operation.
            String subject = (string) e.UserState;

            if (e.Cancelled)
            {
                log.Info(tid + "[EMAIL] Send Cancelled. Subject : " + subject);
            }
            if (e.Error != null)
            {
				log.Error(tid + "[EMAIL] Send Error. Subject : " + subject);
            } 
			else
            {
                log.Debug(tid + "[EMAIL] Sent. Subject : " + subject);
            }
        }

		bool IsValidEmail(String strIn)
		{
			// Return true if strIn is in valid e-mail format.
			return Regex.IsMatch(strIn, @"^([\w-\.]+)@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.)|(([\w-]+\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\]?)$"); 
		}      
	}
}

