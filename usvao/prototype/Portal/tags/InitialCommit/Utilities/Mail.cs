using System;
using System.Web;
using System.Diagnostics;
using System.Text;
using System.Net.Mail;
using System.Text.RegularExpressions;

namespace Utilities
{
	public class Mail
	{
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

        public static void sendError(string sReason, DateTime dt, string sBody, bool bHtml)
        {
            String sHostName = System.Environment.MachineName;
            String sSiteName = System.Web.VirtualPathUtility.ToAbsolute("~");
            String sHostSiteAppName = sHostName + sSiteName;

            string sSubject = sReason + ":" + sHostSiteAppName + " - " + dt.ToShortDateString() + " at " + dt.ToLongTimeString();
            Mail.sendEmail("galex-exception@stsci.edu", "galex-exception@stsci.edu", "", sSubject, sBody, bHtml);
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
            SmtpClient smtp = new SmtpClient("smtp.stsci.edu");

            //to authenticate we set the username and password properites on the SmtpClient
            // smtp.Credentials = new NetworkCredential("username", "secret");
            smtp.Send(mail);
		}

		bool IsValidEmail(String strIn)
		{
			// Return true if strIn is in valid e-mail format.
			return Regex.IsMatch(strIn, @"^([\w-\.]+)@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.)|(([\w-]+\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\]?)$"); 
		}      
	}
}

