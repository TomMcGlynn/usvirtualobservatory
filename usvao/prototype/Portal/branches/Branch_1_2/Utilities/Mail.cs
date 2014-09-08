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

		static string sMailTo = System.Configuration.ConfigurationManager.AppSettings.Get(MAIL_TO_KEY);
		static string sMailFrom = System.Configuration.ConfigurationManager.AppSettings.Get(MAIL_FROM_KEY);
		static string sMailSmtpServer = System.Configuration.ConfigurationManager.AppSettings.Get(MAIL_SMTP_SERVER_KEY);

		public static string mailTo = (sMailTo != null ? sMailTo : "mashup-exception@stsci.edu");
		public static string mailFrom = (sMailFrom != null ? sMailFrom : "mashup-exception@stsci.edu");
		public static string mailSmtpServer = (sMailSmtpServer != null ? sMailSmtpServer : "smtp.stsci.edu");
		
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
	            DateTime dt = DateTime.Now;
	
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
	
	            Mail.sendError("Exception Caught", dt, sb.ToString(), true);
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

        public static void sendError(string sReason, DateTime dt, string sBody, bool bHtml)
        {
            String sHostName = System.Environment.MachineName;
            String sSiteName = System.Web.VirtualPathUtility.ToAbsolute("~");
            String sHostSiteAppName = sHostName + sSiteName;

            string sSubject = sReason + ":" + sHostSiteAppName + " - " + dt.ToShortDateString() + " at " + dt.ToLongTimeString();
            Mail.sendEmail(mailTo, mailFrom, "", sSubject, sBody, bHtml);
        }

		public static void sendEmail(string sTo, string sFrom, string sCc, string sSubject, string sBody, bool bHtml)
		{
            //create the mail message
            MailMessage mail = new MailMessage();

            //set the addresses
            mail.From = new MailAddress(sFrom);
            mail.To.Add(sTo);
            if (sCc != null && sCc.Length > 0)
            {
                mail.CC.Add(sCc);
            }

            //set the content
            mail.Subject = sSubject;
            mail.Body = sBody;
            mail.IsBodyHtml = bHtml;

            //send the message
            SmtpClient smtp = new SmtpClient(mailSmtpServer);

            //to authenticate we set the username and password properites on the SmtpClient
            // smtp.Credentials = new NetworkCredential("username", "secret");
			
			//
			// IMPORTTANT NOTE: 
			// Send Mail Asynchronously so that this call does not block.
			// If it blocks, it can cause timeing problems for the caller.
			// We wire up the Send Complete Callback below.
			//
			smtp.SendCompleted += new SendCompletedEventHandler(onSendCompleted);
            smtp.SendAsync(mail, sSubject);
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

